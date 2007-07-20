package de.dailab.jiactng.agentcore.comm;

/**
 * 
 * @author loeffelholz
 *
 */
public abstract class CommunicationAddress implements ICommunicationAddress {
    private final String _address;
	
	protected CommunicationAddress(String address) {
		_address= address;
	}

	public final boolean exists() {
		return false;
	}

	public final String getAddress() {
		return _address;
	}

    @Override
    public abstract String toString();
}
