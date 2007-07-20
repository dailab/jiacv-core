package de.dailab.jiactng.agentcore.comm;

/**
 * 
 * @author loeffelholz
 *
 */
public class MessageBoxAddress extends CommunicationAddress implements IMessageBoxAddress {
	public MessageBoxAddress(String address) {
		super(address);
	}

	public boolean isLocal() {
		return false;
	}

    @Override
    public String toString() {
        return "://msgbox:" + getAddress();
    }
}
