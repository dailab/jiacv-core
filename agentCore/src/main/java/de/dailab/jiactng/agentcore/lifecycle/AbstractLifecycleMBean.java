package de.dailab.jiactng.agentcore.lifecycle;

/**
 * Common management interface of all lifecycle-aware resources.
 * @author Jan Keiser
 */
public interface AbstractLifecycleMBean {

	/**
	 * Getter for attribute "LifecycleState" of the managed resource.
	 * @return the lifecycle state of this resource
	 */
	public String getLifecycleState();
	
	/**
	 * Getter for attribute "Strict" of the managed resource.
	 * @return the lifecycle mode of this resource
	 */
	public boolean isStrict();
	
	/**
	 * Initializes the managed resource.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#init()
	 */
	public void init() throws Exception;

	/**
	 * Starts the managed resource.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
	 *
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#start()
	 */
	public void start() throws Exception;

	/**
	 * Stops the managed resource.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#stop()
	 */
	public void stop() throws Exception;

	/**
	 * Cleanes up the managed resource.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#cleanup()
	 */
	public void cleanup() throws Exception;

}
