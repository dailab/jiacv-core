package de.dailab.jiactng.agentcore.comm.message;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Eine InterAgent-Message in Jiac. Die Nutzdaten sind vom Typ IJiacContent, die in verschiednen implementierungen
 * verschiedenen inhalte bieten sollen.
 * Momentan existiert FileContent und ObjectContent.
 * 
 * @author janko, l�ffelholz
 */
public interface IJiacMessage extends IFact {

	/**
	 * Liefert die Nutzdaten
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
