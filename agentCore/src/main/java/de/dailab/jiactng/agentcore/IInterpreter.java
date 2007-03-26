package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

/**
 * Interface for interpreter.
 * 
 * @author Jan Keiser
 */
public interface IInterpreter extends ILifecycle {

	/**
	 * Setter for memory of the agent that holds this bean.
	 * 
	 * @param memory the IMemory instance.
	 */
	public void setMemory(IMemory memory);

	/**
	 * Setter for service library of the agent that holds this bean.
	 * 
	 * @param serviceLib the IServiceLibrary instance.
	 */
	public void setServiceLibrary(IServiceLibrary serviceLib);

	/**
	 * Executes a service that is implemented in DFL and deployed 
	 * in the service library of the agent.
	 * 
	 * @param serviceName name of the service
	 */
	public void execute(String serviceName);

}
