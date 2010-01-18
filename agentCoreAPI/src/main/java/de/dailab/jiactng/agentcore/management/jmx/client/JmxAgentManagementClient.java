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
import de.dailab.jiactng.agentcore.management.jmx.DisableLifeCycleAttributeFilter;
import de.dailab.jiactng.agentcore.management.jmx.JmxManager;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * This JMX client enables the remote management of JIAC TNG agents.
 * @author Jan Keiser
 */
public class JmxAgentManagementClient extends JmxAbstractManagementClient {

	private static final AttributeChangeNotificationFilter AGENTNAME_NOTIFICATION_FILTER = new AttributeChangeNotificationFilter();
	private static final AttributeChangeNotificationFilter LIFECYCLE_NOTIFICATION_FILTER = new AttributeChangeNotificationFilter();
	private static final DisableLifeCycleAttributeFilter   PROPERTY_NOTIFICATION_FILTER  = new DisableLifeCycleAttributeFilter();
	
	static {
		AGENTNAME_NOTIFICATION_FILTER.enableAttribute("AgentName");
		LIFECYCLE_NOTIFICATION_FILTER.enableAttribute("LifecycleState");
	}

	/**
	 * Creates a client for the management of an agent.
	 * @param mbsc The JMX connection used for the agent management.
	 * @param agentNodeID The UUID of the managed agent node.
	 * @param agentID The global unique identifier of the agent.
	 * @throws MalformedObjectNameException The agent node UUID or agent identifier contains an illegal character or does not follow the rules for quoting.
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentManagementClient(MBeanServerConnection mbsc, String agentNodeID, String agentID) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgent(agentNodeID, agentID));
	}

	/**
	 * Gets the lifecycle state of the managed agent.
	 * @return The agent's lifecycle state.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#getLifecycleState()
	 */
	final public String getAgentState() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("LifecycleState");
	}

	/**
	 * Adds a listener for changes on the lifecycle state of the managed agent.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	final public void addLifecycleStateListener(NotificationListener listener) throws IOException, InstanceNotFoundException {
		addNotificationListener(listener, LIFECYCLE_NOTIFICATION_FILTER);
	}

	/**
	 * Removes a listener for changes on the lifecycle state from the managed agent.
	 * @param listener The listener object which will no longer handle the notifications from the managed agent.
	 * @throws IOException A communication problem occurred when removing the listener from the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed agent.
	 * @throws SecurityException if the listener can not be removed from the agent for security reasons.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	final public void removeLifecycleStateListener(NotificationListener listener) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		removeNotificationListener(listener, LIFECYCLE_NOTIFICATION_FILTER);
	}

	/**
	 * Initializes the managed agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws LifecycleException if an error occurs during initialization of the agent.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#init()
	 */
	final public void initAgent() throws IOException, InstanceNotFoundException, LifecycleException {
		changeState("init");
	}

	/**
	 * Starts the managed agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws LifecycleException if an error occurs during start of the agent.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#start()
	 */
	final public void startAgent() throws IOException, InstanceNotFoundException, LifecycleException {
		changeState("start");
	}

	/**
	 * Stops the managed agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws LifecycleException if an error occurs during stop of the agent.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#stop()
	 */
	final public void stopAgent() throws IOException, InstanceNotFoundException, LifecycleException {
		changeState("stop");
	}

	/**
	 * Cleans up the managed agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws LifecycleException if an error occurs during cleanup of the agent.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#cleanup()
	 */
	final public void cleanupAgent() throws IOException, InstanceNotFoundException, LifecycleException {
		changeState("cleanup");
	}

	/**
	 * Gets the current log level of the managed agent.
	 * @return The agent's log level.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#getLogLevel()
	 */
	final public String getAgentLogLevel() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("LogLevel");
	}

	/**
	 * Sets the current log level of the managed agent.
	 * @param level The agent's new log level.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#setLogLevel(String)
	 */
	final public void setAgentLogLevel(String level) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("LogLevel", level);
	}

	/**
	 * Gets detailed information about the current logger of the managed agent.
	 * @return Information about the agent's logger.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean#getLogger()
	 */
	final public CompositeData getAgentLogger() throws IOException, InstanceNotFoundException {
		return (CompositeData) getAttribute("Logger");
	}

	/**
	 * Gets the name of the managed agent.
	 * @return The name of the agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getAgentName()
	 */
	final public String getAgentName() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("AgentName");
	}

	/**
	 * Sets the name of the managed agent.
	 * @param agentname The new name of the agent.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#setAgentName(String)
	 */
	final public void setAgentName(String agentname) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("AgentName", agentname);
	}

	/**
	 * Adds a listener for changes on the name of the managed agent.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 * @see de.dailab.jiactng.agentcore.IAgent#setAgentName(String)
	 */
	final public void addAgentNameListener(NotificationListener listener) throws IOException, InstanceNotFoundException {
		addNotificationListener(listener, AGENTNAME_NOTIFICATION_FILTER);
	}

	/**
	 * Removes a listener for changes on the name of the managed agent.
	 * @param listener The listener object which will no longer handle the notifications from the managed agent.
	 * @throws IOException A communication problem occurred when removing the listener from the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed agent.
	 * @throws SecurityException if the listener can not be removed from the agent for security reasons.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 * @see de.dailab.jiactng.agentcore.IAgent#setAgentName(String)
	 */
	final public void removeAgentNameListener(NotificationListener listener) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		removeNotificationListener(listener, AGENTNAME_NOTIFICATION_FILTER);
	}
	
	/**
	 * Adds a listener for changes of properties of the managed agent.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	final public void addAgentPropertyListener(NotificationListener listener) throws IOException, InstanceNotFoundException {
		addNotificationListener(listener, PROPERTY_NOTIFICATION_FILTER);
	}

	/**
	 * Removes a listener for changes of properties of the managed agent.
	 * @param listener The listener object which will no longer handle the notifications from the managed agent.
	 * @throws IOException A communication problem occurred when removing the listener from the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed agent.
	 * @throws SecurityException if the listener can not be removed from the agent for security reasons.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	final public void removeAgentPropertyListener(NotificationListener listener) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		removeNotificationListener(listener, PROPERTY_NOTIFICATION_FILTER);
	}


	/**
	 * Gets the owner of the managed agent.
	 * @return The name of agent's owner.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getOwner()
	 */
	final public String getOwner() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("Owner");
	}

	/**
	 * Sets the owner of the managed agent.
	 * @param owner The name of agent's new owner.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#setOwner(String)
	 */
	final public void setOwner(String owner) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("Owner", owner);
	}

	/**
	 * Gets the agent bean names of the managed agent.
	 * @return The names of the agent beans of the agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getAgentBeanNames()
	 */
	final public List<String> getAgentBeanNames() throws IOException, InstanceNotFoundException {
		return (List<String>) getAttribute("AgentBeanNames");
	}

	/**
	 * Gets the name of actions provided by the managed agent.
	 * @return The name of actions provided by the agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getActionNames()
	 */
	final public List<String> getActionNames() throws IOException, InstanceNotFoundException {
		return (List<String>) getAttribute("ActionNames");
	}

	/**
	 * Gets the agent bean's execution timeout of the managed agent.
	 * @return The execution timeout for the agent beans of the agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getBeanExecutionTimeout()
	 */
	final public long getBeanExecutionTimeout() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("BeanExecutionTimeout");
	}

	/**
	 * Sets the agent bean's execution timeout of the managed agent.
	 * @param beanExecutionTimeout The new execution timeout for the agent beans of the agent.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#setBeanExecutionTimeout(long)
	 */
	final public void setBeanExecutionTimeout(long beanExecutionTimeout) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		setAttribute("BeanExecutionTimeout", beanExecutionTimeout);
	}

	/**
	 * Gets the class name of the execution cycle used by the managed agent.
	 * @return The class name of the execution cycle used by the agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getExecutionCycleClass()
	 */
	final public String getExecutionCycleClass() throws IOException, InstanceNotFoundException {
		return (String) getAttribute("ExecutionCycleClass");
	}

	/**
	 * Gets detailed information about the memory used by the managed agent.
	 * @return Information about the memory used by the agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getMemoryData()
	 */
	final public CompositeData getMemoryData() throws IOException, InstanceNotFoundException {
		return (CompositeData) getAttribute("MemoryData");
	}

	/**
	 * Removes the managed agent from the agent node.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see de.dailab.jiactng.agentcore.AgentMBean#remove()
	 */
	final public void removeAgent() throws IOException, InstanceNotFoundException {
      	invokeOperation("remove", new Object[]{}, new String[]{});
	}

	/**
	 * Gets the date and time for automatically start of the managed agent.
	 * @return the date in msec since 1/1/1970 0am or null if no automatically start will take place.
	 * @throws InstanceNotFoundException The agent or the agent node timer does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when getting the attribute value of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getStartTime()
	 */
	final public Long getStartTime() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("StartTime");
	}

	/**
	 * Sets the date and time for automatically start of the managed agent.
	 * @param startTime the date in msec since 1/1/1970 0am or null if no automatically start should take place.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The agent or the agent node timer does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when setting the attribute value of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#setStartTime(Long)
	 */
	final public void setStartTime(Long startTime) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		this.setAttribute("StartTime", startTime);
	}

	/**
	 * Gets the date and time for automatically stop of the managed agent.
	 * @return the date in msec since 1/1/1970 0am or null if no automatically stop will take place.
	 * @throws InstanceNotFoundException The agent or the agent node timer does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when getting the attribute value of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getStopTime()
	 */
	final public Long getStopTime() throws IOException, InstanceNotFoundException {
		return (Long) getAttribute("StopTime");
	}

	/**
	 * Sets the date and time for automatically stop of the managed agent.
	 * @param stopTime the date in msec since 1/1/1970 0am or null if no automatically stop should take place.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws InstanceNotFoundException The agent or the agent node timer does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when setting the attribute value of the remote agent.
	 * @throws SecurityException if the agent's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 * @see de.dailab.jiactng.agentcore.AgentMBean#setStopTime(Long)
	 */
	final public void setStopTime(Long stopTime) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		this.setAttribute("StopTime", stopTime);
	}
	
	/**
	 * Sets the auto execution service list for the connected agent.
	 * @param actionIds the list of action names.
	 * @throws IOException A communication problem occurred when setting the attribute value of the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	final public void setAutoExecutionServices(List<String> actionIds) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		this.setAttribute("AutoExecutionServices", actionIds);
	}
  
	/**
	 * Gets the auto execution service list for the connected agent.
	 * @return auto execution service id list
	 * @throws IOException A communication problem occurred when getting the attribute value of the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node.
	 */
	final public List<String> getAutoExecutionServices() throws IOException, InstanceNotFoundException {
		return (List<String>) getAttribute("AutoExecutionServices");
	}
  
	/**
	 * Sets the auto execution type for the connected agent.
	 * @param continous <code>true</code> if the automatic actions will be continuously executed.
	 * @throws IOException A communication problem occurred when setting the attribute value of the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	final public void setAutoExecutionType(boolean continous)throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		this.setAttribute("AutoExecutionType", continous);
	}
  
	/**
	 * Gets the auto execution type for the connected agent.
	 * @return <code>true</code> if the automatic actions will be continuously executed.
	 * @throws IOException A communication problem occurred when getting the attribute value of the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node.
	 */
	final public boolean getAutoExecutionType()throws IOException, InstanceNotFoundException {
		return (Boolean) getAttribute("AutoExecutionType");
	}
  
	/**
	 * Gets the spring config xml snippet for the connected agent.
	 * @return Spring Config XML snippet
	 * @throws IOException A communication problem occurred when getting the attribute value of the remote agent.
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node.
	 */
	final public byte[] getSpringConfigXml() throws IOException, InstanceNotFoundException {
		return (byte[]) getAttribute("SpringConfigXml");
	}
  
	/**
	 * Gets the Agent description for the connected agent.
	 * @return Agent Description
	 * @throws InstanceNotFoundException The agent does not exist on the managed agent node.
	 * @throws IOException A communication problem occurred when getting the attribute value of the remote agent.
	 */
	final public IAgentDescription getAgentDescription() throws InstanceNotFoundException, IOException {
		return (IAgentDescription) getAttribute("AgentDescription");
	}
  
  
}
