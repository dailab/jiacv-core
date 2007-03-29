package de.dailab.jiactng.agentcore.communication.jms;

import de.dailab.jiactng.agentcore.communication.ISendingEndpoint;
import de.dailab.jiactng.agentcore.communication.TNGMessage;
import java.io.Serializable;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import org.apache.commons.logging.Log;

/**
 * @author Joachim Fuchs
 */
public class ActiveMQSender extends AbstractActiveMQEndpoint implements ISendingEndpoint {
    
    protected int deliveryMode = DeliveryMode.NON_PERSISTENT;
    
    public void setPersistentDelivery(boolean b) {
        
        if (b == true) {
            
            this.deliveryMode = DeliveryMode.PERSISTENT;
            
        } else {
            
            this.deliveryMode = DeliveryMode.NON_PERSISTENT;
            
        }
        
    }
    
    public boolean isPersistentDelivery() {
        
        return (this.deliveryMode == DeliveryMode.PERSISTENT?true:false);
        
    }
    
    /**
     * @todo add reply handling (MEP based: tng.mep)
     */
    public void send(TNGMessage tngMessage) throws JmsCommunicationException {
        
        if (started == false) {
            
            throw new IllegalStateException(this + " is not started");
            
        }
        
        Destination destination = null;
        
        try {
                                    
            destination = session.createQueue(tngMessage.getDestination());

            if (log.isDebugEnabled()) {
                
                log.debug("using destination: " + destination);
                
            }
            
        } catch (JMSException je) {
            
            throw new JmsCommunicationException("Failed to create destination", je);
            
        }
        
        ObjectMessage message = null;
        
        try {
            
            message = session.createObjectMessage((Serializable) tngMessage.getContent());
            message.setJMSDestination(destination);
            
            for (String s : tngMessage.getMessageProperties().keySet()) {
                
                message.setObjectProperty(s, tngMessage.getMessageProperties().get(s).toString());
                
            }
            
        } catch (JMSException je) {
            
            throw new JmsCommunicationException("Failed to create message", je);
            
        }
        
        try {
            
            MessageProducer producer = session.createProducer(destination);
            producer.setDeliveryMode(deliveryMode);
            producer.send(message);
            
        } catch (JMSException je) {
            
            throw new JmsCommunicationException("Failed to send message", je);
            
        }
        
    }
    
    // ------------------------------------ PROPERTY SETTER --------------------
    
    public void setLog(Log log) {
        
        this.log = log;
        
    }
    
    public void setBrokerUsername(String username) {
        
        this.brokerUsername = username;
        
    }
    
    public void setBrokerPassword(String password) {
        
        this.brokerPassword = password;
        
    }
    
    public void setBrokerUrl(String url) {
        
        this.brokerUrl = url;
        
    }
    
    public String toString() {
        
        return "ActiveMQSender[" + id + "]";
        
    }
    
}
