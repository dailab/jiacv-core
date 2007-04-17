package de.dailab.jiactng.agentcore.comm;

/**
 * Ein Endpunkt einer Kommunikation - stellt also eine Adresse auf dem JIAC-JMS-Bus dar. Ein Endpoint kann das Ende UND
 * der Anfang einer Kommunikation ein.
 * 
 * @author janko
 */
public class EndPoint implements IEndPoint {

	private String _universalId;
	private String _localId;

	public static final char SEPARATOR = '.';

	/**
	 * 
	 * @param uid universal id, should be unique 
	 * @param id
	 */
	public EndPoint(String uid, String id) {
		setLocalId(id);
		setUniversalId(uid);
	}

	/**
	 * Dröselt einen Endpoint in Stringdarstellung, d.h. <universalId>.<localId> in die komponenten auf und erzeugt einen
	 * endpoint daraus.
	 * 
	 * @param endpointAsString
	 */
	public EndPoint(String endpointAsString) {
		int location = endpointAsString.indexOf(SEPARATOR);
		if (location > -1) {
			setUniversalId(endpointAsString.substring(0, location));
			setLocalId(endpointAsString.substring(location+1, endpointAsString.length()));
		} else {
			setUniversalId(endpointAsString);
		}
	}

	public String getUniversalId() {
		return _universalId;
	}

	public String getLocalId() {
		return _localId;
	}

	public void setLocalId(String localId) {
		_localId = localId;
	}

	public void setUniversalId(String universalId) {
		_universalId = universalId;
	}

	public String toString() {
		return getUniversalId() + SEPARATOR + getLocalId();
	}

	public boolean equals(Object o) {
		EndPoint recipient = (EndPoint) o;
		if (recipient != null && _universalId.equals(recipient.getUniversalId()) && _localId.equals(recipient.getLocalId()))
			return true;
		else
			return false;
	}
}
