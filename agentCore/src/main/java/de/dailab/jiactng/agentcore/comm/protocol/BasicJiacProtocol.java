package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import de.dailab.jiactng.agentcore.comm.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.IJiacSender;
import de.dailab.jiactng.agentcore.comm.JiacMessage;

/**
 * Ein Protokoll für die Kommunikation. Die processMessage-Methode wird von einem MessageListener aufgerufen. Die
 * Message wird hier behandelt und mit dem im Konstruktor übergebenen Sender wird eine Antwort gesendet. Der Empfänger
 * der Antwort wird aus der Message gelesen.
 * 
 * @author janko
 */
public class BasicJiacProtocol implements IProtocolHandler {
	// static Logger log4j = Logger.getLogger("de.dailab.jiactng.protocol.BasicJiacProtocol");

	// Diese Befehle versteht das Protokoll
	public static final String CMD_PING = "PING";
	public static final String CMD_GET_AGENTS = "GET_AGENTS";
	public static final String CMD_GET_SERVICES = "GET_SERVICES";
	public static final String CMD_NOP = "NOP";

	// Dies sind die positiven Antworten auf die Kommandos
	public static final String ACK_PING = "ACK_PING";
	public static final String ACK_GET_AGENTS = "ACK_AGENTS";
	public static final String ACK_GET_SERVICES = "ACK_SERVICES";
	public static final String ACK_NOP = "ACK_NOP";

	// Dies sind die negativen Antworten auf die Kommandos
	public static final String ERR_PING = "ERR_PING";
	public static final String ERR_GET_AGENTS = "ERR_AGENTS";
	public static final String ERR_GET_SERVICES = "ERR_SERVICES";
	public static final String ERR_NOP = "ERR_NOP";

	public static final int PROCESSING_FAILED = -1;
	public static final int PROCESSING_SUCCESS = 0;

	public static final long DEFAULT_TTL = 10000L;

	IJiacSender _sender;

	/**
	 * @param sender der Sender MIT dem eine Antwort verschickt wird
	 */
	public BasicJiacProtocol(IJiacSender sender) {
		setSender(sender);
	}

	public void setSender(IJiacSender sender) {
		_sender = sender;
	}

	/**
	 * Behandelt die Nachricht entsprechend des Protokolls.
	 * 
	 * @param msg die JMS-Message
	 * @return PROCESSING_FAILED bei Misserfolg oder PROCESSING_SUCCESS bei Erfolg
	 */
	public int processMessage(Message msg) {
		IJiacMessage jiacMsg = null;
		Destination destination = null;
		if (msg != null && msg instanceof ObjectMessage) {
			ObjectMessage oMsg = (ObjectMessage) msg;
			try {
				jiacMsg = (IJiacMessage) oMsg.getObject();
				destination = msg.getJMSReplyTo();
				System.out.println("processing message, answer will go to:" + destination);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return processJiacMessage(jiacMsg, destination);
	}

	/**
	 * Diese Methode leitet entsprechend des MessageTyps an andere Methoden weiter und schickt die Antwort
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
				return doReply(replyMessage, destination, msg.getSender());
			}
		}
		return PROCESSING_FAILED;
	}

	/**
	 * Reaktion auf AcknowledgeMessages
	 * 
	 * @param msg
	 * @return bisher null.. auf 'ack' braucht nicht reagiert zu werden..
	 */
	private IJiacMessage doAcknowledge(IJiacMessage msg) {
		String operation = msg.getOperation();
		if (ACK_GET_AGENTS.equals(operation)) {
			System.out.println("Alles Roger.. hab die Agenten gekriegt, von " + msg.getStartPoint());
			// _platform.addRemoteAgents((List<AgentStub>) msg.getPayload());
		} else if (ACK_GET_SERVICES.equals(operation)) {
			System.out.println("Alles Roger.. hab die Services gekriegt, von " + msg.getStartPoint());
		} else if (ACK_PING.equals(operation)) {
			System.out.println("Alles Roger.. hab ein Ping-Ack gekriegt, von " + msg.getStartPoint());
		} else if (ACK_NOP.equals(operation)) {
			System.out.println("Ich werd' verrückt.. hab ein NOP-Ack gekriegt, von " + msg.getStartPoint());
		}
		return null;
	}

	/**
	 * Reaktion auf Fehler-messages
	 * 
	 * @param msg
	 * @return
	 */
	private IJiacMessage doError(IJiacMessage msg) {
		String operation = msg.getOperation();
		if (ERR_GET_AGENTS.equals(operation)) {

		} else if (ERR_GET_SERVICES.equals(operation)) {

		} else if (ERR_NOP.equals(operation)) {

		} else if (ERR_NOP.equals(operation)) {

		}
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
		if (CMD_GET_AGENTS.equals(operation)) {
			// List<AgentStub> agents = _platform.getAgents();
			// System.out.println("+++++ schicke ab an " + receivedMsg.getStartPoint());
			// PlatformHelper.debugPrintAgents(agents);
			replyMsg = new JiacMessage(ACK_GET_AGENTS, "agents", receivedMsg.getStartPoint(), receivedMsg.getEndPoint(),
																							receivedMsg.getSender());
		} else if (CMD_GET_SERVICES.equals(operation)) {
			String[] services = { "A()", "B()", "C()" };
		} else if (CMD_PING.equals(operation)) {
			String content = "Pong";
			replyMsg = new JiacMessage(ACK_PING, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(), receivedMsg
																							.getSender());
		} else if (CMD_NOP.equals(operation)) {
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
	 * @return PROCESSING_SUCCESS, wenn versendet; PROCESSING_FAILED wenn Versenden nicht möglich war.
	 */
	private int doReply(IJiacMessage msg, Destination destination, Destination senderAddress) {
		if (msg != null && destination != null && _sender != null) {
			_sender.send(msg, destination); // , senderAddress, DEFAULT_TTL);
			return PROCESSING_SUCCESS;
		}
		return PROCESSING_FAILED;
	}

}
