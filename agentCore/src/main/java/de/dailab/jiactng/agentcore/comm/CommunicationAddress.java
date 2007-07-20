package de.dailab.jiactng.agentcore.comm;

/**
 * 
 * @author loeffelholz
 *
 */
abstract class CommunicationAddress implements ICommunicationAddress {
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
    public final String toString() {
        return getScheme();
    }
}
