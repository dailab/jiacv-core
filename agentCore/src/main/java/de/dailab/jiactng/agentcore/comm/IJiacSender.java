package de.dailab.jiactng.agentcore.comm;

import javax.jms.Destination;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * Ein allgemeiner Jms Sender für Jiac
 * @author janko
 *
 */
public interface IJiacSender {
	/**
	 * Sendet in default Destination
	 * @param message
	 */
	public void send(IJiacMessage message);
	
	/**
	 * Sendet in Destination mit angegebenen Namen
	 * @param message die jiac-message
	 * @param destinationName der Name der Destination, daraus wird das DestinationObjekt erzeugt (widerspricht zwar der JMS spezifikation, aber was macht man nich alle saus bequemlichkeit)
	 */
	public void send(IJiacMessage message, String destinationName);
	
	/**
	 * Sendet in angegebenen Destination
	 * @param message die jiac-message
	 * @param destination die Destination (d.h. Topic oder Queue)
	 */
	public void send(IJiacMessage message, Destination destination);
	
	
	/**
	 * liefert die eigene destination, die an die geantwortet werden soll/kann.
	 * Evtl. unsinnig bei TopicSendern
	 * @return
	 */
	public Destination getReplyToDestination();
	
}
