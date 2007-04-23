package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Message;

import de.dailab.jiactng.agentcore.comm.IJiacSender;

/**
 * Allgemeines Protocol-Interface 
 * @author janko
 *
 */
public interface IProtocolHandler {
	
	/** konstante für das BasicJiacProtocol */
	public static final String BASIC_PROTOCOL = "basic";
	/** konstante für das NodeProtocol */
	public static final String PLATFORM_PROTOCOL = "platform";
	/** konstante für das AgentProtocol */
	public static final String AGENT_PROTOCOL = "agent";
	
	/**
	 * Setzt die Sender. 
	 * @param topicSender
	 * @param queueSender
	 */
	public void setSender(IJiacSender topicSender, IJiacSender queueSender);
	
	/**
	 * bearbeitet die Nachricht.
	 * @param msg
	 * @return
	 */
	public int processMessage(Message msg);
}
