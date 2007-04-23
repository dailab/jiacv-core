package de.dailab.jiactng.agentcore.comm;

import javax.jms.Destination;

/**
 * Ein Objekt, dass Nachrichten, die innerhalb Jiacs verschickt werden, kapselt. Der Recipient bestimmt, wer diese
 * Message bekommen soll. EIne Message ist typisiert, durch das operation-attribut
 * 
 * @author janko
 */
public class JiacMessage implements IJiacMessage {

	public static final String PLATFORM_ENDPOINT_EXTENSION = "TNG";

	IJiacContent _payload;
	IEndPoint _endpoint;
	IEndPoint _startpoint;
	String _operation;
	Destination _replyToDestination;

	public JiacMessage(String operation, IJiacContent payload, IEndPoint recipient) {
		setOperation(operation);
		setPayload(payload);
		setEndPoint(recipient);
	}

	/**
	 * @param operation die Operation
	 * @param payload die Daten der nachricht
	 * @param recipient der empfänger - bei nachrichten in topic nicht definiert
	 * @param startpoint die jaicinterne senderadresse, ein endpoint
	 * @param sender die Queue die gesendet hat, (an die ggf. antwort geschickt werden kann) - bei nachrichten in topic
	 *          nicht definiert
	 */
	public JiacMessage(String operation, IJiacContent payload, IEndPoint recipient, IEndPoint startpoint,
																					Destination sender) {
		this(operation, payload, recipient);
		setSender(sender);
		setStartPoint(startpoint);
	}

	public IJiacContent getPayload() {
		return _payload;
	}

	private void setPayload(IJiacContent payload) {
		_payload = payload;
	}

	public IEndPoint getEndPoint() {
		return _endpoint;
	}

	public void setEndPoint(IEndPoint recipient) {
		_endpoint = recipient;
	}

	public IEndPoint getStartPoint() {
		return _startpoint;
	}

	public void setStartPoint(IEndPoint startpoint) {
		_startpoint = startpoint;
	}

	public String toString() {
		return "[OP:" + getOperation() + ", Payload:" + getPayload().toString() + ", Endpoint:" + getEndPoint()
																						+ ", Startpoint:" + getStartPoint() + ", replyToDest:" + getSender() + "]";
	}

	public String getOperation() {
		return _operation;
	}

	private void setOperation(String operation) {
		_operation = operation;
	}

	public Destination getSender() {
		return _replyToDestination;
	}

	public void setSender(Destination destination) {
		_replyToDestination = destination;
	}

	/**
	 * Wenn es an eine Platform geht, wird der endpoint als String zurückgegeben. Wenn es an einen Agenten geht, wird die
	 * extension abgeschnitten.
	 */
	public String getJiacDestination() {
		IEndPoint endpoint = getEndPoint();
		if (endpoint != null) {
			String epStr = endpoint.toString();
			if (epStr.endsWith(PLATFORM_ENDPOINT_EXTENSION)) {
				return epStr;
			} else {
				String dest = epStr.substring(0, epStr.lastIndexOf('.'));
				return dest;
			}
		}
		return "";
	}
}
