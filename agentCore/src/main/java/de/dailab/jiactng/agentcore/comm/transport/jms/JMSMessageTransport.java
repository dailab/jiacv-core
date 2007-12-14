package de.dailab.jiactng.agentcore.comm.transport.jms;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.BinaryContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;

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

	// Zur Zeit sind logs auskommentiert.
	private ConnectionFactory _connectionFactory;
	private JMSSender _sender;
	private JMSReceiver _receiver;
	
	public JMSMessageTransport() {
		this("jms");
	}
    
    public JMSMessageTransport(String transportIdentifier) {
        super(transportIdentifier);
    }

    /**
	 * Initializes the JMSMessageTransport 
	 * Notes: ConnectionFactory needed!
	 */
	@Override
	public synchronized void doInit() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("JMSMessageTransport initializing...");
		}
	
		if (getConnectionFactory() == null) throw new Exception("NullPointer Exception: No ConnectionFactory Set!");
		
		_sender = new JMSSender(_connectionFactory, createChildLog("sender"));
		_receiver = new JMSReceiver(_connectionFactory, this, createChildLog("receiver"));
		if (log.isDebugEnabled()){
			log.debug("JMSMessageTransport initialized");
		}
		
	}
	
	
	/**
	 * cleans up the JMSMessageTransports and the classes it holds
	 */
	public synchronized void doCleanup() {
		if (log.isDebugEnabled()){
			log.debug("JMSMessageTransport commences Cleanup");
		}
        try {_receiver.doCleanup();} catch (Exception e) {log.warn("clean up receiver failed", e);}
		try {_sender.doCleanup();} catch (Exception e) {log.warn("clean up sender failed", e);}
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
        IJiacContent payload;
        if(message instanceof BytesMessage) {
            int length= (int)((BytesMessage) message).getBodyLength();
            byte[] data= new byte[length];
            ((BytesMessage) message).readBytes(data);
            payload= new BinaryContent(data);
        } else {
            payload= (IJiacContent) ((ObjectMessage)message).getObject();
        }
        
        IJiacMessage result= new JiacMessage(payload);
        for(Enumeration keys= message.getPropertyNames(); keys.hasMoreElements(); ) {
            Object keyObj= keys.nextElement();
            
            if(keyObj instanceof String) {
                String key= (String) keyObj;
                Object valueObj= message.getObjectProperty(key);
                
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
     * @param session	a (jms)session needed to create the message
     * @return	Message a jmsmessage to send over a jms broker
     * @throws JMSException
     */
    static Message pack(IJiacMessage message, Session session) throws JMSException {
        IJiacContent payload= message.getPayload();
        Message result;
        if(payload instanceof BinaryContent) {
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
    }
    

	/*
	 * U S E I N G     T H E      S E N D E R
	 */

	/**
	 * Sends the given JiacMessage
	 * 
	 * @param message 	a JiacMessage
	 * @param commAdd 	a CommunicationAddress, which might be a GroupAddress or
	 * 					a MessageBoxAddress
	 */
	public void send(IJiacMessage message, ICommunicationAddress commAdd) throws CommunicationException {
        if (log.isDebugEnabled()){
        	log.debug("JMSMessageTransport sends Message to address '" + commAdd.toUnboundAddress());
        }
		
		try {
            _sender.send(message, commAdd);
        } catch (JMSException jms) {
        	if (log.isErrorEnabled()){
        		log.error("Sending of Message to address '" + commAdd.toUnboundAddress() + "' through JMS failed!");
        		log.error("Errorcause reads '" + jms.getCause() + "'");
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
	 */
	public void listen(ICommunicationAddress address, IJiacMessage selector) throws CommunicationException {
        if (log.isDebugEnabled()){
        	log.debug("JMSMessageTransports starts to listen at '" + address.toUnboundAddress() 
        			+ "' with selector'" + selector + "'");
        }
		try {
            _receiver.listen(address, selector);
        } catch (JMSException jms) {
        	if (log.isErrorEnabled()){
        		log.error("Listening to address '" + address.toUnboundAddress() + "' through JMS failed!");
        		log.error("Errorcause reads '" + jms.getCause() + "'");
        	}
            throw new CommunicationException("error while registrating", jms);
        }
	}
	
	/**
	 * Stops receivment of the Messages from a given address by removing the listener
	 * aligned to it from the listenerlist (especially useful for temporaryDestinations)
	 * 
	 * @param address	the address you had listen to
	 * @param selector	the selector given with the address when you started to
	 * 					listen to it
	 */
	public void stopListen(ICommunicationAddress address, IJiacMessage selector) { 
		_receiver.stopListen(address, selector);
	}
    
	public ConnectionFactory getConnectionFactory() {
		return _connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		_connectionFactory = connectionFactory;
	}
}
