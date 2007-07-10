/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
class JMSReceiver {
    private class JMSMessageListener implements MessageListener {
        final String selector;
        final JMSCommunicationAddress address;
        private MessageConsumer _associatedConsumer;
        
        public JMSMessageListener(JMSCommunicationAddress address, String selector) {
            this.address= address;
            this.selector= selector;
        }

        public void onMessage(Message message) {
            JMSReceiver.this.onMessage(message, address, selector);
        }
        
        @Override
        public boolean equals(Object obj) {
            if(obj == null || !(obj instanceof JMSMessageListener)) {
                return false;
            }
            JMSMessageListener other= (JMSMessageListener) obj;
            if(!address.equals(other.address)) { 
                return false;
            }
            
            return selector == null ? other.selector == null : (other.selector == null ? false : selector.equals(other.selector));
        }

        void initialise(Session session) throws JMSException {
            _associatedConsumer= session.createConsumer(address.convertToDestination(session), selector);
            _associatedConsumer.setMessageListener(this);
        }
        
        void destroy() throws JMSException {
            try {
                _associatedConsumer.close();
            } finally {
                _associatedConsumer= null;
            }
        }
    }
    
    private final Log _log;
    private final IJMSMessageDelegate _delegate;
    private ConnectionFactory _connectionFactory;
    private Connection _connection;
    private Session _session;
    
    private List<JMSMessageListener> _listenerList = new ArrayList<JMSMessageListener>();

    public JMSReceiver(ConnectionFactory connectionFactory, IJMSMessageDelegate delegate, Log log) throws JMSException {
        _delegate= delegate;
        _log= log;
        _connectionFactory = (ConnectionFactory) connectionFactory;
        doInit();
    }

    private void doInit() throws JMSException {
        _log.debug("initialise JMSReceiver");
        _connection = _connectionFactory.createConnection();
        _session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        _connection.start();
    }
    
    public void doCleanup() throws JMSException {
        try {
            for(JMSMessageListener listener : _listenerList) {
                listener.destroy();
            }
            _session.close();
            _connection.close();
        } finally {
            _listenerList.clear();
            _session= null;
            _connection= null;
            _connectionFactory= null;
        }
    }
    
    public void receive(JMSCommunicationAddress address, String selector) throws JMSException {
        JMSMessageListener listener= new JMSMessageListener(address, selector);
        if(!_listenerList.contains(listener)) {
            listener.initialise(_session);
            _listenerList.add(listener);
        }
    }

    public void stopReceive(JMSCommunicationAddress address, String selector) throws JMSException {
        int index= _listenerList.indexOf(new JMSMessageListener(address, selector));
        if(index >= 0 && index < _listenerList.size()) {
            JMSMessageListener listener= _listenerList.remove(index);
            listener.destroy();
        }
    }
    
    public void onMessage(Message message, JMSCommunicationAddress address, String selector) {
        _log.debug("Message received from " + address);
        if ((message != null) && (message instanceof ObjectMessage)) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Object content = objectMessage.getObject();
                if (content instanceof IJiacMessage) {
                    IJiacMessage jiacMessage = (IJiacMessage) content;
                    _delegate.onMessage(jiacMessage, address, selector);
                }
                message.acknowledge();
            } catch (Exception e) {
                _delegate.onError(address, selector, e);
            }
        }
    }
}
