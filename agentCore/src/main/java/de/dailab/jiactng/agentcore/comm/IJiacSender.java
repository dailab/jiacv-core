package de.dailab.jiactng.agentcore.comm;

import javax.jms.Destination;

public interface IJiacSender {
	/**
	 * Sendet in default Destination
	 * @param message
	 */
	public void send(IJiacMessage message);
	
	/**
	 * Sendet in angegebene Destination
	 * @param message
	 * @param destinationName
	 */
	public void send(IJiacMessage message, String destinationName);
	
	
	public void send(IJiacMessage message, Destination destination);
	
}
