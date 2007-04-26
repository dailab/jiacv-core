package de.dailab.jiactng.agentcore.comm.protocol;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * Hilfsklasse zum response-message zusammenbasteln im Protocol.
 * @author janko
 */
class SendInfo {
	IJiacMessage _msg;
	String _destinationString;

	public SendInfo() {}

	public SendInfo(IJiacMessage msg, String destinationString) {
		setDestinationString(destinationString);
		setMsg(msg);
	}

	public String getDestinationString() {
		return _destinationString;
	}

	public void setDestinationString(String destinationString) {
		_destinationString = destinationString;
	}

	public IJiacMessage getMsg() {
		return _msg;
	}

	public void setMsg(IJiacMessage msg) {
		_msg = msg;
	}

}