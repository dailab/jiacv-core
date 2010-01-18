package de.dailab.jiactng.agentcore;

import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

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
	String getAgentName();

	/**
	 * Setter for attribute "AgentName" of the managed agent.
	 * @param agentname the new name of the agent
	 */
	void setAgentName(String agentname);

	/**
	 * Getter for attribute "AgentId" of the managed agent.
	 * @return the agent identifier of this agent
	 */
	String getAgentId();

	/**
	 * Getter for attribute "Owner" of the managed agent.
	 * @return the owner of this agent
	 */
	String getOwner();

	/**
	 * Setter for attribute "Owner" of the managed agent.
	 * @param owner the new owner of this agent
	 */
	void setOwner(String owner);

	/**
	 * Returns the timeout after which the execution of a bean of the managed agent will be stopped.
	 * @return the timeout in milliseconds
	 */
	long getBeanExecutionTimeout();

	/**
	 * Sets the timeout after which the execution of a bean of the managed agent will be stopped.
	 * @param beanExecutionTimeout the new timeout in milliseconds
	 */
	void setBeanExecutionTimeout(long beanExecutionTimeout);

	/**
	 * Getter for attribute "ActionNames" of the managed agent.
	 * @return name of actions provided by this agent
	 */
	List<String> getActionNames();

	/**
	 * Getter for attribute "MemoryData" of the managed agent.
	 * @return implementation of the memory of this agent
	 */
	CompositeData getMemoryData();

	/**
	 * Getter for attribute "ExecutionCycleClass" of the managed agent.
	 * @return implementation of the execution cycle of this agent
	 */
	String getExecutionCycleClass();

	/**
	 * Undeploys this agent from its agent node.
     *
     * @throws LifecycleException if an error occurs during stop or cleanup of this agent.
	 */
	void remove() throws LifecycleException;

	/**
	 * Getter for attribute "AgentBeanNames" of the managed agent.
	 * @return name of agent beans contained in this agent
	 */
	List<String> getAgentBeanNames();

	/**
	 * Gets the date when this managed resource will be started automatically.
	 * @return the date in msec since 1/1/1970 0am or null if no automatically start will take place.
	 * @throws InstanceNotFoundException if the agent node timer is not available.
	 */
	Long getStartTime() throws InstanceNotFoundException;

	/**
	 * Sets the date when this managed resource will be started automatically.
	 * @param startTime the date in msec since 1/1/1970 0am or null if no automatically start should take place.
	 * @throws InstanceNotFoundException if the agent node timer is not available.
	 */
	void setStartTime(Long startTime) throws InstanceNotFoundException;

	/**
	 * Gets the date when this managed resource will be stopped automatically.
	 * @return the date in msec since 1/1/1970 0am or null if no automatically stop will take place.
	 * @throws InstanceNotFoundException if the agent node timer is not available.
	 */
	Long getStopTime() throws InstanceNotFoundException;

	/**
	 * Sets the date when this managed resource will be stopped automatically.
	 * @param stopTime the date in msec since 1/1/1970 0am or null if no automatically stop should take place.
	 * @throws InstanceNotFoundException if the agent node timer is not available.
	 */
	void setStopTime(Long stopTime) throws InstanceNotFoundException;

	/**
	 * Sets the auto Execution service ID list.
	 * @param actionIds IDs of actions, which should be automatically executed.
	 */
	void setAutoExecutionServices(List<String> actionIds);
	
	/**
	 * Returns the auto execution service ID list.
	 * @return auto execution service list
	 */
	List<String> getAutoExecutionServices();
	
	/**
	 * Sets the auto execution type.
	 * @param continous <code>true</code> if the automatic actions should be continuously executed.
	 */
	void setAutoExecutionType(boolean continous);
	
	/**
	 * Gets the auto execution type.
	 * @return the auto execution type
	 */
	boolean getAutoExecutionType();
	
	/**
	 * Gets the Spring configuration XML snippet.
	 * @return Spring config as byte array
	 */
	byte[] getSpringConfigXml();
	
	/**
	 * Gets the Agent Description for this agent.
	 * @return Agent Description
	 */
	IAgentDescription getAgentDescription();

	
}
