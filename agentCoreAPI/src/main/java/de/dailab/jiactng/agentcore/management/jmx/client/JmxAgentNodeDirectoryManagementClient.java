package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.openmbean.TabularData;

import de.dailab.jiactng.agentcore.management.jmx.JmxManager;

/**
 * This JMX client enables the remote management of JIAC TNG agent node directories.
 * @author Jan Keiser
 */
public class JmxAgentNodeDirectoryManagementClient extends JmxAbstractManagementClient {

	/**
	 * Creates a client for the management of an agent node directory.
	 * @param mbsc The JMX connection used for the agent node directory management.
	 * @param agentNodeID The UUID of the agent node.
	 * @param directoryBeanName The name of the agent node directory bean.
	 * @throws MalformedObjectNameException The agent node UUID or agent identifier contains an illegal character or does not follow the rules for quoting.
	 * @see JmxManager#getMgmtNameOfAgentNodeBean(String, String)
	 */
	protected JmxAgentNodeDirectoryManagementClient(MBeanServerConnection mbsc, String agentNodeID, String directoryBeanName) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentNodeBean(agentNodeID, directoryBeanName));
	}

	/**
	 * Get the interval the node sends an alive message in milliseconds.
	 * @return the time between two alive messages in milliseconds
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#getAliveInterval()
	 */
	public final long getAliveInterval() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("AliveInterval");
	}

	/**
	 * Get the interval the node sends an advertisement in milliseconds.
	 * @return the time between two advertisements in milliseconds
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#getAdvertiseInterval()
	 */
	public final long getAdvertiseInterval() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("AdvertiseInterval");
	}

	/**
	 * Checks whether a dump is printed to the console.
	 * @return <code>true</code> if the dump is printed to the console
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#isDump()
	 */
	public final boolean isDump() throws IOException, InstanceNotFoundException {
		return (Boolean) getAttribute("Dump");
	}

	/**
	 * Returns all locally offered actions of this agent node.
	 * @return information about the actions offered locally
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#getLocalActions()
	 */
	public final TabularData getLocalActions() throws IOException, InstanceNotFoundException {
		return (TabularData) getAttribute("LocalActions");
	}

	/**
	 * Returns all agents residing on this agent node.
	 * @return information about the agents residing on this agent node.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#getLocalAgents()
	 */
	public final TabularData getLocalAgents() throws IOException, InstanceNotFoundException {
		return (TabularData) getAttribute("LocalAgents");
	}

	/**
	 * Returns all actions offered by remote agent nodes.
	 * @return information about the actions offered by remote agent nodes
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#getRemoteActions()
	 */
	public final TabularData getRemoteActions() throws IOException, InstanceNotFoundException {
		return (TabularData) getAttribute("RemoteActions");
	}

	/**
	 * Returns all agents residing on remote agent nodes.
	 * @return information about the agents residing on remote agent nodes
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#getRemoteAgents()
	 */
	public final TabularData getRemoteAgents() throws IOException, InstanceNotFoundException {
		return (TabularData) getAttribute("RemoteAgents");
	}

	/**
	 * Return all (other) known agent nodes.
	 * @return the known agent nodes
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#getKnownNodes()
	 */
	public final TabularData getKnownNodes() throws IOException, InstanceNotFoundException {
		return (TabularData) getAttribute("KnownNodes");
	}

	/**
	 * Set the interval the node sends an alive message in milliseconds.
	 * @param aliveInterval the time between two alive messages in milliseconds
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#setAliveInterval(long)
	 */
	public final void setAliveInterval(long aliveInterval) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("AliveInterval", aliveInterval);
	}

	/**
	 * Set the interval the node sends an advertisement in milliseconds.
	 * @param advertiseInterval the time between two advertisements in milliseconds
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#setAdvertiseInterval(long)
	 */
	public final void setAdvertiseInterval(long advertiseInterval) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("AdvertiseInterval", advertiseInterval);
	}

	/**
	 * Set whether the dump should be printed to console.
	 * @param dump if <code>true</code>, the dump will be printed to console
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.directory.DirectoryAgentNodeBeanMBean#setDump(boolean)
	 */
	public final void setDump(boolean dump) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("Dump", dump);
	}

}
