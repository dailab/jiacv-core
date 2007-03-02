package de.dailab.jiactng.agentcore;

/**
 * @author Jan Keiser
 *
 */
public interface SimpleAgentNodeMBean {

	/**
	 * Getter for attribute "Name" of the managed agent node.
	 */
	public String getName();

	/**
	 * Getter for attribute "UUID" of the managed agent node.
	 */
	public String getUUID();

	//public String getHost();

	//public Agent[] getAgents();

	/**
	 * Getter for attribute "LifecycleState" of the managed agent node.
	 */
	public String getLifecycleState();
	
	//public long getInitTime();
	
	//public long getStartTime();

	//public String getOwner();
	
	//public String getJiacVersion();
	
	//public String getJiacVendor();

	/**
	 * Initializes the managed agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#init()}
	 */
	public void init() throws Exception;

	/**
	 * Starts the managed agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
	 *
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#start()}
	 */
	public void start() throws Exception;

	/**
	 * Stops the managed agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#stop()}
	 */
	public void stop() throws Exception;

	/**
	 * Cleanes up the managed agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#cleanup()}
	 */
	public void cleanup() throws Exception;

	//public void deployAgent(DeploymentDescriptor descriptor);
	
	//public void undeployAgent(Agent agent);
	
}
