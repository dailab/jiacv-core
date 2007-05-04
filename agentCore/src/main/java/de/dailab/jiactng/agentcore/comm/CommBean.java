package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.comm.message.EndPoint;
import de.dailab.jiactng.agentcore.comm.message.EndPointFactory;
import de.dailab.jiactng.agentcore.comm.message.IEndPoint;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessageFactory;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.comm.protocol.AgentProtocol;
import de.dailab.jiactng.agentcore.comm.protocol.BasicJiacProtocol;
import de.dailab.jiactng.agentcore.comm.protocol.IProtocolHandler;
import de.dailab.jiactng.agentcore.comm.protocol.NodeProtocol;

/**
 * Die CommBean hält zwei Communicatoren, einen für topiczugriff und einen für queuezugriff. Über diesen laufen die
 * JMSzugriffe
 * 
 * @author janko
 */
public class CommBean extends AbstractAgentBean {
	Log log = LogFactory.getLog(getClass());

	// über den Communicator läuft die JMS communication
	QueueCommunicator _communicator;
	// eigene Adresse
	EndPoint _address;

	TopicCommunicator _topicCommunicator;

	int _timer = 200;
	int _timerCounter = 0;

	ConnectionFactory _connectionFactory;
	String _defaultTopicName;
	String _defaultQueueName;

	List<CommMessageListener> _commListener = new ArrayList<CommMessageListener>();

	// defaultmässig wird das BasicProkoll erzeugt
	String _protocolType = IProtocolHandler.BASIC_PROTOCOL;

	/* aus dem Namen wird die Adresse gebildet */
	String _agentNodeName;

	public CommBean() {
		super();
	}

	/**
	 * Erzeugt zwei Communicatoren mit dem defaultTopic und der DefaultQueue
	 */
	@Override
	public void doInit() throws Exception {
		super.doInit();
		if (thisAgent != null && thisAgent.getAgentNode() != null) {
			setAgentNodeName(thisAgent.getAgentNode().getName());
		}
		_address = (EndPoint) EndPointFactory.createEndPoint(getAgentNodeName());

		_topicCommunicator = new TopicCommunicator();
		// Ein topic wird verwendet, zum lesen und schreiben - das defaultTopic
		TopicReceiver topicReceiver = new TopicReceiver(this, _connectionFactory, _defaultTopicName);
		TopicSender topicSender = new TopicSender(_connectionFactory, _defaultTopicName);

		_communicator = new QueueCommunicator();
		// auf eine Queue mit dem Namen der eigenen Addresse hören
		QueueReceiver queueReceiver = new QueueReceiver(_connectionFactory, getAddress().toString());
		_communicator.setReceiver(queueReceiver);
		// gesendet wird defaultmässig auf die defaultQueue.. (?)
		QueueSender queueSender = new QueueSender(_connectionFactory, getAddress().toString());
		_communicator.setSender(queueSender);
		IProtocolHandler queueProtocol = createProtocol(topicSender, queueSender);
		_communicator.setProtocol(queueProtocol);
		QueueMessageListener msgListener = new QueueMessageListener(queueProtocol, this);
		_communicator.getReceiver().receive(null, msgListener);

		IProtocolHandler topicProtocol = createProtocol(topicSender, queueSender);
		TopicMessageListener topicMsgListener = new TopicMessageListener(topicProtocol, this);
		_topicCommunicator.setReceiver(topicReceiver);
		_topicCommunicator.setSender(topicSender);
		_topicCommunicator.getReceiver().receive(null, topicMsgListener);
	}

	/**
	 * Erzeugt ein Protokoll für den Queue-/Topic-Listener.
	 * 
	 * @param topicSender der Sender, der antworten des Protokolls in n Topic verschickt.
	 * @param queueSender der Sender, der antworten des Protokolls in ne Queue verschickt.
	 * @return entsprechend des Protokolltyps, wird dieses zurückgeliefert, sonst ein neues standardprotokoll
	 *         zurückgegeben.
	 */
	private IProtocolHandler createProtocol(TopicSender topicSender, QueueSender queueSender) {
		IProtocolHandler protocol;
		if (IProtocolHandler.AGENT_PROTOCOL.equals(getProtocolType())) {
			protocol = (IProtocolHandler) new AgentProtocol(topicSender, queueSender);
			((AgentProtocol) protocol).setAgent(thisAgent);
		} else if (IProtocolHandler.PLATFORM_PROTOCOL.equals(getProtocolType())) {
			protocol = (IProtocolHandler) new NodeProtocol(topicSender, queueSender);
			((NodeProtocol) protocol).setAgent(thisAgent);
			((NodeProtocol) protocol).setCommBean(this);
		} else {
			protocol = (IProtocolHandler) new BasicJiacProtocol(topicSender, queueSender);
		}
		return protocol;
	}

	/**
	 * Wird vom Protocol aufgerufen.. Informiert alle Listener
	 * 
	 * @param message
	 */
	public void messageReceivedFromQueue(Message message) {
		informQueueListener(message);
	}

	/**
	 * Wird vom Protocol aufgerufen.. Informiert alle Listener
	 * 
	 * @param message
	 */
	public void messageReceivedFromTopic(Message message) {
		informTopicListener(message);
	}

	/**
	 * sendet auf die defaultQueue
	 * 
	 * @param message
	 */
	public void send(IJiacMessage message) {
		_communicator.send(message);
	}

	/**
	 * Sendet in die angegebene Queue
	 * 
	 * @param message
	 * @param destinationName
	 */
	public void send(IJiacMessage message, String destinationName) {
		_communicator.send(message, destinationName);
	}

	/**
	 * Sendet in die defaultqueue
	 * 
	 * @param operation
	 * @param payload
	 * @param receiverAddress
	 */
	public void send(String operation, IJiacContent payload, IEndPoint receiverAddress) {
		IJiacMessage msg = JiacMessageFactory.createJiacMessage(operation, payload, _address, receiverAddress, null);
		send(msg);
	}

	public void publish(IJiacMessage message) {
		_topicCommunicator.publish(message);
	}

	public void publish(String operation, IJiacContent payload, IEndPoint destAddress) {
		IJiacMessage msg = JiacMessageFactory.createJiacMessage(operation, payload, _address, destAddress, null);
		publish(msg);
	}

	@Override
	public void execute() {
		if (IProtocolHandler.PLATFORM_PROTOCOL.equals(_protocolType)) {
			publishPlatformAliveMessage();
		} else if (IProtocolHandler.AGENT_PROTOCOL.equals(_protocolType)) {
			// publishAgentPingMessage();
		}
	}

	/**
	 * Schreibt in die Topic eine 'PlatformPing'-Nachricht.. nach anzahl/timer Aufrufen, d.h. wenn timer==10, muss 10mal
	 * aufgerufen werden, damit einmal gesendet wird.
	 */
	private void publishAgentPingMessage() {
		_timerCounter++;
		if (_timerCounter == _timer) {
			_timerCounter = 0;
			ObjectContent content = new ObjectContent("ReplyTo:" + _address.toString());
			IJiacMessage msg = new JiacMessage(NodeProtocol.CMD_PING, content, null, getAddress(), null);
			publish(msg);
			log.debug(this.getBeanName() + ", " + thisAgent.getAgentName());
		}
	}

	/**
	 * Schreibt in die Topic eine 'PlatformPing'-Nachricht.. nach anzahl/timer Aufrufen, d.h. wenn timer==10, muss 10mal
	 * aufgerufen werden, damit einmal gesendet wird.
	 */
	private void publishPlatformAliveMessage() {
		_timerCounter++;
		if (_timerCounter == _timer) {
			_timerCounter = 0;
			ObjectContent content = new ObjectContent("ReplyTo:" + _address.toString());
			IJiacMessage msg = new JiacMessage(NodeProtocol.CMD_PING, content, null, getAddress(), null);
			publish(msg);
			// log.debug(this.getBeanName() + ", " + thisAgent.getAgentName());
		}
	}

	public List getLocalAgents() {
		if (thisAgent != null) {
			IAgentNode agentNode = thisAgent.getAgentNode();
			if (agentNode instanceof SimpleAgentNode) {
				return ((SimpleAgentNode) agentNode).findAgents();
			}
		}
		return null;
	}

	/**
	 * Informiert alle registrierten CommMessageListener, dass eine Message ankam
	 * 
	 * @param message
	 */
	private void informQueueListener(Message message) {
		for (Iterator iter = _commListener.iterator(); iter.hasNext();) {
			CommMessageListener listener = (CommMessageListener) iter.next();
			listener.messageReceivedFromQueue(message);
		}
	}

	/**
	 * Informiert alle registrierten CommMessageListener, dass eine Message ankam
	 * 
	 * @param message
	 */
	private void informTopicListener(Message message) {
		for (Iterator iter = _commListener.iterator(); iter.hasNext();) {
			CommMessageListener listener = (CommMessageListener) iter.next();
			listener.messageReceivedFromTopic(message);
		}
	}

	/**
	 * Fügt einen CommListner der Commbean zu.
	 * 
	 * @param listener
	 */
	public void addCommMessageListener(CommMessageListener listener) {
		_commListener.add(listener);
	}

	public void removeCommMessageListener(CommMessageListener listener) {
		_commListener.remove(listener);
	}

	public QueueCommunicator getCommunicator() {
		return _communicator;
	}

	public void setCommunicator(QueueCommunicator communicator) {
		_communicator = communicator;
	}

	public IEndPoint getAddress() {
		return _address;
	}

	public void setAddress(EndPoint address) {
		_address = address;
	}

	public TopicCommunicator getTopicCommunicator() {
		return _topicCommunicator;
	}

	public void setTopicCommunicator(TopicCommunicator topicCommunicator) {
		_topicCommunicator = topicCommunicator;
	}

	public String getDefaultQueueName() {
		return _defaultQueueName;
	}

	public void setDefaultQueueName(String defaultQueueName) {
		_defaultQueueName = defaultQueueName;
	}

	public String getDefaultTopicName() {
		return _defaultTopicName;
	}

	public void setDefaultTopicName(String defaultTopicName) {
		_defaultTopicName = defaultTopicName;
	}

	public ConnectionFactory getConnectionFactory() {
		return _connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		_connectionFactory = connectionFactory;
	}

	public String getProtocolType() {
		return _protocolType;
	}

	public void setProtocolType(String protocolType) {
		_protocolType = protocolType;
	}

	public String getAgentNodeName() {
		return _agentNodeName;
	}

	public void setAgentNodeName(String agentNodeName) {
		_agentNodeName = agentNodeName;
	}

}
