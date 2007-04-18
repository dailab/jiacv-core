package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import de.dailab.jiactng.agentcore.comm.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.IJiacSender;
import de.dailab.jiactng.agentcore.comm.JiacMessage;
import de.dailab.jiactng.agentcore.comm.ObjectContent;

/**
 * Idee: Ein eigenens Protocol für die Agenten. Zusätzlich zu einem Protocol für
 * die TNG-Platform. Hier werden also die an Agenten adressierten Messages
 * ausgewertet und regiert..
 * 
 * Noch zu machen: Protokoll ausdenken... Worüber müssen sich agenten
 * unterhalten? Was gibts für anfragen/antworten ?
 * 
 * @author janko
 */
public class AgentProtocol implements IProtocolHandler {

	public static final int PROCESSING_FAILED = -1;
	public static final int PROCESSING_SUCCESS = 0;

	public static final long DEFAULT_TTL = 10000L;

	// Diese Befehle versteht das Protokoll
	public static final String CMD_AGT_PING = "AGT_PING";
	public static final String CMD_AGT_PONG = "AGT_PONG";
	public static final String CMD_AGT_GET_SERVICES = "AGT_GET_SERVICES";
	public static final String CMD_AGT_NOP = "AGT_NOP";

	// Dies sind die positiven Antworten auf die Kommandos
	public static final String ACK_AGT_PING = "ACK_AGT_PING";
	public static final String ACK_AGT_PONG = "ACK_AGT_PONG";
	public static final String ACK_AGT_GET_SERVICES = "ACK_AGT_SERVICES";
	public static final String ACK_AGT_NOP = "ACK_AGT_NOP";

	// Dies sind die negativen Antworten auf die Kommandos

	IJiacSender _sender;

	/**
	 * 
	 * @param sender der Sender MIT dem eine Antwort verschickt wird
	 */
	public AgentProtocol(IJiacSender sender) {
		setSender(sender);
	}

	public void setSender(IJiacSender sender) {
		_sender = sender;
	}

	public int processMessage(Message msg) {
		System.out.println("Ist das geil... ein Agent hat ne nachricht gekriegt...");
		IJiacMessage jiacMsg = null;
		Destination destination = null;
		if (msg != null && msg instanceof ObjectMessage) {
			ObjectMessage oMsg = (ObjectMessage) msg;
			try {
				jiacMsg = (IJiacMessage) oMsg.getObject();
				System.out.println("und zwar folgende Nachricht:" + jiacMsg.toString());
				destination = msg.getJMSReplyTo();
				System.out.println("processing Agent message, answer will go to:" + destination);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return processJiacMessage(jiacMsg, destination);
	}

	/**
	 * Diese Methode leitet entsprechend des MessageTyps an andere Methoden weiter
	 * und schickt die Antwort
	 * 
	 * @param msg
	 * @param destination
	 * @return
	 */
	private int processJiacMessage(IJiacMessage msg, Destination destination) {
		if (msg != null) {
			String operation = msg.getOperation();
			if (operation != null) {
				String type = operation.substring(0, 3);
				IJiacMessage replyMessage = null;
				if ("ACK".equals(type)) {
					replyMessage = doAcknowledge(msg);
				} else if ("ERR".equals(type)) {
					replyMessage = doError(msg);
				} else {
					// kein special Prefix, dann ist es ein Kommando
					replyMessage = doCommand(msg);
				}
				// return doReply(replyMessage, destination, msg.getSender());
				Destination sender = replyMessage == null ? null : replyMessage.getSender();
				return doReply(replyMessage, destination, sender);
			}
		}
		return PROCESSING_FAILED;
	}

	/**
	 * Reaktion auf AcknowledgeMessages
	 * 
	 * @param receivedMsg die empfangene JiacMessage
	 * @return
	 */
	private IJiacMessage doAcknowledge(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		if (ACK_AGT_GET_SERVICES.equals(operation)) {
			System.out.println("Alles Roger.. hab die Services gekriegt, von" + receivedMsg.getStartPoint());
		} else if (ACK_AGT_PING.equals(operation)) {
			System.out.println("Alles Roger.. hab ein Ping-Ack gekriegt, von " + receivedMsg.getStartPoint());
			ObjectContent content = new ObjectContent();
			content.setObject(receivedMsg.getPayload() + "PongPong");
			replyMsg = new JiacMessage(CMD_AGT_NOP, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(),
					receivedMsg.getSender());
		} else if (ACK_AGT_PONG.equals(operation)) {
			System.out.println("Alles Roger.. hab ein Pong-Ack gekriegt, von " + receivedMsg.getStartPoint());
		} else if (ACK_AGT_NOP.equals(operation)) {
			System.out.println("Ich werd' verrückt.. hab ein NOP-Ack gekriegt, von " + receivedMsg.getStartPoint());
		}
		return replyMsg;
	}

	/**
	 * Reaktion auf Fehler-messages
	 * 
	 * @param msg
	 * @return
	 */
	private IJiacMessage doError(IJiacMessage msg) {
		String operation = msg.getOperation();
		// if (ERR_GET_AGENTS.equals(operation)) {
		//
		// } else if (ERR_GET_SERVICES.equals(operation)) {
		//
		// } else if (ERR_NOP.equals(operation)) {
		//
		// } else if (ERR_NOP.equals(operation)) {
		//
		// }
		return null;
	}

	/**
	 * Hier sind die interessanten, service-bringenden Funktionen versteckt
	 * 
	 * @param receivedMsg
	 * @return
	 */
	private IJiacMessage doCommand(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		// if (CMD_GET_AGENTS.equals(operation)) {
		// List<AgentStub> agents = _platform.getAgents();
		// System.out.println("+++++ schicke ab an "+receivedMsg.getStartPoint());
		// PlatformHelper.debugPrintAgents(agents);
		// replyMsg = new JiacMessage(ACK_GET_AGENTS, agents,
		// receivedMsg.getStartPoint(), receivedMsg.getEndPoint(),
		// receivedMsg.getSender());
		if (CMD_AGT_GET_SERVICES.equals(operation)) {
			String[] services = { "A()", "B()", "C()" };
		} else if (CMD_AGT_PING.equals(operation)) {
			ObjectContent content = new ObjectContent();
			content.setObject(receivedMsg.getPayload() + "Pong");
			// es wird die platformadresse als absender gesetzt
			replyMsg = new JiacMessage(ACK_AGT_PING, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(), null);
			// receivedMsg.getSender()); // hier falscher Sender/replyToAdress(?)
		} else if (CMD_AGT_NOP.equals(operation)) {
			String content = "psst";
		}
		return replyMsg;
	}

	/**
	 * Verschickt die Antwort
	 * 
	 * @param msg die AntwortNachricht
	 * @param destination die ZielDestination
	 * @param senderAddress die ReplyTo-Destination der JMSMessage
	 * @return PROCESSING_SUCCESS, wenn versendet; PROCESSING_FAILED wenn
	 *         Versenden nicht möglich war.
	 */
	private int doReply(IJiacMessage msg, Destination destination, Destination senderAddress) {
		System.out.println("Agent schickt antwort...");
		if (msg != null && destination != null && _sender != null) {
			_sender.send(msg, destination);//, senderAddress, DEFAULT_TTL);
			return PROCESSING_SUCCESS;
		}
		return PROCESSING_FAILED;
	}
}
