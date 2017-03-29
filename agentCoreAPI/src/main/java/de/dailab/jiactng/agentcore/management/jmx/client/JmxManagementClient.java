package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.security.Key;
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

import org.apache.log4j.Logger;

import de.dailab.jiactng.agentcore.comm.CommunicationBeanMBean;
import de.dailab.jiactng.agentcore.management.jmx.JmxConnectorManager;
import de.dailab.jiactng.agentcore.management.jmx.JmxManager;
import de.dailab.jiactng.agentcore.util.IdFactory;
import de.dailab.jiactng.agentcore.util.sec.CryptoRSA;

/**
 * This JMX client enables the remote management of a JVM.
 * @author Jan Keiser
 */
public class JmxManagementClient {

	/** The timeout for connection tests is 2,000 milliseconds. */
	public static final long CONNECTION_TESTER_TIMEOUT = 2000;

	/** The maximum length of multicast messages is 1,000 bytes. */
	public static final int MAX_MULTICAST_MESSAGE_LENGTH = 1000;

	protected final static Logger log = Logger.getLogger(JmxManagementClient.class);

	/** The connection to the MBeanServer of a JVM used to manage its resources. */
	protected MBeanServerConnection mbsc = null;
	private JMXConnector jmxc = null;

	/**
	 * Gets the URL of all JMX connector server, which are registered in a RMI
	 * registry or agent node directory and are reachable by the client. 
	 * @param host The host of the registry.
	 * @param port The port used by the registry.
	 * @return The list of found JMX URLs.
	 * @throws RemoteException if remote communication with the registry failed. If exception is a <code>ServerException</code> containing an <code>AccessException</code>, then the registry denies the caller access to perform this operation.
	 * @see java.rmi.registry.Registry#list()
	 * @see JMXServiceURL#JMXServiceURL(String, String, int, String)
	 * @see JmxConnectionTester
	 */
	public static List<JMXServiceURL> getURLsFromRegistry(String host, int port) throws RemoteException {
		return getURLsFromRegistry( host, port, null);
	}

	/**
	 * Gets the URL of all JMX connector server of the owner of a given key, 
	 * which are registered in a RMI registry and are reachable by the client. 
	 * @param host The host of the registry.
	 * @param port The port used by the registry.
	 * @param publicKey the stream of the public RSA key
	 * @return The list of found JMX URLs of the key owner.
	 * @throws RemoteException if remote communication with the registry failed. If exception is a <code>ServerException</code> containing an <code>AccessException</code>, then the registry denies the caller access to perform this operation.
	 * @see java.rmi.registry.Registry#list()
	 * @see JMXServiceURL#JMXServiceURL(String, String, int, String)
	 * @see JmxConnectionTester
	 * @see CryptoRSA
	 */
	public static List<JMXServiceURL> getURLsFromRegistry(String host, int port, InputStream publicKey) throws RemoteException {
		Key key = null;
		if (publicKey != null) {
			// extract public RSA key
			key = null;
			try {
				key = CryptoRSA.getKey(publicKey);
			}
			catch (Exception e) {
				log.error("Unable to get public RSA key from stream", e);
				return new ArrayList<JMXServiceURL>();
			}
		}

		// check encrypted IDs from registry
		final JmxConnectionTester tester = new JmxConnectionTester(false);
		final Iterator<String> nodeIdIterator = Arrays.asList(LocateRegistry.getRegistry(host, port).list()).iterator();
		while (nodeIdIterator.hasNext()) {
			final String encryptedNodeId = nodeIdIterator.next();
			String nodeId = encryptedNodeId;
			if (key != null) {
				// decrypt ID from registry
				try {
					final byte[] input = CryptoRSA.fromHexString(encryptedNodeId);
					nodeId = new String(CryptoRSA.decrypt(key, input), Charset.forName(JmxConnectorManager.CHAR_ENC));
				}
				catch (Exception e) {
					log.debug("Decryption of the path suffix of a JMX URL failed: " + e.getLocalizedMessage());
					continue;
				}
			}

			// create URL from ID and add to list of URLs
			if (nodeId.startsWith(IdFactory.IdPrefix.Node.toString())) {
				try {
					final JMXServiceURL url = new JMXServiceURL("rmi", null, 0, JmxConnectorManager.REGISTRY_PREFIX + "rmi://" + host + ":" + port + "/" + encryptedNodeId);
					tester.addURL(url);
				}
				catch (Exception e) {
					log.error("Unable to create JMX URL from RMI registry entry: " + encryptedNodeId, e);
				}
			}				
		}

		// wait for connection tests
		try {
			Thread.sleep(CONNECTION_TESTER_TIMEOUT);
		}
		catch (InterruptedException e) {
			log.warn("JMX connection tester was interrupted", e);
		}

		return tester.getResult();
	}

	/**
	 * Gets the URL of all JMX connector server, which are announced by 
	 * multicast messages or via agent node directories and are reachable by 
	 * the client.
	 * @return The list of found JMX URLs.
	 * @throws IOException A communication problem occurred when creating a multicast socket or receiving multicast packets.
	 * @see MulticastSocket#receive(DatagramPacket)
	 * @see JmxConnectionTester
	 */
	public static List<JMXServiceURL> getURLsFromMulticast() throws IOException {
		return getURLsFromMulticast(null);
	}

	/**
	 * Gets the URL of all JMX connector server of the owner of a given key, 
	 * which are announced by multicast messages and are reachable by 
	 * the client.
	 * @param publicKey the stream of the public RSA key
	 * @return The list of found JMX URLs.
	 * @throws IOException A communication problem occurred when creating a multicast socket or receiving multicast packets.
	 * @see MulticastSocket#receive(DatagramPacket)
	 * @see JmxConnectionTester
	 * @see CryptoRSA
	 */
	public static List<JMXServiceURL> getURLsFromMulticast(InputStream publicKey) throws IOException {
		Key key = null;
		if (publicKey != null) {
			// extract public RSA key
			try {
				key = CryptoRSA.getKey(publicKey);
			}
			catch (Exception e) {
				log.error("Unable to get public RSA key from stream", e);
				return new ArrayList<JMXServiceURL>();
			}
		}

		// check encrypted URLs from multicast
		final JmxConnectionTester tester = new JmxConnectionTester(false);
		byte[] buffer = new byte[MAX_MULTICAST_MESSAGE_LENGTH];
		final long endTime = System.currentTimeMillis() + JmxManager.MULTICAST_PERIOD + CONNECTION_TESTER_TIMEOUT;

		// activate multicast socket
		final InetAddress group = InetAddress.getByName("226.6.6.7");
		final MulticastSocket socket = new MulticastSocket(9999);
		socket.setSoTimeout((int)(JmxManager.MULTICAST_PERIOD+CONNECTION_TESTER_TIMEOUT));
		socket.setTimeToLive(1);
		final DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		socket.joinGroup(group);

		try {
			// read multicast packets
			while (System.currentTimeMillis() < endTime) {
				// read message
				dp.setLength(MAX_MULTICAST_MESSAGE_LENGTH);
				socket.receive(dp);
				buffer = dp.getData();

				// get message from multicast
				String message = null;
				if (key != null) {
					try {
						if (dp.getLength() > CryptoRSA.ENCRYPTION_OUTPUT_LENGTH) {
							// decrypt only message suffix
							final int nonEncryptionLength = dp.getLength() - CryptoRSA.ENCRYPTION_OUTPUT_LENGTH;

							// copy non-encrypted prefix to result
							final byte[] output = new byte[nonEncryptionLength + CryptoRSA.ENCRYPTION_OUTPUT_LENGTH];
							for (int i=0; i<nonEncryptionLength; i++) {
								output[i] = buffer[i];
							}

							// decrypt suffix
							final int decryptionOutputLength = CryptoRSA.decrypt(key, buffer, nonEncryptionLength, CryptoRSA.ENCRYPTION_OUTPUT_LENGTH, output, nonEncryptionLength);
							if (decryptionOutputLength != CryptoRSA.ENCRYPTION_INPUT_LENGTH) {
								log.error("Wrong size of decryption result");
							}

							message = new String(output, 0, nonEncryptionLength+decryptionOutputLength, Charset.forName(JmxConnectorManager.CHAR_ENC));
						}
						else {
							// decrypt full message
							message = new String(CryptoRSA.decrypt(key, buffer), Charset.forName(JmxConnectorManager.CHAR_ENC));
						}
					}
					catch (Exception e) {
						log.debug("Decryption of a JMX URL failed: " + e.getLocalizedMessage());
						continue;
					}
				}
				else {
					message = new String(buffer, 0, dp.getLength());
				}

				// add converted message to list of URLs
				try {
					JMXServiceURL url = new JMXServiceURL(message);
					// replace localhost within URL path
					if (url.getURLPath().contains("localhost")) {
						url = new JMXServiceURL(message.replace("localhost", url.getHost()));
					}
					tester.addURL(url);
				} catch (Exception e) {
					log.error("Got multicast message with wrong JMX URL format", e);
				}					
			}
		} catch (SocketTimeoutException ste) {
			log.error("Timeout for reading multicast packets", ste);
		}
		socket.close();

		return tester.getResult();
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
	public JmxManagementClient(JMXServiceURL url, String username, String password) throws IOException {
		final HashMap<String,Object> env = new HashMap<String,Object>();
	    env.put(JMXConnector.CREDENTIALS, new String[] {username, password});
		jmxc = JMXConnectorFactory.connect(url, env);
		mbsc = jmxc.getMBeanServerConnection();
	}

	/**
	 * Creates a new management client which uses the JMX connection of 
	 * another management client. This connection can only be closed by the 
	 * original management client.
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
	public final void close() throws IOException {
		if (jmxc != null) {
			jmxc.close();
			jmxc = null;
		}
	}

	/**
	 * This method is called by the garbage collector to close the JMX 
	 * connection to the remote agent node if it still exists. 
	 */
	protected void finalize() {
		try {
			close();
		} catch (IOException e) {}
	}

	/**
	 * Gets the UUID of all agent nodes of the managed JVM.
	 * @return The UUID of the found agent nodes.
	 * @throws IOException A communication problem occurred when searching for agent nodes.
	 * @throws SecurityException if the agent node query cannot be made for security reasons.
	 * @see MBeanServerConnection#queryNames(ObjectName, javax.management.QueryExp)
	 */
	public final Set<String> getAgentNodeUUIDs() throws IOException {
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
	 * Gets the UUID of the agent node which provides a JMX connector server with the given URL.
	 * @param url The URL of the JMX connector server.
	 * @return The UUID of the agent node or <code>null</code> if no JMX connector server with the given URL is registered.
	 * @throws IOException A communication problem occurred when searching for agent nodes.
	 * @throws MalformedObjectNameException if the URL contains an illegal character or does not follow the rules for quoting.
	 * @throws SecurityException if the agent node query cannot be made for security reasons.
	 * @see MBeanServerConnection#queryNames(ObjectName, javax.management.QueryExp)
	 */
	public final String getAgentNodeUUID(JMXServiceURL url) throws IOException, MalformedObjectNameException {
		final Set<ObjectName> connectorServers = mbsc.queryNames(new JmxManager().getMgmtNameOfAgentNodeResource("*", "JMXConnectorServer", "*"), null);
		if (url.getURLPath().startsWith(JmxConnectorManager.REGISTRY_PREFIX)) {
			// registry JMX URL
			final String id = new URL(url.getURLPath().substring(JmxConnectorManager.REGISTRY_PREFIX.length()).replaceFirst("[a-z]*:", "http:")).getPath();
			for (ObjectName server : connectorServers) {
				final String quotedServerUrl = server.getKeyProperty("resource");
				final JMXServiceURL serverUrl = new JMXServiceURL(quotedServerUrl.substring(1, quotedServerUrl.length()-1));
				if (serverUrl.getURLPath().startsWith(JmxConnectorManager.REGISTRY_PREFIX)) {
					final String serverId = new URL(serverUrl.getURLPath().substring(JmxConnectorManager.REGISTRY_PREFIX.length()).replaceFirst("[a-z]*:", "http:")).getPath();
					if (id.equals(serverId)) {
						return server.getKeyProperty("agentnode");
					}
				}
			}
		}
		else {
			// multicast JMX URL
			for (ObjectName server : connectorServers) {
				final String quotedServerUrl = server.getKeyProperty("resource");
				final JMXServiceURL serverUrl = new JMXServiceURL(quotedServerUrl.substring(1, quotedServerUrl.length()-1));
				if (url.equals(serverUrl)) {
					return server.getKeyProperty("agentnode");					
				}
			}			
		}
		return null;
	}

	/**
	 * Gets a client for the management of an agent node within the managed JVM.
	 * @param agentNodeID The UUID of the agent node.
	 * @return A management client for the agent node.
	 * @throws MalformedObjectNameException The UUID of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @see JmxAgentNodeManagementClient#JmxAgentNodeManagementClient(MBeanServerConnection, String)
	 */
	public final JmxAgentNodeManagementClient getAgentNodeManagementClient(String agentNodeID) throws MalformedObjectNameException {
		return new JmxAgentNodeManagementClient(mbsc, agentNodeID);
	}

	/**
	 * Gets a client for the management of an agent node timer within the managed JVM.
	 * @param agentNodeID The UUID of the agent node.
	 * @return A management client for the agent node timer.
	 * @throws MalformedObjectNameException The UUID of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @see JmxAgentNodeTimerManagementClient#JmxAgentNodeTimerManagementClient(MBeanServerConnection, String)
	 */
	public final JmxAgentNodeTimerManagementClient getAgentNodeTimerManagementClient(String agentNodeID) throws MalformedObjectNameException {
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
	public final JmxAgentManagementClient getAgentManagementClient(String agentNodeID, String agentID) throws MalformedObjectNameException {
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
	public final JmxAgentBeanManagementClient getAgentBeanManagementClient(String agentNodeID, String agentID, String agentBeanName) throws MalformedObjectNameException {
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
	public final JmxAgentNodeDirectoryManagementClient getDirectoryManagementClient(String agentNodeID) throws MalformedObjectNameException, InstanceNotFoundException, IOException {
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
	public final JmxAgentMemoryManagementClient getAgentMemoryManagementClient(String agentNodeID, String agentID) throws MalformedObjectNameException {
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
	public final JmxAgentExecutionCycleManagementClient getAgentExecutionCycleManagementClient(String agentNodeID, String agentID) throws MalformedObjectNameException {
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
	public final Set<JmxAgentCommunicationManagementClient> getAgentCommunicationManagementClients(String agentNodeID, String agentID) throws MalformedObjectNameException, IOException {
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
