package de.dailab.jiactng.agentcore.comm;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 
 * @author loeffelholz
 *
 */
class MessageBoxAddress extends CommunicationAddress implements IMessageBoxAddress {
    static final String PREFIX= "msgbox";
	
    MessageBoxAddress(String name) throws URISyntaxException {
		super(PREFIX, name.toLowerCase());
	}
    
    MessageBoxAddress(URI predefined) {
        super(predefined, PREFIX);
    }
    
    private MessageBoxAddress(MessageBoxAddress unboundAddress, String transportIdentifier) throws URISyntaxException {
        super(unboundAddress, transportIdentifier);
    }
    
	@SuppressWarnings("unchecked")
    public IMessageBoxAddress toUnboundAddress() {
        return super.toUnboundAddress();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    MessageBoxAddress bind(String transportId) throws URISyntaxException {
        return new MessageBoxAddress(this, transportId);
    }

    public boolean isLocal() {
		return false;
	}
}
