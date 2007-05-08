package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.IJiacSender;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;

/**
 * Ein 'Basic'-Protokoll für die Kommunikation. Es wird standardmässig verwendet. 
 * Die processMessage-Methode wird von einem MessageListener aufgerufen. Die
 * Message wird hier behandelt und mit dem im Konstruktor übergebenen Sender wird
 * eine Antwort gesendet. Der Empfänger der Antwort wird aus der Message gelesen.
 * 
 * @author janko
 */
public class BasicJiacProtocol implements IProtocolHandler {
	// static Logger log4j = Logger.getLogger("de.dailab.jiactng.protocol.BasicJiacProtocol");
	Log log = LogFactory.getLog(getClass());

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
	
	// Replys for testing purposes
	public static final String ACK_TEST_SUCESS = "ACK_TEST_SUCESS";

	public static final int PROCESSING_FAILED = -1;
	public static final int PROCESSING_SUCCESS = 0;

	public static final long DEFAULT_TTL = 10000L;

	IJiacSender _topicSender;
	IJiacSender _queueSender;

	/**
	 * @param sender der Sender MIT dem eine Antwort verschickt wird
	 */
	public BasicJiacProtocol(IJiacSender topicSender, IJiacSender queueSender) {
		setSender(topicSender, queueSender);
	}

	public void setSender(IJiacSender topicSender, IJiacSender queueSender) {
		setTopicSender(topicSender);
		setQueueSender(queueSender);
	}

	public void setTopicSender(IJiacSender sender) {
		_topicSender = sender;
	}

	public void setQueueSender(IJiacSender sender) {
		_queueSender = sender;
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
				log.debug("processing message, answer will go to:" + destination);
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
				return doReply(replyMessage, destination, msg.getSender(), getDefaultSender());
			}
		}
		return PROCESSING_FAILED;
	}

	/*
	 * temporäre hilfsmethode. wenn später ordentlich auf commands reagiert wird, sollt edort festgelegt werden, mit
	 * welchen sender gesendet wird.
	 */
	private IJiacSender getDefaultSender() {
		return _queueSender;
	}

	/**
	 * Reaktion auf AcknowledgeMessages
	 * 
	 * @param receivedMsg
	 * @return nur TestNachrichten. Auf Ack braucht bisher sonst nicht reagiert zu werden...
	 */
	protected IJiacMessage doAcknowledge(IJiacMessage receivedMsg) {
		IJiacMessage replyMsg = null;
		String operation = receivedMsg.getOperation();
		if (ACK_GET_AGENTS.equals(operation)) {
			log.debug("Alles Roger.. hab die Agenten gekriegt, von " + receivedMsg.getStartPoint());
			// _platform.addRemoteAgents((List<AgentStub>) msg.getPayload());
			
			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + "Gotcha Agent!"), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
			
		} else if (ACK_GET_SERVICES.equals(operation)) {
			log.debug("Alles Roger.. hab die Services gekriegt, von " + receivedMsg.getStartPoint());

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + "Gotcha Service!"), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
			
		} else if (ACK_PING.equals(operation)) {
			log.debug("Alles Roger.. hab ein Ping-Ack gekriegt, von " + receivedMsg.getStartPoint());

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + "Gotcha Ping!"), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
			
		} else if (ACK_NOP.equals(operation)) {
			log.debug("Ich werd' verrückt.. hab ein NOP-Ack gekriegt, von " + receivedMsg.getStartPoint());

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + "Nothing to do *yawns*"), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
		}
		return replyMsg;
	}

	/**
	 * Reaktion auf Fehler-messages
	 * 
	 * @param receivedMsg
	 * @return bisher nur Testnachrichten
	 */
	protected IJiacMessage doError(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		if (ERR_GET_AGENTS.equals(operation)) {

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + ERR_GET_AGENTS), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
		} else if (ERR_GET_SERVICES.equals(operation)) {

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + ERR_GET_SERVICES), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
		} else if (ERR_NOP.equals(operation)) {

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + ERR_NOP), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
		} else if (ERR_PING.equals(operation)) {

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + ERR_PING), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
		}
		return replyMsg;
	}

	/**
	 * Hier sind die interessanten, service-bringenden Funktionen versteckt
	 * 
	 * @param receivedMsg
	 * @return
	 */
	protected IJiacMessage doCommand(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		if (CMD_GET_AGENTS.equals(operation)) {
			// List<AgentStub> agents = _platform.getAgents();
			// System.out.println("+++++ schicke ab an " + receivedMsg.getStartPoint());
			// PlatformHelper.debugPrintAgents(agents);
			ObjectContent content = new ObjectContent("agents");
			replyMsg = new JiacMessage(ACK_GET_AGENTS, 
					content, 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					receivedMsg.getSender());
		} else if (CMD_GET_SERVICES.equals(operation)) {
			String[] services = { "A()", "B()", "C()" };

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(receivedMsg.getPayload() + CMD_GET_SERVICES), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
			
		} else if (CMD_PING.equals(operation)) {
			ObjectContent content = new ObjectContent("Pong");
			replyMsg = new JiacMessage(ACK_PING, 
					content, 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					receivedMsg.getSender());
		} else if (CMD_NOP.equals(operation)) {
			String content = "psst";

			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_TEST_SUCESS, 
					new ObjectContent(content), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
		}
		return replyMsg;
	}

	/**
	 * Verschickt die Antwort
	 * 
	 * @param msg die AntwortNachricht
	 * @param destination die ZielDestination
	 * @param senderAdress die ReplyTo-Destination der JMSMessage
	 * @return PROCESSING_SUCCESS, wenn versendet; PROCESSING_FAILED wenn Versenden nicht möglich war.
	 */
	private int doReply(IJiacMessage msg, Destination destination, Destination senderAdress, IJiacSender sender) {
		if (msg != null && destination != null && senderAdress != null && sender != null) {
			sender.send(msg, destination); // , senderAdress, DEFAULT_TTL);
			return PROCESSING_SUCCESS;
		}
		return PROCESSING_FAILED;
	}

}
