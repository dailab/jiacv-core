package de.dailab.jiactng.agentcore.management.jmx;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.management.remote.JMXConnectorServer;

/**
 * This class holds information about an active, multicast-enabled network 
 * interface and its corresponding JMX connector servers.
 * @author Jan Keiser
 */
public class MulticastInterface {

	private InetAddress address;
	private Map<JMXConnectorServer,byte[]> connectors;

	/**
	 * Constructor.
	 * @param address the IP address used for this network interface.
	 * @param connectors the created JMX connector server and the corresponding (encoded) URLs for this network interface.
	 */
	public MulticastInterface(InetAddress address, Map<JMXConnectorServer,byte[]> connectors) {
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
		return connectors.keySet();
	}

	/**
	 * Get the (encoded) URLs of the created JMX connector servers for this network interface.
	 * @return the set of URLs
	 */
	public Collection<byte[]> getJmxURLs() {
		return connectors.values();
	}
}
