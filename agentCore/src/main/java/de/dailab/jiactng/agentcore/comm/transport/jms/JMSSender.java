package de.dailab.jiactng.agentcore.comm.transport.jms;

import javax.jms.Connection;
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
	
	private final Session _session;
	private final MessageProducer _producer;

	public JMSSender(Connection connection, Log log) throws JMSException {
        this.log= log;
        _session= connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        _producer= _session.createProducer(null);
	}
	
	public void send(IJiacMessage message, ICommunicationAddress address, long timeToLive) throws JMSException {
		Destination destination = null;
		
		if (address instanceof IGroupAddress) {
			destination = _session.createTopic(address.getName());
        } else {
			destination = _session.createQueue(address.getName());
        }
		
		sendMessage(message, destination, timeToLive);
	}
	
	private void sendMessage(IJiacMessage message, Destination destination, long timeToLive) throws JMSException {
		_producer.setTimeToLive(timeToLive);
        
        if (log.isDebugEnabled()){
        	log.debug("pack message");
        }
        final Message jmsMessage= JMSMessageTransport.pack(message, _session);
        jmsMessage.setJMSDestination(destination);
		_producer.send(destination, jmsMessage);
        if (log.isDebugEnabled()){
        	log.debug("JMSSender sent Message to '" + destination + "'");
        }
	}
}
