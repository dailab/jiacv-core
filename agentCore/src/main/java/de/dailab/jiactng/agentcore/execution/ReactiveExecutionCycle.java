package de.dailab.jiactng.agentcore.execution;

import java.util.Set;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * A reactive ExecutionCycle implementation. It only executes the active 
 * agent beans (<code>executionInterval</code> greater than 0) in a cycle. 
 * Action requests (<code>DoAction</code>) and their results 
 * (<code>ActionResult</code>) are directly processed by a memory observer.
 * All bean operations will be executed in a parallel manner by using the
 * thread pool of the agent node.
 * 
 * @author Jan Keiser
 * 
 * @see IExecutionCycle
 * @see DoAction
 * @see ActionResult
 * @see SpaceObserver
 */
public class ReactiveExecutionCycle extends NonBlockingExecutionCycle {

	/**
	 * Implementation of the memory observer, which handles action requests
	 * and action results. 
	 * @author Jan Keiser
	 */
	@SuppressWarnings("serial")
	private class ActionObserver implements SpaceObserver<IFact> {

		public void notify(SpaceEvent<? extends IFact> event) {
			// check only for write events in memory
			if (event instanceof WriteCallEvent) {
				IFact obj = ((WriteCallEvent<IFact>) event).getObject();

				if (obj instanceof DoAction) {
		            log.debug("New DoAction to be handled.");

		            //handle all instances of DoAction
		            handleAllDoActions();
				}

				else if (obj instanceof ActionResult) {
		            log.debug("New ActionResult to be handled.");
					
		            //handle all instances of ActionResult
		            handleAllActionResults();
				}
			}
		}
	}

	private ActionObserver observer = new ActionObserver();

	/**
	 * Starts the memory observer and handles already existing action requests
	 * and action results.
	 * @see de.dailab.jiactng.agentcore.knowledge.IMemory#attach(SpaceObserver)
	 */
	@Override
	public void doStart() throws Exception {
		super.doStart();

		//start listen for DoAction and ActionResult
		memory.attach(observer);

		//handle already existing instances of DoAction and ActionResult
        handleAllDoActions();
        handleAllActionResults();
	}

	/**
	 * Stops the memory observer.
	 * @see de.dailab.jiactng.agentcore.knowledge.IMemory#detach(SpaceObserver)
	 */
	@Override
	public void doStop() throws Exception {
		// stop listen for DoAction and ActionResult
		memory.detach(observer);
		super.doStop();
	}

	/**
	 * Cancels timed-out threads and processes auto-execution services, the
	 * agent bean execution and action cancellations.
	 * @see NonBlockingExecutionCycle#processFutureTimeouts()
	 * @see NonBlockingExecutionCycle#processAutoExecutionServices()
	 * @see NonBlockingExecutionCycle#processBeanExecution()
	 * @see NonBlockingExecutionCycle#processSessionTimeouts()
	 */
	@Override
	public void run() {
		processFutureTimeouts();

		// check if lifecycle has been started --> execute if STARTED
		if (getState() == LifecycleStates.STARTED) {
			processAutoExecutionServices();

			// execute the ripest bean
			processBeanExecution();

			// Session-Cleanup, if Session has a timeout
			processSessionTimeouts();
		}

		// reject execution if ReactiveExecutionCycle hasn't been started
		else {
			log.error("Trying to run ReactiveExecutionCycle in state " + getState());
		}
	}

	/**
	 * Handles all action requests stored in the memory.
	 */
	protected void handleAllDoActions() {
        Set<DoAction> doActions = memory.removeAll(DOACTION_TEMPLATE);
        for (DoAction act : doActions) {
        	processDoAction(act);
        }		
	}

	/**
	 * Handles all action results stored in the memory.
	 */
	protected void handleAllActionResults() {
        Set<ActionResult> actionResults = memory.removeAll(ACTIONRESULT_TEMPLATE);
        for (ActionResult result : actionResults) {
        	processActionResult(result);
        }
	}

	/**
	 * Updates the workload only for agent bean executions. The workload of
	 * processing action requests and action results are ignored.
	 */
	@Override
	protected void updateWorkload(int type, boolean active) {
		// only execution workload is relevant for ReactiveExecutionCycle
		// because handling DoAction and ActionResult are delegated to memory observer
		if (type == EXECUTION) {
			super.updateWorkload(type, active);
		}
	}

}
