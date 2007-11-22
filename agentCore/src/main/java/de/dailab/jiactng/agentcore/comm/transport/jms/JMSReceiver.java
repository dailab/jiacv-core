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

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.Selector;

/**
 * 
 * 
 * JMSReceiver is creating consumers for JMS-Destinations and delegates the messages up to its "parent" JMSReceiver is
 * hold by a JMSMessageTransport, which extends a MessageTransport. The delegate where the message will be handed over
 * to is set through a setter in the extended part of the class.
 * 
 * @author Loeffelholz
 * 
 */
class JMSReceiver {
    /**
     * @param address
     * @param selector
     * @return a standardized String Expression for address and selector
     */
    protected static String getStringRepresentation(ICommunicationAddress address, Selector selector) {
        StringBuilder result = new StringBuilder();
        result.append(address.toString());

        if (selector != null) {
            result.append('?').append(selector.getKey()).append('=').append(selector.getValue());
        }

        return result.toString();
    }

    /**
     * Helping class for organizing destinations. It holds the consumer, the addres, a selector and is stored within the
     * _listeners list, where it is mapped with a key depending on address and selector if selector != null.
     * 
     */
    protected class JMSMessageListener implements MessageListener {
        private final ICommunicationAddress _address;

        private final Selector _selector;

        private MessageConsumer _consumer;

        protected JMSMessageListener(ICommunicationAddress address, Selector selector) {
            _address = address;
            _selector = selector;
        }

        /**
         * if a message arrives it is handed over the the delegate in the superior MessageTransport.
         */
        public void onMessage(Message message) {
            try {
                /*
                 * By default JMS only delivers a message once per session. So it doesn't matter for which selector we
                 * received it, we won't get it through another consumer!
                 */

                if (log.isDebugEnabled()) {
                    log.debug("JMSReceiver receiving message... delegating message");
                }

                _parent.delegateMessage(JMSMessageTransport.unpack(message), _address);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("An error receiving a message occured. Cause is '" + e.getCause() + "'");
                }
                _parent.delegateException(e);
            }
        }

        /**
         * setting up consumer. This will be the listener to all consumers created here. if something goes wrong
         * creating the consumer, the consumer is destroyed and and an exception is thrown.
         * 
         * @param session
         *            the JMSSession used for creating the consumers
         * @throws JMSException
         */
        void initialise(Session session) throws JMSException {

            try {
                if (log.isDebugEnabled()) {
                    log.debug("Listener initializing for '" + _address.toString() + "' with '" + _selector + "'");
                }
                String add = _address.getName();
                Destination dest = _address instanceof IGroupAddress ? session.createTopic(add) : session
                        .createQueue(add);
                _consumer = session.createConsumer(dest, _selector == null ? null : (_selector.getKey() + " = '"
                        + _selector.getValue() + "'"));
                _consumer.setMessageListener(this);
            } catch (JMSException e) {
                if (log.isErrorEnabled()) {
                    log.error("Listener couldn't be initialized cause of '" + e.getCause() + "'");
                    log.error("Listener will be destroyed");
                }
                destroy();
                throw e;
            }

        }

        /**
         * cleans up the consumer.
         */
        void destroy() {
            if (_consumer != null) {
                try {
                    _consumer.close();
                } catch (JMSException e) {
                    if (log.isErrorEnabled()) {
                        log.error("Couldn't destroy consumer cause of '" + e.getCause() + "'");
                    }
                }
            }
        }
    }

    protected final Log log;

    private ConnectionFactory _connectionFactory;

    private Connection _connection;

    private Session _session;

    private JMSMessageTransport _parent;

    private Map<String, JMSMessageListener> _listeners;

    public JMSReceiver(ConnectionFactory connectionFactory, JMSMessageTransport parent, Log log) throws JMSException {
        _connectionFactory = (ConnectionFactory) connectionFactory;
        _parent = parent;
        this.log = log;
        _listeners = new HashMap<String, JMSMessageListener>();
        doInit();
    }

    /**
     * Initializes the JiacReceiver creating and starting session and connection. throws an JMSException if something
     * goes wrong doing so.
     */
    public synchronized void doInit() throws JMSException {
        if (log.isDebugEnabled()) {
            log.debug("JMSReceiver is initializing...");
        }
        _connection = _connectionFactory.createConnection();
        _session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        _connection.start();
        if (log.isDebugEnabled()) {
            log.debug("JMSReceiver initialized.");
        }
    }

    /**
     * commence Cleanup procedures, closing all consumers and connections.
     */
    public synchronized void doCleanup() throws JMSException {
        if (log.isDebugEnabled()) {
            log.debug("JMSReceiver commencing cleanup...");
        }
        stopListenAll();
        _session.close();
        _connection.close();
        if (log.isDebugEnabled()) {
            log.debug("JMSReceiver cleaned up");
        }
    }

    /**
     * Initializing a new Consumer using a given ICommunicationAddress
     * 
     * @param address
     *            the address that should be listen to.
     * @param selector
     *            if you just want these special messages.
     */
    public synchronized void listen(ICommunicationAddress address, Selector selector) throws JMSException {
        // first check if we already have a listener like that
        String key = getStringRepresentation(address, selector);
        if (_listeners.containsKey(key)) {
            if (log.isWarnEnabled()) {
                log.warn("there is already a listener for '" + key + "' registered");
            }
            return;
        }
        // then creating a listener if needed and map it with the others.
        JMSMessageListener listener = new JMSMessageListener(address, selector);
        listener.initialise(_session);
        _listeners.put(key, listener);
        if (log.isDebugEnabled()) {
            log.debug("Now listening to '" + key + "'");
        }
    }

    /**
     * Stops receivment of the Messages from a given destination by removing the consumer aligned to it from the
     * consumerlist (especially useful for temporaryDestinations)
     * 
     * @param address
     *            the communicationaddress we don't want to listen anymore to
     * @param selector
     *            the selector given while the listener was created in the first place
     */
    public synchronized void stopListen(ICommunicationAddress address, Selector selector) {
        String key = getStringRepresentation(address, selector);
        JMSMessageListener listener = _listeners.remove(key);

        if (listener != null) {
            listener.destroy();
        }
        if (log.isDebugEnabled()) {
            log.debug("stopped listening to address '" + address + "' with selector '" + selector + "'");
        }
    }

    /**
     * stops receiving Messages from all enlisted Destinations.
     */
    public synchronized void stopListenAll() {
        for (JMSMessageListener listener : _listeners.values()) {
            listener.destroy();
        }

        _listeners.clear();

        if (log.isDebugEnabled()) {
            log.debug("Stopped Listening to all JMS Addresses");
        }
    }

}