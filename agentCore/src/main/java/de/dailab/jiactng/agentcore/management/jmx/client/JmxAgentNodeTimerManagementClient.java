package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;
import java.util.Date;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import de.dailab.jiactng.agentcore.management.jmx.JmxManager;

/**
 * This JMX client enables the remote management of JIAC TNG agent node timers.
 * @author Jan Keiser
 */
public class JmxAgentNodeTimerManagementClient extends
		JmxAbstractManagementClient {

	/**
	 * Creates a client for the management of an agent node timer.
	 * @param mbsc The JMX connection used for the agent node timer management.
	 * @param agentNodeName The name of the managed agent node.
	 * @throws MalformedObjectNameException The name of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentNodeTimerManagementClient(MBeanServerConnection mbsc, String agentNodeName) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentNodeResource(agentNodeName, "Timer"));
	}

	/**
	 * Adds a listener for timer notifications to the managed agent node timer.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent node timer.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent node timer.
	 * @throws InstanceNotFoundException The agent node timer does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent node timer for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	public void addTimerNotificationListener(NotificationListener listener) throws IOException, InstanceNotFoundException {
		addNotificationListener(listener, null);
	}

	/**
	 * Removes a listener for timer notifications from the managed agent node timer.
	 * @param listener The listener object which will no longer handle the notifications from the managed agent node timer.
	 * @throws IOException A communication problem occurred when removing the listener from the remote agent node timer.
	 * @throws InstanceNotFoundException The agent node timer does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed agent node timer.
	 * @throws SecurityException if the listener can not be removed from the agent node timer for security reasons.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	public void removeTimerNotificationListener(NotificationListener listener) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		removeNotificationListener(listener, null);
	}

	/**
	 * Adds a timer notification to the managed agent node timer.
	 * @param type The timer notification type.
	 * @param message The timer notification detailed message.
	 * @param userDate The timer notification user data object.
	 * @param date The date when the notification occurs.
	 * @return The identifier of the new created timer notification.
	 * @throws IllegalArgumentException The date is null.
	 * @throws InstanceNotFoundException The agent node timer does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node timer.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see javax.management.timer.TimerMBean#addNotification(String, String, Object, Date)
	 */
	public Integer addNotification(String type, String message, Object userDate, Date date) throws IOException, InstanceNotFoundException {
		return (Integer) invokeOperation("addNotification", new Object[]{type, message, userDate, date}, new String[]{"java.lang.String", "java.lang.String", "java.lang.Object", "java.util.Date"});
	}

	/**
	 * Removes the timer notification corresponding to the specified identifier from the managed agent node timer.
	 * @param id The timer notification identifier.
	 * @throws InstanceNotFoundException A timer notification with the given id or the agent node timer does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node timer.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see javax.management.timer.TimerMBean#removeNotification(Integer)
	 */
	public void removeNotification(Integer id) throws IOException, InstanceNotFoundException {
		invokeOperation("removeNotification", new Object[]{id}, new String[]{"java.lang.Integer"});
	}

	/**
	 * Gets the date of a timer notification corresponding to the specified identifier from the managed agent node timer.
	 * @param id The timer notification identifier.
	 * @return The date or null if the identifier is not mapped to any timer notification.
	 * @throws InstanceNotFoundException The agent node timer does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node timer.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see javax.management.timer.TimerMBean#getDate(Integer)
	 */
	public Date getDate(Integer id) throws IOException, InstanceNotFoundException {
		return (Date) invokeOperation("getDate", new Object[]{id}, new String[]{"java.lang.Integer"});
	}

}
