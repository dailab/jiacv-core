package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

/**
 * Interface for service library.
 * 
 * @author Jan Keiser
 */
public interface IServiceLibrary extends ILifecycle {

	/**
	 * Getter for attribute "code" of the service library.
	 * @return the DFL code of the services
	 */
	public String getServices();

	/**
	 * Setter for attribute "code" of the service library.
	 * @param code the DFL code of the services
	 */
	public void setServices(String code);

	/**
	 * Setter for memory of the agent that holds this bean.
	 * 
	 * @param memory the IMemory instance.
	 */
	public void setMemory(IMemory memory);

}
