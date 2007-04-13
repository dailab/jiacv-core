package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Message;

import de.dailab.jiactng.agentcore.comm.IJiacSender;

public interface IProtocolHandler {
	/**
	 * setzt den Sender. 
	 * @param sender Erstmal vom Typ Object. Das Protocol muss dann selbst casten.
	 */
	public void setSender(IJiacSender sender);
	
	public int processMessage(Message msg);
}
