package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import de.dailab.jiactng.agentcore.management.jmx.MessageExchangeNotificationFilter;

/**
 * This JMX client enables the remote management of JIAC TNG agent communication beans.
 * @author Jan Keiser
 */
public class JmxAgentCommunicationManagementClient extends JmxAbstractManagementClient {

	/**
	 * Creates a client for the management of an agent communication bean.
	 * @param mbsc The JMX connection used for the agent communication bean management.
	 * @param communicationBean The agent's communication bean.
	 */
	protected JmxAgentCommunicationManagementClient(MBeanServerConnection mbsc, ObjectName communicationBean) {
		super(mbsc, communicationBean);
	}

	/**
	 * Adds a listener for messages exchanged by the managed agent communication bean.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent communication bean.
	 * @param filter The message filter. If filter is null, no filtering will be performed before handling notifications.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent communication bean.
	 * @throws InstanceNotFoundException The agent communication bean does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent communication bean for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	public final void addMessageExchangeListener(NotificationListener listener, MessageExchangeNotificationFilter filter) throws IOException, InstanceNotFoundException {
		addNotificationListener(listener, filter);
	}

	/**
	 * Removes a listener for messages exchanged by the managed agent communication bean.
	 * @param listener The listener object which will no longer handle the notifications from the managed agent communication bean.
	 * @param filter The used filter object.
	 * @throws IOException A communication problem occurred when removing the listener from the remote agent communication bean.
	 * @throws InstanceNotFoundException The agent communication bean does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed agent communication bean, or it is not registered with the given filter.
	 * @throws SecurityException if the listener can not be removed from the agent communication bean for security reasons.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	public final void removeMessageExchangeListener(NotificationListener listener, MessageExchangeNotificationFilter filter) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		removeNotificationListener(listener, filter);
	}

}
