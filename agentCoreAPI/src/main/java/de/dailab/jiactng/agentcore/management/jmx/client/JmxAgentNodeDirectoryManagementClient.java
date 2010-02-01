package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.openmbean.CompositeData;

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
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentNodeDirectoryManagementClient(MBeanServerConnection mbsc, String agentNodeID, String directoryBeanName) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentNodeBean(agentNodeID, directoryBeanName));
	}

	/**
	 * Gets the agent ping interval of the managed directory.
	 * @return The directory's agent ping interval.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getAgentPingInterval()
	 */
	public final long getAgentPingInterval() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("AgentPingInterval");
	}

	/**
	 * Checks whether the cache of the managed directory is activated or not.
	 * @return <code>true</code> if the directory's cache is activated.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getCacheIsActive()
	 */
	public final boolean getCacheIsActive() throws IOException, InstanceNotFoundException {
		return (Boolean) getAttribute("CacheIsActive");
	}

	/**
	 * Gets the change propagate interval of the managed directory.
	 * @return The directory's change propagate interval.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getChangePropagateInterval()
	 */
	public final long getChangePropagateInterval() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("ChangePropagateInterval");
	}

	/**
	 * Gets the delay until first refresh of the managed directory.
	 * @return The directory's delay for first refresh.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getFirstRefresh()
	 */
	public final long getFirstRefresh() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("FirstRefresh");
	}

	/**
	 * Checks whether the instant propagation of the managed directory is activated or not.
	 * @return <code>true</code> if the instant propagation of the directory is activated.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getInstantPropagation()
	 */
	public final boolean getInstantPropagation() throws IOException, InstanceNotFoundException {
		return (Boolean) getAttribute("InstantPropagation");
	}

	/**
	 * Gets the message transport identifier of the managed directory.
	 * @return The directory's message transport identifier.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getMessageTransportIdentifier()
	 */
	public final String getMessageTransportIdentifier() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("MessageTransportIdentifier");
	}

	/**
	 * Gets the node group of the managed directory.
	 * @return The directory's node group.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getNodeGroup()
	 */
	public final String getNodeGroup() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("NodeGroup");
	}

	/**
	 * Gets the node group address of the managed directory.
	 * @return The name of directory's node group address.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getNodeGroupAddressName()
	 */
	public final String getNodeGroupAddress() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("NodeGroupAddressName");
	}

	/**
	 * Gets the identifier of the other agent nodes which are in federation with the managed directory.
	 * @return The global unique identifier of the other agent nodes.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getOtherNodes()
	 */
	public final Set<String> getOtherNodes() throws IOException, InstanceNotFoundException {
		return (Set<String>) getAttribute("OtherNodes");
	}

	/**
	 * Gets the refreshing interval of the managed directory.
	 * @return The directory's refreshing interval.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getRefreshingInterval()
	 */
	public final long getRefreshingInterval() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("RefreshingInterval");
	}

	/**
	 * Gets detailed information about the content of the managed directory.
	 * @return Information about the directory's content.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#getSpace()
	 */
	public final CompositeData getSpace() throws IOException, InstanceNotFoundException {
		return (CompositeData) getAttribute("Space");
	}

	/**
	 * Checks whether the message transport of the managed directory is active or not.
	 * @return <code>true</code> if the directory's message transport is active.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#isMessageTransportActive()
	 */
	public final boolean isMessageTransportActive() throws IOException, InstanceNotFoundException {
		return (Boolean) getAttribute("MessageTransportActive");
	}

	/**
	 * Sets the agent ping interval of the managed directory.
	 * @param agentPingInterval The directory's new agent ping interval.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#setAgentPingInterval(long)
	 */
	public final void setAgentPingInterval(long agentPingInterval) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("AgentPingInterval", agentPingInterval);
	}

	/**
	 * Activates or deactivates the cache of the managed directory.
	 * @param isActive <code>true</code> for activation of the directory's cache.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#setCacheIsActive(boolean)
	 */
	public final void setCacheIsActive(boolean isActive) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("CacheIsActive", isActive);
	}

	/**
	 * Sets the change propagate interval of the managed directory.
	 * @param cpInterval The directory's new change propagate interval.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#setChangePropagateInterval(long)
	 */
	public final void setChangePropagateInterval(long cpInterval) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("ChangePropagateInterval", cpInterval);
	}

	/**
	 * Sets the delay for first refresh of the managed directory.
	 * @param firstRefresh The directory's new delay for first refresh.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#setFirstRefresh(long)
	 */
	public final void setFirstRefresh(long firstRefresh) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("FirstRefresh", firstRefresh);
	}

	/**
	 * Activates or deactivates the instant propagation of the managed directory.
	 * @param instantPropagation <code>true</code> for activation of the directory's instant propagation.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#setInstantPropagation(boolean)
	 */
	public final void setInstantPropagation(boolean instantPropagation) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("InstantPropagation", instantPropagation);
	}

	/**
	 * Sets the node group name of the managed directory.
	 * @param groupName The name of directory's new node group.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#setNodeGroup(String)
	 */
	public final void setNodeGroup(String groupName) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("NodeGroup", groupName);
	}

	/**
	 * Sets the refreshing interval of the managed directory.
	 * @param interval The directory's new refreshing interval.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The directory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote directory.
	 * @throws SecurityException if the directory's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBeanMBean#setRefreshingInterval(long)
	 */
	public final void setRefreshingInterval(long interval) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("RefreshingInterval", interval);
	}

}
