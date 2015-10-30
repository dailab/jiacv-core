package de.dailab.jiactng.examples.jmxExample;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentNodeManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient;

/**
 * This class is an example how to use the JMX-based JIAC management client to get information about agent nodes and agents
 * from outside the JIAC application. This API also allows to change the application at runtime or to register as listener 
 * for events.
 *  
 * @author Jan Keiser
 */
public class ShowLocalAgents {

	/**
	 * Prints information about all agent nodes running on the local host.
	 * @param args this parameter will be ignored
	 */
	public static void main(String[] args) {
		// get all local IP addresses
		List<String> addresses = getLocalIPAddresses();
		if (addresses.isEmpty()) {
			System.err.println("No IP address found for a local active network interface");
			return;
		}		

		// searching for JIAC nodes
		List<JMXServiceURL> urls;
		System.out.println("Searching for JIAC nodes ...");
		try {
			urls = JmxManagementClient.getURLsFromMulticast();
		}
		catch (IOException e) {
			System.err.println("Unable to search for JIAC nodes: " + e.getLocalizedMessage());
			return;
		}

		// print information about all local nodes
		System.out.println("Following local nodes were found: ");
		List<String> nodeIds = new ArrayList<String>();
		for (JMXServiceURL url : urls) {
			if (!addresses.contains(url.getHost())) {
				// it is not a local URL => skip this node
				continue;
			}
			try {
				JmxManagementClient jmxClient = new JmxManagementClient(url);
				String nodeId = jmxClient.getAgentNodeUUID(url);
				// nodes may have more than one JMX URL => ignore additional ones
				if (!nodeIds.contains(nodeId)) {
					nodeIds.add(nodeId);
					System.out.println(nodeId + " (connected via " + url.getHost() + ")");
					JmxAgentNodeManagementClient nodeClient = jmxClient.getAgentNodeManagementClient(nodeId);
					System.out.println("\tname: " + nodeClient.getAgentNodeName());
					System.out.println("\towner: " + nodeClient.getAgentNodeOwner());
					System.out.println("\tstate: " + nodeClient.getAgentNodeState());
					System.out.println("\tplatform: " + nodeClient.getPlatformName());
					System.out.println("\tssl: " + nodeClient.isSslInUse());
					List<String> agentIds = nodeClient.getAgents();
					System.out.println("\tagents (" + agentIds.size() + "): ");

					// print information about the agents of the node
					for (String agentId: agentIds) {
						System.out.println("\t" + agentId);
						JmxAgentManagementClient agentClient = jmxClient.getAgentManagementClient(nodeId, agentId);
						System.out.println("\t\tname: " + agentClient.getAgentName());
						System.out.println("\t\towner: " + agentClient.getOwner());
						System.out.println("\t\tstate: " + agentClient.getAgentState());
						System.out.println("\t\tbeans: " + agentClient.getAgentBeanNames().size());
					}
				}
				jmxClient.close();
			}
			catch (Exception e) {
				System.err.println("Error while connecting to a node: " + e.getLocalizedMessage());
			}
		}
	}

	private static List<String> getLocalIPAddresses() {
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} 
		catch (Exception e) {
			System.err.println("Unable to get local network interfaces: " + e.getLocalizedMessage());
			return null;
		}

		ArrayList<String> hosts = new ArrayList<String>();
		InetAddress loopback = null;
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface ifc = networkInterfaces.nextElement();
			try {
				// consider only active network interfaces
				if (ifc.isUp() && ifc.supportsMulticast()) {
					// choose best address of the network interface
					InetAddress address = null;
					Enumeration<InetAddress> addresses = ifc.getInetAddresses();
					while (addresses.hasMoreElements()) {
						InetAddress addr = addresses.nextElement();
						// prefer IPv4 address
						if (addr instanceof Inet4Address) {
							if (ifc.isLoopback()) {
								loopback = addr;
							}
							else {
								address = addr;
							}
							break;
						} else if (!addr.isLinkLocalAddress()) {
							if (ifc.isLoopback() && (loopback == null)) {
								loopback = addr;
							}
							else {
								address = addr;
							}
						}
					}
					if (address != null) {
						// found valid address
						hosts.add(address.getHostAddress());
					}
				}
			} 
			catch (Exception e) {
				System.err.println("Unable to get information about network interface " + ifc.getName() + ": " + e.getLocalizedMessage());
			}
		}

		// add loopback address if no other addresses are available
		if (hosts.isEmpty() && (loopback != null)) {
			hosts.add(loopback.getHostAddress());
		}
		return hosts;
	}
}
