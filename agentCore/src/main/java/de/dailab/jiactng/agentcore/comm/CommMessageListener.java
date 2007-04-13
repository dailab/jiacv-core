package de.dailab.jiactng.agentcore.comm;

import javax.jms.Message;

/**
 * Listener, der auf empfangene reagieren Nachrichten etc. kann.
 * Jeder der Nachrichten haben will, muss dieses Interface implementieren, und sich bei der CommBean anmelden.
 * Diese leitet empfangenen Nachrichten an registrierte CommMessageListener weiter.
 * @author janko
 *
 */
public interface CommMessageListener {
	public void messageReceivedFromQueue(Message message);
	public void messageReceivedFromTopic(Message message);	
}
