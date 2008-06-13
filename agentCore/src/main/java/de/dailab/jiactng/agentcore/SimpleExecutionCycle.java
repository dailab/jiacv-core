/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.SessionEvent;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.management.jmx.ActionPerformedNotification;

/**
 * A simple ExecutionCycle implementation. This class executes active agentbeans
 * (those agentbeans where the <code>executionInterval</code> is set to a
 * value greater than 0) and takes care of action requests (<code>DoAction</code>)
 * and their results (<code>ActionResult</code>).
 * 
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see IExecutionCycle
 * @see DoAction
 * @see ActionResult
 */
public class SimpleExecutionCycle extends AbstractAgentBean implements
		IExecutionCycle {

	private Set<ActionResult> pendingResults = new HashSet<ActionResult>();

	/**
	 * Run-method for the execution cycle. The method iterates over the list of
	 * agentbeans and calls the execute method of each <i>active</i> agentbean.
	 * 
	 * This method also takes care of new <code>DoAction</code>s and
	 * <code>ActionResult</code>s.
	 * 
	 * The <code>SimpleExecutionCycle</code> only executes agentbeans and
	 * handles DoActions and ActionResults when it has reached
	 * <code>LifecycleStates.STARTED</code>.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#run()
	 * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
	 */
	public void run() {
		// check if lifecycle has been started --> execute if STARTED
		if (getState() == LifecycleStates.STARTED) {
			// execute the ripest bean
			IAgentBean minBean = null;
			long minExecutionTime = Long.MAX_VALUE;
			long now = System.currentTimeMillis();
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
			if (minBean != null) {
				try {
					minBean.execute();
				} catch (Exception ex) {
					log.error("Error when executing bean \'"
							+ minBean.getBeanName() + "\'", ex);
				}

				// reschedule bean
				minBean
						.setNextExecutionTime(now
								+ minBean.getExecuteInterval());
			}
			// else {
			// log.debug("No active beans to execute");
			// }

			// process one doAction
			// TODO: check if read can be used
			DoAction act = memory.remove(new DoAction(null, null, null, null));

			if (act != null) {
				synchronized (this) {
					performDoAction(act);
				}
			}

			// process one actionResult
			// TODO: check if read can be used
			Set<ActionResult> resultSet = memory.removeAll(new ActionResult(
					null, null));
			int countNew = 0;
			for (ActionResult ar : resultSet) {
				if (ar.getSource() != null
						&& ((DoAction) ar.getSource()).getSource() != null) {
					synchronized (this) {
						pendingResults.add(ar);
						countNew++;
					}
				}
			}

			if (!pendingResults.isEmpty()) {
				synchronized (this) {
					ActionResult actionResult = pendingResults.iterator()
							.next();
					processResult(actionResult);
					pendingResults.remove(actionResult);
				}
			}

			// ActionResult actionResult = memory.remove(new ActionResult(null,
			// null));
			// if (actionResult != null) {
			// synchronized (this) {
			// processResult(actionResult);
			// }
			// }

			/*
			 * Session-Cleanup
			 * 
			 * If Session has a timeout
			 */
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

									Action action = (Action) doAction
											.getAction();
									log.debug("canceling DoAction " + doAction);
									ActionResult result = action.getProviderBean().cancelAction(
											doAction);
									
									if (session.getSource() != null){
										log.debug("sending timeout Result to source of Session " + session);
										ResultReceiver receiver = session.getSource();
										
										if (result == null){
											result = new ActionResult(
													doAction, new TimeoutException(
													"DoAction has timeout"));	
										}
										receiver.receiveResult(result);
									} else {
									log.warn("Session without Source: DoAction has to be canceled due to sessiontimeout "
													+ doAction);
									}
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
			log.error("Trying to run SimpleExecutionCycle in state "
					+ getState());
		}
	}

	private void performDoAction(DoAction act) {
		long start = System.nanoTime();
		boolean success = false;

		if (((Action) act.getAction()).getProviderBean() != null) {
			try {
				((Action) act.getAction()).getProviderBean().doAction(act);
				success = true;
				if (act.getAction().getResultTypes()!=null){
					memory.write(act.getSession());
				} 
			} catch (Throwable t) {
				log.error("--- action failed: " + act.getAction().getName(),t);
			}
		} else {
			log.error("--- found action without bean: "
					+ act.getAction().getName());
		}

		long end = System.nanoTime();
		actionPerformed(act, end - start, success);
	}

	private void processResult(ActionResult actionResult) {
		// entweder den ResultReceiver informieren oder das Ergebnis in
		// den Memory schreiben
		if (((DoAction) actionResult.getSource()) == null
				|| ((DoAction) actionResult.getSource()).getSource() == null) {
			// memory.write(actionResult);
			;
		} else {
			DoAction doAct = ((DoAction) actionResult.getSource());
			// Session template= new
			// Session(doAct.getSessionId(),doAct.getSession().getCreationTime(),null,null);
			// memory.remove(new ActionResult(null,template,null,null));
			// DoAction doAct = ((DoAction) actionResult.getSource());
			if (memory.remove(doAct.getSession()) == null){
				log.warn("ActionResult for Action" + actionResult.getAction().getName() + " written with non existing Session.");
			}
			// memory.remove(template);
			((ResultReceiver) doAct.getSource()).receiveResult(actionResult);

		}
		// ArrayList history = actionResult.getSession().getHistory();
		// for (int i = history.size() - 1; i >= 0; i--) {
		// if (history.get(i) instanceof DoAction) {
		// ((ResultReceiver) ((DoAction) history.get(i))
		// .getSource()).receiveResult(actionResult);
		// break;
		// }
		// }
	}

	/**
	 * Uses JMX to send notifications that an action was performed by the
	 * managed execution cycle of an agent.
	 * 
	 * @param action
	 *            the performed action.
	 * @param duration
	 *            the duration of the execution.
	 */
	public void actionPerformed(DoAction action, long duration, boolean success) {
		Notification n = new ActionPerformedNotification(this,
				sequenceNumber++, System.currentTimeMillis(),
				"Action performed", action, duration, success);

		sendNotification(n);
	}

	/**
	 * Gets information about all notifications this execution cycle instance
	 * may send. This contains also information about the
	 * <code>ActionPerformedNotification</code> to notify about performed
	 * actions.
	 * 
	 * @return list of notification information.
	 */
	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		MBeanNotificationInfo[] parent = super.getNotificationInfo();
		int size = parent.length;
		MBeanNotificationInfo[] result = new MBeanNotificationInfo[size + 1];
		for (int i = 0; i < size; i++) {
			result[i] = parent[i];
		}

		String[] types = new String[] { ActionPerformedNotification.ACTION_PERFORMED };
		String name = ActionPerformedNotification.class.getName();
		String description = "An action was performed";
		MBeanNotificationInfo info = new MBeanNotificationInfo(types, name,
				description);
		result[size] = info;
		return result;
	}

	/**
	 * Registers the execution cycle for management
	 * 
	 * @param manager
	 *            the manager for this executionCycle
	 */
	public void enableManagement(Manager manager) {
		// do nothing if management already enabled
		if (isManagementEnabled()) {
			return;
		}

		// register execution cycle for management
		try {
			manager.registerAgentResource(thisAgent, "ExecutionCycle", this);
		} catch (Exception e) {
			System.err
					.println("WARNING: Unable to register execution cycle of agent "
							+ thisAgent.getAgentName()
							+ " of agent node "
							+ thisAgent.getAgentNode().getName()
							+ " as JMX resource.");
			System.err.println(e.getMessage());
		}

		_manager = manager;
	}

	/**
	 * Deregisters the execution cycle from management
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}

		// deregister execution cycle from management
		try {
			_manager.unregisterAgentResource(thisAgent, "ExecutionCycle");
		} catch (Exception e) {
			System.err
					.println("WARNING: Unable to deregister execution cycle of agent "
							+ thisAgent.getAgentName()
							+ " of agent node "
							+ thisAgent.getAgentNode().getName()
							+ " as JMX resource.");
			System.err.println(e.getMessage());
		}

		_manager = null;
	}

	@SuppressWarnings("serial")
	public static class TimeoutException extends RuntimeException {
		public TimeoutException(String s) {
			super(s);
		}
	}

}
