package de.dailab.jiactng.agentcore.execution;

import java.util.ArrayList;
import java.util.Set;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.SessionEvent;

/**
 * Intermediate super-class for Simple- and Non-Blocking execution cycle
 * containing much of the very similar behavior of the two.
 * 
 * In fact, besides the mode of execution (sequential vs. parallel) and some
 * additional behavior in NonBlockingExecutionCycle, 98% of the code was the same!
 * 
 * This could maybe also be merged into AbstractExecutionCycle.
 *
 * @author kuester
 */
public abstract class BasicExecutionCycle extends AbstractExecutionCycle {

	protected static final Session      SESSION_TEMPLATE      = new Session(null, null, null, null);
	protected static final ActionResult ACTIONRESULT_TEMPLATE = new ActionResult(null, null);
	protected static final DoAction     DOACTION_TEMPLATE     = new DoAction(null, null, null, null);

	/**
	 * Perform the given process. Depending on the type of Execution cycle, this 
	 * can be executed directly, or performed in a thread.
	 * 
	 * @param process	the process to be executed
	 */
	protected abstract void performExecutionCycleProcess(Runnable process);

	/**
	 * Find one bean whose execute Method is due and execute it.
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

			final IAgentBean finalMinBean = minBean;
			performExecutionCycleProcess(new Runnable() {
				public void run() {
					try {
						finalMinBean.execute();
					} catch (Exception ex) {
						log.error("Error when executing bean \'" + finalMinBean.getBeanName() + "\'", ex);
					}
				}
			});

			// reschedule bean
			minBean.setNextExecutionTime(now + minBean.getExecutionInterval());
		}
		updateWorkload(EXECUTION, executionDone);
	}
	
	/**
	 * Starts processing of a given DoAction.
	 * @param act the DoAction
	 * @see DoActionHandler
	 * @see AbstractExecutionCycle#updateWorkload(int, boolean)
	 */
	protected void processDoAction(final DoAction act) {
		boolean actionPerformed = false;
		if (act != null) {
			actionPerformed = true;
			
			performExecutionCycleProcess(new Runnable() {
				public void run() {
					performDoAction(act);
				}
			});
		}
		updateWorkload(DO_ACTION, actionPerformed);		
	}
	
	/**
	 * Starts processing of a given ActionResult.
	 * @param result the ActionResult
	 * @see ActionResultHandler
	 * @see AbstractExecutionCycle#updateWorkload(int, boolean)
	 */
	protected void processActionResult(final ActionResult result) {
		boolean resultProcessed = false;
		if (result != null) {
			resultProcessed = true;
			
			performExecutionCycleProcess(new Runnable() {
				public void run() {
					processResult(result);
				}
			});
		}
		updateWorkload(ACTION_RESULT, resultProcessed);
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
			for (final Session session : sessions){
				if (session.isTimeout()){
					// session has timeout
					log.warn(TIMEOUT_MESSAGE + session);

					final ArrayList<SessionEvent<?>> history = session.getHistory();

					// Does Session is related to DoAction?
					boolean doActionFound = false;
					for (SessionEvent<?> event : history) {
						if (event instanceof DoAction) {
							// doAction found
							doActionFound = true;
							final DoAction doAction = (DoAction) event;
							memory.remove(doAction);
							
							if (doAction.getAction() instanceof Action) {
								// Got an Action, so let's cancel this doAction
								performExecutionCycleProcess(new Runnable() {
									public void run() {
										processSessionTimeout(session, doAction);
									}
								});
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
