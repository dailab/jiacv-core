package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import de.dailab.jiactng.agentcore.management.jmx.ActionPerformedNotificationFilter;
import de.dailab.jiactng.agentcore.management.jmx.JmxManager;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAbstractManagementClient;

/**
 * This JMX client enables the remote management of JIAC TNG agent execution cycles.
 * @author Jan Keiser
 */
public class JmxAgentExecutionCycleManagementClient extends JmxAbstractManagementClient {

	/**
	 * Creates a client for the management of an agent execution cycle.
	 * @param mbsc The JMX connection used for the agent execution cycle management.
	 * @param agentNodeName The name of the managed agent node.
	 * @param agentID The global unique identifier of the agent.
	 * @throws MalformedObjectNameException The agent node name or agent identifier contains an illegal character or does not follow the rules for quoting.
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentExecutionCycleManagementClient(MBeanServerConnection mbsc, String agentNodeName, String agentID) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentResource(agentNodeName, agentID, "ExecutionCycle"));
	}

	/**
	 * Adds a listener for actions performed by the managed agent execution cycle.
	 * @param listener The listener object which will handle the notifications emitted by the managed agent execution cycle.
	 * @param filter The action filter. If filter is null, no filtering will be performed before handling notifications.
	 * @throws IOException A communication problem occurred when adding the listener to the remote agent execution cycle.
	 * @throws InstanceNotFoundException The agent execution cycle does not exist in the JVM.
	 * @throws SecurityException if the listener can not be added to the agent execution cycle for security reasons.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 * @see de.dailab.jiactng.agentcore.SimpleExecutionCycle#actionPerformed(de.dailab.jiactng.agentcore.action.DoAction, long, boolean)
	 */
	public void addActionPerformedListener(NotificationListener listener, ActionPerformedNotificationFilter filter) throws IOException, InstanceNotFoundException {
		addNotificationListener(listener, filter);
	}

	/**
	 * Removes a listener for actions performed by the managed agent execution cycle.
	 * @param listener The listener object which will no longer handle the notifications from the managed agent execution cycle.
	 * @param filter The used filter object.
	 * @throws IOException A communication problem occurred when removing the listener from the remote agent execution cycle.
	 * @throws InstanceNotFoundException The agent execution cycle does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed agent execution cycle, or it is not registered with the given filter.
	 * @throws SecurityException if the listener can not be removed from the agent execution cycle for security reasons.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 * @see de.dailab.jiactng.agentcore.SimpleExecutionCycle#actionPerformed(de.dailab.jiactng.agentcore.action.DoAction, long, boolean)
	 */
	public void removeActionPerformedListener(NotificationListener listener, ActionPerformedNotificationFilter filter) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		removeNotificationListener(listener, filter);
	}

}
