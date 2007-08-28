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
	private final Log _log;
	
	private ConnectionFactory _connectionFactory;

	private Session _session = null;
	private Connection _connection = null;
	
	private int _defaultTimeOut = 1000;

	public JMSSender(ConnectionFactory connectionFactory, Log log) throws JMSException {
		_connectionFactory = (ConnectionFactory)connectionFactory;
        _log= log;
		doInit();
	}
	
	public void doInit() throws JMSException {
		if (_log.isDebugEnabled()){
			_log.debug("JMSSender is initializing...");
		}
		_connection = _connectionFactory.createConnection();
		_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		_connection.start();
        if (_log.isDebugEnabled()){
        	_log.debug("JMSSender initialized.");
        }
	}
	
	public void doCleanup() throws JMSException {
		if(_log.isDebugEnabled()){
			_log.debug("JMSSender is commencing cleanup...");
		}
		_session.close();
		_connection.close();
        if (_log.isDebugEnabled()){
        	_log.debug("JMSSender cleaned up.");
        }
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
		if (_log.isDebugEnabled()){
			_log.debug("JMSSender start sending message to '" + destination + "'");
		}
		MessageProducer producer = null;

		producer = _session.createProducer(destination);
		producer.setTimeToLive(timeToLive);
        
        if (_log.isDebugEnabled()){
        	_log.debug("pack message");
        }
        Message jmsMessage= JMSMessageTransport.pack(message, _session);
        jmsMessage.setJMSDestination(destination);
		producer.send(jmsMessage);
		producer.close();
        if (_log.isDebugEnabled()){
        	_log.debug("JMSSender sent Message to '" + destination + "'");
        }
	}
}
