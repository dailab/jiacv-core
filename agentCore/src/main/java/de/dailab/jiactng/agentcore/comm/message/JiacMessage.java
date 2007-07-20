package de.dailab.jiactng.agentcore.comm.message;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;


/**
 * Ein Objekt, dass Nachrichten, die innerhalb Jiacs verschickt werden, kapselt. Der Recipient bestimmt, wer diese
 * Message bekommen soll. EIne Message ist typisiert, durch das operation-attribut
 * 
 * @author janko, loeffelholz
 */
public class JiacMessage implements IJiacMessage {

	public static final String PLATFORM_ENDPOINT_EXTENSION = "TNG";

	IJiacContent _payload;
	ICommunicationAddress _address;
	
	public JiacMessage(IJiacContent payload, ICommunicationAddress address) {
		_payload = payload;
		_address = address;
	}

	public IJiacContent getPayload() {
		return _payload;
	}

	
	public String toString() {
		return ("[Payload: " + getPayload().toString() 
				+ ", Address: " + getSender().toString());
	}
	
	public ICommunicationAddress getSender(){
		return _address;
	}
	
	}
