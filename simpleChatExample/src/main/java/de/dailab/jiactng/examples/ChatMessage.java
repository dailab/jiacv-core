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
	public final String sendername;
	public final String message;
	public final Long sendtimestamp;

	public ChatMessage() {
		/*
		 * To avoid uninitialized field, this constructor calls the paramized
		 * constructor with null values.
		 */
		this(null, null, null);
	}

	public ChatMessage(final String name, final String msg, final Long time) {
		this.sendername = name;
		this.message = msg;
		this.sendtimestamp = time;
	}

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
		String ret = sb.append(']').toString();
		return ret;
	}
}
