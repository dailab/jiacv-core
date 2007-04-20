package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Message;

import de.dailab.jiactng.agentcore.comm.IJiacSender;

public interface IProtocolHandler {	
	public static final String BASIC_PROTOCOL = "basic";
	public static final String PLATFORM_PROTOCOL = "platform";	
	public static final String AGENT_PROTOCOL = "agent";
	
	/**
	 * setzt die Sender. 
	 * @param topicSender
	 * @param queueSender
	 */
	public void setSender(IJiacSender topicSender, IJiacSender queueSender);
	
	public int processMessage(Message msg);
}
