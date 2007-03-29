package de.dailab.jiactng.agentcore.communication.jms;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import org.apache.activemq.ConnectionClosedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Joachim Fuchs
 */
public abstract class AbstractActiveMQEndpoint extends AbstractLifecycle implements ExceptionListener {
    
    protected static int instanceCount = 0;
    
    protected Log log = LogFactory.getLog(getClass());
    
    protected int id = ++instanceCount;
    
    protected ConnectionFactory connectionFactory = null;
    
    protected Connection connection = null;
    
    protected Session session = null;
    
    protected String brokerUsername = null;
    
    protected String brokerPassword = null;
    
    protected String brokerUrl = null;
    
    protected boolean initialized = false;
    
    protected boolean started = false;
        
    public AbstractActiveMQEndpoint() {
        
        // loose lifecycle management
        super(false);
        
    }
    
    public void setLog(Log log) {
        
        this.log = log;
        
    }
    
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        
        this.connectionFactory = connectionFactory;

    }
    
    // --------------------------------------- LIFECYCLE -----------------------
    
    public void doInit() throws LifecycleException {                
        
        if (connectionFactory == null) {
            
            throw new LifecycleException("No connection factory set.");
            
        }
        
        try {
            
            if (this.brokerUsername == null) {
                
                connection = connectionFactory.createConnection();
                
            } else {
                
                connection = connectionFactory.createConnection(
                        brokerUsername, brokerPassword);
                
            }
            
            connection.setExceptionListener(this);
            
        } catch (JMSException je) {
            
            throw new LifecycleException(null, je);
            
        }
        
        initialized = true;
        
    }
    
    public void doStart() throws LifecycleException {
        
        if (initialized == false) {
            
            init();
            
        }
        
        try {
            
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            
        } catch (JMSException je) {
            
            throw new LifecycleException(null, je);
            
        }
        
        started = true;
        
    }
    
    public void doStop() throws LifecycleException {
        
        started = false;
        
        if (connection != null) {
            
            try {
                
                connection.close();
                
            } catch (ConnectionClosedException cce) {
                
                // an already closed connection is not a serious condition
                log.debug(cce);
                
            } catch (JMSException je) {
                
                throw new LifecycleException(null, je);
                
            }
            
        }
        
    }
        
    public void doCleanup() throws LifecycleException {
        
        session = null;
        connection = null;
        initialized = false;
        
    }
    
    // ---------------------------------- JMS EXCEPTION LISTENER ---------------
    
    public void onException(JMSException exception) {
        
        log.error(exception);
        
    }
    
}
