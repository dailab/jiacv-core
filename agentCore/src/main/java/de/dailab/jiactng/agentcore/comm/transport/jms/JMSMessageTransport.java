package de.dailab.jiactng.agentcore.comm.transport.jms;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.BinaryContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;

/**
 * Die CommBean beinhaltet einen JiacSender und einen JiacReceiver.
 * Der Sender 	�bernimmt das verschicken von Nachrichten welche dem IJiacMessage 
 * 				Interface entsprechen. Dazu wird auf einen JMS Broker zur�ckgegriffen.
 * Der Receiver	erm�glicht es mittels MessageListeners an JMS Destinations zu lauschen.
 * 				Der Empfang verl�uft dabei asynchron und Eventgetriggert.
 * 
 * @author Janko, Loeffelholz
 */


public class JMSMessageTransport extends MessageTransport {
    private final static String SENDER_KEY= "JiacTNG-sender-address";
    
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
        ICommunicationAddress sender= CommunicationAddressFactory.createFromURI(message.getStringProperty(SENDER_KEY));

        IJiacMessage result= new JiacMessage(payload, sender);
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
        
        result.setStringProperty(SENDER_KEY, message.getSender().toURI().toString());
        
        for(String key : message.getHeaderKeys()) {
            result.setStringProperty(key, message.getHeader(key));
        }
        
        return result;
    }
    
	Log log = LogFactory.getLog(getClass());
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
	 * Initialisiert die CommBean. Notwendige Parameter: ConnectionFactory, AgentNodeName
	 */
	@Override
	public void doInit() throws Exception {
		log.debug("doInit");
	
		if (getConnectionFactory() == null) throw new Exception("NullPointer Exception: No ConnectionFactory Set!");
		
		_sender = new JMSSender(_connectionFactory);
		_receiver = new JMSReceiver(_connectionFactory, this);
		log.debug("doneInit");
		
	}
	
	public void doCleanup() {
		log.debug("doCleanup");
        try {_receiver.doCleanup();} catch (Exception e) {log.warn("cleaned up receiver", e);}
		try {_sender.doCleanup();} catch (Exception e) {log.warn("cleaned up sender", e);}
		log.debug("doneCleanup");
	}

	/*
	 * U S E I N G     T H E      S E N D E R
	 */

	/**
	 * 
	 * @param message 	a JiacMessage
	 * @param commAdd 	a CommunicationAddress, which might be a GroupAddress or
	 * 					a MessageBoxAddress
	 */
	public void send(IJiacMessage message, ICommunicationAddress commAdd) throws CommunicationException {
        try {
            _sender.send(message, commAdd);
        } catch (JMSException jms) {
            throw new CommunicationException("error while sending message", jms);
        }
	}
	
	/*
	 * U S E I N G       T H E      R E C E I V E R
	 */

	/**
	 * Initialisiert einen neuen Consumer f�r die gegebenene destination und h�ngt den gegebenen Listener dran.
	 * 
	 * @param listener the MessageListener used to get onto the messages
	 * @param destinationName the Name of the destination from which the Messages will be sent
	 * @param topic    is the destination to listen on a topic? (true/false)
	 */
	public void listen(ICommunicationAddress address, String selector) throws CommunicationException {
        try {
            _receiver.listen(address, selector);
        } catch (JMSException jms) {
            throw new CommunicationException("error while registrating", jms);
        }
	}
	
	/**
	 * Stops receivment of the Messages from a given destination by removing the consumer
	 * aligned to it from the consumerlist (especially useful for temporaryDestinations)
	 * 
	 * @param destinationName	the name of the destination we don't want to listen anymore to
	 * @param selector			a selector to recieve only special messages
	 */
	public void stopListen(ICommunicationAddress address, String selector) { 
		_receiver.stopListen(address, selector);
	}
    
	public ConnectionFactory getConnectionFactory() {
		return _connectionFactory;
	}

	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		_connectionFactory = connectionFactory;
	}
}
