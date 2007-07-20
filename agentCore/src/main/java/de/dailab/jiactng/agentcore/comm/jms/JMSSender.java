package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;


/**
 * JiacSender is used within the CommBeanV2 to send JMS Messages through an ActiveMQBroker
 * @author Loeffelholz
 *
 */
class JMSSender {
	Log log = LogFactory.getLog(getClass());
	
	private ConnectionFactory _connectionFactory;

	private Session _session = null;
	private Connection _connection = null;
	
	private int _defaultTimeOut = 1000;

	public JMSSender(ConnectionFactory connectionFactory) throws JMSException {
		log.debug("Creating JMSSender");
		_connectionFactory = (ConnectionFactory)connectionFactory;
		doInit();
	}
	
	public void doInit() throws JMSException {
		log.debug("JMSSender initialising");
		_connection = _connectionFactory.createConnection();
		_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}
	
	public void doCleanup() throws JMSException {
		log.debug("JMSSender.doCleanup");
		_session.close();
		_connection.close();
	}

	public void send(IJiacMessage message, ICommunicationAddress address) throws JMSException {
		log.debug("creating Destination: " + address);
		Destination destination = null;
		if (address instanceof IGroupAddress) {
			destination = _session.createTopic(address.getAddress());
        } else {
			destination = _session.createQueue(address.getAddress());
        }
		
		sendMessage(message, destination, _defaultTimeOut);
	}
	
	/*
	 * the real sendMessage :-)
	 * 
	 * sends message to destination using replyToDestination as replyToAdress giving the message timeToLive in ms
	 * timeToLive = 0 means: no timeout
	 */
	public void sendMessage(IJiacMessage message, Destination destination, long timeToLive) throws JMSException {
		log.debug("Begin of sending procedure... now!");
		MessageProducer producer = null;

		producer = _session.createProducer(destination);
		producer.setTimeToLive(timeToLive);
        
        Message jmsMessage= JMSMessageTransport.pack(message, _session);
        jmsMessage.setJMSDestination(destination);
		producer.send(jmsMessage);
		producer.close();
		log.debug("Sending Procedure done");
	}
}
