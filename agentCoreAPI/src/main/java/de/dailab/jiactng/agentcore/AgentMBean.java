package de.dailab.jiactng.agentcore;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * JMX compliant management interface of agents to get information
 * about them and to change their life-cycle states.
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
	 * @param newOwner the new owner of this agent
	 */
	void setOwner(String newOwner);

	/**
	 * Returns the timeout after which the execution of a bean of the managed agent will be stopped.
	 * @return the timeout in milliseconds
	 */
	long getBeanExecutionTimeout();

	/**
	 * Sets the timeout after which the execution of a bean of the managed agent will be stopped.
	 * @param newBeanExecutionTimeout the new timeout in milliseconds
	 */
	void setBeanExecutionTimeout(long newBeanExecutionTimeout);

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
	 * @return the date in milliseconds since 1/1/1970 0am or null if no automatically start will take place.
	 * @throws InstanceNotFoundException if the agent node timer is not available.
	 */
	Long getStartTime() throws InstanceNotFoundException;

	/**
	 * Sets the date when this managed resource will be started automatically.
	 * @param newStartTime the date in milliseconds since 1/1/1970 0am or null if no automatically start should take place.
	 * @throws InstanceNotFoundException if the agent node timer is not available.
	 */
	void setStartTime(Long newStartTime) throws InstanceNotFoundException;

	/**
	 * Gets the date when this managed resource will be stopped automatically.
	 * @return the date in milliseconds since 1/1/1970 0am or null if no automatically stop will take place.
	 * @throws InstanceNotFoundException if the agent node timer is not available.
	 */
	Long getStopTime() throws InstanceNotFoundException;

	/**
	 * Sets the date when this managed resource will be stopped automatically.
	 * @param newStopTime the date in milliseconds since 1/1/1970 0am or null if no automatically stop should take place.
	 * @throws InstanceNotFoundException if the agent node timer is not available.
	 */
	void setStopTime(Long newStopTime) throws InstanceNotFoundException;

	/**
	 * Sets the auto Execution service ID list.
	 * @param actionIds IDs of actions, which should be automatically executed.
	 */
	void setAutoExecutionServices(Map<String, Map<String, Serializable>> autoExecutionServices);
	
	/**
	 * Returns the auto execution service ID list.
	 * @return auto execution service list
	 */
	Map<String, Map<String, Serializable>> getAutoExecutionServices();
	
	/**
	 * Gets the Spring configuration XML snippet.
	 * @return Spring configuration as byte array
	 */
	byte[] getSpringConfigXml();
	
	/**
	 * Gets the Agent Description for this agent.
	 * @return Agent Description
	 */
	IAgentDescription getAgentDescription();

	/**
	 * Gets the name of the agent specific JARs.
	 * @return the list of JAR names or <code>null</code> if the agent does not use a JARClassLoader.
	 * @see de.dailab.jiactng.agentcore.util.jar.JARClassLoader
	 */
	List<String> getJarNames();

	/**
	 * Tries to load a given class.
	 * @param className the name of the class.
	 * @throws ClassNotFoundException if the class was not found by the agent's class loader.
	 */
	void loadClass(String className) throws ClassNotFoundException;

}
