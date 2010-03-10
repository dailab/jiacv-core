package de.dailab.jiactng.agentcore.comm.transport;

import java.net.URI;

/**
 * JMX-compliant management interface of all message transports.
 * @author Jan Keiser
 */
public interface MessageTransportMBean {

	/**
	 * Getter for attribute "TransportIdentifier".
	 * @return the ID of the message transport
	 */
    String getTransportIdentifier();

    /**
     * Gets the connection entry point to this message transport,
     * i.e. if this transport maintains a server socket for TCP communication
     * then the URI might look like <code>tcp://192.168.3.42:4321</code>.
     * @return the connection entry point
     */
    URI getConnectorURI();

	/**
	 * Get the time-to-live of messages, which are send without a time-to-live parameter.
	 * @return the time-to-live of messages in milliseconds or 0 for no expiration
	 */
	long getTimeToLive();

	/**
	 * Set the time-to-live of messages, which are send without a time-to-live parameter.
	 * Please consider that the clocks of different hosts may run asynchronous!
	 * @param newTimeToLive the time-to-live of messages in milliseconds or 0 for no expiration
	 */
	void setTimeToLive(long newTimeToLive);
}
