package de.dailab.jiactng.agentcore.comm.transport.jms;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.BinaryContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * The JMSMessageTransports holds a JMSReceiver and a JMSSender.
 * All Messages received from any of the listeners will be delegated
 * to the defaultDelegate set to this transport.
 * 
 * 
 * Notes: 
 * A defaultDelegate should be set with setDefaultDelegate before using it.
 * 
 * @author Janko, Loeffelholz
 */
public class JMSMessageTransport extends MessageTransport {

	private ConnectionFactory _connectionFactory;
	private Connection _connection;
	private JMSSender sender;
	private JMSReceiver receiver;

	/**
	 * Creates a JMS message transport with the transport identifier "jms".
	 * @see #JMSMessageTransport(String)
	 */
	public JMSMessageTransport() {
		this("jms");
	}

	/**
	 * Creates a JMS message transport with a given transport identifier.
	 * @param transportIdentifier the transport identifier
	 * @see MessageTransport#MessageTransport(String)
	 */
    public JMSMessageTransport(String transportIdentifier) {
        super(transportIdentifier);
    }

    /**
	 * Initializes the JMSMessageTransport. 
	 * Notes: ConnectionFactory needed!
	 * @throws Exception if no logger or connection factory is set, or if the creation of the JMS sender or receiver failed. 
	 * @see {@link JMSSender#JMSSender(ConnectionFactory, org.apache.commons.logging.Log)}
	 * @see JMSReceiver#JMSReceiver(ConnectionFactory, JMSMessageTransport, org.apache.commons.logging.Log)
	 */
	@Override
	public synchronized void doInit() throws Exception {
		
		if (log == null) {
			throw new Exception("logging was not set!");
		}
		
		if (log.isDebugEnabled()) {
			log.debug("JMSMessageTransport initializing...");
		}
	
		if (_connectionFactory == null) {
			throw new Exception("NullPointer Exception: No ConnectionFactory Set!");
		}
		
		_connection= _connectionFactory.createConnection();
		
		sender = new JMSSender(_connection, createChildLog("sender"));
		receiver = new JMSReceiver(_connection, this, createChildLog("receiver"));
		if (log.isDebugEnabled()){
			log.debug("JMSMessageTransport initialized");
		}
		
		_connection.start();
	}
	
	
	/**
	 * cleans up the JMSMessageTransports and the classes it holds
	 */
	public synchronized void doCleanup() {
		if (log.isDebugEnabled()){
			log.debug("JMSMessageTransport commences Cleanup");
		}
		
		try {
            _connection.stop();
        } catch (JMSException e) {
            log.warn("could not stop JMS connection ", e);
        }
		
        try {
            _connection.close();
        } catch (Exception e) {
            log.warn("could not close connection ", e);
        }
        
        _connection= null;
		if (log.isDebugEnabled()){
			log.debug("JMSMessageTransport cleaned up");
		}
	}

	
	
	/**
     * Retrieves JiacMessages from JMSMessages
     * 
     * @param message	a JMSMessage received
     * @return			the JiacMessage included within the JMSMessage
     * @throws JMSException
     */
    static IJiacMessage unpack(Message message) throws JMSException {
        IFact payload;
        if(message instanceof BytesMessage) {
            final int length= (int)((BytesMessage) message).getBodyLength();
            final byte[] data= new byte[length];
            ((BytesMessage) message).readBytes(data);
            payload= new BinaryContent(data);
        } else {
            payload= (IFact) ((ObjectMessage)message).getObject();
        }
        
        final IJiacMessage result= new JiacMessage(payload);
        for(final Enumeration<?> keys= message.getPropertyNames(); keys.hasMoreElements(); ) {
            final Object keyObj= keys.nextElement();
            
            if(keyObj instanceof String) {
                final String key= (String) keyObj;
                final Object valueObj= message.getObjectProperty(key);
                
                if(valueObj instanceof String) {
                    result.setHeader(key, (String)valueObj);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Puts a JiacMessage into a JMSMessage which could then be send using JMS
     * 
     * @param message	the JiacMessage to sent
     * @param session	a (JMS)session needed to create the message
     * @return	Message a JMS message to send over a JMS broker
     * @throws JMSException
     */
    static Message pack(IJiacMessage message, Session session) throws JMSException {
    	final IFact payload= message.getPayload();
    	try {
//	        IFact payload= message.getPayload();
	        Message result;
	        
	        if(payload != null && payload instanceof BinaryContent) {
	            result= session.createBytesMessage();
	            ((BytesMessage)result).writeBytes(((BinaryContent)payload).getData());
	        } else {
	            result= session.createObjectMessage();
	            ((ObjectMessage)result).setObject(payload);
	        }
	        
	        for(String key : message.getHeaderKeys()) {
	            result.setStringProperty(key, message.getHeader(key));
	        }
	        
	        return result;
    	} catch(RuntimeException re) {
    		if(payload != null) {
    			System.out.println("\n\tund das is der payload: " + payload.getClass());
    			System.out.println("\terror message: " + re.getMessage() +"\n");
    		}
    		throw re;
    	}
    }
    

	/*
	 * U S E I N G     T H E      S E N D E R
	 */

	/**
	 * Sends the given JiacMessage
	 * If the timeout is reached, the message will be expired. Please consider that the clocks 
	 * of different hosts may run asynchronous!
	 * 
	 * @param message 	a JiacMessage
	 * @param commAdd 	a CommunicationAddress, which might be a GroupAddress or
	 * 					a MessageBoxAddress
     * @param ttl		the time-to-live of the message in milliseconds or 0 for using timeout specified by this message transport
	 * @throws CommunicationException if an error occurs while sending the message
	 */
	public void send(IJiacMessage message, ICommunicationAddress commAdd, long ttl) throws CommunicationException {
        if (log.isDebugEnabled()){
        	log.debug("JMSMessageTransport sends Message to address '" + commAdd.toUnboundAddress() + "'");
        }
		
		try {
            sender.send(message, commAdd, (ttl==0)? timeToLive:ttl);
        } catch (JMSException jms) {
        	if (log.isErrorEnabled()){
        		log.error("Sending of Message to address '" + commAdd.toUnboundAddress() + 
        				"' through JMS failed! Errorcause reads '" + jms.getCause() + "'");
        	}
        	throw new CommunicationException("error while sending message", jms);
        }
	}
	
	/*
	 * U S E I N G       T H E      R E C E I V E R
	 */

	/**
	 * Initializes a new listener for the given address and selector
	 * 
	 * @param address 	the address to listen to
	 * @param selector	if you want to get only special messages use this to select them
	 * @throws CommunicationException if an error occurs while creating the message listener
	 */
	public void listen(ICommunicationAddress address, IJiacMessage selector) throws CommunicationException {
        if (log.isDebugEnabled()){
        	log.debug("JMSMessageTransports starts to listen at '" + address.toUnboundAddress() 
        			+ "' with selector'" + selector + "'");
        }
		try {
            receiver.listen(address, selector);
        } catch (JMSException jms) {
        	if (log.isErrorEnabled()){
        		log.error("Listening to address '" + address.toUnboundAddress() + "' through JMS failed!");
        		log.error("Errorcause reads '" + jms.getCause() + "'");
        	}
            throw new CommunicationException("error while registrating", jms);
        }
	}
	
	/**
	 * Stops receiving messages from a given address by removing the listener
	 * aligned to it from the listener list (especially useful for temporaryDestinations).
	 * 
	 * @param address	the address you had listen to
	 * @param selector	the selector given with the address when you started to
	 * 					listen to it
	 */
	public void stopListen(ICommunicationAddress address, IJiacMessage selector) { 
		receiver.stopListen(address, selector);
	}
//
//	/**
//	 * Get the connection factory used for creating sender and receiver.
//	 * @return the connection factory
//	 */
//	public ConnectionFactory getConnectionFactory() {
//		return connectionFactory;
//	}

	/**
	 * Set the connection factory to be used for creating sender and receiver.
	 * @param newConnectionFactory the connection factory
	 */
	public void setConnectionFactory(ConnectionFactory newConnectionFactory) {
		_connectionFactory = newConnectionFactory;
	}
}
