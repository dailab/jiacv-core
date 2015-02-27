package de.dailab.jiactng.agentcore.management.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.IAgentNode;

/**
 * This class supports the periodically management of the JMX connector servers. This includes getting the actual list
 * of active and multicast supporting network interfaces, creating and removing connector server, and sending multicast
 * packets of the JMX URLs via the corresponding network interface.
 * 
 * @author Jan Keiser
 */
public final class JmxConnectorManager extends TimerTask {

	/** SSL identifier */
	private static final String SSL_USAGE_IDENTIFIER = "de.dailab.jiactng.agentcore.sslInUse";
	private static final String SSL_LIMITED_CIPHER_SUITES = "de.dailab.jiactng.agentcore.limitedCipherSuites";

	private JmxManager manager;
	private IAgentNode node;
	private int multicastPort;
	private InetAddress group;
	private MulticastSocket socket;
	private HashMap<String, MulticastInterface> interfaces = new HashMap<String, MulticastInterface>();
	private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	private Log log = null;

	/**
	 * Constructor for JMX connector manager. It creates a multicast socket for a given port and a given time to live for
	 * sending JMX connector URLs.
	 * 
	 * @param node The agent node, which will provide the connectors.
	 * @param port The port of the multicast socket to use.
	 * @param netaddr The group (multicast address) to join before sending packets.
	 * @param ttl The time-to-live for the packets to control the scope (0 &le; ttl &le; 255).
	 * @throws UnknownHostException if the multicast address is unknown.
	 * @throws IOException if creating multicast socket on given port failed.
	 */
	public JmxConnectorManager(IAgentNode node, int port, String netaddr, int ttl) throws UnknownHostException,
																					IOException {
		manager = new JmxManager();
		this.node = node;
		log = LogFactory.getLog(node.getUUID() + "." + getClass().getSimpleName());
		multicastPort = port;
		group = InetAddress.getByName(netaddr);
		socket = new MulticastSocket(multicastPort);
		try {
			socket.setTimeToLive(ttl);
		} catch (Exception e) {
			log.error("Unable to set time-to-live of multicast packets.");
		}
	}

	/**
	 * For each network interface, it joins the multicast group, updates the connector server and sends multicast packets
	 * via the network interface.
	 */
	@Override
	public void run() {
		// get all network interfaces
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (Exception e) {
			log.error("Unable to get network interfaces: " + e.getLocalizedMessage());
			return;
		}

		// joining multicast group
		try {
			socket.joinGroup(group);
		} catch (Exception e) {
			log.error("Unable to join multicast group " + group.getHostAddress());
			return;
		}

		// manage JMX connector servers for all network interfaces
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface ifc = networkInterfaces.nextElement();
			String ifcName = ifc.getName();
			try {
				if (ifc.isUp() && ifc.supportsMulticast()) {
					// handle active interface
					try {
						// choose best address of the network interface
						InetAddress address = null;
						Enumeration<InetAddress> addresses = ifc.getInetAddresses();
						while (addresses.hasMoreElements()) {
							InetAddress addr = addresses.nextElement();
							if (addr instanceof Inet4Address) {
								address = addr;
								break;
							} else if (!addr.isLinkLocalAddress()) {
								address = addr;
							}
						}
						if (address == null) {
							// found no valid address => continue with next interface
							continue;
						}

						// check connector server for the address
						if (!interfaces.containsKey(ifcName)) {
							// connectors not yet exist for this interface
							// => create connector servers
							log.warn("Adding JMX connector servers for new interface " + ifcName + " with address "
																											+ address.getHostAddress());
							addConnectors(ifcName, address);
						} else if (!interfaces.get(ifcName).getAddress().equals(address)) {
							// connectors already exist, but IP address of this interface has been changed
							// => re-add connector servers
							log.info("Re-adding JMX connector servers for changed interface " + ifcName); // System.out.println("Re-adding JMX connector servers for changed interface "
																																														// + ifcName);
							removeConnectors(ifcName);
							addConnectors(ifcName, address);
						}

						// send multicast messages
						socket.setNetworkInterface(ifc);
						for (String jmxURL : interfaces.get(ifcName).getJmxURLs()) {
							final byte[] buffer = jmxURL.getBytes();
							final DatagramPacket dp = new DatagramPacket(buffer, buffer.length, group, multicastPort);
							socket.send(dp);
						}
					} catch (Exception e1) {
						log.error("Unable to send multicast message on interface " + ifcName);
						e1.printStackTrace();
					}
				} else {
					// handle inactive interface
					if (interfaces.containsKey(ifcName)) {
						log.warn("Removing JMX connector servers for deactivated interface " + ifcName);
						removeConnectors(ifcName);
					}
				}
			} catch (Exception e) {
				// handle unknown interface
				if (interfaces.containsKey(ifcName)) {
					log.error("Unable to get information about interface " + ifcName + ": " + e.getLocalizedMessage());
					log.info("Removing JMX connector servers for unknown interface " + ifcName);
					removeConnectors(ifcName);
				}
			}
		}

		// removing connectors of old network interface
		for (String name : interfaces.keySet()) {
			try {
				if (NetworkInterface.getByName(name) == null) {
					log.info("Removing JMX connector servers for removed interface " + name);
					removeConnectors(name);
				}
			} catch (Exception e) {
				log.error("Unable to search for interface " + name + ": " + e.getLocalizedMessage());
			}
		}

		// leaving multicast group
		try {
			socket.leaveGroup(group);
		} catch (Exception e) {
			log.error("Unable to leave multicast group " + group.getHostAddress());
		}
	}

	/**
	 * Removes connector servers of all network interfaces.
	 */
	public void removeAll() {
		// create set of interface names to avoid ConcurrentModificationException
		Set<String> ifcNames = new HashSet<String>();
		for (String ifcName : interfaces.keySet()) {
			ifcNames.add(ifcName);
		}

		// remove connectors of all network interfaces
		for (String ifcName : ifcNames) {
			removeConnectors(ifcName);
		}
	}

	private JMXConnectorServer createConnector(JmxConnector conf, InetAddress address, String ifcName) {
		// check interface
		String ifc = conf.getInterface();
		if ((ifc != null) && !ifc.equals(ifcName)) {
			return null;
		}

		// get parameters of connector server
		String host = address.getHostAddress();
		final String protocol = conf.getProtocol();
		if (protocol == null) {
			log.warn("WARNING: No protocol specified for a JMX connector server");
			return null;
		}
		int port = conf.getPort();
		String path = conf.getPath();
		final JMXAuthenticator authenticator = conf.getAuthenticator();

		if (protocol.equals("rmi")) {
			// FIXME !!! Find a solution that works the same on all OSs
			if (!(System.getProperty("os.name").startsWith("Mac"))) {
				System.setProperty("java.rmi.server.hostname", address.getHostAddress());
			}

			// check use of RMI registry
			final int registryPort = ((RmiJmxConnector) conf).getRegistryPort();
			final String registryHost = ((RmiJmxConnector) conf).getRegistryHost();
			if ((registryPort > 0) || (registryHost != null)) {
				path = "/jndi/rmi://" + ((registryHost == null) ? address.getHostAddress() : registryHost)
																								+ ((registryPort > 0) ? ":" + registryPort : "") + "/" + node.getUUID()
																								+ "/" + ifcName;
			}
		}

		// configure authentication
		final HashMap<String, Object> env = new HashMap<String, Object>();
		if (authenticator != null) {
			env.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
		}

		synchronized (SSL_USAGE_IDENTIFIER) {

			boolean useSsl = false;
			try {
				useSsl = Boolean.parseBoolean(System.getProperty(SSL_USAGE_IDENTIFIER));
			} catch (Exception e) {
				log.error("Could not read ssl state!", e);
				useSsl = false;
			}
			if (useSsl) {
				// set ssl environments within environment map for jmx
				Object ks = System.getProperties().get("javax.net.ssl.keyStore");
				Object ksPwd = System.getProperties().get("javax.net.ssl.keyStorePassword");
				Object ksType = System.getProperties().get("javax.net.ssl.keyStoreType");
				if (ksType == null)
					ksType = "JKS";
				if (ks != null && ksPwd != null) {
					env.put("javax.net.ssl.keyStore", ks);
					env.put("javax.net.ssl.keyStoreType", ksType);
					env.put("javax.net.ssl.keyStorePassword", ksPwd);
				}
				Object ts = System.getProperties().get("javax.net.ssl.trustStore");
				Object tsPwd = System.getProperties().get("javax.net.ssl.trustStorePassword");
				Object tsType = System.getProperties().get("javax.net.ssl.trustStoreType");
				String commaDelimitedList = System.getProperty(SSL_LIMITED_CIPHER_SUITES);
				if (tsType == null)
					tsType = "JKS";
				if (ts != null && tsPwd != null) {
					env.put("javax.net.ssl.trustStore", ts);
					env.put("javax.net.ssl.trustStoreType", tsType);
					env.put("javax.net.ssl.trustStorePassword", tsPwd);
				}
				env.put("com.sun.management.jmxremote.registry.ssl", true);
				env.put("com.sun.management.jmxremote.ssl.need.client.auth", true);
				env.put("com.sun.management.jmxremote.ssl", true);
				if (commaDelimitedList != null)
					env.put("com.sun.management.jmxremote.ssl.enabled.cipher.suites", commaDelimitedList);
				SslRMIClientSocketFactory csf = new SslRMIClientSocketFactory();
				SslRMIServerSocketFactory ssf = new SslRMIServerSocketFactory();
				env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, csf);
				env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, ssf);
			}
		}

		// construct server URL
		JMXServiceURL jurl = null;
		try {
			jurl = new JMXServiceURL(protocol, host, port, path);
		} catch (Exception e) {
			log.error("WARNING: Unable to construct URL of JMX connector server.");
			log.error("It is not possible to find the local host name, or the protocol " + protocol + ", port " + port
																							+ " or path " + path + " is incorrect.");
			log.error(e.getMessage());
			return null;
		}

		// create connector server
		log.info("Creating JMX connector server: " + jurl);
		try {
			return JMXConnectorServerFactory.newJMXConnectorServer(jurl, env, mbs);
		} catch (MalformedURLException e) {
			log.error("WARNING: Unable to create JMX connector server for " + jurl);
			log.error("Missing provider implementation for the specified protocol.");
			log.error(e.getMessage());
			return null;
		} catch (Exception e) {
			log.error("WARNING: Unable to create JMX connector server for " + jurl);
			log.error("Communication problem, or the found provider implementation for the specified protocol can not be used.");
			log.error(e.getMessage());
			return null;
		}
	}

	private void addConnectors(String ifcName, InetAddress address) {
		Set<JMXConnectorServer> connectors = new HashSet<JMXConnectorServer>();
		for (JmxConnector conf : node.getJmxConnectors()) {
			// create connector server
			JMXConnectorServer cs = createConnector(conf, address, ifcName);
			if (cs == null) {
				continue;
			}

			// start connector server
			try {
				cs.start();
			} catch (Exception e) {
				log.error("WARNING: Start of JMX connector server failed for protocol " + conf.getProtocol());
				if (conf instanceof RmiJmxConnector) {
					final int registryPort = ((RmiJmxConnector) conf).getRegistryPort();
					final String registryHost = ((RmiJmxConnector) conf).getRegistryHost();
					if ((registryPort > 0) || (registryHost != null)) {
						log.error("Please ensure that a rmi registry is started on "
																										+ ((registryHost == null) ? address.getHostAddress() : registryHost)
																										+ ((registryPort > 0) ? ":" + registryPort : ""));
					}
				}
				log.error(e.getMessage());
				continue;
			}
			log.warn("JMX connector server successfully started: " + cs.getAddress());
			connectors.add(cs);

			// register connector server as JMX resource
			try {
				manager.registerAgentNodeResource(node, JmxManager.CATEGORY_JMX_CONNECTOR_SERVER,
																								"\"" + cs.getAddress() + "\"", cs);
			} catch (Exception e) {
				log.error("WARNING: Unable to register JMX connector server \"" + cs.getAddress() + "\" as JMX resource.");
				log.error(e.getMessage());
			}

		}

		// add all connector servers
		MulticastInterface mcInterface = new MulticastInterface(address, connectors);
		interfaces.put(ifcName, mcInterface);
	}

	private void removeConnectors(String ifcName) {
		// deregister and stop all connector servers
		for (JMXConnectorServer connector : interfaces.get(ifcName).getConnectors()) {
			// TODO deregister the connector server from the server directory

			// deregister connector server as JMX resource
			try {
				manager.unregisterAgentNodeResource(node, JmxManager.CATEGORY_JMX_CONNECTOR_SERVER,
																								"\"" + connector.getAddress() + "\"");
			} catch (Exception e) {
				log.error("WARNING: Unable to deregister JMX connector server \"" + connector.getAddress()
																								+ "\" as JMX resource.");
				log.error(e.getMessage());
			}

			// stop connector server
			log.info("Stop connector server " + connector.getAddress().toString());
			try {
				connector.stop();
			} catch (Exception e) {
				log.error("WARNING: Unable to stop JMX connector server!");
				log.error(e.getMessage());
			}
		}

		// remove all connector servers
		interfaces.remove(ifcName);
	}
}
