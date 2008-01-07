package de.dailab.jiactng.agentcore.management.jmx;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

import javax.management.Notification;

public class MessageExchangeNotification extends Notification {

	private static final long serialVersionUID = 1L;

	public static String MESSAGE_EXCHANGE = "jiactng.message.exchange";
	
	private MessageExchangeAction _action;
	private ICommunicationAddress _receiver;
	private IJiacMessage _message;
	private String _transport;

	public enum MessageExchangeAction {

        /** sending a message to another agent */
        SEND,

        /** receiving a message from another agent */
        RECEIVE,		
	};
	
	public MessageExchangeNotification(Object source, long sequenceNumber,
			long timeStamp, String msg, MessageExchangeAction action,
			ICommunicationAddress receiver, 
			IJiacMessage jiacMessage, String transport) {
		super(MESSAGE_EXCHANGE, source, sequenceNumber, timeStamp, msg);
		_action = action;
		_receiver = receiver;
		_message = jiacMessage;
		_transport = transport;
	}

	public MessageExchangeAction getAction() {
		return _action;
	}
	
	public ICommunicationAddress getReceiver() {
		return _receiver;
	}
	
	public IJiacMessage getJiacMessage() {
		return _message;
	}
	
	public String getTransport() {
		return _transport;
	}
}
