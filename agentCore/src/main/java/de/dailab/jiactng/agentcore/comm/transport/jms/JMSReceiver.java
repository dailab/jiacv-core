package de.dailab.jiactng.agentcore.comm.transport.jms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * JMSReceiver is creating consumers for JMS-Destinations and delegates the messages up to its "parent" JMSReceiver is
 * hold by a JMSMessageTransport, which extends a MessageTransport. The delegate where the message will be handed over
 * to is set through a setter in the extended part of the class.
 * 
 * @author Loeffelholz
 * 
 */
class JMSReceiver implements MessageListener {
    /**
     * @param address
     * @param selector
     * @return a standardized String Expression for address and selector
     */
    protected static String getStringRepresentation(ICommunicationAddress address, String selector) {
        final StringBuilder result = new StringBuilder();
        result.append(address.toString());

        if (selector != null) {
        	  result.append('?').append(selector);
        }

        return result.toString();
    }
    
    protected static String templateToSelector (IJiacMessage jiacMessage) {
        if(jiacMessage == null) {
            return null;
        }
        
        final StringBuilder selector= new StringBuilder();
        final Set<String> keys= jiacMessage.getHeaderKeys();
        final String[] sortedKeys= keys.toArray(new String[keys.size()]);
        Arrays.sort(sortedKeys);
        
        for(String key : sortedKeys) {
            if(selector.length() > 0) {
                selector.append(" AND ");
            }
            
            selector.append(key).append(" = '").append(jiacMessage.getHeader(key)).append("'");
        }
        
        return selector.toString();
    }

    /**
     * Helping class for organizing destinations. It holds the consumer, the addres, a selector and is stored within the
     * _listeners list, where it is mapped with a key depending on address and selector if selector != null.
     * 
     */
   
    protected final Log log;

    private Session session;
    private JMSMessageTransport parent;
    private Map<String, MessageConsumer> consumers;

    public JMSReceiver(Connection connection, JMSMessageTransport parent, Log log) throws JMSException {
        this.parent = parent;
        this.log = log;
        session= connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        consumers = new HashMap<String, MessageConsumer>();
    }

    /**
     * Initializing a new Consumer using a given ICommunicationAddress
     * 
     * @param address
     *            the address that should be listen to.
     * @param selectorTemplate
     *            if you just want these special messages.
     */
    public synchronized void listen(ICommunicationAddress address, IJiacMessage selectorTemplate) throws JMSException {
        // first check if we already have a listener like that
        final String selector= templateToSelector(selectorTemplate);
        final String key = getStringRepresentation(address, selector);
        if (consumers.containsKey(key)) {
            if (log.isWarnEnabled()) {
                log.warn("there is already a listener for '" + key + "' registered");
            }
            return;
        }
        // then creating a listener if needed and map it with the others.
        final MessageConsumer consumer = initialiseConsumer(address, selector);
        consumers.put(key, consumer);
        if (log.isDebugEnabled()) {
            log.debug("Now listening to '" + key + "'");
        }
    }

    /**
     * Stops receiving messages from a given destination by removing the consumer aligned to it from the
     * consumer list (especially useful for temporaryDestinations)
     * 
     * @param address
     *            the communication address we don't want to listen anymore to
     * @param selector
     *            the selector given while the listener was created in the first place
     */
    public synchronized void stopListen(ICommunicationAddress address, IJiacMessage selectorTemplate) {
        final String selector= templateToSelector(selectorTemplate);
        final String key = getStringRepresentation(address, selector);
        final MessageConsumer consumer = consumers.remove(key);

        if (consumer != null) {
            destroyConsumer(consumer);
        }
        if (log.isDebugEnabled()) {
            log.debug("stopped listening to address '" + address + "' with selector '" + selector + "'");
        }
    }

    /**
     * stops receiving Messages from all enlisted Destinations.
     */
    public synchronized void stopListenAll() {
        for (MessageConsumer consumer : consumers.values()) {
            destroyConsumer(consumer);
        }

        consumers.clear();

        if (log.isDebugEnabled()) {
            log.debug("Stopped Listening to all JMS Addresses");
        }
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

            final Destination receivedAt = message.getJMSDestination();
            ICommunicationAddress at = null;
            if (receivedAt instanceof Topic){
            	final Topic topic = (Topic) receivedAt;
            	at = CommunicationAddressFactory.createGroupAddress(topic.getTopicName());
            } else {
            	final Queue queue = (Queue) receivedAt;
            	at = CommunicationAddressFactory.createMessageBoxAddress(queue.getQueueName());
            }
            
            parent.delegateMessage(JMSMessageTransport.unpack(message), at);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("An error receiving a message occured. Cause is '" + e.getCause() + "'");
            }
            parent.delegateException(e);
        }
    }
    
    /**
     * setting up consumer. This will be the listener to all consumers created here. if something goes wrong
     * creating the consumer, the consumer is destroyed and and an exception is thrown.
     * 
     * @param _session
     *            the JMSSession used for creating the consumers
     * @throws JMSException
     */
    private MessageConsumer initialiseConsumer(ICommunicationAddress address, String selector) throws JMSException {

    	MessageConsumer consumer = null;
    	
        try {
            if (log.isDebugEnabled()) {
                log.debug("Listener initializing for '" + address.toString() + "' with '" + selector + "'");
            }
            final String add = address.getName();
            final Destination dest = address instanceof IGroupAddress ? session.createTopic(add) : session
                    .createQueue(add);
            consumer = session.createConsumer(dest, selector == null ? null : selector);
            consumer.setMessageListener(this);
        } catch (JMSException e) {
            if (log.isErrorEnabled()) {
                log.error("Listener couldn't be initialized cause of '" + e.getCause() + "'");
                log.error("Listener will be destroyed");
            }
            destroyConsumer(consumer);
            throw e;
        }
        return consumer;
    }
    
    /**
     * cleans up the consumer.
     */
    void destroyConsumer(MessageConsumer consumer) {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (JMSException e) {
                if (log.isErrorEnabled()) {
                    log.error("Couldn't destroy consumer cause of '" + e.getCause() + "'");
                }
            }
        }
    }
}