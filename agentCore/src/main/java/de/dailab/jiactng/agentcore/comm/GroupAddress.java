package de.dailab.jiactng.agentcore.comm;

/**
 * 
 * @author loeffelholz
 *
 */
public class GroupAddress extends CommunicationAddress implements IGroupAddress {
	public GroupAddress(String address) {
		super(address);
	}

	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public String toString() {
        return "://group:" + getAddress();
    }
}
