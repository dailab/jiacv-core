package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jms.ConnectionFactory;
import javax.jms.Message;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.DoRemoteAction;
import de.dailab.jiactng.agentcore.action.RemoteAction;
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
import de.dailab.jiactng.agentcore.comm.protocol.IAgentProtocol;
import de.dailab.jiactng.agentcore.comm.protocol.IProtocolHandler;
import de.dailab.jiactng.agentcore.comm.protocol.NodeProtocol;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.OtherAgentDescription;
import de.dailab.jiactng.agentcore.ontology.ThisAgentDescription;

/**
 * Die CommBean hält zwei Communicatoren, einen für topiczugriff und einen für queuezugriff. Über diesen laufen die
 * JMSzugriffe
 * 
 * @author janko
 */
public class CommBean extends AbstractAgentBean implements IEffector {
	Log log = LogFactory.getLog(getClass());

	// über den Communicator läuft die JMS communication
	QueueCommunicator _communicator;
	// eigene Adresse
	IEndPoint _address;

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
		
		if (_connectionFactory == null) throw new Exception("NullPointer Exception: No ConnectionFactory Set!");
	
		TopicReceiver topicReceiver;
		TopicSender topicSender;
		
		if (thisAgent != null && thisAgent.getAgentNode() != null) {
			setAgentNodeName(thisAgent.getAgentNode().getName());
		}
		_address = (EndPoint) EndPointFactory.createEndPoint(getAgentNodeName());
		memory.update(new ThisAgentDescription(null, null, null, null), new ThisAgentDescription(null, null, null, _address));

		_topicCommunicator = new TopicCommunicator();
		// Ein topic wird verwendet, zum lesen und schreiben - das defaultTopic
		if (_defaultTopicName != null){
			topicReceiver = new TopicReceiver(this, _connectionFactory, _defaultTopicName);
			topicSender = new TopicSender(_connectionFactory, _defaultTopicName);
		} else {
			throw new Exception("No Default Topic Name Set!");
		}

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
	 * {@inheritDoc}
	 */
	@Override
	public void doStart() throws Exception {
		super.doStart();
		Set<Action> actions = memory.readAll(new Action(null, null, null, null));
		for (Action action:actions) {
			System.out.println("CommBean::Found Action: "+action.getName());
		}
		ArrayList<IAgent> agents = thisAgent.getAgentNode().findAgents();
		for (IAgent agent:agents) {
			System.out.println("CommBean::Found Agent: "+agent.getAgentName());
		}
		ThisAgentDescription tad=memory.read(new ThisAgentDescription(null, null, null, null));
		OtherAgentDescription descr=new OtherAgentDescription(tad);
		
		//Addvertise all actions to other agents of this node
		//TODO filter those actions that must not be distributed
		//TODO advertise actions to agents on other nodes
		for (IAgent agent:agents) {
			if (!descr.getAid().equals(agent.getAgentDescription().getAid())) {
				System.out.println("Sending from "+descr.getAid()+" to "+agent.getAgentDescription().getAid());
				for (Action action:actions) {
	//				send(operation, payload, receiverAddress)
					log.debug("Sending "+IAgentProtocol.ADV_REMOTE_ACTION+" to "+agent.getAgentDescription().getEndpoint());
					
					JiacMessage message = new JiacMessage(
							IAgentProtocol.ADV_REMOTE_ACTION,
							new RemoteAction(action, descr),
							agent.getAgentDescription().getEndpoint(),
							descr.getEndpoint(),
							null);
					
					send(message, agent.getAgentDescription().getEndpoint().toString());
				}
			}
		}
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
			((AgentProtocol) protocol).setMemory(memory);
			((AgentProtocol) protocol).setCommBean(this);
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
	 * @deprecated
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
	 * @deprecated
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
		for (Iterator<CommMessageListener> iter = _commListener.iterator(); iter.hasNext();) {
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
		for (Iterator<CommMessageListener> iter = _commListener.iterator(); iter.hasNext();) {
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

	/**
	 * {@inheritDoc}
	 */
	public void doAction(DoAction doAction) {
		if (doAction.getAction().getName().equals("DoRemoteAction")) {
			// do nothing at the moment
			log.debug("DoRemoteAction");
//			DoRemoteAction raction = new DoRemoteAction((DoAction)doAction.getParams()[0]);
//			IEndPoint address = ((AgentDescription)doAction.getParams()[1]).getEndpoint();
//			send(IAgentProtocol.CMD_AGT_REMOTE_DOACTION, raction, address);
//			doAction.getSession().addToSessionHistory(this);
		} else {
			RemoteAction ra = memory.read(new RemoteAction(doAction.getAction(), null));
			if (ra != null) {
				log.debug("Found RemoteAction "+ra.getAction().getName()+" provisioned by "+ra.getAgentDescription().getName());
			} else {
				log.error("Cannot find RemoteAction for DoAction "+doAction.getAction().getName());
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<Action> getActions() {
		Class[] params = new Class[2];
		params[0] = DoAction.class;
		params[1] = AgentDescription.class;
		
		Class[] results = new Class[1];
		results[0] = ActionResult.class;
		ArrayList<Action> actions = new ArrayList<Action>();
		Action doRemoteAction = new Action(
				"DoRemoteAction",
				this,
				params,
				results
		);
		actions.add(doRemoteAction);
		return actions;
	}

}
