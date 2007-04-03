package de.dailab.jiactng.agentcore.communication.jms.broker;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An embedded AMQ broker.
 * @todo add configuration support for e.g. multicast
 *
 * @author Joachim Fuchs
 */
public class ActiveMQBroker extends AbstractLifecycle {
    
    /**
     * The logger we use, if it is not set by DI, we create our own
     */
    protected Log log = null;
    
    /**
     * The URL the broker binds to
     */
    protected String brokerUrl = null;
    
    /**
     * The broker's name
     */
    protected String brokerName = null;
    
    /**
     * Wether or not the broker uses JMX
     */
    protected boolean brokerUseJmx = false;
    
    /**
     * Wether or not the broker uses persistence
     */
    protected boolean brokerUsePersistence = false;
    
    /**
     * The embedded broker we use if no other broker is running on our host machine
     */
    protected BrokerService broker = null;
    
    /**
     * The connector we use to connect to the broker
     */
    protected TransportConnector connector = null;
    
    public void setLog(Log log) {
        
        this.log = log;
        
    }
    
    public void setBrokerUrl(String brokerUrl) {
        
        this.brokerUrl = brokerUrl;
        
    }
    
    public void setBrokerName(String brokerName) {
        
        this.brokerName = brokerName;
        
    }
    
    public void setBrokerUseJmx(boolean b) {
        
        this.brokerUseJmx = b;
        
    }
    
    public void setBrokerUsePersistence(boolean b) {
        
        this.brokerUsePersistence = b;
        
    }
    
    public void doInit() throws Exception {
        
        if (this.log == null) {
            
            this.log = LogFactory.getLog(getClass());
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("initializing embedded broker");
            
        }
        
        broker = new BrokerService();
        broker.setBrokerName(brokerName);
        broker.setUseJmx(brokerUseJmx);
        broker.setPersistent(brokerUsePersistence);
        
        connector = broker.addConnector(brokerUrl);
        
        if (log.isDebugEnabled()) {
            
            log.debug("embedded broker initialized. url = " + brokerUrl);
            
        }
        
    }
    
    public void doStart() throws Exception {
        
        if (log.isDebugEnabled()) {
            
            log.debug("starting broker");
            
        }
        
        // start broker
        if (broker != null) {
            
            connector.start();
            broker.start();
            
            if (log.isDebugEnabled()) {
                
                log.debug("broker started");
                
            }
            
        } else {
            
            log.warn("no broker found to start");
            
        }
        
    }
    
    public void doStop() throws Exception {
        
        if (log.isDebugEnabled()) {
            
            log.debug("stopping broker");
            
        }
        
        // stop broker
        if (broker != null) {
            
            connector.stop();
            broker.stop();
            
            if (log.isDebugEnabled()) {
                
                log.debug("broker stopped");
                
            }
            
        }
        
    }
    
    public void doCleanup() throws Exception {
        
        if (log.isDebugEnabled()) {
            
            log.debug("cleaning up broker");
            
        }
        
        if (broker != null && connector != null) {

            broker.removeConnector(connector);
                            
        }
        
        broker = null;
        
        if (log.isDebugEnabled()) {
            
            log.debug("broker cleaned up");
            
        }
        
    }
    
    /**
     * dummy for tests only
     */
    public void springStart() throws Exception {
        
        init();
        start();
        
    }
    
    /**
     * dummy for tests only
     */
    public void springStop() throws Exception {
        
        stop();
        
        try {
            
            cleanup();
            
        } catch (LifecycleException le) {
            
            le.printStackTrace();
            
        }
        
    }
    
}
