/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.Map;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

/**
 * Interface for Agent ExecutionCycles. This interface is used to decouple
 * different ExecutionCycle implementations from the agent. The basic
 * Functionality of an ExecutionCycle is to manage the thread that is used to
 * execute the different agentbeans of the agent, and decide in which order these
 * agentbeans should get executiontime. The actual strategy of the ExecutionCycle
 * is hidden from the agent, and the interface presents only an extension of
 * runnable as well as a doStep method that triggers the next component.
 * 
 * @author Thomas Konnerth
 */
public interface IExecutionCycle extends ILifecycle, Runnable {

  /*
   * This method triggers the execution of the next agentbean in the list.
   */
//  void doStep();

	/**
	 * Sets the agent of this execution cycle.
	 * @param agent the agent
	 */
  void setThisAgent(IAgent agent);

	/**
	 * Sets the memory to be used by this execution cycle.
	 * @param memory the agent memory
	 */
  void setMemory(IMemory memory);
  
	/**
	 * Sets the actions which will be automatically executed.
	 * @param actionIds the list of action names
	 */
  void setAutoExecutionServices(Map<String, Map<String, Serializable>> autoExecutionServices);
  
	/**
	 * Gets the actions which are automatically executed.
	 * @return the list of action names
	 */
  Map<String, Map<String, Serializable>> getAutoExecutionServices();
  
}