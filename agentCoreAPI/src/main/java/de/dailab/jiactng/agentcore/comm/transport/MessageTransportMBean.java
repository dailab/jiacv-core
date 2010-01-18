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
}
