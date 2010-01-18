package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.comm.CommunicationBeanMBean;
import de.dailab.jiactng.agentcore.management.jmx.JmxManager;
import de.dailab.jiactng.agentcore.util.IdFactory;

/**
 * This JMX client enables the remote management of a JVM.
 * @author Jan Keiser
 */
public class JmxManagementClient {

	/** The connection to the MBeanServer of a JVM used to manage its resources. */
	protected MBeanServerConnection mbsc = null;
	private JMXConnector jmxc = null;

	/**
	 * Gets the URL of all JMX connector server registered in a RMI registry. 
	 * @param host The host of the registry.
	 * @param port The port used by the registry.
	 * @return URLs of the registered JMX connector server.
	 * @throws RemoteException if remote communication with the registry failed. If exception is a <code>ServerException</code> containing an <code>AccessException</code>, then the registry denies the caller access to perform this operation.
	 * @see java.rmi.registry.Registry#list()
	 * @see JMXServiceURL#JMXServiceURL(String, String, int, String)
	 */
	public static List<JMXServiceURL> getURLsFromRegistry(String host, int port) throws RemoteException {
		final List<JMXServiceURL> urls = new ArrayList<JMXServiceURL>();
		final Iterator<String> nodeIdIterator = Arrays.asList(LocateRegistry.getRegistry(host, port).list()).iterator();
		while (nodeIdIterator.hasNext()) {
			final String nodeId = nodeIdIterator.next();
			if (nodeId.startsWith(IdFactory.IdPrefix.Node.toString())) {
				try {
					urls.add(new JMXServiceURL("rmi", null, 0, "/jndi/rmi://" + host + ":" + port + "/" + nodeId));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return urls;
	}

	/**
	 * Gets the URL of all JMX connector server announced by multicast messages within a time frame of 5 seconds.
	 * @return The list of received JMX URLs.
	 * @throws IOException A communication problem occurred when creating a multicast socket or receiving multicast packets.
	 * @see MulticastSocket#receive(DatagramPacket)
	 */
	public static List<JMXServiceURL> getURLsFromMulticast() throws IOException {
		final List<JMXServiceURL> urls = new ArrayList<JMXServiceURL>();
		byte[] buffer = new byte[1000];
		final long endTime = System.currentTimeMillis() + 5000;

		// activate multicast socket
		final InetAddress group = InetAddress.getByName("226.6.6.7");
		final MulticastSocket socket = new MulticastSocket(9999);
		socket.setTimeToLive(1);
		final DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		socket.joinGroup(group);

		// read multicast packets
		while (System.currentTimeMillis() < endTime) {
			// read message
			dp.setLength(1000);
			socket.receive(dp);
			buffer = dp.getData();
			final String message = new String(buffer, 0, dp.getLength());

			// add converted message to list of URLs
			try {
				JMXServiceURL url = new JMXServiceURL(message);
				// replace localhost within URL path
				if (url.getURLPath().contains("localhost")) {
					url = new JMXServiceURL(message.replace("localhost", url.getHost()));
				}
				// check whether the URL is already known
				if (!urls.contains(url)) {
					urls.add(url);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return urls;
	}

	/**
	 * Creates a management client for this JVM.
	 * @see ManagementFactory#getPlatformMBeanServer()
	 */
	public JmxManagementClient() {
		System.setProperty("jmx.invoke.getters", "");
		mbsc = ManagementFactory.getPlatformMBeanServer();
	}

	/**
	 * Creates a management client for a remote JVM by establishing a JMX connection. 
	 * @param url The URL of the JMX connector server.
	 * @throws IOException if the connector client or the connection cannot be made because of a communication problem.
	 * @throws NullPointerException if the URL is null.
	 * @throws SecurityException if the connection cannot be made for security reasons.
	 * @see JMXConnectorFactory#connect(JMXServiceURL)
	 * @see JMXConnector#getMBeanServerConnection()
	 */
	public JmxManagementClient(JMXServiceURL url) throws IOException {
		jmxc = JMXConnectorFactory.connect(url);
		mbsc = jmxc.getMBeanServerConnection();
	}

	/**
	 * Creates a management client for a secured JVM by establishing an authenticated JMX connection. 
	 * @param url The URL of the JMX connector server.
	 * @param username The name of the JVM administrator used for authentication. 
	 * @param password The password of the JVM administrator used for authentication.
	 * @throws IOException if the connector client or the connection cannot be made because of a communication problem.
	 * @throws SecurityException if the authentication failed.
	 * @throws NullPointerException if the URL is null.
	 * @see JMXConnectorFactory#connect(JMXServiceURL, java.util.Map)
	 * @see JMXConnector#getMBeanServerConnection()
	 */
	public JmxManagementClient(JMXServiceURL url, String username, String password) throws IOException, SecurityException {
		final HashMap<String,Object> env = new HashMap<String,Object>();
	    env.put(JMXConnector.CREDENTIALS, new String[] {username, password});
		jmxc = JMXConnectorFactory.connect(url, env);
		mbsc = jmxc.getMBeanServerConnection();
	}

	/**
	 * Creates a new management client which uses the JMX connection of another management client. This 
	 * connection can only be closed by the original management client.
	 * @param client The original management client.
	 */
	public JmxManagementClient(JmxManagementClient client) {
		mbsc = client.mbsc;
	}

	/**
	 * Closes the JMX connection to the remote agent node if exists.
	 * @throws IOException if the connection cannot be closed cleanly.
	 * @see JMXConnector#close()
	 */
	public void close() throws IOException {
		if (jmxc != null) {
			jmxc.close();
			jmxc = null;
		}
	}

	/**
	 * Gets the UUID of all agent nodes of the managed JVM.
	 * @return The UUID of the found agent nodes.
	 * @throws IOException A communication problem occurred when searching for agent nodes.
	 * @throws SecurityException if the agent node query cannot be made for security reasons.
	 * @see MBeanServerConnection#queryNames(ObjectName, javax.management.QueryExp)
	 */
	public Set<String> getAgentNodeUUIDs() throws IOException {
		try {
			final Set<ObjectName> agentNodes = mbsc.queryNames(new JmxManager().getMgmtNameOfAgentNode("*"), null);
			final HashSet<String> agentNodeUUIDs = new HashSet<String>();
			for (ObjectName agentNode : agentNodes) {
				agentNodeUUIDs.add(agentNode.getKeyProperty("agentnode"));
			}
			return agentNodeUUIDs;
		}
		catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets a client for the management of an agent node within the managed JVM.
	 * @param agentNodeID The UUID of the agent node.
	 * @return A management client for the agent node.
	 * @throws MalformedObjectNameException The UUID of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @see JmxAgentNodeManagementClient#JmxAgentNodeManagementClient(MBeanServerConnection, String)
	 */
	public JmxAgentNodeManagementClient getAgentNodeManagementClient(String agentNodeID) throws MalformedObjectNameException {
		return new JmxAgentNodeManagementClient(mbsc, agentNodeID);
	}

	/**
	 * Gets a client for the management of an agent node timer within the managed JVM.
	 * @param agentNodeID The UUID of the agent node.
	 * @return A management client for the agent node timer.
	 * @throws MalformedObjectNameException The UUID of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @see JmxAgentNodeTimerManagementClient#JmxAgentNodeTimerManagementClient(MBeanServerConnection, String)
	 */
	public JmxAgentNodeTimerManagementClient getAgentNodeTimerManagementClient(String agentNodeID) throws MalformedObjectNameException {
		return new JmxAgentNodeTimerManagementClient(mbsc, agentNodeID);
	}

	/**
	 * Gets a client for the management of an agent within the managed JVM.
	 * @param agentNodeID The UUID of the agent node where the agent resides on.
	 * @param agentID The global unique ID of the agent.
	 * @return A management client for the agent.
	 * @throws MalformedObjectNameException The UUID of the agent node or the agent identifier contains an illegal character or does not follow the rules for quoting.
	 * @see JmxAgentManagementClient#JmxAgentManagementClient(MBeanServerConnection, String, String)
	 */
	public JmxAgentManagementClient getAgentManagementClient(String agentNodeID, String agentID) throws MalformedObjectNameException {
		return new JmxAgentManagementClient(mbsc, agentNodeID, agentID);
	}

	/**
	 * Gets a client for the management of an agent bean within the managed JVM.
	 * @param agentNodeID The UUID of the agent node where the agent of the agent bean resides on.
	 * @param agentID The global unique ID of the agent which contains the agent bean.
	 * @param agentBeanName the name of the agent bean.
	 * @return A management client for the agent bean.
	 * @throws MalformedObjectNameException The UUID of the agent node or agent bean or the agent identifier contains an illegal character or does not follow the rules for quoting.
	 * @see JmxAgentBeanManagementClient#JmxAgentBeanManagementClient(MBeanServerConnection, String, String, String)
	 */
	public JmxAgentBeanManagementClient getAgentBeanManagementClient(String agentNodeID, String agentID, String agentBeanName) throws MalformedObjectNameException {
		return new JmxAgentBeanManagementClient(mbsc, agentNodeID, agentID, agentBeanName);
	}

	/**
	 * Gets a client for the management of an agent node directory within the managed JVM.
	 * @param agentNodeID The UUID of the agent node of the directory.
	 * @return A management client for the agent node directory.
	 * @throws MalformedObjectNameException The UUID of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent node does not exist. 
	 * @throws IOException A communication problem occurred when querying the name of the <code>DirectoryAgentNodeBean</code> from the remote agent node.
	 * @throws SecurityException if the name of the <code>DirectoryAgentNodeBean</code> can not be queried for security reasons.
	 * @see JmxAgentNodeDirectoryManagementClient#JmxAgentNodeDirectoryManagementClient(MBeanServerConnection, String, String)
	 * @see JmxAgentNodeManagementClient#getDirectoryName()
	 */
	public JmxAgentNodeDirectoryManagementClient getDirectoryManagementClient(String agentNodeID) throws MalformedObjectNameException, InstanceNotFoundException, IOException {
		final String directoryBeanName = getAgentNodeManagementClient(agentNodeID).getDirectoryName();
		return new JmxAgentNodeDirectoryManagementClient(mbsc, agentNodeID, directoryBeanName);
	}

	/**
	 * Gets a client for the management of an agent memory within the managed JVM.
	 * @param agentNodeID The UUID of the agent node where the agent of the agent memory resides on.
	 * @param agentID The global unique ID of the agent which contains the agent memory.
	 * @return A management client for the agent memory.
	 * @throws MalformedObjectNameException The UUID of the agent node or the agent identifier contains an illegal character or does not follow the rules for quoting.
	 * @see JmxAgentMemoryManagementClient#JmxAgentMemoryManagementClient(MBeanServerConnection, String, String)
	 */
	public JmxAgentMemoryManagementClient getAgentMemoryManagementClient(String agentNodeID, String agentID) throws MalformedObjectNameException {
		return new JmxAgentMemoryManagementClient(mbsc, agentNodeID, agentID);
	}

	/**
	 * Gets a client for the management of an agent execution cycle within the managed JVM.
	 * @param agentNodeID The UUID of the agent node where the agent of the agent execution cycle resides on.
	 * @param agentID The global unique ID of the agent which contains the agent execution cycle.
	 * @return A management client for the agent execution cycle.
	 * @throws MalformedObjectNameException The UUID of the agent node or the agent identifier contains an illegal character or does not follow the rules for quoting.
	 * @see JmxAgentExecutionCycleManagementClient#JmxAgentExecutionCycleManagementClient(MBeanServerConnection, String, String)
	 */
	public JmxAgentExecutionCycleManagementClient getAgentExecutionCycleManagementClient(String agentNodeID, String agentID) throws MalformedObjectNameException {
		return new JmxAgentExecutionCycleManagementClient(mbsc, agentNodeID, agentID);
	}

	/**
	 * Gets the clients for the management of the communication beans of an agent within the managed JVM.
	 * @param agentNodeID The UUID of the agent node where the agent of the agent communication bean resides on.
	 * @param agentID The global unique ID of the agent which contains the agent communication bean.
	 * @return The management clients for the instances of <code>CommunicationBean</code>.
	 * @throws MalformedObjectNameException The UUID of the agent node or the agent identifier contains an illegal character or does not follow the rules for quoting.
	 * @throws IOException A communication problem occurred when searching for a communication bean of the remote agent.
	 * @throws SecurityException if the search cannot be made for security reasons.
	 * @see MBeanServerConnection#queryMBeans(ObjectName, javax.management.QueryExp)
	 * @see JmxAgentCommunicationManagementClient#JmxAgentCommunicationManagementClient(MBeanServerConnection, ObjectName)
	 */
	public Set<JmxAgentCommunicationManagementClient> getAgentCommunicationManagementClients(String agentNodeID, String agentID) throws MalformedObjectNameException, IOException {
		final Set<JmxAgentCommunicationManagementClient> clients = new HashSet<JmxAgentCommunicationManagementClient>();
		final Set<ObjectInstance> beans = mbsc.queryMBeans(new JmxManager().getMgmtNameOfAgentBean(agentNodeID, agentID, "*"), null);
		for (ObjectInstance bean : beans) {
			try {
				if (mbsc.isInstanceOf(bean.getObjectName(), CommunicationBeanMBean.class.getName())) {
					clients.add(new JmxAgentCommunicationManagementClient(mbsc, bean.getObjectName()));
				}
			} catch (InstanceNotFoundException infe) {
				infe.printStackTrace();
			}
		}
		return clients;
	}
}
