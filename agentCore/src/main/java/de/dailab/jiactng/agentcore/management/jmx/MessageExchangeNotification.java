package de.dailab.jiactng.agentcore.management.jmx;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

import javax.management.Notification;

/**
 * This class represents a JMX-compliant notification about an exchanged
 * message between agents.
 * @author Jan Keiser
 */
public class MessageExchangeNotification extends Notification {

	private static final long serialVersionUID = 1L;

	/** Notification type which indicates that a message was exchanged. */
	public static String MESSAGE_EXCHANGE = "jiactng.message.exchange";

	/** Indicates if the message was sent or received. */
	private MessageExchangeAction _action;

	/** The address of the message receiver. */
	private ICommunicationAddress _receiver;

	/** The exchanged message. */
	private IJiacMessage _message;

	/** The used message transport mechanism. */
	private String _transport;

	
	/**
	 * This enumeration defines the two actions of message exchange.
	 * @author Jan Keiser
	 */
	public enum MessageExchangeAction {

        /** Sending a message to another agent. */
        SEND,

        /** Receiving a message from another agent. */
        RECEIVE,		
	};

	
	/**
	 * Constructs a notification about an exchanged message between agents.
	 * @param source The notification producer, that is the communication which has sent or received a message.
	 * @param sequenceNumber The notification sequence number within the source object.
	 * @param timeStamp The date at which the notification is being sent.
	 * @param msg A String containing the message of the notification.
	 * @param action Indicates if the message was sent or received.
	 * @param receiver The address of the message receiver.
	 * @param jiacMessage The exchanged message.
	 * @param transport The used message transport mechanism.
	 */
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

	/**
	 * Gets the action that is related to the message exchange.
	 * @return Whether the message was sent or received.
	 */
	public MessageExchangeAction getAction() {
		return _action;
	}

	/**
	 * Gets the address of the message receiver.
	 * @return The address of the message receiver.
	 */
	public ICommunicationAddress getReceiver() {
		return _receiver;
	}

	/**
	 * Gets the exchanged message.
	 * @return The exchanged message.
	 */
	public IJiacMessage getJiacMessage() {
		return _message;
	}

	/**
	 * Gets the used message transport mechanism.
	 * @return The used message transport mechanism.
	 */
	public String getTransport() {
		return _transport;
	}
}
