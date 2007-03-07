package de.dailab.jiactng.agentcore;

/**
 * JMX compliant management interface of agents to get information
 * about them and to change their lifecycle states.
 * 
 * @author Jan Keiser
 */
public interface AgentMBean {

	/**
	 * Getter for attribute "Name" of the managed agent.
	 */
	public String getName();

	/**
	 * Getter for attribute "AgentNodeUUID" of the managed agent.
	 */
	public String getAgentNodeUUID();

	//public Service[] getServices();
	//public String getDeploymentDescriptor();

	//public URL[] getAddresses();

	/**
	 * Getter for attribute "LifecycleState" of the managed agent.
	 */
	public String getLifecycleState();
	
	//public long getInitTime();
	
	//public long getStartTime();

	//public String getOwner();

	/**
	 * Initializes the managed agent.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#init()}
	 */
	public void init() throws Exception;

	/**
	 * Starts the managed agent.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
	 *
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#start()}
	 */
	public void start() throws Exception;

	/**
	 * Stops the managed agent.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#stop()}
	 */
	public void stop() throws Exception;

	/**
	 * Cleanes up the managed agent.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#cleanup()}
	 */
	public void cleanup() throws Exception;

}
