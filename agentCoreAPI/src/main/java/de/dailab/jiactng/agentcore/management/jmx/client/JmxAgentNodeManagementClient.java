package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;
import java.util.List;

import javax.management.AttributeChangeNotificationFilter;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.jmx.JmxManager;
import de.dailab.jiactng.agentcore.util.jar.JARMemory;

/**
 * This JMX client enables the remote management of JIAC TNG agent nodes.
 * @author Jan Keiser
 */
public class JmxAgentNodeManagementClient extends JmxAbstractManagementClient {

	private static final AttributeChangeNotificationFilter agentsNotificationFilter = new AttributeChangeNotificationFilter();
	private static final AttributeChangeNotificationFilter lifecycleNotificationFilter = new AttributeChangeNotificationFilter();

	static {
		agentsNotificationFilter.enableAttribute("Agents");
		lifecycleNotificationFilter.enableAttribute("LifecycleState");
	}

	/**
	 * Creates a client for the management of an agent node.
	 * @param mbsc The JMX connection used for the agent node management.
	 * @param agentNodeID The UUID of the managed agent node.
	 * @throws MalformedObjectNameException The UUID of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentNodeManagementClient(MBeanServerConnection mbsc, String agentNodeID) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentNode(agentNodeID));
	}

	/**
	 * Gets the lifecycle state of the managed agent node.
	 * @return The agent node's lifecycle state.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#getLifecycleState()
	 */
	public String getAgentNodeState() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("LifecycleState");
	}

	/**
	 * Adds a listener for changes on the lifecycle state of the managed agent node.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent node.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent node.
	 * @throws InstanceNotFoundException The agent node does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent node for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	public void addLifecycleStateListener(NotificationListener listener) throws IOException, InstanceNotFoundException {
		addNotificationListener(listener, lifecycleNotificationFilter);
	}

	/**
	 * Removes a listener for changes on the lifecycle state from the managed agent node.
	 * @param listener The listener object which will no longer handle the notifications from the managed agent node.
	 * @throws IOException A communication problem occurred when removing the listener from the remote agent node.
	 * @throws InstanceNotFoundException The agent node does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed agent node.
	 * @throws SecurityException if the listener can not be removed from the agent node for security reasons.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	public void removeLifecycleStateListener(NotificationListener listener) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		removeNotificationListener(listener, lifecycleNotificationFilter);
	}

	/**
	 * Initializes the managed agent node.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws LifecycleException if an error occurs during initialization of the agent node.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#init()
	 */
	public void initAgentNode() throws IOException, LifecycleException, InstanceNotFoundException {
		changeState("init");
	}

	/**
	 * Starts the managed agent node.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws LifecycleException if an error occurs during start of the agent node.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#start()
	 */
	public void startAgentNode() throws IOException, LifecycleException, InstanceNotFoundException {
		changeState("start");
	}

	/**
	 * Stops the managed agent node.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws LifecycleException if an error occurs during stop of the agent node.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#stop()
	 */
	public void stopAgentNode() throws IOException, LifecycleException, InstanceNotFoundException {
		changeState("stop");
	}

	/**
	 * Cleans up the managed agent node.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws LifecycleException if an error occurs during cleanup of the agent node.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#cleanup()
	 */
	public void cleanupAgentNode() throws IOException, LifecycleException, InstanceNotFoundException {
		changeState("cleanup");
	}

	/**
	 * Gets the current log level of the managed agent node.
	 * @return The agent node's current log level.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#getLogLevel()
	 */
	public String getAgentNodeLogLevel() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("LogLevel");
	}

	/**
	 * Sets the current log level of the managed agent node.
	 * @param level The agent node's new log level.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#setLogLevel(String)
	 */
	public void setAgentNodeLogLevel(String level) throws IOException, InvalidAttributeValueException, InstanceNotFoundException {
		setAttribute("LogLevel", level);
	}

	/**
	 * Gets detailed information about the current logger of the managed agent node.
	 * @return The agent node's current logger.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#getLogger()
	 */
	public CompositeData getAgentNodeLogger() throws IOException, InstanceNotFoundException {
		return (CompositeData) getAttribute("Logger");
	}

	/**
	 * Gets the global unique identifier of the managed agent node.
	 * @return The agent node's global unique identifier.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getUUID()
	 */
	public String getAgentNodeID() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("UUID");
	}

	/**
	 * Gets the owner of the managed agent node.
	 * @return The name of agent node's owner.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getOwner()
	 */
	public String getAgentNodeOwner() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("Owner");
	}

	/**
	 * Gets all agents of the managed agent node.
	 * @return The list of the global unique agent identifiers.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getAgents()
	 */
	public List<String> getAgents() throws IOException, InstanceNotFoundException {
		return (List<String>) getAttribute("Agents");
	}

	/**
	 * Deploys and starts new agents on the managed agent node.
	 * @param configuration The spring configuration of the agents in XML syntax.
	 * @param libraries The list of agent-specific JARs.
	 * @param owner The owner of the new agents.
	 * @return The global unique identifier of the created agents.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#addAgents(byte[], List, String)
	 */
	public List<String> addAgents(byte[] configuration, List<JARMemory> libraries, String owner) throws IOException, InstanceNotFoundException {
		return (List<String>) invokeOperation("addAgents", new Object[] {configuration, libraries, owner}, new String[] {"[B","java.util.List","java.lang.String"});
	}

	/**
	 * Adds a listener for adding or removing agents of the managed agent node.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent node.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent node.
	 * @throws InstanceNotFoundException The agent node does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent node for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	public void addAgentsListener(NotificationListener listener) throws IOException, InstanceNotFoundException {
		addNotificationListener(listener, agentsNotificationFilter);
	}

	/**
	 * Removes a listener for adding or removing agents of the managed agent node.
	 * @param listener The listener object which will no longer handle the notifications from the managed agent node.
	 * @throws IOException A communication problem occurred when removing the listener from the remote agent node.
	 * @throws InstanceNotFoundException The agent node does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed agent node.
	 * @throws SecurityException if the listener can not be removed from the agent node for security reasons.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	public void removeAgentsListener(NotificationListener listener) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		removeNotificationListener(listener, agentsNotificationFilter);
	}

	/**
	 * Gets the class names of all agent node beans of the managed agent node.
	 * @return The list of class names.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getAgentNodeBeanClasses()
	 */
	public List<String> getAgentNodeBeanClasses() throws IOException, InstanceNotFoundException {
		return (List<String>) getAttribute("AgentNodeBeanClasses");
	}

	/**
	 * Gets the name of the <code>DirectoryAgentNodeBean</code> of the managed agent node.
	 * @return the name of the directory agent node bean or null if not exists.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getDirectoryName()
	 */
	public String getDirectoryName() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("DirectoryName");
	}

	/**
	 * Gets the host name of the managed agent node.
	 * @return The name of the agent node's host.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getHost()
	 */
	public String getHost() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("Host");
	}

	/**
	 * Gets the vendor of JIAC on which the managed agent node is based on.
	 * @return The vendor of the JIAC agent framework.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getJiacVendor()
	 */
	public String getJiacVendor() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("JiacVendor");
	}

	/**
	 * Gets the version of JIAC on which the managed agent node is based on.
	 * @return The version of the JIAC agent framework.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getJiacVersion()
	 */
	public String getJiacVersion() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("JiacVersion");
	}

	/**
	 * Adds a socket appender to the logger of the managed agent node, agent node beans
	 * and agents, which connects to a remote server at specified address and port.
	 * @param address The IP address of the logging server.
	 * @param port The port of the logging port.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#addLog4JSocketAppender(String, int)
	 */
	public void addLog4JSocketAppender(String address, int port) throws IOException, InstanceNotFoundException {
		invokeOperation("addLog4JSocketAppender", new Object[]{address, port}, new String[]{"java.lang.String", "int"});
	}

	/**
	 * Removes a socket appender from the logger of the managed agent node, agent node beans
	 * and agents, which connects to a remote server at specified address and port.
	 * @param address The IP address of the used logging server.
	 * @param port The port of the used logging port.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#removeLog4JSocketAppender(String, int)
	 */
	public void removeLog4JSocketAppender(String address, int port) throws IOException, InstanceNotFoundException {
		invokeOperation("removeLog4JSocketAppender", new Object[]{address, port}, new String[]{"java.lang.String", "int"});
	}

	/**
	 * Gets the name of the managed agent node.
	 * @return The agent node's name.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#getName()
	 */
	public String getAgentNodeName() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("Name");
	}

	/**
	 * Changes the name of the managed agent node.
	 * @param name The agent node's new name.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the agent node's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#setName(String)
	 */
	public void setAgentNodeName(String name) throws IOException, InvalidAttributeValueException, InstanceNotFoundException {
		setAttribute("Name", name);
	}

	/**
	 * Shuts down the managed agent node.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.SimpleAgentNodeMBean#shutdown()
	 */
	public void shutdownAgentNode() throws IOException, InstanceNotFoundException {
		invokeOperation("shutdown", new Object[]{}, new String[]{});
	}

}
