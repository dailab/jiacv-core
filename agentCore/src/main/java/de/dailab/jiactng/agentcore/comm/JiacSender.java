package de.dailab.jiactng.agentcore.comm;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.pool.ConnectionPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;


/**
 * JiacSender is used within the CommBean to send JMS Messages through an ActiveMQBroker
 * @author Loeffelholz
 *
 */
public class JiacSender implements IJiacSender{
	
	Log log = LogFactory.getLog(getClass());
	
	private ConnectionFactory _connectionFactory;
	private Destination _defaultReplyTo = null;

	private Session _session = null;
	private Connection _connection = null;
	private String _debugId;
	
	private int _defaultTimeOut = 0;

	public JiacSender(ConnectionFactory connectionFactory) {
		//log.debug("Creating JiacSender");
		_connectionFactory = (ConnectionFactory)connectionFactory;
		doInit();
	}
	
	public void doInit() {
		try {
			//log.debug("JiacSender initializing");
			System.out.println("JiacSender.init");
			_connection = _connectionFactory.createConnection();
			_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	
	private Queue createQueue(String queueName) {
		Queue queue = null;
		try {
			queue = _session.createQueue(queueName);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return queue;
	}
	
	private Topic createTopic(String topicName) {
		Topic topic = null;
		try {
			topic = _session.createTopic(topicName);
		} catch (JMSException e) {
			e.printStackTrace(System.err);
		}
		return topic;
	}
	
	protected MessageProducer createProducer(Destination destination){
		MessageProducer producer = null;
		try {
			producer = _session.createProducer(destination);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		return producer;
	}
	
	
	public Destination getReplyToDestination() {
		return _defaultReplyTo;
	}
	
	public void setDefaultReplyTo(Destination destination){
		_defaultReplyTo = destination;
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
			e.printStackTrace(System.err);
		}
	}

	public void publish(IJiacMessage message){
		try {
			sendMessage(message, null, null, true, _defaultTimeOut);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	/*
	 * JiacMessage, DestinationName
	 */
	public void send(IJiacMessage message, String destinationName, boolean topic) {
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
			e.printStackTrace(System.err);
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
			e.printStackTrace(System.err);
		}
	}

	public void publish(IJiacMessage message, Destination destination) {
		send(message, destination);
	}
	
	/*
	 * the real sendMessage :-)
	 */
	public void sendMessage(IJiacMessage message, Destination destination, Destination replyToDestination, boolean topic, long timeToLive) {
		// if parameterDestinations == null, try to get destinations from message
		// if messageDestinations == null too, use defaultValues
//		System.err.println("Sending Message to " + destination.toString());
		if (destination == null){
			if (message.getEndPoint() != null){
				if (topic)
					destination = createTopic(message.getEndPoint().toString());
				else
					destination = createQueue(message.getEndPoint().toString());
			}
		}
		//log.debug("Sending Message to " + destination);
		
		if (replyToDestination == null){
			replyToDestination = _defaultReplyTo;
			if (message.getStartPoint() != null){
				try {
					replyToDestination = _session.createQueue(message.getStartPoint().toString());
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
		//log.debug("using replytoDestination " + replyToDestination);
		
		//log.debug("Begin of sending procedure... now!");
		MessageProducer producer = null;
		try {
			producer = createProducer(destination);
			producer.setTimeToLive(timeToLive);
			ObjectMessage objectMessage = _session.createObjectMessage(message);
			objectMessage.setStringProperty(Constants.ADDRESS_PROPERTY, message.getJiacDestination());
			System.out.println(Constants.ADDRESS_PROPERTY + "===" + message.getJiacDestination());
 
			objectMessage.setJMSReplyTo(replyToDestination);
			objectMessage.setJMSDestination(destination);

			producer.send(objectMessage);
		} catch (JMSException e) {
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			Util.closeAll(null, null, producer, null);
		}
		//log.debug("Sending Procedure done");
	}
	
	
}
