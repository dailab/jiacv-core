package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;

import de.dailab.jiactng.agentcore.management.jmx.JmxManager;

/**
 * This JMX client enables the remote management of JIAC TNG agent beans.
 * @author Jan Keiser
 */
public class JmxAgentBeanManagementClient extends JmxAbstractManagementClient {

	/**
	 * Creates a client for the management of an agent bean.
	 * @param mbsc The JMX connection used for the agent management.
	 * @param agentNodeName The name of the agent node where the agent of the agent bean resides on.
	 * @param agentID The global unique identifier of the agent which contains the agent bean.
	 * @param agentBeanName The name of the agent bean.
	 * @throws MalformedObjectNameException The agent node name, agent identifier or agent bean name contains an illegal character or does not follow the rules for quoting.
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentBeanManagementClient(MBeanServerConnection mbsc, String agentNodeName, String agentID, String agentBeanName) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentBean(agentNodeName, agentID, agentBeanName));
	}

	/**
	 * Gets detailed information about all actions provided by the managed <code>AbstractMethodExposingBean</code>.
	 * @return Information about the provided actions.
	 * @throws InstanceNotFoundException The agent bean does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent bean.
	 * @throws AttributeNotFoundException if the specified agent bean is not an <code>AbstractMethodExposingBean</code>.
	 * @throws SecurityException if the agent bean's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.action.AbstractMethodExposingBeanMBean#getActionList()
	 */
	public TabularData getActionList() throws IOException, InstanceNotFoundException, AttributeNotFoundException {
		try {
			return (TabularData) getAttribute("ActionList");
		}
		catch (RuntimeException e) {
			if ((e.getCause() != null) && (e.getCause() instanceof AttributeNotFoundException)) {
				throw ((AttributeNotFoundException) e.getCause());
			}
			else {
				throw e;
			}
		}
	}

}
