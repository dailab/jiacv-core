package de.dailab.jiactng.agentcore;

import java.util.ArrayList;

/**
 * JMX compliant management interface of agents to get information
 * about them and to change their lifecycle states.
 * 
 * @author Jan Keiser
 */
public interface AgentMBean {

	/**
	 * Getter for attribute "Name" of the managed agent.
	 * @return the name of this agent
	 */
	public String getName();

	/**
	 * Getter for attribute "AgentId" of the managed agent.
	 * @return the agent identifier of this agent
	 */
	public String getAgentId();

	/**
	 * Getter for attribute "AgentNodeUUID" of the managed agent.
	 * @return the UUID of the agent node where this agent is running on
	 */
	public String getAgentNodeUUID();

	//public Service[] getServices();
	//public String getDeploymentDescriptor();

	//public URL[] getAddresses();

	/**
	 * Getter for attribute "LifecycleState" of the managed agent.
	 * @return the lifecycle state of this agent
	 */
	public String getLifecycleState();
	
	//public long getInitTime();
	
	//public long getStartTime();

	/**
	 * Getter for attribute "Owner" of the managed agent.
	 * @return the owner of this agent
	 */
	public String getOwner();

	/**
	 * Undeploys this agent from its agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
	 */
	public void remove() throws Exception;

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

	/**
	 * Getter for attribute "AgentBeanNames" of the managed agent.
	 * @return name of agent beans contained in this agent
	 */
	public ArrayList<String> getAgentBeanNames();

}
