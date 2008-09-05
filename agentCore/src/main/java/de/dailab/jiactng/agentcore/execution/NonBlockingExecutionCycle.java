package de.dailab.jiactng.agentcore.execution;

import java.util.ArrayList;
import java.util.HashSet;
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

	private Set<ActionResult> pendingResults = new HashSet<ActionResult>();
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
	public void run() {
		// cancel and remove futures which has reached timeout
		long now = System.currentTimeMillis();
		while (!futures.isEmpty() && (futures.firstKey() < now)) {
			Future<?> future = futures.pollFirstEntry().getValue();
			if (future.cancel(true)) {
				log.warn("Handler was interrupted by the execution cycle due to timeout constraints");
			} else if (!future.isCancelled() && !future.isDone()) {
				log.warn("Handler can not be canceled by the execution cycle");
			}
		}
		// remove futures which are already done or canceled
		Long[] keys = futures.keySet().toArray(new Long[0]);
		for (int i=0; i<keys.length; i++) {
			Future<?> future = futures.get(keys[i]);
			if (future.isCancelled() || future.isDone()) {
				futures.remove(keys[i]);
			}
		}

		// check if lifecycle has been started --> execute if STARTED
		if (getState() == LifecycleStates.STARTED) {
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
				if (bean.getExecuteInterval() <= 0) {
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
				Future<?> executionFuture = thisAgent.getThreadPool().submit(
						new ExecutionHandler(minBean));
				futures.put(timeout++, executionFuture);

				// reschedule bean
				minBean
						.setNextExecutionTime(now
								+ minBean.getExecuteInterval());
			}
			updateWorkload(EXECUTION, executionDone);

			// process one doAction
			// TODO: check if read can be used
			DoAction act = memory.remove(new DoAction(null, null, null, null));

			boolean actionPerformed = false;
			if (act != null) {
				actionPerformed = true;
				synchronized (this) {
					Future<?> doActionFuture = thisAgent.getThreadPool().submit(
							new DoActionHandler(act));
					futures.put(timeout++, doActionFuture);
				}
			}
			updateWorkload(DO_ACTION, actionPerformed);

			// process one actionResult
			// TODO: check if read can be used
			Set<ActionResult> resultSet = memory.removeAll(new ActionResult(
					null, null));
			int countNew = 0;
			for (ActionResult ar : resultSet) {
				synchronized (this) {
					pendingResults.add(ar);
					countNew++;
				}
			}

			boolean resultProcessed = false;
			if (!pendingResults.isEmpty()) {
				resultProcessed = true;
				synchronized (this) {
					ActionResult actionResult = pendingResults.iterator()
							.next();
					Future<?> actionResultFuture = thisAgent.getThreadPool().submit(
							new ActionResultHandler(actionResult));
					futures.put(timeout++, actionResultFuture);
					pendingResults.remove(actionResult);
				}
			}
			updateWorkload(ACTION_RESULT, resultProcessed);

			// Session-Cleanup, if Session has a timeout
			synchronized (memory) {
				Set<Session> sessions = memory.readAll(new Session());
				for (Session session : sessions){
					if (session.isTimeout()){
						// session has timeout
						ArrayList<SessionEvent> history = session.getHistory();

						// Does Session is related to DoAction?
						boolean doActionFound = false;
						for (SessionEvent event : history) {
							if (event instanceof DoAction) {
								// doAction found
								doActionFound = true;
								DoAction doAction = (DoAction) event;
								if (doAction.getAction() instanceof Action) {
									// Got an Action, so let's cancel this
									// doAction

									Future<?> sessionTimeoutFuture = thisAgent.getThreadPool().submit(
											new SessionTimeoutHandler(session, doAction));
									futures.put(timeout++, sessionTimeoutFuture);
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
		private IAgentBean _minBean;

		public ExecutionHandler(IAgentBean minBean) {
			_minBean = minBean;
		}

		public void run() {
			try {
				_minBean.execute();
			} catch (Exception ex) {
				log.error("Error when executing bean \'"
						+ _minBean.getBeanName() + "\'", ex);
			}
		}
	}

	private class DoActionHandler implements Runnable {
		private DoAction _act;

		public DoActionHandler(DoAction act) {
			_act = act;
		}

		public void run() {
			performDoAction(_act);
		}
	}

	private class ActionResultHandler implements Runnable {
		private ActionResult _actionResult;

		public ActionResultHandler(ActionResult actionResult) {
			_actionResult = actionResult;
		}

		public void run() {
			processResult(_actionResult);
		}
	}

	private class SessionTimeoutHandler implements Runnable {
		private Session _session;
		private DoAction _doAction;

		public SessionTimeoutHandler(Session session, DoAction doAction) {
			_session = session;
			_doAction = doAction;
		}

		public void run() {
			Action action = (Action) _doAction.getAction();
			log.debug("canceling DoAction " + _doAction);
			ActionResult result = action.getProviderBean().cancelAction(_doAction);
	
			if (_session.getSource() != null) {
				log.debug("sending timeout Result to source of Session " + _session);
				ResultReceiver receiver = _session.getSource();
		
				if (result == null){
					result = new ActionResult(
							_doAction, new TimeoutException("DoAction has timeout"));	
				}
				receiver.receiveResult(result);
			} else {
				log.warn("Session without Source: DoAction has to be canceled due to sessiontimeout "
					+ _doAction);
			}
		}
	}
}
