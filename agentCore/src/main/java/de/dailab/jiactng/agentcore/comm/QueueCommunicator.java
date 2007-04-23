package de.dailab.jiactng.agentcore.comm;

import javax.jms.Destination;
import javax.jms.MessageListener;

import de.dailab.jiactng.agentcore.comm.protocol.IProtocolHandler;

/**
 * Klasse soll Send/Receive bzw. Request/Response ermöglichen. Dazu wird ein Sender und ein Receiver verwendet - die die
 * jeweilige Funkionalität kapseln. Die Kommunikation läuft über eine JMS-Queue.
 * 
 * @author janko
 */
public class QueueCommunicator {
	static int counter = 0;
	IProtocolHandler _protocol;
	QueueReceiver _receiver;
	QueueSender _sender;

	/**
	 * Sendet eine Nachricht
	 * 
	 * @param message
	 * @param destination die Destination
	 * @param replyToDestination
	 * @param timeToLive
	 */
	public void send(IJiacMessage message, Destination destination, Destination replyToDestination, long timeToLive) {
		_sender.sendMessage(message, destination, replyToDestination, timeToLive);
	}

	/**
	 * Sendet eine Nachricht
	 * 
	 * @param message
	 */
	public void send(IJiacMessage message) {
		_sender.send(message);
	}
	
	/**
	 * Sendet eine Nachricht
	 * 
	 * @param message
	 */
	public void send(IJiacMessage message, String destinationName) {
		_sender.send(message, destinationName);
	}
	
	/**
	 * Initialisiert einen Consumer. Wie am besten den Consumer wieder loswerden? und wann? Man könnte sie in einer Liste
	 * sammeln und explizit löschen.. aber dann müsste der Entwickler immer eine Referenz halten
	 * 
	 * @param selector
	 * @param listener
	 */
	public void doReceive(String selector, QueueMessageListener listener) {
		_receiver.receive(selector, listener);
	}

	/**
	 * Initialisiert einen Consumer. Wie am besten den Consumer wieder loswerden? und wann? Man könnte sie in einer Liste
	 * sammeln und explizit löschen.. aber dann müsste der Entwickler immer eine Referenz halten
	 * 
	 * @param selector
	 * @param listener
	 * @return die temporäre Destination
	 */
	public Destination doReceiveFromTemporaryQueue(String selector, MessageListener listener) {
		return _receiver.receiveFromTemporaryQueue(selector, listener);
	}

	public QueueReceiver getReceiver() {
		return _receiver;
	}

	public QueueSender getSender() {
		return _sender;
	}

	public void setReceiver(QueueReceiver receiver) {
		_receiver = receiver;
	}

	public void setSender(QueueSender sender) {
		_sender = sender;
	}
		
	
	public IProtocolHandler getProtocol() {
		return _protocol;
	}

	public void setProtocol(IProtocolHandler protocol) {
		_protocol = protocol;
	}

	
}
