package de.dailab.jiactng.agentcore.comm.transport.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;

/**
 * 
 * JiacReceiver is a ServiceClass for CommBeanV2, offering functionality for managing messagelisteners
 * on JMS destinations. 
 * 
 * @author Loeffelholz
 *
 */
class JMSReceiver {
    protected static String getStringRepresentation(ICommunicationAddress address, String selector) {
        StringBuilder result= new StringBuilder();
        result.append(address.toString());
        
        if(selector != null) {
            result.append('?').append(selector);
        }
        
        return result.toString();
    }
    
    protected class JMSMessageListener implements MessageListener {
        private final ICommunicationAddress _address;
        private final String _selector;
        private MessageConsumer _consumer;
        
        protected JMSMessageListener(ICommunicationAddress address, String selector) {
            _address= address;
            _selector= selector;
        }
        
        public void onMessage(Message message) {
            log.debug("receiving message");
            try {
                _parent.delegateMessage(JMSMessageTransport.unpack(message), _address, _selector);
            } catch (Exception e) {
                _parent.delegateException(e);
            }
        }
        
        void initialise(Session session) throws JMSException {
            try {
                String add= _address.getName();
                Destination dest= _address instanceof IGroupAddress ? session.createTopic(add) : session.createQueue(add);
                _consumer= session.createConsumer(dest, _selector);
                _consumer.setMessageListener(this);
            } catch (JMSException e) {
                destroy();
                throw e;
            }
            
        }
        
        void destroy() {
            
        }
    }
    
	Log log = LogFactory.getLog(getClass());
	private ConnectionFactory _connectionFactory;
	private Connection _connection;
	private Session _session;
    private JMSMessageTransport _parent;
    private Map<String, JMSMessageListener> _listeners;
	
	public JMSReceiver(ConnectionFactory connectionFactory, JMSMessageTransport parent) throws JMSException {
		_connectionFactory = (ConnectionFactory) connectionFactory;
		_parent = parent;
        _listeners= new HashMap<String, JMSMessageListener>();
		doInit();
	}

	/**
	 * Initializes the JiacReceiver creating a MessageConsumer for receiving messages to the CommBean
	 */
	public void doInit() throws JMSException {
		log.debug("doInit");
		_connection = _connectionFactory.createConnection();
		_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		_connection.start();
		log.debug("doneInit");
	}
	
	/**
	 * commence Cleanup procedures, closing all consumers and connections.
	 */
	public void doCleanup() throws JMSException {
		log.debug("doCleanup");
		stopListenAll();
		_session.close();
		_connection.close();
        log.debug("doneCleanup");
	}
	
	/**
	 * Initializing a new Consumer using a given ICommunicationAddress
	 * 
	 * @param listener
	 * @param commAdd
	 * @param selector
	 */
	public synchronized void listen(ICommunicationAddress address, String selector) throws JMSException {
        // first check if we already have a listener like that
        String key= getStringRepresentation(address, selector);
        if(_listeners.containsKey(key)) {
            return;
        }
        
        JMSMessageListener listener= new JMSMessageListener(address, selector);
        listener.initialise(_session);
        _listeners.put(key, listener);
	}

	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist (especially useful for temporaryDestinations)
	 * 
	 * @param destinationName	the name of the destination we don't want to listen anymore to
	 * @param selector			a selector to recieve only special messages
	 */
	public synchronized void stopListen(ICommunicationAddress address, String selector){
        String key= getStringRepresentation(address, selector);
        JMSMessageListener listener= _listeners.remove(key);
        
        if(listener != null) {
            listener.destroy();
        }
	}
	
	/**
	 * stops receiving Messages from all enlisted Destinations except the Commbean itself.
	 */
	public synchronized void stopListenAll(){
        for(JMSMessageListener listener : _listeners.values()) {
            listener.destroy();
        }
        
        _listeners.clear();
	}	
}