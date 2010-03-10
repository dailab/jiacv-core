package de.dailab.jiactng.agentcore.comm.transport.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;


/**
 * JiacSender is used within the CommBeanV2 to send JMS Messages through an ActiveMQBroker
 * @author Loeffelholz
 *
 */
class JMSSender {
	private final Log log;
	
	private ConnectionFactory connectionFactory;

	private Session session = null;
	private Connection connection = null;

	public JMSSender(ConnectionFactory connectionFactory, Log log) throws JMSException {
		this.connectionFactory = connectionFactory;
        this.log= log;
		doInit();
	}
	
	public void doInit() throws JMSException {
		if (log.isDebugEnabled()){
			log.debug("JMSSender is initializing...");
		}
		connection = connectionFactory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		connection.start();
        if (log.isDebugEnabled()){
        	log.debug("JMSSender initialized.");
        }
	}
	
	public void doCleanup() throws JMSException {
		if(log.isDebugEnabled()){
			log.debug("JMSSender is commencing cleanup...");
		}
		session.close();
		connection.close();
        if (log.isDebugEnabled()){
        	log.debug("JMSSender cleaned up.");
        }
	}

	public void send(IJiacMessage message, ICommunicationAddress address, long timeToLive) throws JMSException {
		Destination destination = null;
		
		if (address instanceof IGroupAddress) {
			destination = session.createTopic(address.getName());
        } else {
			destination = session.createQueue(address.getName());
        }
		
		sendMessage(message, destination, timeToLive);
	}
	
	private void sendMessage(IJiacMessage message, Destination destination, long timeToLive) throws JMSException {
        final MessageProducer producer = session.createProducer(destination);
		producer.setTimeToLive(timeToLive);
        
        if (log.isDebugEnabled()){
        	log.debug("pack message");
        }
        final Message jmsMessage= JMSMessageTransport.pack(message, session);
        jmsMessage.setJMSDestination(destination);
		producer.send(jmsMessage);
		producer.close();
        if (log.isDebugEnabled()){
        	log.debug("JMSSender sent Message to '" + destination + "'");
        }
	}
}
