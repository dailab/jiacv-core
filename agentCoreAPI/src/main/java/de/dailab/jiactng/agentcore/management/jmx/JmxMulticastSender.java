package de.dailab.jiactng.agentcore.management.jmx;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.TimerTask;

/**
 * This class supports the periodically sending of multicast packets.
 * @author Jan Keiser
 */
public final class JmxMulticastSender extends TimerTask {

	private int multicastPort;
	private String groupAddress;
	private String[] jmxURLs;
	private InetAddress group; 
	private MulticastSocket socket;
	private boolean connected = true;

	/**
	 * Creates a multicast socket for a given port and a given time to live.
	 * @param port The port of the multicast socket to use.
	 * @param netaddr The group (multicast address) to join before sending packets.
	 * @param ttl The time-to-live for the packets to control the scope (0 &le; ttl &le; 255).
	 * @param jmxURLs The messages to be sent via multicast.
	 */
	public JmxMulticastSender(int port, String netaddr, int ttl, String[] jmxURLs) {
		multicastPort = port;
		groupAddress = netaddr;
		this.jmxURLs = jmxURLs;
		try {
	    	group = InetAddress.getByName(groupAddress);
	    	socket = new MulticastSocket(multicastPort);
	    	socket.setTimeToLive(ttl);
		}
	    catch (Exception e) {
	    	e.printStackTrace();
	    }	
	}

	/**
	 * For each message, it joins the multicast group and sends a multicast packet.
	 */
	@Override
	public void run() {
		for (int i=0; i<jmxURLs.length; i++) {
			final String message = jmxURLs[i];
			try {
				socket.joinGroup(group);
				if (!connected) {
					System.out.println("Network connection established to send on multicast socket.");
					connected = true;
				}
				final byte[] buffer = message.getBytes();
				final DatagramPacket dp = new DatagramPacket(buffer, buffer.length, group, multicastPort);
	      
				socket.send(dp);
				socket.leaveGroup(group);
			}
			catch (Exception e) {
				if (connected) {
					System.err.println("Missing network connection to send on multicast socket!");
					connected = false;
				}
			}
		}
	}

}
