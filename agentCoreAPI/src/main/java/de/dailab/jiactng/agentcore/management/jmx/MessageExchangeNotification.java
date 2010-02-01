package de.dailab.jiactng.agentcore.management.jmx;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
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
	public static final String MESSAGE_EXCHANGE = "jiactng.message.exchange";

	/** Indicates if the message was sent or received. */
	private MessageExchangeAction action;

	/** The address of the message sender. */
	private String sender;

	/** The address of the message receiver. */
	private String receiver;

	/** The description of the exchanged message. */
	private Object message;

	/** The used message transport mechanism. */
	private String transport;

	/** Indicates if the message was sent to a group address. */
	private boolean groupMessage;

	
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
		this.action = action;
		this.sender = (jiacMessage.getSender()==null)? null:jiacMessage.getSender().getName();
		this.receiver = (receiver==null)? null:receiver.getName();
		try {
			this.message = ((JmxDescriptionSupport)jiacMessage).getDescription();
		} catch (Exception e) {
			this.message = jiacMessage.toString();
		}
		this.transport = transport;
		this.groupMessage = receiver instanceof IGroupAddress;
	}

	/**
	 * Gets the action that is related to the message exchange.
	 * @return Whether the message was sent or received.
	 */
	public final MessageExchangeAction getAction() {
		return action;
	}

	/**
	 * Gets the address of the message sender.
	 * @return The address of the message sender.
	 */
	public final String getSender() {
		return sender;
	}

	/**
	 * Gets the address of the message receiver.
	 * @return The address of the message receiver.
	 */
	public final String getReceiver() {
		return receiver;
	}

	/**
	 * Gets the description of the exchanged message.
	 * @return The description of the exchanged message. The representation is either a string 
	 * or based on JMX open types.
	 * @see Object#toString()
	 * @see JmxDescriptionSupport#getDescription()
	 */
	public final Object getJiacMessage() {
		return message;
	}

	/**
	 * Gets the used message transport mechanism.
	 * @return The used message transport mechanism.
	 */
	public final String getTransport() {
		return transport;
	}

	/**
	 * Checks if the message was sent to a group address.
	 * @return <code>true</code> if the message was sent to a group address.
	 */
	public final boolean isGroupMessage() {
		return groupMessage;
	}
}
