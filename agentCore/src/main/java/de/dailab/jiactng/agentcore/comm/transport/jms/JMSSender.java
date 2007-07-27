package de.dailab.jiactng.agentcore.comm.transport.jms;

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
		_connectionFactory = (ConnectionFactory)connectionFactory;
		doInit();
	}
	
	public void doInit() throws JMSException {
		log.debug("doInit");
		_connection = _connectionFactory.createConnection();
		_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        log.debug("doneInit");
	}
	
	public void doCleanup() throws JMSException {
		log.debug("doCleanup");
		_session.close();
		_connection.close();
        log.debug("doneCleanup");
	}

	public void send(IJiacMessage message, ICommunicationAddress address) throws JMSException {
		Destination destination = null;
		if (address instanceof IGroupAddress) {
			destination = _session.createTopic(address.getName());
        } else {
			destination = _session.createQueue(address.getName());
        }
		
		sendMessage(message, destination, _defaultTimeOut);
	}
	
	private void sendMessage(IJiacMessage message, Destination destination, long timeToLive) throws JMSException {
        log.debug("start sending...");
		MessageProducer producer = null;

		producer = _session.createProducer(destination);
		producer.setTimeToLive(timeToLive);
        
        log.debug("pack message");
        Message jmsMessage= JMSMessageTransport.pack(message, _session);
        jmsMessage.setJMSDestination(destination);
		producer.send(jmsMessage);
		producer.close();
        log.debug("sending done");
	}
}
