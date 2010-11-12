package de.dailab.jiactng.agentcore.management.jmx;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import javax.management.remote.JMXConnectorServer;

/**
 * This class holds information about an active, multicast-enabled network 
 * interface and its corresponding JMX connector servers.
 * @author Jan Keiser
 */
public class MulticastInterface {

	private InetAddress address;
	private Set<JMXConnectorServer> connectors;

	/**
	 * Constructor.
	 * @param address the IP address used for this network interface.
	 * @param connectors the created JMX connector server for this network interface.
	 */
	public MulticastInterface(InetAddress address, Set<JMXConnectorServer> connectors) {
		this.address = address;
		this.connectors = connectors;
	}

	/**
	 * Get the IP address used for this network interface.
	 * @return the IP address
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * Get the created JMX connector servers for this network interface.
	 * @return the set of JMX connector server
	 */
	public Set<JMXConnectorServer> getConnectors() {
		return connectors;
	}

	/**
	 * Get the URLs of the created JMX connector servers for this network interface.
	 * @return the set of URLs
	 */
	public Set<String> getJmxURLs() {
		Set<String> jmxURLs = new HashSet<String>();
		for (JMXConnectorServer connector : connectors) {
			jmxURLs.add(connector.getAddress().toString());
		}
		return jmxURLs;
	}
}
