package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.DoRemoteAction;
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

/**
 * Die CommBean beinhaltet einen JiacSender und einen JiacReceiver.
 * Der Sender 	übernimmt das verschicken von Nachrichten welche dem IJiacMessage 
 * 				Interface entsprechen. Dazu wird auf einen JMS Broker zurückgegriffen.
 * Der Receiver	ermöglicht es mittels MessageListeners an JMS Destinations zu lauschen.
 * 				Der Empfang verläuft dabei asynchron und Eventgetriggert.
 * 
 * @author Janko, Loeffelholz
 */


public class CommBeanV2 extends AbstractAgentBean implements IEffector {
	Log log = LogFactory.getLog(getClass());
	// Zur Zeit sind logs auskommentiert.
	
	int _timer = 200;
	int _timerCounter = 0;

	ConnectionFactory _connectionFactory;

	List<CommMessageListener> _commListener = new ArrayList<CommMessageListener>();

	// defaultmässig wird das BasicProkoll erzeugt
	String _protocolType = IProtocolHandler.BASIC_PROTOCOL;

	/* aus dem Namen wird die Adresse gebildet */
	String _agentNodeName;
	String _platformName;

	// eigene Adresse
	IEndPoint _address;
	IEndPoint _platformAddress;
	
	private int _sessionTracking = 0;
	private int _correlationNumber = 0;
	
	private JiacSender _sender;
	private JiacReceiver _receiver;
	
	
	public CommBeanV2() {
		super();
		log.debug("CommBeanV2 created");
	}

	/**
	 * Initialisiert die CommBean. Notwendige Parameter: AgentNodeName
	 */
	@Override
	public void doInit() throws Exception {
		log.debug("doInit");
		super.doInit();
	
		if (getConnectionFactory() == null) throw new Exception("NullPointer Exception: No ConnectionFactory Set!");
		if ((_agentNodeName == null) || (_agentNodeName == ""))
			throw new Exception("No AgentNodeName set!");
		
		if (thisAgent != null && thisAgent.getAgentNode() != null) {
			setAgentNodeName(thisAgent.getAgentNode().getName());
		}
		
		
		_address = (EndPoint) EndPointFactory.createEndPoint(getAgentNodeName());
		_platformName = _address.getUniversalId();
		_platformAddress = new EndPoint(_platformName, "PLATFORM");
		
		_sender = new JiacSender(_connectionFactory);
		_receiver = new JiacReceiver(_connectionFactory, this);
		log.debug("doneInit");
		
	}
	
	public void doCleanup(){
		log.debug("doCleanup");
		_receiver.doCleanup();
		_sender.doCleanup();
		log.debug("doneCleanup");
	}


	
	
	/*
	 * U S E I N G     T H E      S E N D E R
	 */
	
	
	
	
	/**
	 * Sends message to Queue of receiver-endpoint
	 * 
	 * @param message
	 */
	public void send(IJiacMessage message) {
		_sender.send(message);
	}

	/**
	 * sends message to destination, assuming it's a Queue 
	 * 
	 * @param message
	 * @param destinationName
	 */
	public void send(IJiacMessage message, String destinationName) {
		_sender.send(message, destinationName, false);
	}
	
	public void send(IJiacMessage message, Destination destination){
		_sender.send(message, destination);
	}
	
	/**
	 * sends message to destination considering if it's a topic or a queue
	 * 
	 * @param message
	 * @param destinationName
	 * @param topic is the Destination a topic? (true/false)
	 */
	public void send(IJiacMessage message, String destinationName, boolean topic) {
		_sender.send(message, destinationName, topic);
	}

	/**
	 * sends to given receiverAddress considering if it's a topic or not
	 * 
	 * @param operation
	 * @param payload
	 * @param receiverAddress	Endpoint to send to
	 * @param topic	is receiver a topic? (true/false)
	 */
	public void send(String operation, IJiacContent payload, IEndPoint receiverAddress, boolean topic) {
		IJiacMessage msg = new JiacMessage(operation, payload, receiverAddress, _address, null); 
		if (topic)
			publish(msg);
		else
			send(msg);
	}

	public void publish(IJiacMessage message) {
		_sender.publish(message);
	}

	public void publish(String operation, IJiacContent payload, IEndPoint receiverAddress) {
		IJiacMessage msg = new JiacMessage(operation, payload, receiverAddress, _address, null);
		publish(msg);
	}

	
	
	
	
	
	
	
	/*
	 * U S E I N G       T H E      R E C E I V E R
	 */
	
	/**
	 * Initialisiert einen neuen Consumer für die gegebenene destination und hängt den gegebenen Listener dran.
	 * 
	 * @param listener the MessageListener used to get onto the messages
	 * @param destinationName the Name of the destination from which the Messages will be sent
	 * @param topic    is the destination to listen on a topic? (true/false)
	 */
	public void receive(MessageListener listener, String destinationName, boolean topic, String selector) {
		_receiver.receive(listener, destinationName, topic, selector);
	}
	
	/**
	 * Initialisiert einen neuen Consumer für die gegebenene destination und hängt den gegebenen Listener dran.
	 * 
	 * @param listener the MessageListener used to get onto the messages
	 * @param destinationName the Name of the destination from which the Messages will be sent
	 * @param topic    is the destination to listen on a topic? (true/false)
	 */
	public void receive(MessageListener listener, Destination destination, String selector) {
		_receiver.receive(listener, destination, selector);
	}
	
	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist
	 * 
	 * @param destinationName	the name of the destination we don't want to listen anymore to
	 * @param topic				is this destionation a topic? (true/false)
	 * @param selector			a selector to recieve only special messages
	 */
	public void stopReceive(String destinationName, boolean topic, MessageListener listener, String selector){
		_receiver.stopReceive(destinationName, topic, listener, selector);
	}
	
	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist (especially useful for temporaryDestinations)
	 * 
	 * @param destinationName	the name of the destination we don't want to listen anymore to
	 * @param selector			a selector to recieve only special messages
	 */
	public void stopReceive(Destination destination, MessageListener listener, String selector){
		_receiver.stopReceive(destination, listener, selector);
	}
	
	/**
	 * Initialisiert einen neuen Consumer für eine temporäre Queue und hängt den gegebenen Listener dran. und gibt die
	 * erzeugte temporary Queue zurück.
	 * 
	 * @param listener	the Listener whom should get all the messages from this Destination
	 * @param selector	a selector to recieve only special messages
	 * @return die Queue auf die gesendet werden kann
	 */
	public TemporaryQueue receiveFromTemporaryQueue(MessageListener listener, String selector) {
		return _receiver.receiveFromTemporaryQueue(listener, selector);
	}
	
	/**
	 * Initialisiert einen neuen Consumer für eine temporäre Queue und hängt den gegebenen Listener dran. und gibt die
	 * erzeugte temporary Queue zurück.
	 * 
	 * @param listener	the Listener whom should get all the messages from this Destination
	 * @param selector	a selector to recieve only special messages
	 * @return die Queue auf die gesendet werden kann
	 */
	public TemporaryTopic receiveFromTemporaryTopic(MessageListener listener, String selector) {
		return _receiver.receiveFromTemporaryTopic(listener, selector);
	}
	
	
	
	
	
	
	/*
	 * M I S C E L L A N E O U S
	 */
	
	
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
			IJiacMessage msg = new JiacMessage(NodeProtocol.CMD_PING, content, getPlatformAddress(), getAddress(), null);
			// getPlatformAddress() statt null eingesetzt
			publish(msg);
			//log.debug(this.getBeanName() + ", " + thisAgent.getAgentName());
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
			IJiacMessage msg = new JiacMessage(NodeProtocol.CMD_PING, content, getPlatformAddress(), getAddress(), null);
			publish(msg);
			// //log.debug(this.getBeanName() + ", " + thisAgent.getAgentName());
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

	public IEndPoint getAddress() {
		return _address;
	}

	public void setAddress(EndPoint address) {
		_address = address;
	}
	
	public IEndPoint getPlatformAddress() {
		return _platformAddress;
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
			//log.debug("DoRemoteAction");
			DoRemoteAction raction = new DoRemoteAction((DoAction)doAction.getParams()[0]);
			IEndPoint address = ((AgentDescription)doAction.getParams()[1]).getEndpoint();
			send(IAgentProtocol.CMD_AGT_REMOTE_DOACTION, raction, address, false);
			doAction.getSession().addToSessionHistory(this);
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
	
	
	/*
	 * creates a running number for a new CorrelationID, counting up doing so
	 * although it's very impropable that there will be a single commbean sending
	 * as much messages as Interger.MAX_VALUE, history teaches to be prepared even
	 * for that event.
	 * @author Loeffelholz
	 */
	private int getCorrelationNumber(){
		if (_correlationNumber == Integer.MAX_VALUE -1)
			_correlationNumber = 0;
		return _correlationNumber++;
	}
	
	/*
	 * creates a new CorrelationID using the unique EndpointAdress of this Commbean
	 * and a running Number for each new ID
	 * @author Loeffelholz
	 */
	public String getNewCorrelationID(){
		return this.getAddress().toString() + ":" + this.getCorrelationNumber();
	}
	
	
	/*
	 * sets completely new SessionTracking mask
	 */
	public void setSessionTracking(int newSessionTracking){
		_sessionTracking = newSessionTracking;
	}
	
	/*
	 * gets current SessionTracking mask
	 */
	public int getSessionTracking(int newSessionTracking){
		return _sessionTracking;
	}
	
	/*
	 * adds SessionTracking mask to current mask
	 */
	public void addSessionTracking(int newSessionTracking){
		_sessionTracking = _sessionTracking | newSessionTracking;
	}
	
	/*
	 * removes parts of SessionTracking mask
	 */
	public void removeSessionTracking(int newSessionTracking){
		_sessionTracking = _sessionTracking & ~newSessionTracking;
	}
	
}
