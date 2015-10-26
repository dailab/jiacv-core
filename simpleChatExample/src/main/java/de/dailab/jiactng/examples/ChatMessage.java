package de.dailab.jiactng.examples;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * A {@link ChatMessage} is a simple class that holds information about chat
 * messages.
 *
 * @author mib
 *
 */
public class ChatMessage implements IFact {

	private static final long serialVersionUID = -245679977354931415L;
	/*
	 * a chat message for a 'global' chat contains three important information:
	 * sender's name, sender's timestamp and the message. Therefor this IFact
	 * contains these three fields.
	 *
	 * There is no need to update the fields, therefor the fields are final. To
	 * save lines of code, we set the fields public and avoid methods to access
	 * the field's content.
	 */
	/**
	 * The senders display name.
	 */
	public final String sendername;
	/**
	 * The message to display.
	 */
	public final String message;
	/**
	 * The creation date of the message at sender's host.
	 */
	public final Long sendtimestamp;

	/**
	 * Empty constructor used for template creation. This constructor uses the
	 * parameterized constructor with <code>null</code>.
	 */
	public ChatMessage() {
		/*
		 * To avoid uninitialized field, this constructor calls the parameterized
		 * constructor with null values.
		 */
		this(null, null, null);
	}

	/**
	 * The full parameterized constructor for constructive chat messages.
	 *
	 * @param name
	 *          the sender's name
	 * @param msg
	 *          the sender's message
	 * @param time
	 *          the senders's timestamp
	 */
	public ChatMessage(final String name, final String msg, final Long time) {
		this.sendername = name;
		this.message = msg;
		this.sendtimestamp = time;
	}

	/**
	 * This is a method to print chat messages in console for debugging use. It is
	 * slightly expensive, so don't use this just in debugging cases.
	 *
	 * @return a formatted print of a chat message
	 */
	@Override
	public String toString() {
		/*
		 * maybe a little bit complex, this toString method is intended to be used
		 * in debugging cases.
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("ChatMessage[sender=");
		sb.append(this.sendername);
		sb.append(",time=");
		if (this.sendtimestamp == null) {
			sb.append("null");
		}
		else {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm");
			sb.append(sdf.format(new Date(this.sendtimestamp)));
		}
		sb.append(": ").append(this.message);
		return sb.append(']').toString();
	}
}
