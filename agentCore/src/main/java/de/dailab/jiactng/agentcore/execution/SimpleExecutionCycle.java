/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.execution;

import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;

/**
 * A simple ExecutionCycle implementation. This class executes active agentbeans (those agentbeans where the
 * <code>executionInterval</code> is set to a value greater than 0) and takes care of action requests (
 * <code>DoAction</code>) and their results (<code>ActionResult</code>).
 * 
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see IExecutionCycle
 * @see DoAction
 * @see ActionResult
 */
public final class SimpleExecutionCycle extends BasicExecutionCycle {

	/**
	 * execution cycle processes are exeuted directly, in the same thread.
	 */
	@Override
	protected void performExecutionCycleProcess(Runnable process) {
		process.run();
	}
	
  /**
   * Run-method for the execution cycle. The method iterates over the list of agentbeans and calls the execute method of
   * each <i>active</i> agentbean.
   * 
   * This method also takes care of new <code>DoAction</code>s and <code>ActionResult</code>s.
   * 
   * The <code>SimpleExecutionCycle</code> only executes agentbeans and handles DoActions and ActionResults when it has
   * reached <code>LifecycleStates.STARTED</code>.
   * 
   * @see de.dailab.jiactng.agentcore.execution.IExecutionCycle#run()
   * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
   */
  public void run() {
    // check if lifecycle has been started --> execute if STARTED
    if (getState() == LifecycleStates.STARTED) {

      // execute one Beans execute Method
      processBeanExecution();

      // process one DoAction
      final DoAction act = memory.remove(DOACTION_TEMPLATE);
      processDoAction(act);

      // process one ActionResult
      final ActionResult result = memory.remove(ACTIONRESULT_TEMPLATE);
      processActionResult(result);

      processSessionTimeouts();
      
      processAutoExecutionServices();

    }

    // reject execution if SimpleExecutionCycle hasn't been started
    else {
      log.error("Trying to run SimpleExecutionCycle in state " + getState());
    }
  }

}
