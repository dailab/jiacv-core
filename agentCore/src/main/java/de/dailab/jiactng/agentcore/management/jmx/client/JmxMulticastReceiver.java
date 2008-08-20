package de.dailab.jiactng.agentcore.management.jmx.client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

import javax.management.remote.JMXServiceURL;

/**
 * This class supports the permanently receiving of multicast packets containing the
 * JMX-URL of agent nodes which will be monitored. It also supports the periodically 
 * waiting for network connection, if it is interrupted.
 * @author Jan Keiser
 */
public class JmxMulticastReceiver implements Runnable {

	private ArrayList<JMXServiceURL> urls = new ArrayList<JMXServiceURL>();
	private boolean active = false;

	private String message;
	private byte[] buffer = new byte[1000];
	private int multicastPort;
	private String groupAddress;
	private int ttl = 1;
	private boolean connected = true;

	/**
	 * Creates a multicast receiver.
	 * @param port The port of the multicast socket to use.
	 * @param netaddr The group (multicast address) to join before receiving packets.
	 * @param ttl The time-to-live for the packets to control the scope (0 <= ttl <= 255).
	 */
	public JmxMulticastReceiver(int port, String netaddr, int ttl) {
		multicastPort = port;
		groupAddress = netaddr;
		this.ttl=ttl;
	}

	/**
	 * Creates a multicast socket for the port and time-to-live, joins it to the group and
	 * permanently waits for receiving packets, which contains the JMX-URL of agent nodes
	 * to be monitored, until this thread is finished. If the connection is interrupted,
	 * the activation of the multicast socket will be retried after one second.
	 */
	@Override
	public void run() {
		active = true;
		while (active) {
			try {
				// activate multicast socket
				InetAddress group = InetAddress.getByName(groupAddress);
				MulticastSocket socket = new MulticastSocket(multicastPort);
				socket.setTimeToLive(ttl);
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
				socket.joinGroup(group);

				// network connection ok
				if (!connected) {
					System.out.println("Network connection established to read on multicast socket.");
					connected = true;
				}

				// read multicast packets
				while (active) {
					// read message
					dp.setLength(1000);
					socket.receive(dp);
					buffer = dp.getData();
					message = new String(buffer, 0, dp.getLength());

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

			} catch (Exception e) {
				// interrupted network connection
				if (connected) {
					System.err.println("Missing network connection to read on multicast socket!");
					connected = false;
				}

				// wait one second for next try of multicast socket activation
				try {
					wait(1000);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}

	/**
	 * Stops this thread after a specified duration and returns the received JMX URLs.  
	 * @param duration The time to wait in milliseconds.
	 * @return The list of received JMX URLs.
	 */
	public ArrayList<JMXServiceURL> getResult(long duration) {
		try {
			wait(duration);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		active = false;
		return urls;
	}
}
