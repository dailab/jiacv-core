package de.dailab.jiactng.agentcore.comm.message;

import javax.jms.Destination;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Eine InterAgent-Message in Jiac. Die Nutzdaten sind vom Typ IJiacContent, die in verschiednen implementierungen
 * verschiedenen inhalte bieten sollen.
 * Momentan existiert FileContent und ObjectContent.
 * 
 * @author janko
 * 
 * 
 * FIXME: refactoring request:
 *      1. remove all JMS stuff from here!
 *      2. destinations have to be set in the action. We want to support series message and subsequent destination
 *         changes on this message will also effect our previously issued inform actions!
 */
public interface IJiacMessage extends IFact {

	/**
	 * Liefert die Nutzdaten
	 * 
	 * @return
	 */
	public IJiacContent getPayload();

	/**
	 * Liefert den Endpunkt der Nachricht. Dieser wird in der JMS-Nachricht als Property gesetzt, damit danach im Consumer
	 * selektiert werden kann Der Endpoint ist der Empf�nger der Nachricht.
	 * 
	 * @return
	 */
	public IEndPoint getEndPoint();

	/**
	 * Der Typ der Nachricht, Befehl/Ergebnis/Fehlerhinweis
	 * 
	 * @return
	 */
	public String getOperation();

	/**
	 * Der Sender ist die JMS-Queue oder Topic
	 * 
	 * @return
	 */
	public Destination getSender();

	/**
	 * Der Sender ist die JMS-Queue oder Topic
	 * 
	 * @return
	 */
	public void setSender(Destination destination);

	/**
	 * Der Absender der Nachricht
	 * 
	 * @return
	 */
	public IEndPoint getStartPoint();

	/**
	 * Ermittelt die jiacAdressierung aus vorhandenen daten. Unterscheidet zwischen To-Agent und To-Platform Adresse.
	 * Dieser Wert wird dann als Property an der JMS-Message gesetzt, so dass danach selektiert werden kann.
	 * 
	 * @return
	 */
	public String getJiacDestination();
}
