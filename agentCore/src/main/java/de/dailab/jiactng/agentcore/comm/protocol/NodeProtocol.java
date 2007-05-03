package de.dailab.jiactng.agentcore.comm.protocol;

import java.io.Serializable;
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
import de.dailab.jiactng.agentcore.comm.CommBean;
import de.dailab.jiactng.agentcore.comm.IJiacSender;
import de.dailab.jiactng.agentcore.comm.Util;
import de.dailab.jiactng.agentcore.comm.message.IEndPoint;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.servicediscovery.IServiceDescription;
import de.dailab.jiactng.agentcore.servicediscovery.IServiceDirectory;

/**
 * Das NodeProtocol reagiert auf nachrichten zwischen den Tng-Knoten
 * 
 * @author janko
 */
public class NodeProtocol implements INodeProtocol {
	public static final char PLATFORM_VS_AGENT_SEPARATOR = '@';
	static final String[] OPERATIONS = { CMD_PING, CMD_GET_AGENTS, CMD_GET_SERVICES, CMD_NOP, ACK_PING, ACK_GET_AGENTS,
																					ACK_GET_SERVICES, ACK_NOP, ERR_PING, ERR_GET_AGENTS, ERR_GET_SERVICES,
																					ERR_NOP };

	Log log = LogFactory.getLog(getClass());

	IJiacSender _topicSender;
	IJiacSender _queueSender;

	IAgent _agent;
	CommBean _commBean;

	/**
	 * @param topicSender der Sender MIT dem eine Antwort in die topic verschickt wird
	 * @param queueSender der Sender MIT dem eine Antwort auf ne queue verschickt wird
	 */
	public NodeProtocol(IJiacSender topicSender, IJiacSender queueSender) {
		setSender(topicSender, queueSender);
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
				// Destination senderDest = (replyMessage == null) ? null : replyMessage.getSender();
				// return doReply(sendInfo._msg, sendInfo._destinationString, senderDest, getDefaultSender());
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
		} else if (ACK_GET_AGENTS.equals(operation)) {
			// welch meisterlicher aufruf/cast/whatever :D
			ObjectContent oc = (ObjectContent) receivedMsg.getPayload();
			List<String> agentNames = (List<String>) (oc).getObject();
			log.warn("Alles Roger.. hab die Agenten gekriegt, von" + receivedMsg.getStartPoint());
			// printStringList(agentNames);
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
	 * @param receivedMsg die NAchricht auf die reagiert werden soll.
	 * @return eigentlich soll die zu verschickende Antwort zurückgegeben werden, momentan wird direkt aus dieser methode
	 *         versendet.. (das ist mist)
	 */
	private SendInfo doCommand(IJiacMessage receivedMsg) {
		String operation = receivedMsg.getOperation();
		IJiacMessage replyMsg = null;
		SendInfo sendInfo = new SendInfo();
		if (CMD_GET_AGENTS.equals(operation)) {
			// eigene agenten werden geschickt.
			doGetAgents(receivedMsg, _queueSender);
		} else if (CMD_GET_SERVICES.equals(operation)) {
			// eigene Servicebecshreibungen werden geschickt
			doGetServices(receivedMsg, _queueSender);
		} else if (CMD_PING.equals(operation)) {
			// auf PING reagieren..
			acknowledgePing(receivedMsg, sendInfo);
			// als reaktion auf ping wird gleich mal nach den dort vorhandenen Agenten gefragt.
			//requestAgentDescriptions(receivedMsg, _queueSender);
		} else if (CMD_NOP.equals(operation)) {
			String content = "psst";
		}
		return sendInfo;
	}

	// Hack, zusatzinfos wurden in Payload gesteckt - diese methode zieht diese infos wieder raus; den absender
	private String getRequestor(IJiacMessage receivedMsg) {
		String pay = receivedMsg.getPayload().toString();
		return pay.substring(pay.indexOf(':') + 1);
	}

	/**
	 * Liefert Ergebnis des GetAgents-Kommandos
	 * 
	 * @param receivedMsg
	 * @param sender
	 */
	public int doGetAgents(IJiacMessage receivedMsg, IJiacSender sender) {
		IJiacMessage requestMsg;
		SendInfo sendInfo = new SendInfo();
		List<AgentDescription> agentDescs = getAgentDescriptions();

		requestMsg = new JiacMessage(ACK_GET_AGENTS, new ObjectContent((Serializable) agentDescs), receivedMsg
																						.getStartPoint(), _commBean.getAddress(), null);
		sendInfo.setDestinationString(receivedMsg.getStartPoint().toString());
		sendInfo.setMsg(requestMsg);
		log.info("sending agentnames from " + sendInfo.getDestinationString());
		return doReply(sendInfo._msg, sendInfo._destinationString, null, sender);
	}

	/**
	 * Sendet eigene Services weg
	 * 
	 * @param receivedMsg die empfangen Nachricht - der request; daraus werden Infos gezogen um die antwortmessage
	 *          zusammenzubauen
	 * @param sender der sender mit dem verschickt wird
	 */
	public int doGetServices(IJiacMessage receivedMsg, IJiacSender sender) {
		IJiacMessage msgToSend;
		SendInfo sendInfo = new SendInfo();
		IServiceDirectory serviceDirectory = _agent.getAgentNode().getServiceDirectory();
		List<IServiceDescription> serviceDescList = serviceDirectory.getAllServices();

		msgToSend = new JiacMessage(ACK_GET_SERVICES, new ObjectContent((Serializable) serviceDescList), receivedMsg
																						.getStartPoint(), _commBean.getAddress(), sender.getReplyToDestination());
		sendInfo.setDestinationString(receivedMsg.getStartPoint().toString());
		sendInfo.setMsg(msgToSend);
		log.debug("sending services from " + sendInfo.getDestinationString());
		return doReply(sendInfo._msg, sendInfo._destinationString, null, sender);
	}

	private void printStringList(List<String> strings) {
		for (Iterator iter = strings.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			System.out.print(">>>>agent:" + element + " ");
		}
	}

	/**
	 * Fragt die platform nach ihren Agenten
	 * 
	 * @param receivedMsg in der receivedMsg sind empfängeradresse drin
	 * @param sender der sender mit dem verschikt werden soll
	 */
	public int requestAgentDescriptions(IJiacMessage receivedMsg, IJiacSender sender) {
		IJiacMessage requestMsg;
		SendInfo sendInfo = new SendInfo();
		String destinationName = getRequestor(receivedMsg);

		requestMsg = new JiacMessage(CMD_GET_AGENTS, new ObjectContent("agents"), receivedMsg.getStartPoint(), _commBean
																						.getAddress(), null);
		sendInfo.setDestinationString(destinationName);
		sendInfo.setMsg(requestMsg);
		log.warn("requesting agentnames from " + sendInfo.getDestinationString());
		return doReply(sendInfo._msg, sendInfo._destinationString, null, sender);
	}

	/*
	 * Reaktion auf Ping - nur um das mal beispielhaft zu haben
	 */
	private int acknowledgePing(IJiacMessage receivedMsg, SendInfo sendInfo) {
		IJiacMessage replyMsg;
		ObjectContent content = new ObjectContent(receivedMsg.getPayload() + "Pong");
		String destinationName = getRequestor(receivedMsg);

		replyMsg = new JiacMessage(ACK_PING, content, receivedMsg.getStartPoint(), receivedMsg.getEndPoint(), null);
		sendInfo.setDestinationString(destinationName);
		sendInfo.setMsg(replyMsg);
		return doReply(sendInfo._msg, receivedMsg.getStartPoint().toString(), null, getDefaultSender());
	}

	/*
	 * holt die AgentenNamen des aktuellen AgentNodes und liefert sie als liste
	 */
	private List<AgentDescription> getAgentDescriptions() {
		List<IAgent> agents = _agent.getAgentNode().findAgents();
		String platformName = _agent.getAgentNode().getName();
		List<AgentDescription> agentDescs = new ArrayList<AgentDescription>();
		for (Iterator iter = agents.iterator(); iter.hasNext();) {
			IAgent agent = (IAgent) iter.next();
			AgentDescription agentDescription = new AgentDescription(agent.getAgentName(), agent.getAgentName()
																							+ PLATFORM_VS_AGENT_SEPARATOR + platformName, Util.getLcsName(agent
																							.getState()), getCommEndPointFromAgent(agent));
			agentDescs.add(agentDescription);
		}
		return agentDescs;
	}

	/**
	 * holt aus nem Agenten den CommBean-Address-Endpoint. Problem: was, wenn mehrere CommBeans innerhalb eines Agenten ?
	 * 
	 * @param agent
	 * @return Endpoint der commbean eines agenten.
	 */
	private IEndPoint getCommEndPointFromAgent(IAgent agent) {
		for (Iterator iter = agent.getAgentBeans().iterator(); iter.hasNext();) {
			IAgentBean element = (IAgentBean) iter.next();
			if (element instanceof CommBean) {
				return ((CommBean) element).getAddress();
			}
		}
		return null;
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

	public IAgent getAgent() {
		return _agent;
	}

	public void setAgent(IAgent agent) {
		_agent = agent;
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

	public CommBean getCommBean() {
		return _commBean;
	}

	public void setCommBean(CommBean commBean) {
		_commBean = commBean;
	}

	public static String[] getOperations() {
		return OPERATIONS;
	}

}
