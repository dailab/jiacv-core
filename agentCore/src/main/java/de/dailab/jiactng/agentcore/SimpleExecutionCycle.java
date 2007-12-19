/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.management.Manager;

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

	/**
	 * Activity flag. Used by statechanges
	 */
	private boolean active = false;

	/**
	 * Run-method for the execution cycle. The method iterates over the list of
	 * adators and calls the execute method of each agentbean. The call is only
	 * performed when the syncFlag-object is notified via the doStep-method.
	 * Note that the list of adators is updated every cycle, i.e. whenever all
	 * agentbeans have been executed a new list is retrieved from the
	 * agent-reference. The run method stays active only as long as the
	 * active-flag is set to true.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#run()
	 */
	public void run() {
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
			minBean.setNextExecutionTime(now + minBean.getExecuteInterval());
		}

		// process one doAction
		// TODO: check if read can be used
		DoAction act = memory.remove(new DoAction(null, null, null, null));

		if (act != null) {
			synchronized (this) {
				if (active) {
					performDoAction(act);
				}
			}
		}

		// process one actionResult
		// TODO: check if read can be used
		ActionResult actionResult = memory.remove(new ActionResult(null, null,
				null, null));
		if (actionResult != null) {
			synchronized (this) {
				if (active) {
					processResult(actionResult);
				}
			}
		}

	}

	private void processResult(ActionResult actionResult) {
		// entweder den ResultReceiver informieren oder das Ergebnis in
		// den Memory schreiben
		if (((DoAction) actionResult.getSource()) == null
				|| ((DoAction) actionResult.getSource()).getSource() == null) {
			memory.write(actionResult);
		} else {
			((ResultReceiver) ((DoAction) actionResult.getSource()).getSource())
					.receiveResult(actionResult);
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

	private void performDoAction(DoAction act) {
		if (act.getAction().getProviderBean() != null) {
			memory.write(act.getSession());
			act.getAction().getProviderBean().doAction(act);
		} else {
			log.error("--- found action without bean: "
					+ act.getAction().getName());
		}
	}

	/**
	 * Start-method for the lifecycle. The active-flag is set to true and the
	 * thread is started.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doStart()
	 */
	@Override
	public synchronized void doStart() {
		active = true;
	}

	/**
	 * Stop-method for the lifecycle. The active-flag is set to false.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doStop()
	 */
	@Override
	public synchronized void doStop() {
		active = false;
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
	 * 
	 * @param manager
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
}
