package de.dailab.jiactng.agentcore.comm;

/**
 * 
 * @author loeffelholz
 *
 */
public class GroupAddress implements IGroupAddress {

	String _address;
	
	public GroupAddress(String address) {
		_address = address;
	}

	public boolean isClosed() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getAddress() {
		return _address;
	}

}
