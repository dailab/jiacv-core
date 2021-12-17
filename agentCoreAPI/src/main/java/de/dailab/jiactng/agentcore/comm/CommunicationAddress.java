package de.dailab.jiactng.agentcore.comm;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Abstract implementation of all types of communication addresses.
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 *
 */
abstract class CommunicationAddress implements ICommunicationAddress {
	private final CommunicationAddress unboundAddress;
    private final URI uri;
    private final String transportId;
	
	protected CommunicationAddress(String scheme, String schemeSpecificPart) throws URISyntaxException {
        uri= new URI(scheme + ':' + schemeSpecificPart);
        unboundAddress= this;
        transportId= null;
	}
    
    protected CommunicationAddress(URI predefined, String expectedScheme) {
        if(!predefined.getScheme().equals(expectedScheme)) {
            throw new IllegalArgumentException("'" + predefined + "' is not applicable to this address type");
        }
        uri= predefined;
        unboundAddress= this;
        transportId= null;
    }
    
    protected CommunicationAddress(CommunicationAddress unboundAddress, String transportId) throws URISyntaxException {
        if(unboundAddress.isBoundToTransport()) {
            throw new IllegalArgumentException("communication address can only be initialised with unbound address");
        }
        this.unboundAddress= unboundAddress;
        this.transportId= transportId;
        uri= new URI(transportId + ':' + unboundAddress.toURI());
    }

	public final boolean isBoundToTransport() {
        return transportId != null;
    }
    
    public CommunicationAddress toUnboundAddress() {
        return unboundAddress;
    }

    public final String getName() {
		return unboundAddress.toURI().getSchemeSpecificPart();
	}

    public final URI toURI() {
        return uri;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if(! getClass().isInstance(obj)) {
            return false;
        }
        
        if(!(obj instanceof CommunicationAddress)) {
        	return false;
        }
        
        final CommunicationAddress other= (CommunicationAddress) obj;
        return toURI().equals(other.toURI());
    }

    @Override
    public final int hashCode() {
        return uri.hashCode();
    }

    @Override
    public final String toString() {
        return toURI().toString();
    }
    
    abstract CommunicationAddress bind(String transportId) throws URISyntaxException;
}
