package de.dailab.jiactng.examples.jmxExample;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.management.jmx.MessageExchangeNotification;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentCommunicationManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentNodeManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient;

/**
 * This class is an example how to use the JMX-based JIAC management client to get notified about exchanged messages of 
 * an agent outside the JIAC application. This API also allows to change the application at runtime.
 *  
 * @author Jan Keiser
 */
public class ShowMessages implements NotificationListener {

	/**
	 * Starter for observing messages of an agent.
	 * @param args (1) IP address of the agent node, (2) name of the agent node, (3) name of the agent
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("WARN: No arguments given => observing PingAgent from the PingPong3 example running on localhost.");
			new ShowMessages().showMessages("127.0.0.1", "PingNode", "PingAgent");
		}
		else {
			new ShowMessages().showMessages(args[0], args[1], args[2]);
		}
	}

	/**
	 * Searches for the given agent and registers as listener for exchanged messages until Enter was pressed.
	 * @param address IP address of the agent node
	 * @param nodeName name of the agent node
	 * @param agentName name of the agent
	 */
	private void showMessages(String address, String nodeName, String agentName) {
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

		// connect to right node and register message event listener
		for (JMXServiceURL url : urls) {
			if (url.getHost().equals(address)) {
				// it is the right host
				try {
					JmxManagementClient jmxClient = new JmxManagementClient(url);
					String nodeId = jmxClient.getAgentNodeUUID(url);
					JmxAgentNodeManagementClient nodeClient = jmxClient.getAgentNodeManagementClient(nodeId);
					if (nodeClient.getAgentNodeName().equals(nodeName)) {
						// found the right agent node
						List<String> agentIds = nodeClient.getAgents();
						for (String agentId: agentIds) {
							JmxAgentManagementClient agentClient = jmxClient.getAgentManagementClient(nodeId, agentId);
							if (agentClient.getAgentName().equals(agentName)) {
								// found the right agent
								Set<JmxAgentCommunicationManagementClient> commClients = jmxClient.getAgentCommunicationManagementClients(nodeId, agentId);
								if (commClients.isEmpty()) {
									System.err.println("Agent has no communication bean!");
								}
								else {
									if (commClients.size() > 1) {
										System.err.println("Agent has more than one communication bean!");
									}
									// add listener for message exchange
									JmxAgentCommunicationManagementClient communication = commClients.iterator().next();
									communication.addMessageExchangeListener(this, null);
									System.out.println("Started listening for messages (press <Enter> to stop).");
									try {
									    System.in.read();
									} catch (IOException e) {
									    e.printStackTrace();
									}
									// remove listener for message exchange
									communication.removeMessageExchangeListener(this, null);
									System.out.println("Stopped listening for messages.");
									break;
								}
							}
						}
					}
					jmxClient.close();
				}
				catch (Exception e) {
					System.err.println("Error while connecting to a node: " + e.getLocalizedMessage());
				}
			}
		}
	}

	/**
	 * Prints payload and header information of the exchanged message.
	 */
	@Override
	public void handleNotification(Notification notification, Object handback) {
		if (notification instanceof MessageExchangeNotification) {
			MessageExchangeNotification n = (MessageExchangeNotification) notification;
			System.out.println(n.getAction().toString() + " message:");
			CompositeDataSupport message = (CompositeDataSupport) n.getJiacMessage();
			System.out.println("\tpayload: " + message.get(IJiacMessage.ITEMNAME_PAYLOAD));
			System.out.println("\theaders:");
			CompositeDataSupport headers = (CompositeDataSupport) message.get(IJiacMessage.ITEMNAME_HEADERS);
			Iterator<String> keys = headers.getCompositeType().keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				System.out.println("\t\t" + key + ": " + headers.get(key));
			}
		}
	}

}
