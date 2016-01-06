package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
public class NonBlockingExecutionCycle extends AbstractExecutionCycle 
	implements NonBlockingExecutionCycleMBean {

	protected static final Session SESSION_TEMPLATE = new Session(null, null, null, null);
	protected static final ActionResult ACTIONRESULT_TEMPLATE = new ActionResult(null, null);
	protected static final DoAction DOACTION_TEMPLATE = new DoAction(null, null, null, null);
	protected TreeMap<Long,List<Future<?>>> futures = new TreeMap<Long,List<Future<?>>>();

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
	public void run() {
		processFutureTimeouts();

		// check if lifecycle has been started --> execute if STARTED
		if (getState() == LifecycleStates.STARTED) {
			processAutoExecutionServices();

			// execute the ripest bean
			processBeanExecution();

			// process one doAction
			// TODO: check if read can be used
			final DoAction act = memory.remove(DOACTION_TEMPLATE);
			processDoAction(act);

			// process one actionResult
			// TODO: check if read can be used
			ActionResult result = memory.remove(ACTIONRESULT_TEMPLATE);
			processActionResult(result);			
			
			// Session-Cleanup, if Session has a timeout
			processSessionTimeouts();
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
		synchronized (futures) {
			int result = 0;
			for (List<Future<?>> futureList : futures.values()) {
				result += futureList.size();
			}
			return result;
		}
	}

	/**
	 * A handler for processing one bean execution within an own thread.
	 * @author Jan Keiser
	 * @see IAgentBean#execute()
	 */
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

	/**
	 * A handler for processing one DoAction within an own thread.
	 * @author Jan Keiser
	 * @see AbstractExecutionCycle#performDoAction(DoAction)
	 */
	private class DoActionHandler implements Runnable {
		private DoAction act;

		public DoActionHandler(DoAction act) {
			this.act = act;
		}

		public void run() {
			performDoAction(act);
		}
	}

	/**
	 * A handler for processing one ActionResult within an own thread.
	 * @author Jan Keiser
	 * @see AbstractExecutionCycle#processResult(ActionResult)
	 */
	private class ActionResultHandler implements Runnable {
		private ActionResult actionResult;

		public ActionResultHandler(ActionResult actionResult) {
			this.actionResult = actionResult;
		}

		public void run() {
			processResult(actionResult);
		}
	}

	/**
	 * A handler for processing the cancellation of one DoAction within an own thread.
	 * @author Jan Keiser
	 * {@link de.dailab.jiactng.agentcore.environment.IEffector#cancelAction(DoAction)}
	 */
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

	/* Adds a runnable thread to the internal data structure with the agent's bean execution timeout. */
	private void addFuture(Future<?> executionFuture) {
		synchronized (futures) {
			long timeout = System.currentTimeMillis() + thisAgent.getBeanExecutionTimeout();
			List<Future<?>> futureList = futures.get(timeout);
			if (futureList == null) {
				futureList = new ArrayList<Future<?>>();
				futures.put(timeout, futureList);
			}
			futureList.add(executionFuture);
		}		
	}

	/**
	 * Starts in an own thread the execution of the most urgent active bean, 
	 * which execution interval is expired.
	 * @see IAgentBean#getExecutionInterval()
	 * @see IAgentBean#getNextExecutionTime()
	 * @see ExecutionHandler
	 * @see AbstractExecutionCycle#updateWorkload(int, boolean)
	 */
	protected void processBeanExecution() {
		IAgentBean minBean = null;
		long minExecutionTime = Long.MAX_VALUE;
		long now = System.currentTimeMillis();
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
			final Future<?> executionFuture = thisAgent.getThreadPool().submit(new ExecutionHandler(minBean));
			addFuture(executionFuture);

			// reschedule bean
			minBean.setNextExecutionTime(now + minBean.getExecutionInterval());
		}
		updateWorkload(EXECUTION, executionDone);		
	}

	/**
	 * Starts processing of a given DoAction within an own thread.
	 * @param act the DoAction
	 * @see DoActionHandler
	 * @see AbstractExecutionCycle#updateWorkload(int, boolean)
	 */
	protected void processDoAction(DoAction act) {
		boolean actionPerformed = false;
		if (act != null) {
			actionPerformed = true;
			final Future<?> doActionFuture = thisAgent.getThreadPool().submit(new DoActionHandler(act));
			addFuture(doActionFuture);
		}
		updateWorkload(DO_ACTION, actionPerformed);		
	}

	/**
	 * Starts processing of a given ActionResult within an own thread.
	 * @param result the ActionResult
	 * @see ActionResultHandler
	 * @see AbstractExecutionCycle#updateWorkload(int, boolean)
	 */
	protected void processActionResult(ActionResult result) {
		boolean resultProcessed = false;
		if (result != null) {
			resultProcessed = true;
			final Future<?> actionResultFuture = thisAgent.getThreadPool().submit(new ActionResultHandler(result));
			addFuture(actionResultFuture);
		}
		updateWorkload(ACTION_RESULT, resultProcessed);
	}

	/**
	 * Cancels all threads, which timeout was reached, 
	 * and updates the internal data structure.
	 * @see Future#cancel(boolean)
	 */
	protected void processFutureTimeouts() {
		synchronized(futures) {
			final long now = System.currentTimeMillis();

			// cancel and remove futures which has reached timeout
			while (!futures.isEmpty() && (futures.firstKey().longValue() < now)) {
				final List<Future<?>> futureList = futures.pollFirstEntry().getValue();
				for (Future<?> future : futureList) {
					if (future.cancel(true)) {
						log.warn("Handler was interrupted by the execution cycle due to timeout constraints");
					} else if (!future.isCancelled() && !future.isDone()) {
						log.warn("Handler can not be canceled by the execution cycle");
					}
				}
			}

			// remove futures which are already done or canceled
			final Long[] keys = futures.keySet().toArray(new Long[futures.keySet().size()]);
			for (int i=0; i<keys.length; i++) {
				final Iterator<Future<?>> iterator = futures.get(keys[i]).iterator();
				while (iterator.hasNext()) {
					final Future<?> future = iterator.next();
					if (future.isCancelled() || future.isDone()) {
						iterator.remove();
					}
				}
				if (futures.get(keys[i]).isEmpty()) {
					futures.remove(keys[i]);
				}
			}
		}		
	}

	/**
	 * Starts canceling all DoActions, which session has a timeout.
	 * @see Session#isTimeout()
	 * @see Session#getHistory()
	 * @see SessionTimeoutHandler
	 */
	protected void processSessionTimeouts() {
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
								addFuture(sessionTimeoutFuture);
							}
						}
					}

					if (!doActionFound) {
						log.warn("Session with no DoAction was deleted due to timeout. Session: " + session);
					}
					//last but not least remove timeout session from memory
					memory.remove(session);
				}
			}
		}		
	}
}
