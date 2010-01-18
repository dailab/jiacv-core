package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;
import java.util.Date;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationListener;

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
	 * @param agentNodeID The UUID of the managed agent node.
	 * @throws MalformedObjectNameException The UUID of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentNodeTimerManagementClient(MBeanServerConnection mbsc, String agentNodeID) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentNodeResource(agentNodeID, "Timer"));
	}

	/**
	 * Adds a listener for timer notifications to the managed agent node timer.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent node timer.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent node timer.
	 * @throws InstanceNotFoundException The agent node timer does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent node timer for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	final public void addTimerNotificationListener(NotificationListener listener) throws IOException, InstanceNotFoundException {
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
	final public void removeTimerNotificationListener(NotificationListener listener) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
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
	final public Integer addNotification(String type, String message, Object userDate, Date date) throws IOException, InstanceNotFoundException {
		return (Integer) invokeOperation("addNotification", new Object[]{type, message, userDate, date}, new String[]{"java.lang.String", "java.lang.String", "java.lang.Object", "java.util.Date"});
	}

	/**
	 * Adds a timer notification to the managed agent node timer.
	 * @param type The timer notification type.
	 * @param message The timer notification detailed message.
	 * @param userDate The timer notification user data object.
	 * @param date The date when the notification occurs.
	 * @param period The period of the timer notification (in milliseconds).
	 * @return The identifier of the new created timer notification.
	 * @throws IllegalArgumentException The date is null or the period is negative.
	 * @throws InstanceNotFoundException The agent node timer does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node timer.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see javax.management.timer.TimerMBean#addNotification(String, String, Object, Date)
	 */
	final public Integer addNotification(String type, String message, Object userDate, Date date, long period) throws IOException, InstanceNotFoundException {
		return (Integer) invokeOperation("addNotification", new Object[]{type, message, userDate, date, period}, new String[]{"java.lang.String", "java.lang.String", "java.lang.Object", "java.util.Date", "long"});
	}

	/**
	 * Adds a timer notification to the managed agent node timer.
	 * @param type The timer notification type.
	 * @param message The timer notification detailed message.
	 * @param userDate The timer notification user data object.
	 * @param date The date when the notification occurs.
	 * @param period The period of the timer notification (in milliseconds).
	 * @param nbOccurences The total number the timer notification will be emitted.
	 * @return The identifier of the new created timer notification.
	 * @throws IllegalArgumentException The date is null or the period or the number of occurrences is negative.
	 * @throws InstanceNotFoundException The agent node timer does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node timer.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see javax.management.timer.TimerMBean#addNotification(String, String, Object, Date)
	 */
	final public Integer addNotification(String type, String message, Object userDate, Date date, long period, long nbOccurences) throws IOException, InstanceNotFoundException {
		return (Integer) invokeOperation("addNotification", new Object[]{type, message, userDate, date, period, nbOccurences}, new String[]{"java.lang.String", "java.lang.String", "java.lang.Object", "java.util.Date", "long", "long"});
	}

	/**
	 * Adds a timer notification to the managed agent node timer.
	 * @param type The timer notification type.
	 * @param message The timer notification detailed message.
	 * @param userDate The timer notification user data object.
	 * @param date The date when the notification occurs.
	 * @param period The period of the timer notification (in milliseconds).
	 * @param nbOccurences The total number the timer notification will be emitted.
	 * @param fixedRate If <code>true</code>, the notification is scheduled with a fixed-rate execution scheme. If <code>false</code>, the notification is scheduled with a fixed-delay execution scheme.
	 * @return The identifier of the new created timer notification.
	 * @throws IllegalArgumentException The date is null or the period or the number of occurrences is negative.
	 * @throws InstanceNotFoundException The agent node timer does not exist. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent node timer.
	 * @throws SecurityException if the operation cannot be invoked for security reasons.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 * @see javax.management.timer.TimerMBean#addNotification(String, String, Object, Date)
	 */
	final public Integer addNotification(String type, String message, Object userDate, Date date, long period, long nbOccurences, boolean fixedRate) throws IOException, InstanceNotFoundException {
		return (Integer) invokeOperation("addNotification", new Object[]{type, message, userDate, date, period, nbOccurences, fixedRate}, new String[]{"java.lang.String", "java.lang.String", "java.lang.Object", "java.util.Date", "long", "long", "boolean"});
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
	final public void removeNotification(Integer id) throws IOException, InstanceNotFoundException {
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
	final public Date getDate(Integer id) throws IOException, InstanceNotFoundException {
		return (Date) invokeOperation("getDate", new Object[]{id}, new String[]{"java.lang.Integer"});
	}

}
