/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * A simple ExecutionCycle implementation. This class implements a round robin cycle for the adaptors.
 * 
 * @author Thomas Konnerth
 */
public class SimpleExecutionCycle extends AbstractLifecycle implements IExecutionCycle {

	/**
	 * time to sleep to be nice to other threads/processes, in milliseconds
	 */
	private int BE_NICE_TIMER = 20;

	/**
	 * Reference to the agent. Used to retrieve the list of adaptors. Note that the list is actualized only after each
	 * adaptor has been called once in a cycle.
	 */
	private IAgent agent = null;

	/**
	 * Synchronization flag for the thread. Used by the doStep method.
	 */
	private Boolean syncFlag = new Boolean(true);

	/**
	 * Activity flag. Used by statechanges
	 */
	private boolean active = false;

	/**
	 * Constructor for the class. For creation the reference to the agent is needed, as the list of adators is taken from
	 * that reference.
	 * 
	 * @param agent the reference to the agent.
	 */
	public SimpleExecutionCycle() {
	// this.agent = agent;
	}

	/*
	 * This method triggers the execution of the next adaptor in the list. The syncFlag is used to notify the Thread.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doStep() public void doStep() { synchronized (syncFlag) {
	 *      syncFlag.notify(); } }
	 */

	/**
	 * Run-method for the execution cycle. The method iterates over the list of adators and calls the execute method of
	 * each adaptor. The call is only performed when the syncFlag-object is notified via the doStep-method. Note that the
	 * list of adators is updated every cycle, i.e. whenever all adaptors have been executed a new list is retrieved from
	 * the agent-reference. The run method stays active only as long as the active-flag is set to true.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#run()
	 */
	public void run() {
		if (active) {
			for (IAgentBean a : agent.getAdaptors()) {
				if (LifecycleStates.STARTED.equals(a.getState())) {
					try {
						Thread.sleep(BE_NICE_TIMER);
						a.execute();
					} catch (Exception ex) {
						ex.printStackTrace();
						try {
							a.stop();
						} catch (LifecycleException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						// Agent.setBeanState(a.beanName, LifecycleStates.STOPPED);
					}
					// throw new RuntimeException(((DummyBean)a).getTest());
				}
			}
		}
	}

	/**
	 * Cleanup-method for the lifecycle. The active-flag is set to false an the thread is set to null.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doCleanup()
	 */
	@Override
	public void doCleanup() {
		synchronized (syncFlag) {
			active = false;
		}
	}

	/**
	 * Init-method for the lifecycle. The thread is created if no thread exists.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doInit()
	 */
	@Override
	public void doInit() {
		synchronized (syncFlag) {

		}
	}

	/**
	 * Start-method for the lifecycle. The active-flag is set to true and the thread is started.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doStart()
	 */
	@Override
	public void doStart() {
		synchronized (syncFlag) {
			active = true;
			// agent.getThreadPool().execute(this);
		}
	}

	/**
	 * Stop-method for the lifecycle. The active-flag is set to false.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#doStop()
	 */
	@Override
	public void doStop() {
		synchronized (syncFlag) {
			active = false;
		}
	}

	public void setAgent(IAgent agent) {
		this.agent = agent;
	}

}
