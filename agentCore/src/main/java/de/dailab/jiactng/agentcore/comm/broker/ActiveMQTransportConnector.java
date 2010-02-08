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
    private String _transportURI= null;
    private String _networkURI = null;
	private String _discoveryURI= null;
	private boolean duplex = true;
	private int networkTTL = 20;
    
    /**
     * The URI where remote connections are accepted. Examples are:
     * <pre>
     *   tcp://localhost:61616
     *   stomp://dai076.dai-lab.de:61613
     * </pre>
     * 
     * @param transportURI
     */
    public void setTransportURI(String transportURI) {
        _transportURI= transportURI;
    }
    
    /**
     * The URI for the discovery agent of this connector. For example:
     * <pre>
     *   multicast://239.255.2.45:5555
     * </pre>
     * 
     * @param discoveryURI  the discovery URI or <code>null</code>
     *                      if discovery should be deactivated
     */
    public void setDiscoveryURI(String discoveryURI) {
        _discoveryURI= discoveryURI;
    }
    
    public String getTransportURI() {
        return _transportURI;
    }
    
    public String getDiscoveryURI() {
        return _discoveryURI;
    }

    public String getNetworkURI() {
		return _networkURI;
	}

	public void setNetworkURI(String networkURI) {
		_networkURI = networkURI;
	}

    public boolean isDuplex() {
		return duplex;
	}

	public void setDuplex(boolean duplex) {
		this.duplex = duplex;
	}

	public int getNetworkTTL() {
		return networkTTL;
	}
	public void setNetworkTTL(int networkTTL) {
		this.networkTTL = networkTTL;
	}

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
