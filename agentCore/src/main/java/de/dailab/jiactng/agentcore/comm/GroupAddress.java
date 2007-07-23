package de.dailab.jiactng.agentcore.comm;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * @author loeffelholz
 * @author Marcel Patzlaff
 *
 */
final class GroupAddress extends CommunicationAddress implements IGroupAddress {
    static final String PREFIX= "group";
    
	GroupAddress(String name) throws URISyntaxException {
		super(PREFIX, name.toLowerCase());
	}
    
    GroupAddress(URI uri) {
        super(uri, PREFIX);
    }
    
    private GroupAddress(GroupAddress copy, String transportId) throws URISyntaxException {
        super(copy, transportId);
    }
    
    public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

    @SuppressWarnings("unchecked")
    public IGroupAddress toUnboundAddress() {
        return super.toUnboundAddress();
    }

    @SuppressWarnings("unchecked")
    @Override
    GroupAddress bind(String transportId) throws URISyntaxException {
        return new GroupAddress(this, transportId);
    }
}
