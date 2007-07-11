/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.comm.Constants;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 * @version $Revision$
 */
class JMSSender {
    private final Log _log;
    private final IJMSMessageDelegate _delegate;
    
    private ConnectionFactory _connectionFactory;
//    private Destination _defaultReplyTo = null;

    private Session _session = null;
    private Connection _connection = null;
    
    private int _defaultTimeOut = 0;

    public JMSSender(ConnectionFactory connectionFactory, IJMSMessageDelegate delegate, Log log) throws JMSException {
        _log= log;
        _delegate= delegate;
        _connectionFactory = (ConnectionFactory)connectionFactory;
        doInit();
    }
    
    private void doInit() throws JMSException {
        _log.debug("JMSSender:: initialise");
        _connection = _connectionFactory.createConnection();
        _connection.setExceptionListener(_delegate);
        _session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
    
    public void doCleanup() {
        try {
            try {_session.close();} catch(JMSException je){}
            try {_connection.close();} catch(JMSException je){}
        } finally {
            _session= null;
            _connection= null;
            _connectionFactory= null;
        }
    }
    
    protected MessageProducer createProducer(Destination destination) {
        MessageProducer producer = null;
        try {
            producer = _session.createProducer(destination);
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return producer;
    }
    
    
//    public Destination getReplyToDestination() {
//        return _defaultReplyTo;
//    }
//    
//    public void setDefaultReplyTo(Destination destination) {
//        _defaultReplyTo = destination;
//    }

    public void send(IJiacMessage message, JMSCommunicationAddress address) throws JMSException {
        Destination destination = address.convertToDestination(_session);
        sendMessage(message, destination, _defaultTimeOut);
    }

    /*
     * the real sendMessage :-)
     */
    public void sendMessage(IJiacMessage message, Destination destination, long timeToLive) throws JMSException {
        _log.debug("JMSSender:: sending message to " + destination);
        
//        if (replyToDestination == null){
//            replyToDestination = _defaultReplyTo;
//            if (message.getStartPoint() != null){
//                try {
//                    replyToDestination = _session.createQueue(message.getStartPoint().toString());
//                } catch (JMSException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        log.debug("using replytoDestination " + replyToDestination);
        
        _log.debug("JMSSender:: begin of sending procedure... now!");
        MessageProducer producer = null;
        try {
            producer = createProducer(destination);
            producer.setTimeToLive(timeToLive);
            ObjectMessage objectMessage = _session.createObjectMessage(message);
            objectMessage.setStringProperty(Constants.ADDRESS_PROPERTY, message.getJiacDestination());
            System.out.println(Constants.ADDRESS_PROPERTY + "===" + message.getJiacDestination());
 
//            objectMessage.setJMSReplyTo(replyToDestination);
            objectMessage.setJMSDestination(destination);

            producer.send(objectMessage);
        } finally {
            try {
                // TODO figure out whether we can use a blank (with no default destination) MessageProducer without closing it
                producer.close();
            } catch (JMSException jmse) {
                _log.warn("JMSSender:: exception occured while closing producer", jmse);
            }
        }
        _log.debug("JMSSender:: sending procedure done");
    }
}
