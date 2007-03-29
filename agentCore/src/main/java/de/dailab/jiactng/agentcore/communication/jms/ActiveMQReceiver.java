package de.dailab.jiactng.agentcore.communication.jms;

import de.dailab.jiactng.agentcore.communication.IMessageReceiver;
import de.dailab.jiactng.agentcore.communication.IReceivingEndpoint;
import de.dailab.jiactng.agentcore.communication.TNGMessage;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import java.util.Enumeration;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

/**
 * @author Joachim Fuchs
 */
public class ActiveMQReceiver extends AbstractActiveMQEndpoint
        implements IReceivingEndpoint, MessageListener {
    
    protected IMessageReceiver messageReceiver = null;
    
    protected String destinationName = null;
    
    protected boolean publishSubscribe = false;
    
    protected MessageConsumer consumer = null;
    
    public void setDestinationName(String name) {
        
        this.destinationName = name;
        
    }
    
    public void setPublishSubscribe(boolean b) {
        
        this.publishSubscribe = b;
        
    }
    
    // ----------------------------------- LIFECYCLE ---------------------------
    
    public void doStart() throws LifecycleException {
        
        super.doStart();
        
        if (log.isDebugEnabled()) {
            
            log.debug("starting receiver for queue " + destinationName);
            
        }
        
        try {
            
            Destination destination = null;
            
            if (publishSubscribe == true) {
                
                destination = session.createTopic(destinationName);
                
            } else {
                
                destination = session.createQueue(destinationName);
                
            }
            
            consumer = session.createConsumer(destination);         
            
            registerMessageListener();
            
        } catch (JMSException je) {
            
            throw new LifecycleException(null, je);
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("receiver started");
            
        }
        
    }
    
    protected void registerMessageListener() throws JMSException {
        
        consumer.setMessageListener(this);
        
    }
    
    // ----------------------------------- JMS MESSAGE LISTENER ----------------
    
    public void onMessage(Message message) {
        
        if (this.started == false) {
            
            return;
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("onMessage() : " + message);
            
        }
        
        try {
            
//            registerMessageListener();
            
            Enumeration properties = message.getPropertyNames();
            
            TNGMessage tngMessage = new TNGMessage();
            
            // Extract content
            if (message instanceof ObjectMessage){
                
                //copy content from Objectmessage to TNGMessage
                ObjectMessage objMessage = (ObjectMessage) message;
                tngMessage.setContent((Object) objMessage.getObject());
                
                // copy properties from ObjectMessage to TNGMessage
                while(properties.hasMoreElements()){
                    
                    String name = (String)properties.nextElement();
                    Object value = objMessage.getObjectProperty(name);
                    tngMessage.setMessageProperty(name, value.toString());
                    
                }
                
            } else if (message instanceof TextMessage) {
                
                tngMessage.setContent(((TextMessage)message).getText());
                
            } else {
                
                log.warn("message of type " + message.getClass().getName() + 
                        " received, dropping it");
                
            }
            
            if (messageReceiver != null){
                
                messageReceiver.receiveMessage(tngMessage);
                
            }
                        
        } catch (JMSException e) {
            
            e.printStackTrace();
            
        } 
        
    }
    
    public String toString() {
        
        return "ActiveMQReceiver[" + id + "]";
        
    }
    
    public void setMessageReceiver(IMessageReceiver receiver) {
        
        this.messageReceiver = receiver;
        
    }
    
}
