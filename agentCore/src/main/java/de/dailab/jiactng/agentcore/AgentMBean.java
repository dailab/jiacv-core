package de.dailab.jiactng.agentcore;

import java.util.ArrayList;

import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * JMX compliant management interface of agents to get information
 * about them and to change their lifecycle states.
 * 
 * @author Jan Keiser
 */
public interface AgentMBean extends AbstractLifecycleMBean {

	/**
	 * Getter for attribute "AgentName" of the managed agent.
	 * @return the name of this agent
	 */
	public String getAgentName();

	/**
	 * Setter for attribute "AgentName" of the managed agent.
	 * @param agentname the new name of the agent
	 */
	public void setAgentName(String agentname);

	/**
	 * Getter for attribute "AgentId" of the managed agent.
	 * @return the agent identifier of this agent
	 */
	public String getAgentId();

	/**
	 * Getter for attribute "Owner" of the managed agent.
	 * @return the owner of this agent
	 */
	public String getOwner();

	/**
	 * Setter for attribute "Owner" of the managed agent.
	 * @param owner the new owner of this agent
	 */
	public void setOwner(String owner);

	/**
	 * Returns the timeout after which the execution of a bean of the managed agent will be stopped.
	 * @return the timeout in milliseconds
	 */
	public long getBeanExecutionTimeout();

	/**
	 * Sets the timeout after which the execution of a bean of the managed agent will be stopped.
	 * @param beanExecutionTimeout the new timeout in milliseconds
	 */
	public void setBeanExecutionTimeout(long beanExecutionTimeout);

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
     *
     * @throws LifecycleException if an error occurs during stop or cleanup of this agent.
	 */
	public void remove() throws LifecycleException;

	/**
	 * Getter for attribute "AgentBeanNames" of the managed agent.
	 * @return name of agent beans contained in this agent
	 */
	public ArrayList<String> getAgentBeanNames();

}
