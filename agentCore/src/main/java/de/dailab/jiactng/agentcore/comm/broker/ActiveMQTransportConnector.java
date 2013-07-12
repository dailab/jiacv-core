/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.broker;


/**
 * This class is used for the description of ActiveMQ transport connectors.
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ActiveMQTransportConnector implements ActiveMQTransportConnectorMBean {
    private String transportURI= null;
    private String networkURI = null;
	private String discoveryURI= null;
	private String name = "NC";
	private boolean duplex = true;
	private int networkTTL = 20;

    /**
	 * {@inheritDoc}
     */
    public void setTransportURI(String newTransportURI) {
        transportURI= newTransportURI;
    }

    /**
	 * {@inheritDoc}
     */
    public void setDiscoveryURI(String newDiscoveryURI) {
        discoveryURI= newDiscoveryURI;
    }

    /**
	 * {@inheritDoc}
     */
    public String getTransportURI() {
        return transportURI;
    }

    /**
	 * {@inheritDoc}
     */
    public String getDiscoveryURI() {
        return discoveryURI;
    }

    /**
	 * {@inheritDoc}
     */
    public String getNetworkURI() {
		return networkURI;
	}

    /**
	 * {@inheritDoc}
     */
	public void setNetworkURI(String newNetworkURI) {
		networkURI = newNetworkURI;
	}

	/**
	 * {@inheritDoc}
	 */
    public boolean isDuplex() {
		return duplex;
	}

    /**
	 * {@inheritDoc}
     */
	public void setDuplex(boolean newDuplex) {
		duplex = newDuplex;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getNetworkTTL() {
		return networkTTL;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNetworkTTL(int newNetworkTTL) {
		networkTTL = newNetworkTTL;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setName(String name) {
		this.name = name;
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
