package de.dailab.jiactng.agentcore.comm;

/**
 * 
 * @author loeffelholz
 *
 */
class GroupAddress extends CommunicationAddress implements IGroupAddress {
    static final String PREFIX= "group";
    
	public GroupAddress(String address) {
		super(address);
	}

	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

    public String getScheme() {
        return PREFIX + "/" + getAddress();
    }
}
