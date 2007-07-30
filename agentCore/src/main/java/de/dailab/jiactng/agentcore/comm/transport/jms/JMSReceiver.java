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
 * 
 * JMSReceiver is creating consumers for JMS-Destinations and delegates the messages up to its "parent"
 * JMSReceiver is hold by a JMSMessageTransport, which extends a MessageTransport. 
 * The delegate where the message will be handed over to is set through a setter in the extended part of
 * the class.
 * 
 * @author Loeffelholz
 *
 */
class JMSReceiver {

	Log log = LogFactory.getLog(getClass());
	private ConnectionFactory _connectionFactory;
	private Connection _connection;
	private Session _session;
    private JMSMessageTransport _parent;
    private Map<String, JMSMessageListener> _listeners;
	
    /**
	 * @param address
	 * @param selector
	 * @return 	a standardized String Expression for address and selector
	 */
	protected static String getStringRepresentation(ICommunicationAddress address, String selector) {
        StringBuilder result= new StringBuilder();
        result.append(address.toString());
        
        if(selector != null) {
            result.append('?').append(selector);
        }
        
        return result.toString();
    }
    
    
    /**
     * Helping class for organizing destinations.
     * It holds the consumer, the addres, a selector and is stored 
     * within the _listeners list, where it is mapped with a key
     * depending on address and selector if selector != null.
     *
     */
    protected class JMSMessageListener implements MessageListener {
        private final ICommunicationAddress _address;
        private final String _selector;
        private MessageConsumer _consumer;
        
        protected JMSMessageListener(ICommunicationAddress address, String selector) {
            _address= address;
            _selector= selector;
        }
        /**
         * if a message arrives it is handed over the the delegate in the superior MessageTransport.
         */
        public void onMessage(Message message) {
            log.debug("receiving message");
            try {
                _parent.delegateMessage(JMSMessageTransport.unpack(message), _address, _selector);
            } catch (Exception e) {
                _parent.delegateException(e);
            }
        }
        
        
        /**
         * setting up consumer. This will be the listener to all consumers created here.
         * if something goes wrong creating the consumer, the consumer is destroyed 
         * and and an exception is thrown.
         * 
         * @param session	the JMSSession used for creating the consumers
         * @throws JMSException	
         */
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
        
        /**
         * cleans up the consumer.
         */
        void destroy() {
            if (_consumer != null){
            	try {
					_consumer.close();
				} catch (JMSException e) {
					log.error(e.getCause());
				}
            }
        }
    }
    
	
	public JMSReceiver(ConnectionFactory connectionFactory, JMSMessageTransport parent) throws JMSException {
		_connectionFactory = (ConnectionFactory) connectionFactory;
		_parent = parent;
        _listeners= new HashMap<String, JMSMessageListener>();
		doInit();
	}

	/**
	 * Initializes the JiacReceiver creating and starting session and connection.
	 * throws an JMSException if something goes wrong doing so.
	 */
	public synchronized void doInit() throws JMSException {
		log.debug("doInit");
		_connection = _connectionFactory.createConnection();
		_session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		_connection.start();
		log.debug("doneInit");
	}
	
	/**
	 * commence Cleanup procedures, closing all consumers and connections.
	 */
	public synchronized void doCleanup() throws JMSException {
		log.debug("doCleanup");
		stopListenAll();
		_session.close();
		_connection.close();
        log.debug("doneCleanup");
	}
	
	/**
	 * Initializing a new Consumer using a given ICommunicationAddress
	 * 
	 * @param address 	the address that should be listen to.
	 * @param selector	if you just want these special messages.
	 */
	public synchronized void listen(ICommunicationAddress address, String selector) throws JMSException {
        // first check if we already have a listener like that
        String key= getStringRepresentation(address, selector);
        if(_listeners.containsKey(key)) {
            return;
        }
        // then creating a listener if needed and map it with the others.
        JMSMessageListener listener= new JMSMessageListener(address, selector);
        listener.initialise(_session);
        _listeners.put(key, listener);
	}

	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist (especially useful for temporaryDestinations)
	 * 
	 * @param address	the communicationaddress we don't want to listen anymore to
	 * @param selector	the selector given while the listener was created in the first place
	 */
	public synchronized void stopListen(ICommunicationAddress address, String selector){
        String key= getStringRepresentation(address, selector);
        JMSMessageListener listener= _listeners.remove(key);
        
        if(listener != null) {
            listener.destroy();
        }
	}
	
	/**
	 * stops receiving Messages from all enlisted Destinations.
	 */
	public synchronized void stopListenAll(){
        for(JMSMessageListener listener : _listeners.values()) {
            listener.destroy();
        }
        
        _listeners.clear();
	}
	
	
}