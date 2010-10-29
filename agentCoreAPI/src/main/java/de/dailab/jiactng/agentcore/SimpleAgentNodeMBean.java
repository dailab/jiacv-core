package de.dailab.jiactng.agentcore;

import java.net.UnknownHostException;
import java.util.List;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.util.jar.JARMemory;

/**
 * JMX compliant management interface of agent nodes to get information
 * about them and to change their life-cycle states.
 * 
 * @author Jan Keiser
 */
public interface SimpleAgentNodeMBean extends AbstractLifecycleMBean {

	/**
	 * Getter for attribute "Name" of the managed agent node.
	 * @return the name of this agent node
	 */
	String getName();

	/**
	 * Setter for attribute "Name" of the managed agent node.
	 * @param newName the new name of this agent node
	 */
	void setName(String newName);

	/**
	 * Getter for attribute "UUID" of the managed agent node.
	 * @return the UUID of this agent node
	 */
	String getUUID();

	/**
	 * Getter for attribute "Host" of the managed agent node.
	 * @throws UnknownHostException if no IP address for the host could be found.
	 * @return the host where this agent node is running
	 */
	String getHost() throws UnknownHostException;

	/**
	 * Gets the name of the agent platform, which is defined by the group of the discovery URI.
	 * Only agent nodes which belongs to the same agent platform are able to communicate.
	 * @return the platform name or <code>null</code> if no network connector exist or the
	 * discovery URI of the network connector does not define a group. 
	 */
	String getPlatformName();

	/**
	 * Getter for attribute "Agents" of the managed agent node.
	 * @return the unique identifier of agents running on this agent node
	 */
	List<String> getAgents();

	/**
	 * Deploys but does not initialize new agents on this agent node.
	 * @param configuration The spring configuration of the agents in XML syntax.
	 * @param libraries The list of agent-specific JARs.
	 * @param owner The owner of the new agents.
	 * @return the IDs of the created agents.
	 * @throws Exception if the agents can not be created.
	 */
	List<String> addAgents(byte[] configuration, List<JARMemory> libraries, String owner) throws Exception;

	/**
	 * Getter for attribute "Owner" of the managed agent node.
	 * @return the owner of this agent node
	 */
	String getOwner();
	
	/**
	 * Getter for attribute "JiacVersion" of the managed agent node.
	 * @return the version of JIAC TNG used by this agent node
	 */
	String getJiacVersion();
	
	/**
	 * Getter for attribute "JiacVendor" of the managed agent node.
	 * @return the vendor of JIAC TNG used by this agent node
	 */
	String getJiacVendor();

	/**
	 * Getter for attribute "LoggingConfig" of the managed agent node.
	 * @return the filename of the logging configuration
	 */
	String getLoggingConfig();

	/**
	 * Adds a socket appender to the logger of the agent node, all agent
	 * node beans and all agents, which connects to a remote server at 
	 * specified address and port.
	 * @param address The IP address of the logging server.
	 * @param port The port of the logging port.
	 */
	void addLog4JSocketAppender(String address, int port);

	/**
	 * Removes a socket appender from the logger of the agent node, all agent
	 * node beans and all agents, which connects to a remote server at 
	 * specified address and port.
	 * @param address The IP address of the logging server.
	 * @param port The port of the logging port.
	 */
	void removeLog4JSocketAppender(String address, int port);

	/**
	 * Getter for attribute "AgentNodeBeanClasses" of the managed agent node.
	 * @return the class of agent beans running in this agent node
	 */
	List<String> getAgentNodeBeanClasses();

	/**
	 * Getter for attribute "DirectoryName" of the managed agent node.
	 * @return the name of the directory agent node bean or null if not exists
	 */
	String getDirectoryName();

	/**
	 * Shuts down the managed agent node.
     * @throws LifecycleException if an error occurs during stop and cleanup of this agent node.
	 */
	void shutdown() throws LifecycleException;
}
