/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

/**
 * Interface for Agent ExecutionCycles. This interface is used to decouple
 * different ExecutionCycle implementations from the agent. The basic
 * Functionality of an ExecutionCycle is to manage the thread that is used to
 * execute the different adaptors of the agent, and decide in which order these
 * adaptors should get executiontime. The actual strategy of the ExecutionCycle
 * is hidden from the agent, and the interface presents only an extension of
 * runnable as well as a doStep method that triggers the next component.
 * 
 * @author Thomas Konnerth
 */
public interface IExecutionCycle extends ILifecycle, Runnable {

  /**
   * This method triggers the execution of the next adaptor in the list.
   */
  public void doStep();

  public Agent getAgent();

  public void setAgent(Agent agent);
}