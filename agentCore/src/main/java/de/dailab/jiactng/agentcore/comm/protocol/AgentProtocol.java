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
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.DoRemoteAction;
import de.dailab.jiactng.agentcore.action.RemoteActionResult;
import de.dailab.jiactng.agentcore.comm.IJiacSender;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.knowledge.IMemory;

/**
 * Idee: Ein eigenens Protocol für die Agenten. Zusätzlich zu einem Protocol für die TNG-Platform. Hier werden also die
 * an Agenten adressierten Messages ausgewertet und regiert.. Noch zu machen: Protokoll ausdenken... Worüber müssen sich
 * agenten unterhalten? Was gibts für anfragen/antworten ?
 * 
 * @author janko
 */
public class AgentProtocol implements IAgentProtocol {
	Log log = LogFactory.getLog(getClass());

	static final String[] OPERATIONS = { 
		CMD_AGT_PING, 
		CMD_AGT_PONG, 
		CMD_AGT_GET_SERVICES, 
		CMD_AGT_GET_BEANNAMES,
		CMD_AGT_NOP,
		CMD_AGT_REMOTE_DOACTION,
		ACK_AGT_PING, 
		ACK_AGT_PONG, 
		ACK_AGT_GET_SERVICES,
		ACK_AGT_GET_BEANNAMES,
		ACK_AGT_NOP,
		ACK_AGT_REMOTE_ACTIONRESULT
	};

	IJiacSender _topicSender;
	IJiacSender _queueSender;
	/** Der agent erbringt die leistungen, getServices, execService, .. etc. */
	IAgent _agent;
	/** Agent's factbase.*/
	IMemory memory;

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

	/**
	 * @return the memory
	 */
	public IMemory getMemory() {
		return memory;
	}

	/**
	 * @param memory the memory to set
	 */
	public void setMemory(IMemory memory) {
		this.memory = memory;
	}

	/**
	 * Takes a Message, checks if it is an objectmessage and extracts the real jiacmessage
	 * and it's JMSReplyTo adress out of it, then it returns the reply to it.
	 * 
	 * @param msg the Message received by the agent.
	 */
	public int processMessage(Message msg) {
//		log.debug("Ist das geil... ein Agent hat ne nachricht gekriegt...");
		IJiacMessage jiacMsg = null;
		Destination destination = null;
		if (msg != null && msg instanceof ObjectMessage) {
			ObjectMessage oMsg = (ObjectMessage) msg;
			try {
				jiacMsg = (IJiacMessage) oMsg.getObject();
//				log.debug("und zwar folgende Nachricht:" + jiacMsg.toString());
				destination = msg.getJMSReplyTo();
//				log.debug("processing Agent message, answer will go to:" + destination);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return processJiacMessage(jiacMsg, destination);
	}

	/**
	 * Diese Methode leitet entsprechend des MessageTyps an andere Methoden weiter und schickt die Antwort
	 * 
	 * @param msg the message received by the agent and probably passed over through processJiacMessage(Message)
	 * @param destination the JmsReplyTo Adress of msg
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
	protected IJiacMessage doAcknowledge(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		if (ACK_AGT_GET_SERVICES.equals(operation)) {
			log.debug("Alles Roger.. hab die Services gekriegt, von" + receivedMsg.getStartPoint());

			// BEGIN TESTING CODE 
			// creating replyMsg for Testresponse
			replyMsg = new JiacMessage(ACK_AGT_GET_SERVICES_SUCESS, null, receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(), null);
			// END TESTING CODE
			
		} else if (ACK_AGT_REMOTE_ACTIONRESULT.equals(operation)) {
			//TODO send actionresult back to requester
		} else if (ACK_AGT_GET_BEANNAMES.equals(operation)) {
			List<String> beanNames = extractBeanNames(receivedMsg);
			
			//BEGIN TESTING CODE
			// creating replyMsg for Testresponse;
			replyMsg = new JiacMessage(operation, new ObjectContent((java.io.Serializable) beanNames),
					receivedMsg.getStartPoint(), receivedMsg.getEndPoint(), null);
			//END TESTING CODE 
			
			for (Iterator iter = beanNames.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				if (iter.hasNext()){
					System.out.print(element+ ", ");
				} else {
					System.out.println(element + ".");
				}
			}
		} else if (ACK_AGT_PING.equals(operation)) {
			System.out.println("Alles Roger.. hab ein Ping-Ack gekriegt, von " + receivedMsg.getStartPoint());
			ObjectContent content = new ObjectContent(receivedMsg.getPayload() + "PongPong");
			replyMsg = new JiacMessage(CMD_AGT_NOP, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(),
																							receivedMsg.getSender());
		} else if (ACK_AGT_PONG.equals(operation)) {
			log.debug("Alles Roger.. hab ein Pong-Ack gekriegt, von " + receivedMsg.getStartPoint());
			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_AGT_PONG_SUCESS,receivedMsg.getPayload(), 
					receivedMsg.getEndPoint(), receivedMsg.getStartPoint(), null);
			// END TESTING CODE
		} else if (ACK_AGT_NOP.equals(operation)) {
			log.debug("Ich werd' verrückt.. hab ein NOP-Ack gekriegt, von " + receivedMsg.getStartPoint());
			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_AGT_NOP_SUCESS,receivedMsg.getPayload(), 
					receivedMsg.getEndPoint(), receivedMsg.getStartPoint(), null);
			// END TESTING CODE
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
		// if (CMD_GET_AGENTS.equals(operation)) {
		// List<AgentStub> agents = _platform.getAgents();
		// System.out.println("+++++ schicke ab an "+receivedMsg.getStartPoint());
		// PlatformHelper.debugPrintAgents(agents);
		// replyMsg = new JiacMessage(ACK_GET_AGENTS, agents,
		// receivedMsg.getStartPoint(), receivedMsg.getEndPoint(),
		// receivedMsg.getSender());
		if (CMD_AGT_GET_SERVICES.equals(operation)) {
			//BEGIN TESTING CODE
			replyMsg = new JiacMessage(CMD_AGT_GET_SERVICES_SUCESS, 
					null, 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
		} else if (CMD_AGT_REMOTE_DOACTION.equals(operation)) {
			//TODO publish DoAction object and send back result
			log.debug(CMD_AGT_REMOTE_DOACTION);
			ActionResult result = processDoRemoteAction(((DoRemoteAction)receivedMsg.getPayload()).getAction());
			replyMsg = new JiacMessage(
					ACK_AGT_REMOTE_ACTIONRESULT,
					new RemoteActionResult(result),
					receivedMsg.getStartPoint(),
					receivedMsg.getEndPoint(),
					null);
		} else if (CMD_AGT_GET_BEANNAMES.equals(operation)) {
			List beanNames = getBeanNames();
			//BEGIN TESTING CODE
			replyMsg = new JiacMessage(CMD_AGT_GET_SERVICES_SUCESS, 
					new ObjectContent((java.io.Serializable) beanNames), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
		} else if (CMD_AGT_PING.equals(operation)) {
			ObjectContent content = new ObjectContent(receivedMsg.getPayload() + "Pong");
			// es wird die platformadresse als absender gesetzt
			replyMsg = new JiacMessage(ACK_AGT_PING, 
					content, 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(), 
					null);
			// receivedMsg.getSender()); // hier falscher Sender/replyToAdress(?)
		} else if (CMD_AGT_NOP.equals(operation)) {
			// nüscht machen :D
			// BEGIN TESTING CODE
			replyMsg = new JiacMessage(ACK_AGT_NOP_SUCESS, 
					receivedMsg.getPayload(), 
					receivedMsg.getStartPoint(), 
					receivedMsg.getEndPoint(),
					null);
			//END TESTING CODE
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
	 * 
	 * @param message
	 * @return
	 */
	private List<String> extractBeanNames(IJiacMessage message) {
		// indem fall muss es ein ObjectContent sein..
		ObjectContent content = (ObjectContent) message.getPayload();
		// in dem fall muss es ne Liste sein
		List<String> list = (List<String>) content.getObject();
		return list;
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
		log.debug("Agent schickt antwort...");
		if (msg != null && destination != null && senderAdress!= null && sender != null) {
			sender.send(msg, destination);// , senderAdress, DEFAULT_TTL);
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

	public static String[] getOperations() {
		return OPERATIONS;
	}

	private ActionResult processDoRemoteAction(DoAction action) {
		log.debug("processDoRemoteAction");
		memory.write(action);
		//memory.read(new ActionResult(null, action, null, null, null), Long.MAX_VALUE);
		return null;
	}

}
