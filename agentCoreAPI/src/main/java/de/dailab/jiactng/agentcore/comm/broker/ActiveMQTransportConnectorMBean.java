package de.dailab.jiactng.agentcore.comm.broker;

/**
 * Management interface of transport connectors. Changes on the values will be
 * considered after re-initialization of the broker.
 * @author Jan Keiser
 */
public interface ActiveMQTransportConnectorMBean {

	public static String RESOURCE_TYPE = "TransportConnector";

    /**
     * Set the URI where remote connections are accepted. Examples are:
     * <pre>
     *   tcp://localhost:61616
     *   stomp://dai076.dai-lab.de:61613
     * </pre>
     * 
     * @param newTransportURI the transport URI
     */
    void setTransportURI(String newTransportURI);
    
    /**
     * Set the URI for the discovery agent of this connector. For example:
     * <pre>
     *   multicast://239.255.2.45:5555
     * </pre>
     * 
     * @param newDiscoveryURI  the discovery URI or <code>null</code>
     *                         if discovery should be deactivated
     */
    void setDiscoveryURI(String newDiscoveryURI);

    /**
     * Get the URI where remote connections are accepted.
     * @return the transport URI
     * @see #setTransportURI(String)
     */
    String getTransportURI();

    /**
     * Get the URI for the discovery agent of this connector.
     * @return the discovery URI
     * @see #setDiscoveryURI(String)
     */
    String getDiscoveryURI();

    /**
     * Get the discovery address of the network connector.
     * @return the discovery address
     */
    String getNetworkURI();

    /**
     * Set the discovery address of the network connector.
     * @param newNetworkURI the discovery address to set
     */
	void setNetworkURI(String newNetworkURI);

	/**
	 * Indicates whether the network connector supports duplex communication or not.
	 * @return <code>true</code> for duplex communication
	 */
    boolean isDuplex();

    /**
	 * Set whether the network connector will support duplex communication or not.
     * @param newDuplex <code>true</code> for duplex communication
     */
	void setDuplex(boolean newDuplex);

	/**
	 * Get TTL of the network connector.
	 * @return the TTL
	 */
	int getNetworkTTL();

	/**
	 * Set TTL of the network connector.
	 * @param newNetworkTTL the TTL to set
	 */
	void setNetworkTTL(int newNetworkTTL);
	
	/**
	 * Get name of the network connector.
	 * @return the name
	 */
	String getName();
	
	/**
	 * Set name of the network connector.
	 * @param name to set
	 */
	void setName(String name);

}
