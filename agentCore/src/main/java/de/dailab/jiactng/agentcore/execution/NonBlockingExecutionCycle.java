package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Future;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.SessionEvent;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * A non-blocking ExecutionCycle implementation. This class executes active agentbeans
 * (those agentbeans where the <code>executionInterval</code> is set to a
 * value greater than 0) and takes care of action requests (<code>DoAction</code>)
 * and their results (<code>ActionResult</code>) in a parallel manner by using the
 * thread pool of the agent node.
 * 
 * @author Jan Keiser
 * 
 * @see IExecutionCycle
 * @see DoAction
 * @see ActionResult
 */
public final class NonBlockingExecutionCycle extends AbstractExecutionCycle 
	implements NonBlockingExecutionCycleMBean {

	private static final Session SESSION_TEMPLATE = new Session(null, null, null, null);
	private static final ActionResult ACTIONRESULT_TEMPLATE = new ActionResult(null, null);
	private static final DoAction DOACTION_TEMPLATE = new DoAction(null, null, null, null);
	private TreeMap<Long,Future<?>> futures = new TreeMap<Long,Future<?>>();

	/**
	 * Run-method for the execution cycle. The method iterates over the list of
	 * agentbeans and calls the execute method of each <i>active</i> agentbean.
	 * 
	 * This method also takes care of new <code>DoAction</code>s and
	 * <code>ActionResult</code>s.
	 * 
	 * The <code>NonBlockingExecutionCycle</code> only executes agentbeans and
	 * handles DoActions and ActionResults when it has reached
	 * <code>LifecycleStates.STARTED</code>.
	 * 
	 * @see de.dailab.jiactng.agentcore.execution.IExecutionCycle#run()
	 * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
	 */
	public final void run() {
		// cancel and remove futures which has reached timeout
		final long now = System.currentTimeMillis();
		while (!futures.isEmpty() && (futures.firstKey().longValue() < now)) {
			final Future<?> future = futures.pollFirstEntry().getValue();
			if (future.cancel(true)) {
				log.warn("Handler was interrupted by the execution cycle due to timeout constraints");
			} else if (!future.isCancelled() && !future.isDone()) {
				log.warn("Handler can not be canceled by the execution cycle");
			}
		}
		// remove futures which are already done or canceled
		final Long[] keys = futures.keySet().toArray(new Long[futures.keySet().size()]);
		for (int i=0; i<keys.length; i++) {
			final Future<?> future = futures.get(keys[i]);
			if (future.isCancelled() || future.isDone()) {
				futures.remove(keys[i]);
			}
		}

		// check if lifecycle has been started --> execute if STARTED
		if (getState() == LifecycleStates.STARTED) {
			 processAutoExecutionServices();
			// get timeout
			long timeout = now+thisAgent.getBeanExecutionTimeout();

			// execute the ripest bean
			IAgentBean minBean = null;
			long minExecutionTime = Long.MAX_VALUE;
			for (IAgentBean bean : thisAgent.getAgentBeans()) {
				// check bean's state, if not started --> reject
				if (bean.getState() != LifecycleStates.STARTED) {
					continue;
				}

				// check if bean has cyclic behavior, if not --> reject
				if (bean.getExecutionInterval() <= 0) {
					continue;
				}

				// execution time not reached yet --> reject
				if (bean.getNextExecutionTime() > now) {
					continue;
				}

				// execution time is not minimum --> reject
				if (bean.getNextExecutionTime() > minExecutionTime) {
					continue;
				}

				minBean = bean;
				minExecutionTime = bean.getNextExecutionTime();
			}

			// if there is a minBean then execute
			boolean executionDone = false;
			if (minBean != null) {
				executionDone = true;
				final Future<?> executionFuture = thisAgent.getThreadPool().submit(
						new ExecutionHandler(minBean));
				futures.put(Long.valueOf(timeout++), executionFuture);

				// reschedule bean
				minBean
						.setNextExecutionTime(now
								+ minBean.getExecutionInterval());
			}
			updateWorkload(EXECUTION, executionDone);

			// process one doAction
			// TODO: check if read can be used
			final DoAction act = memory.remove(DOACTION_TEMPLATE);

			boolean actionPerformed = false;
			if (act != null) {
				actionPerformed = true;
				synchronized (this) {
					final Future<?> doActionFuture = thisAgent.getThreadPool().submit(
							new DoActionHandler(act));
					futures.put(Long.valueOf(timeout++), doActionFuture);
				}
			}
			updateWorkload(DO_ACTION, actionPerformed);

			// process one actionResult
			// TODO: check if read can be used
			ActionResult result = memory.remove(ACTIONRESULT_TEMPLATE);
			boolean resultProcessed = false;
			if (result != null) {
			  resultProcessed = true;
			  synchronized (this) {
			    final Future<?> actionResultFuture = thisAgent.getThreadPool().submit(
			       new ActionResultHandler(result));
			    futures.put(Long.valueOf(timeout++), actionResultFuture);
			  }
			}
			updateWorkload(ACTION_RESULT, resultProcessed);
			
			
			// Session-Cleanup, if Session has a timeout
			synchronized (memory) {
				final Set<Session> sessions = memory.readAll(SESSION_TEMPLATE);
				for (Session session : sessions){
					if (session.isTimeout()){
	          // session has timeout
	          log.warn(TIMEOUT_MESSAGE + session);

						final ArrayList<SessionEvent> history = session.getHistory();

						// Does Session is related to DoAction?
						boolean doActionFound = false;
						for (SessionEvent event : history) {
							if (event instanceof DoAction) {
								// doAction found
								doActionFound = true;
								final DoAction doAction = (DoAction) event;
								memory.remove(doAction);
								
								if (doAction.getAction() instanceof Action) {
									// Got an Action, so let's cancel this doAction

									final Future<?> sessionTimeoutFuture = thisAgent.getThreadPool().submit(
											new SessionTimeoutHandler(session, doAction));
									futures.put(Long.valueOf(timeout++), sessionTimeoutFuture);
								}
							}
						}

						if (!doActionFound) {
							log
									.warn("Session with no DoAction was deleted due to timeout. Session: "
											+ session);
						}
						//last but not least remove timeout session from memory
						memory.remove(session);
					}
				}
			}
		}

		// reject execution if SimpleExecutionCycle hasn't been started
		else {
			log.error("Trying to run NonBlockingExecutionCycle in state "
					+ getState());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRunningHandlers() {
		return futures.size();
	}

	private class ExecutionHandler implements Runnable {
		private IAgentBean minBean;

		public ExecutionHandler(IAgentBean minBean) {
			this.minBean = minBean;
		}

		public void run() {
			try {
				minBean.execute();
			} catch (Exception ex) {
				log.error("Error when executing bean \'"
						+ minBean.getBeanName() + "\'", ex);
			}
		}
	}

	private class DoActionHandler implements Runnable {
		private DoAction act;

		public DoActionHandler(DoAction act) {
			this.act = act;
		}

		public void run() {
			performDoAction(act);
		}
	}

	private class ActionResultHandler implements Runnable {
		private ActionResult actionResult;

		public ActionResultHandler(ActionResult actionResult) {
			this.actionResult = actionResult;
		}

		public void run() {
			processResult(actionResult);
		}
	}

	private class SessionTimeoutHandler implements Runnable {
		private Session session;
		private DoAction doAction;

		public SessionTimeoutHandler(Session session, DoAction doAction) {
			this.session = session;
			this.doAction = doAction;
		}

		public void run() {
			final Action action = (Action) doAction.getAction();
			log.debug("canceling DoAction " + doAction);

			ActionResult result = null;
      if ((action == null)) {
        log.warn("Found doAction with missing action:" + doAction);
      } else if (action.getProviderBean() == null) {
        // TODO: this happens always with transmitted doActions due to transient fields. Is there a better solution?
        if(thisAgent.getAgentDescription().getAid().equals(action.getProviderDescription().getAid())) {
          log.info("Found doAction with missing providerBean:" + doAction);
        }
      } else {
        result = action.getProviderBean().cancelAction(doAction);
      }
			
      // if no result was created, use TimeoutExecption as default result
      if (result == null) {
        if(doAction.getSource()!=null) {
          result = new ActionResult(doAction, new TimeoutException(TIMEOUT_MESSAGE));
        }
      }
      
			if ((doAction.getSource() != null) && (doAction.getSource() instanceof ResultReceiver)) {
				log.debug("sending timeout Result to source of Session " + session);
				final ResultReceiver receiver = (ResultReceiver)doAction.getSource();
		
				receiver.receiveResult(result);
			} else {
			  // TODO: this happens always with transmitted doActions due to transient fields. Is there a better solution? 
				log.info("Session without Result-Receiver Source: DoAction had to be canceled due to sessiontimeout " + doAction);
			}
		}
		
	}
	
	private void processAutoExecutionServices(){
		  if(autoExecutionServices != null){
			  List<String> trash = new ArrayList<String>();
			  for(String actionName : autoExecutionServices.keySet()){
				  Map<String, Serializable> config = autoExecutionServices.get(actionName);
				  Object startTimeO = config.get("startTime");
				  Object intervalTimeO = config.get("intervalTime");
				  String providerName = (String) config.get("provider");
				  Integer startTime = null;
				  Integer intervalTime = null;
				  if(startTimeO != null){
					  startTime = Integer.parseInt(startTimeO.toString());
				  }if(intervalTimeO != null){
					 intervalTime = Integer.parseInt(intervalTimeO.toString());
				  }
				  if(startTime != null){
					  if(startTime > (System.currentTimeMillis() - time)){
						  continue;
					  }
					  
					  if(intervalTime == null ){
						  if(servicesExecutionTimes.get(actionName) >= 1){
							  trash.add(actionName);
							  continue;
						  }
					  }else if((System.currentTimeMillis() - time - startTime) / intervalTime < servicesExecutionTimes.get(actionName)){
						  continue;
					  }
				  }else{
					 if(intervalTime == null){
						 if(servicesExecutionTimes.get(actionName) >= 1){
							 trash.add(actionName);
							 continue;
						 }
					 }else if((System.currentTimeMillis() - time) / intervalTime < servicesExecutionTimes.get(actionName)){
						  continue;					  
					  }
				  }
				  servicesExecutionTimes.put(actionName, servicesExecutionTimes.get(actionName) + 1);
				  Action action = new Action(actionName);
				  IActionDescription actionD = null;
				  if(providerName == null){
					  actionD = thisAgent.searchAction(action);
				  }else{
					  List<IActionDescription> actions = thisAgent.searchAllActions(action);
					  for(IActionDescription a : actions){
						  if(a.getProviderDescription().getName().equals(providerName)){
							  actionD = a;
						  }
					  }
				  }
				  if(actionD == null){
					  log.warn("Action: " + actionName + " not found");
					  continue;
				  }
				  List<Serializable> params = (List<Serializable>) config.get("params");
				  Serializable[] p = null;
				  if(params != null){
					  List<Class<?>> paramTypes = null;
					  try {
							paramTypes = actionD.getInputTypes();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
						}
					  p = new Serializable[params.size()];
					  for(int i = 0; i < params.size(); i++){
						  Class<?> inputType = paramTypes.get(i);
						  String inputTypeAsString = inputType.toString();
						  if(inputTypeAsString.equals("int") || inputTypeAsString.equals("class java.lang.Integer")){
							  p[i] = (Serializable) Integer.parseInt((String)params.get(i));
						  }else if(inputTypeAsString.equals("double") || inputTypeAsString.equals("class java.lang.Double")){
							  p[i] = (Serializable) Double.parseDouble((String)params.get(i));
						  }else if(inputTypeAsString.equals("boolean") || inputTypeAsString.equals("class java.lang.Boolean")){
							  p[i] = (Serializable) Boolean.parseBoolean((String)params.get(i));
						  }else{
							  p[i] = (Serializable) inputType.cast(params.get(i));
						  }
						 
					  }
				  }
				  invoke(actionD, p);

			  }
			  for(String actionName : trash){
				  autoExecutionServices.remove(actionName);
				  servicesExecutionTimes.remove(actionName);
			  }
		  }
	  }
}
