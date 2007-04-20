package de.dailab.jiactng.agentcore.comm.protocol;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.IJiacSender;
import de.dailab.jiactng.agentcore.comm.JiacMessage;
import de.dailab.jiactng.agentcore.comm.ObjectContent;

public class NodeProtocol implements IProtocolHandler {
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

	public static final int PROCESSING_FAILED = -1;
	public static final int PROCESSING_SUCCESS = 0;

	public static final long DEFAULT_TTL = 10000L;

	IJiacSender _topicSender;
	IJiacSender _queueSender;

	/**
	 * @param sender der Sender MIT dem eine Antwort verschickt wird
	 */
	public NodeProtocol(IJiacSender topicSender, IJiacSender queueSender) {
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

	public int processMessage(Message msg) {
		IJiacMessage jiacMsg = null;
		Destination replyToDestination = null;
		if (msg != null && msg instanceof ObjectMessage) {
			ObjectMessage oMsg = (ObjectMessage) msg;
			try {
				jiacMsg = (IJiacMessage) oMsg.getObject();
				replyToDestination = msg.getJMSReplyTo();
				log.debug("processing message, answer will go to:" + replyToDestination);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return processJiacMessage(jiacMsg, replyToDestination);
	}

	/**
	 * Diese Methode leitet entsprechend des MessageTyps an andere Methoden weiter und schickt die Antwort
	 * 
	 * @param msg
	 * @param destination
	 * @return
	 */
	private int processJiacMessage(IJiacMessage msg, Destination replyToDestination) {
		if (msg != null) {
			String operation = msg.getOperation();
			if (operation != null) {
				String type = operation.substring(0, 3);
				IJiacMessage replyMessage = null;
				SendInfo sendInfo = null;
				if ("ACK".equals(type)) {
					sendInfo = doAcknowledge(msg);
				} else if ("ERR".equals(type)) {
					sendInfo = doError(msg);
				} else {
					// kein special Prefix, dann ist es ein Kommando
					sendInfo = doCommand(msg);
				}
				Destination senderDest = (replyMessage == null) ? null : replyMessage.getSender();
				return doReply(sendInfo._msg, sendInfo._destinationString, senderDest, getDefaultSender());
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
	 * @param receivedMsg die empfangene JiacMessage
	 * @return
	 */
	private SendInfo doAcknowledge(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		SendInfo sendInfo = new SendInfo();
		if (ACK_GET_SERVICES.equals(operation)) {
			log.debug("Alles Roger.. hab die Services gekriegt, von" + receivedMsg.getStartPoint());
		} else if (ACK_PING.equals(operation)) {
			System.out.println("Alles Roger.. hab ein Ping-Ack gekriegt, von " + receivedMsg.getStartPoint());
			// ObjectContent content = new ObjectContent();
			// content.setObject(receivedMsg.getPayload() + "PongPong");
			// replyMsg = new JiacMessage(CMD_NOP, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(), null);
			// sendInfo.setDestinationString("");
			// sendInfo.setMsg(replyMsg);
		} else if (ACK_PING.equals(operation)) {
			log.warn("Alles Roger.. hab ein Pong-Ack gekriegt, von " + receivedMsg.getStartPoint());
		} else if (ACK_NOP.equals(operation)) {
			log.debug("Ich werd' verrückt.. hab ein NOP-Ack gekriegt, von " + receivedMsg.getStartPoint());
		}
		return sendInfo;
	}

	/**
	 * Reaktion auf Fehler-messages
	 * 
	 * @param msg
	 * @return
	 */
	private SendInfo doError(IJiacMessage msg) {
		String operation = msg.getOperation();
		// if (ERR_GET_AGENTS.equals(operation)) {
		// } else if (ERR_GET_SERVICES.equals(operation)) {
		// } else if (ERR_NOP.equals(operation)) {
		// } else if (ERR_NOP.equals(operation)) {
		// }
		return null;
	}

	/**
	 * Hier sind die interessanten, service-bringenden Funktionen versteckt
	 * 
	 * @param receivedMsg
	 * @return
	 */
	private SendInfo doCommand(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		SendInfo sendInfo = new SendInfo();
		// if (CMD_GET_AGENTS.equals(operation)) {
		// List<AgentStub> agents = _platform.getAgents();
		// System.out.println("+++++ schicke ab an "+receivedMsg.getStartPoint());
		// PlatformHelper.debugPrintAgents(agents);
		// replyMsg = new JiacMessage(ACK_GET_AGENTS, agents,
		// receivedMsg.getStartPoint(), receivedMsg.getEndPoint(),
		// receivedMsg.getSender());
		if (CMD_GET_SERVICES.equals(operation)) {
			String[] services = { "A()", "B()", "C()" };
		} else if (CMD_PING.equals(operation)) {
			ObjectContent content = new ObjectContent();
			content.setObject(receivedMsg.getPayload() + "Pong");
			String pay = receivedMsg.getPayload().toString();
			String destinationName = pay.substring(pay.indexOf(':') + 1);
			replyMsg = new JiacMessage(ACK_PING, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(), null);
			sendInfo.setDestinationString(destinationName);
			sendInfo.setMsg(replyMsg);
		} else if (CMD_NOP.equals(operation)) {
			String content = "psst";
		}
		return sendInfo;
	}

	/**
	 * Verschickt die Antwort
	 * 
	 * @param msg die AntwortNachricht
	 * @param destination die ZielDestination
	 * @param senderAddress die ReplyTo-Destination der JMSMessage
	 * @return PROCESSING_SUCCESS, wenn versendet; PROCESSING_FAILED wenn Versenden nicht möglich war.
	 */
	private int doReply(IJiacMessage msg, Destination destination, Destination senderAddress, IJiacSender sender) {
		log.debug("Knoten schickt antwort...");
		if (msg != null && destination != null && sender != null) {
			sender.send(msg, destination);// , senderAddress, DEFAULT_TTL);
			return PROCESSING_SUCCESS;
		}
		return PROCESSING_FAILED;
	}

	/**
	 * Verschickt die Antwort
	 * 
	 * @param msg die AntwortNachricht
	 * @param destination die ZielDestination
	 * @param senderAddress die ReplyTo-Destination der JMSMessage
	 * @return PROCESSING_SUCCESS, wenn versendet; PROCESSING_FAILED wenn Versenden nicht möglich war.
	 */
	private int doReply(IJiacMessage msg, String destinationName, Destination senderAddress, IJiacSender sender) {
		log.debug("Knoten schickt antwort...");
		if (msg != null && destinationName != null && sender != null) {
			sender.send(msg, destinationName);// , senderAddress, DEFAULT_TTL);
			return PROCESSING_SUCCESS;
		}
		return PROCESSING_FAILED;
	}

	class SendInfo {
		IJiacMessage _msg;
		String _destinationString;

		public SendInfo() {}

		public SendInfo(IJiacMessage msg, String destinationString) {
			setDestinationString(destinationString);
			setMsg(msg);
		}

		public String getDestinationString() {
			return _destinationString;
		}

		public void setDestinationString(String destinationString) {
			_destinationString = destinationString;
		}

		public IJiacMessage getMsg() {
			return _msg;
		}

		public void setMsg(IJiacMessage msg) {
			_msg = msg;
		}

	}
}
