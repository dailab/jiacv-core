package de.dailab.jiactng.agentcore;

import java.util.ArrayList;

import javax.management.openmbean.CompositeData;

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
	 * Returns the timeout after which the execution of a bean of the managed agent will be stopped.
	 * @return the timeout in milliseconds
	 */
	public long getBeanExecutionTimeout();

	/**
	 * Getter for attribute "ActionNames" of the managed agent.
	 * @return name of actions provided by this agent
	 */
	public ArrayList<String> getActionNames();

	/**
	 * Getter for attribute "Logger" of the managed agent.
	 * @return information about the logger of this agent
	 */
	public CompositeData getLogger();

	/**
	 * Getter for attribute "MemoryData" of the managed agent.
	 * @return implementation of the memory of this agent
	 */
	public CompositeData getMemoryData();

	/**
	 * Getter for attribute "ExecutionCycleClass" of the managed agent.
	 * @return implementation of the execution cycle of this agent
	 */
	public String getExecutionCycleClass();

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
