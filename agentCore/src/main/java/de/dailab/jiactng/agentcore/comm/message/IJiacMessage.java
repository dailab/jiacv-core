package de.dailab.jiactng.agentcore.comm.message;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * This interface defines the message type used for inter-agent-communication.
 * 
 * It provides meta-information about the message content, a payload section
 * and a sender field.
 * 
 * This interface is <strong>not</strong> intended to be subclassed by clients!
 * 
 * @author Janko Dimitroff
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 */
public interface IJiacMessage extends IFact {

	/**
	 * Returns the payload of this message. There are several different payload
     * types available.
	 * 
	 * @return
	 */
	public IJiacContent getPayload();

	/**
	 * Who did sent me this Message?
	 * 
	 * @return ICommunicationAddress	the address from where the Message is sent.
	 */
	public ICommunicationAddress getSender();
}
