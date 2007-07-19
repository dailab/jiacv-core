package de.dailab.jiactng.agentcore.comm;

/**
 * 
 * @author loeffelholz
 *
 */
public class MessageBoxAddress implements IMessageBoxAddress {

	String _address;
	
	public MessageBoxAddress(String address) {
		_address = address;
	}

	public boolean isLocal() {
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
