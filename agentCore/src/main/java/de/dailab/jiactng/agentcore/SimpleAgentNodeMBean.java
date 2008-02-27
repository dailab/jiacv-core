package de.dailab.jiactng.agentcore;

import java.net.UnknownHostException;
import java.util.List;

import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;

/**
 * JMX compliant management interface of agent nodes to get information
 * about them and to change their lifecycle states.
 * 
 * @author Jan Keiser
 */
public interface SimpleAgentNodeMBean extends AbstractLifecycleMBean {

	/**
	 * Getter for attribute "Name" of the managed agent node.
	 * @return the name of this agent node
	 */
	public String getName();

	/**
	 * Getter for attribute "UUID" of the managed agent node.
	 * @return the UUID of this agent node
	 */
	public String getUUID();

	/**
	 * Getter for attribute "Host" of the managed agent node.
	 * @throws UnknownHostException if no IP address for the host could be found.
	 * @return the host where this agent node is running
	 */
	public String getHost() throws UnknownHostException;

	/**
	 * Getter for attribute "Agents" of the managed agent node.
	 * @return the name of agents running on this agent node
	 */
	public List<String> getAgents();

	/**
	 * Deploys new agents on this agent node.
	 * @param configFile name of the XML file which contains the spring configuration of the agents
	 */
	public void addAgents(String configFile);

	/**
	 * Getter for attribute "Owner" of the managed agent node.
	 * @return the owner of this agent node
	 */
	public String getOwner();
	
	/**
	 * Getter for attribute "JiacVersion" of the managed agent node.
	 * @return the version of JIAC TNG used by this agent node
	 */
	public String getJiacVersion();
	
	/**
	 * Getter for attribute "JiacVendor" of the managed agent node.
	 * @return the vendor of JIAC TNG used by this agent node
	 */
	public String getJiacVendor();

	/**
	 * Getter for attribute "LoggingConfig" of the managed agent node.
	 * @return the filename of the logging configuration
	 */
	public String getLoggingConfig();

	/**
	 * Getter for attribute "Logger" of the managed agent node.
	 * @return information about the logger of this agent node
	 */
	public CompositeData getLogger();

	/**
	 * Getter for attribute "AgentNodeBeanClasses" of the managed agent node.
	 * @return the class of agent beans running in this agent node
	 */
	public List<String> getAgentNodeBeanClasses();

	/**
	 * Getter for attribute "AmqBroker" of the managed agent node.
	 * @return the configuration of the embedded ActiveMQ broker of this agent node
	 */
	public CompositeData getAmqBrokerValues();

	/**
	 * Shuts down the managed agent node.
     * @throws LifecycleException if an error occurs during stop and cleanup of this agent node.
	 */
	public void shutdown() throws Exception;
}
