package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.GroupAddress;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.IJiacSender;
import de.dailab.jiactng.agentcore.comm.MessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;


/**
 * JiacSender is used within the CommBeanV2 to send JMS Messages through an ActiveMQBroker
 * @author Loeffelholz
 *
 */
public class JiacSender implements IJiacSender{
	
	Log log = LogFactory.getLog(getClass());
	
	private ConnectionFactory _connectionFactory;
	private String _defaultReplyToName = null;
	private Destination _defaultReplyToDestination = null;
	private boolean _defaultReplyToIsTopic = false;

	private Session _session = null;
	private Connection _connection = null;
	private String _debugId;
	
	private int _defaultTimeOut = 1000;

	public JiacSender(ConnectionFactory connectionFactory) {
		log.debug("Creating JiacSender");
		_connectionFactory = (ConnectionFactory)connectionFactory;
		doInit();
	}
	
	public void doInit() {
		log.debug("JiacSender.doInit");
		try {
			log.debug("JiacSender initializing");
			_connection = _connectionFactory.createConnection();
			_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception e) {
			log.error(e.getCause());
		}
	}
	
	public void doCleanup(){
		log.debug("JiacSender.doCleanup");
		try {
			_session.close();
			_connection.close();
		} catch (JMSException e) {
			log.error(e.getCause());
		}
	}
	
	
	private Queue createQueue(String queueName) {
		log.debug("creating Queue: " + queueName);
		Queue queue = null;
		try {
			queue = _session.createQueue(queueName);
		} catch (JMSException e) {
			log.error(e.getCause());
		}
		return queue;
	}
	
	private Topic createTopic(String topicName) {
		log.debug("creating Topic: " + topicName);
		Topic topic = null;
		try {
			topic = _session.createTopic(topicName);
		} catch (JMSException e) {
			log.error(e.getCause());
		}
		return topic;
	}
	
	protected MessageProducer createProducer(Destination destination){
		log.debug("creating MessageProducer for Destination: " + destination.toString());
		MessageProducer producer = null;
		try {
			producer = _session.createProducer(destination);
		} catch (JMSException e) {
			log.error(e.getCause());
		}
		return producer;
	}
	
	
	public ICommunicationAddress getReplyToDestination() {
		if (_defaultReplyToIsTopic){
			return new GroupAddress(_defaultReplyToName);
		} else {
			return new MessageBoxAddress(_defaultReplyToName);
		}
			
	}
	
	public void setDefaultReplyTo(ICommunicationAddress destination){
		if (destination instanceof IGroupAddress){
			_defaultReplyToIsTopic = true;
			_defaultReplyToName = destination.getAddress();
		} else {
			_defaultReplyToIsTopic = false;
			_defaultReplyToName = destination.getAddress();
		}
		
		try {
			if (_defaultReplyToIsTopic){
				_defaultReplyToDestination = _session.createTopic(_defaultReplyToName);
			} else {
				_defaultReplyToDestination = _session.createQueue(_defaultReplyToName);
			}
		} catch (JMSException e) {
			log.error(e.getCause());
		}

		
	}

	
	/**
	 * Verschickt per JMS eine JIAC-Nachricht.
	 * Sollte kein Empfänger oder Absender angegeben sein, werden die Lücken durch Defaults gefüllt.
	 * 
	 * @param message die Nachricht
	 */
	public void send(IJiacMessage message) {
				// tries to send the message
		try {
			sendMessage(message, null, null, false, _defaultTimeOut);
		} catch (Exception e) {
			log.error(e.getCause());
		}
	}

	public void publish(IJiacMessage message){
		try {
			sendMessage(message, null, null, true, _defaultTimeOut);
		} catch (Exception e) {
			log.error(e.getCause());
		}
	}
	
	/*
	 * JiacMessage, DestinationName
	 */
	public void send(IJiacMessage message, String destinationName, boolean topic) {
		log.debug("creating Destination: " + destinationName);
		Destination destination = null;
		if ((destinationName != null) || (destinationName != "")){
			if (topic)
				destination = createTopic(destinationName);
			else
				destination = createQueue(destinationName);
		}
		
		try {
			sendMessage(message, destination, null, topic, _defaultTimeOut);
		} catch (Exception e) {
			log.error(e.getCause());
		}
	}
	
	public void send(IJiacMessage message, String destinationName){
		send(message, destinationName, false);
	}
	
	public void publish(IJiacMessage message, String destinationName){
		send(message, destinationName, true);
	}
	

	/*
	 * IJiacMessage, Destination
	 */
	public void send(IJiacMessage message, Destination destination) {
		try {
			sendMessage(message, destination, null, (destination.getClass() == Topic.class), _defaultTimeOut);
		} catch (Exception e) {
			log.error(e.getCause());
		}
	}

	public void publish(IJiacMessage message, Destination destination) {
		send(message, destination);
	}
	
	/*
	 * the real sendMessage :-)
	 * 
	 * sends message to destination using replyToDestination as replyToAdress giving the message timeToLive in ms
	 * timeToLive = 0 means: no timeout
	 */
	public void sendMessage(IJiacMessage message, Destination destination, Destination replyToDestination, boolean topic, long timeToLive) {
		// if parameterDestinations == null, try to get destinations from message
		// if messageDestinations == null too, use defaultValues

		//		Aus neuer Nachrichtenversion ist kein Empfänger mehr zu entnehmen.
//		if (destination == null){
//			if (message.getSender() != null){
//				if (topic)
//					destination = createTopic(message.getSender().getAddress());
//				else
//					destination = createQueue(message.getSender().getAddress());
//			}
//		}
		if (destination == null){
			log.error("No sendTo Destination set! Couldn't sent message: " + message.toString());
			return;
		}
		log.debug("Sending Message to " + destination.toString());
		
		if (replyToDestination == null){
			replyToDestination = _defaultReplyToDestination;
			if (message.getSender() != null){
				try {
					replyToDestination = _session.createQueue(message.getSender().getAddress());
				} catch (JMSException e) {
					log.error(e.getCause());
				}
			}
		}
		log.debug("using replytoDestination " + replyToDestination);
		
		log.debug("Begin of sending procedure... now!");
		MessageProducer producer = null;

		try {
			producer = createProducer(destination);
			producer.setTimeToLive(timeToLive);
			ObjectMessage objectMessage = _session.createObjectMessage(message);
			objectMessage.setJMSReplyTo(replyToDestination);
			objectMessage.setJMSDestination(destination);

			producer.send(objectMessage);
			producer.close();
		} catch (JMSException e) {
			log.error(e.getCause());
		}
		log.debug("Sending Procedure done");
	}
	
	
}
