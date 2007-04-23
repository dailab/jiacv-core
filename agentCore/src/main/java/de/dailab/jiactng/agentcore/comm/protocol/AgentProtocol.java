package de.dailab.jiactng.agentcore.comm.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.comm.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.IJiacSender;
import de.dailab.jiactng.agentcore.comm.JiacMessage;
import de.dailab.jiactng.agentcore.comm.ObjectContent;

/**
 * Idee: Ein eigenens Protocol für die Agenten. Zusätzlich zu einem Protocol für die TNG-Platform. Hier werden also die
 * an Agenten adressierten Messages ausgewertet und regiert.. Noch zu machen: Protokoll ausdenken... Worüber müssen sich
 * agenten unterhalten? Was gibts für anfragen/antworten ?
 * 
 * @author janko
 */
public class AgentProtocol implements IAgentProtocol {
	Log log = LogFactory.getLog(getClass());

	IJiacSender _topicSender;
	IJiacSender _queueSender;
	/** Der agent erbringt die leistungen, getServices, execService, .. etc. */
	IAgent _agent;

	/**
	 * @param sender der Sender MIT dem eine Antwort verschickt wird
	 */
	public AgentProtocol(IJiacSender topicSender, IJiacSender queueSender) {
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
		log.debug("Ist das geil... ein Agent hat ne nachricht gekriegt...");
		IJiacMessage jiacMsg = null;
		Destination destination = null;
		if (msg != null && msg instanceof ObjectMessage) {
			ObjectMessage oMsg = (ObjectMessage) msg;
			try {
				jiacMsg = (IJiacMessage) oMsg.getObject();
				log.debug("und zwar folgende Nachricht:" + jiacMsg.toString());
				destination = msg.getJMSReplyTo();
				log.debug("processing Agent message, answer will go to:" + destination);
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
				} else {
					// kein special Prefix, dann ist es ein Kommando
					replyMessage = doCommand(msg);
				}
				// return doReply(replyMessage, destination, msg.getSender());
				Destination senderDest = replyMessage == null ? null : replyMessage.getSender();
				return doReply(replyMessage, destination, senderDest, getDefaultSender());
			}
		}
		return PROCESSING_FAILED;
	}

	/*
	 * temporäre hilfsmethode. wenn später ordentlich auf commands reagiert wird, sollte dort festgelegt werden, mit
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
	private IJiacMessage doAcknowledge(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		if (ACK_AGT_GET_SERVICES.equals(operation)) {
			log.debug("Alles Roger.. hab die Services gekriegt, von" + receivedMsg.getStartPoint());
		} else if (ACK_AGT_GET_BEANNAMES.equals(operation)) {
			List<String> beanNames = extractBeanNames(receivedMsg);
			for (Iterator iter = beanNames.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				System.out.print(element+ ", ");
			}
		} else if (ACK_AGT_PING.equals(operation)) {
			System.out.println("Alles Roger.. hab ein Ping-Ack gekriegt, von " + receivedMsg.getStartPoint());
			ObjectContent content = new ObjectContent(receivedMsg.getPayload() + "PongPong");
			replyMsg = new JiacMessage(CMD_AGT_NOP, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(),
																							receivedMsg.getSender());
		} else if (ACK_AGT_PONG.equals(operation)) {
			log.debug("Alles Roger.. hab ein Pong-Ack gekriegt, von " + receivedMsg.getStartPoint());
		} else if (ACK_AGT_NOP.equals(operation)) {
			log.debug("Ich werd' verrückt.. hab ein NOP-Ack gekriegt, von " + receivedMsg.getStartPoint());
		}
		return replyMsg;
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

		} else if (CMD_AGT_GET_BEANNAMES.equals(operation)) {
			List beanNames = getBeanNames();
		} else if (CMD_AGT_PING.equals(operation)) {
			ObjectContent content = new ObjectContent(receivedMsg.getPayload() + "Pong");
			// es wird die platformadresse als absender gesetzt
			replyMsg = new JiacMessage(ACK_AGT_PING, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(), null);
			// receivedMsg.getSender()); // hier falscher Sender/replyToAdress(?)
		} else if (CMD_AGT_NOP.equals(operation)) {
			// nüscht machen :D
		}
		return replyMsg;
	}

	/**
	 * holt alle beannamen des agenten
	 * 
	 * @return ne liste mit den namen der agenten.
	 */
	private List<String> getBeanNames() {
		List<String> list = new ArrayList<String>();
		if (_agent != null) {
			List beans = _agent.getAgentBeans();
			for (Iterator iter = beans.iterator(); iter.hasNext();) {
				IAgentBean bean = (IAgentBean) iter.next();
				list.add(bean.getBeanName());
			}
		}
		return list;
	}

	/**
	 * Zieht aus ner JiacMessage die Liste mit den Beannamen raus.
	 * @param message
	 * @return
	 */
	private List<String> extractBeanNames(IJiacMessage message) {
		// indem fall muss es ein ObjectContent sein..
		ObjectContent content = (ObjectContent)message.getPayload();
		// in dem fall muss es ne Liste sein
		List<String> list = (List<String>)content.getObject();
		return list;
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
		log.debug("Agent schickt antwort...");
		if (msg != null && destination != null && sender != null) {
			sender.send(msg, destination);// , senderAddress, DEFAULT_TTL);
			return PROCESSING_SUCCESS;
		}
		return PROCESSING_FAILED;
	}

	public IAgent getAgent() {
		return _agent;
	}

	public void setAgent(IAgent agent) {
		_agent = agent;
	}
}
