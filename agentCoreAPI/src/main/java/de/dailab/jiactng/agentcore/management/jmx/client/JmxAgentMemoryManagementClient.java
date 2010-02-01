package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.management.jmx.JmxManager;

/**
 * This JMX client enables the remote management of JIAC TNG agent memories.
 * @author Jan Keiser
 */
public class JmxAgentMemoryManagementClient extends JmxAbstractManagementClient {

	/**
	 * Creates a client for the management of an agent memory.
	 * @param mbsc The JMX connection used for the agent memory management.
	 * @param agentNodeID The UUID of the agent node where the agent of the memory resides on.
	 * @param agentID The global unique identifier of the agent which contains the memory.
	 * @throws MalformedObjectNameException The agent node UUID, agent identifier or agent bean name contains an illegal character or does not follow the rules for quoting.
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentMemoryManagementClient(MBeanServerConnection mbsc, String agentNodeID, String agentID) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentResource(agentNodeID, agentID, "Memory"));
	}

	/**
	 * Gets detailed information about the content of the managed memory.
	 * @return Information about the memory content.
	 * @throws InstanceNotFoundException The agent memory does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent memory.
	 * @throws SecurityException if the agent memory's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.knowledge.MemoryMBean#getSpace()
	 */
	public final CompositeData getSpace() throws IOException, InstanceNotFoundException {
		return (CompositeData) getAttribute("Space");
	}

}
