package de.dailab.jiactng.agentcore.comm;

/**
 * 
 * @author loeffelholz
 *
 */
class MessageBoxAddress extends CommunicationAddress implements IMessageBoxAddress {
    static final String PREFIX= "msgbox";
	public MessageBoxAddress(String address) {
		super(address);
	}

	public boolean isLocal() {
		return false;
	}

    public String getScheme() {
        return PREFIX + "/" + getAddress();
    }
}
