package de.dailab.jiactng.agentcore.comm;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 *
 */
abstract class CommunicationAddress implements ICommunicationAddress {
    private final CommunicationAddress _unboundAddress;
    private final URI _uri;
    private final String _transportId;
	
	protected CommunicationAddress(String scheme, String schemeSpecificPart) throws URISyntaxException {
        _uri= new URI(scheme + ':' + schemeSpecificPart);
        _unboundAddress= this;
        _transportId= null;
	}
    
    protected CommunicationAddress(URI predefined, String expectedScheme) {
        if(!predefined.getScheme().equals(expectedScheme)) {
            throw new IllegalArgumentException("'" + predefined + "' is not applicable to this address type");
        }
        _uri= predefined;
        _unboundAddress= this;
        _transportId= null;
    }
    
    protected CommunicationAddress(CommunicationAddress unboundAddress, String transportId) throws URISyntaxException {
        if(unboundAddress.isBoundToTransport()) {
            throw new IllegalArgumentException("communication address can only be initialised with unbound address");
        }
        _unboundAddress= unboundAddress;
        _transportId= transportId;
        _uri= new URI(_transportId + ':' + unboundAddress.toURI());
    }

	public final boolean exists() {
		return false;
	}
    
	public final boolean isBoundToTransport() {
        return _transportId != null;
    }
    
    @SuppressWarnings("unchecked")
    public <T extends ICommunicationAddress> T toUnboundAddress() {
        return (T)_unboundAddress;
    }

    public final String getName() {
		return _unboundAddress.toURI().getSchemeSpecificPart();
	}

    public final URI toURI() {
        return _uri;
    }
    
    @Override
    public final boolean equals(Object obj) {
        if(obj == null || !getClass().isInstance(obj)) {
            return false;
        }
        
        if(!(obj instanceof CommunicationAddress)) {
        	return false;
        }
        
        CommunicationAddress other= (CommunicationAddress) obj;
        return toURI().equals(other.toURI());
    }

    @Override
    public final int hashCode() {
        return _uri.hashCode();
    }

    @Override
    public final String toString() {
        return toURI().toString();
    }
    
    abstract <T extends CommunicationAddress> T bind(String transportId) throws URISyntaxException;
}
