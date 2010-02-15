/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.broker;


/**
 * This class is used for the description of ActiveMQ transport connectors.
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ActiveMQTransportConnector {
    private String transportURI= null;
    private String networkURI = null;
	private String discoveryURI= null;
	private boolean duplex = true;
	private int networkTTL = 20;
    
    /**
     * Set the URI where remote connections are accepted. Examples are:
     * <pre>
     *   tcp://localhost:61616
     *   stomp://dai076.dai-lab.de:61613
     * </pre>
     * 
     * @param newTransportURI the transport URI
     */
    public void setTransportURI(String newTransportURI) {
        transportURI= newTransportURI;
    }
    
    /**
     * Set the URI for the discovery agent of this connector. For example:
     * <pre>
     *   multicast://239.255.2.45:5555
     * </pre>
     * 
     * @param newDiscoveryURI  the discovery URI or <code>null</code>
     *                         if discovery should be deactivated
     */
    public void setDiscoveryURI(String newDiscoveryURI) {
        discoveryURI= newDiscoveryURI;
    }

    /**
     * Get the URI where remote connections are accepted.
     * @return the transport URI
     * @see #setTransportURI(String)
     */
    public String getTransportURI() {
        return transportURI;
    }

    /**
     * Get the URI for the discovery agent of this connector.
     * @return the discovery URI
     * @see #setDiscoveryURI(String)
     */
    public String getDiscoveryURI() {
        return discoveryURI;
    }

    public String getNetworkURI() {
		return networkURI;
	}

	public void setNetworkURI(String newNetworkURI) {
		networkURI = newNetworkURI;
	}

    public boolean isDuplex() {
		return duplex;
	}

	public void setDuplex(boolean newDuplex) {
		duplex = newDuplex;
	}

	public int getNetworkTTL() {
		return networkTTL;
	}

	public void setNetworkTTL(int newNetworkTTL) {
		networkTTL = newNetworkTTL;
	}

	/**
	 * Returns a single-line text which contains the transport URI, discovery URI, 
	 * network URI, network TTL, and duplex of the ActiveMQ transport connector.
	 * @return a string representation of the transport connector
	 */
	@Override
    public String toString() {
        return "transportURI='" 
        	+ getTransportURI() 
        	+ "'; discoverURI='" 
        	+ getDiscoveryURI() 
        	+ "'; networkURI='" 
        	+ getNetworkURI() 
        	+ "'; networkTTL='"
        	+ networkTTL
        	+ "'; duplex='"
        	+ duplex
        	+ "'";
    }
}
