/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.util.ArrayList;

import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * A simple ExecutionCycle implementation. This class implements a round robin
 * cycle for the agentbeans.
 * 
 * @author Thomas Konnerth
 */
/**
 * @author moekon
 * 
 */
public class SimpleExecutionCycle extends AbstractAgentBean implements
		IExecutionCycle {

	/** List of times at which execute-methods should be called */
	private ArrayList<Long> executeTimes = null;

	/** List of beans which have execute-methods that should be called. */
	private ArrayList<IAgentBean> executeList = null;

	/**
	 * time to sleep to be nice to other threads/processes, in milliseconds
	 */
	private int BE_NICE_TIMER = 20;

	/**
	 * Reference to the agent. Used to retrieve the list of agentbeans. Note
	 * that the list is actualized only after each agentbean has been called
	 * once in a cycle.
	 */
	private IAgent agent = null;

	/**
	 * Activity flag. Used by statechanges
	 */
	private boolean active = false;

	/**
	 * Constructor for the class. For creation the reference to the agent is
	 * needed, as the list of adators is taken from that reference.
	 */
	public SimpleExecutionCycle() {
		// this.agent = agent;
	}

	/*
	 * This method triggers the execution of the next agentbean in the list. The
	 * syncFlag is used to notify the Thread.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doStep() public void
	 *      doStep() { synchronized (syncFlag) { syncFlag.notify(); } }
	 */

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
		// call execute-methods of beans
		long currentTime = System.currentTimeMillis();
		if (executeTimes.size() > 0 && (executeTimes.get(0) <= currentTime)) {
			IAgentBean a = executeList.remove(0);
			executeTimes.remove(0);
			if (a.getExecuteIntervall() > -1) {
				synchronized (this) {
					if (active) {
						callBeanExecute(a);
						scheduleNextExecute(currentTime, a);
					}
				}

			}
		}

		// for (IAgentBean a : agent.getAgentBeans()) {
		// if (a.getExecuteIntervall()>-1) {
		// synchronized (this) {
		// if (active) {
		// callBeanExecute(a);
		// long nextCall = System.currentTimeMillis()+a.getExecuteIntervall();
		// executeMap.put();
		// }
		// }
		// }
		// }

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

	/**
	 * Utility-Method for inserting the next call-time for an agentbeans
	 * execute-method into the list.
	 * 
	 * @param currentTime
	 *            the time at which the execute-method should be called (best
	 *            effort).
	 * @param a
	 *            the bean for which the execute-method should be called.
	 */
	private void scheduleNextExecute(long currentTime, IAgentBean a) {
		if (a.getExecuteIntervall() > -1) {
			long nextCall = currentTime + a.getExecuteIntervall();
			int index = 0;
			for (Long l : executeTimes) {
				if (l > nextCall) {
					index++;
				} else {
					break;
				}
			}
			executeTimes.add(index, nextCall);
			executeList.add(index, a);
			// TODO: removed beans should be taken out.
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

	private void callBeanExecute(IAgentBean a) {
		if (LifecycleStates.STARTED.equals(a.getState())) {
			try {
				Thread.sleep(BE_NICE_TIMER);
				a.execute();
			} catch (Exception ex) {
				log.error("Error when executing bean \'" + a.getBeanName()
						+ "\'", ex);
				try {
					a.stop();
				} catch (LifecycleException e) {
					// TODO Auto-generated catch block
					log.error(
							"Could not stop Bean \'" + a.getBeanName() + "\'",
							e);
				}
				// Agent.setBeanState(a.beanName,
				// LifecycleStates.STOPPED);
			}
			// throw new RuntimeException(((DummyBean)a).getTest());
		}
	}

	/**
	 * Cleanup-method for the lifecycle. The active-flag is set to false an the
	 * thread is set to null.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doCleanup()
	 */
	@Override
	public void doCleanup() {
		executeTimes = null;
		executeList = null;
	}

	/**
	 * Init-method for the lifecycle. The thread is created if no thread exists.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doInit()
	 */
	@Override
	public void doInit() {
		executeList = new ArrayList<IAgentBean>();
		executeTimes = new ArrayList<Long>();
		long currentTime = System.currentTimeMillis();
		for (IAgentBean a : agent.getAgentBeans()) {
			scheduleNextExecute(currentTime, a);
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
	 * {@inheritDoc}
	 */
	public void setAgent(IAgent agent) {
		this.agent = agent;
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
			manager.registerAgentResource(agent, "ExecutionCycle", this);
		} catch (Exception e) {
			System.err
					.println("WARNING: Unable to register execution cycle of agent "
							+ agent.getAgentName()
							+ " of agent node "
							+ agent.getAgentNode().getName()
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
			_manager.unregisterAgentResource(agent, "ExecutionCycle");
		} catch (Exception e) {
			System.err
					.println("WARNING: Unable to deregister execution cycle of agent "
							+ agent.getAgentName()
							+ " of agent node "
							+ agent.getAgentNode().getName()
							+ " as JMX resource.");
			System.err.println(e.getMessage());
		}

		_manager = null;
	}
}
