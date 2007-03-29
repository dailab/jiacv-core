package de.dailab.jiactng.agentcore.communication;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.communication.jms.ActiveMQReceiver;
import de.dailab.jiactng.agentcore.communication.jms.ActiveMQSender;
import de.dailab.jiactng.agentcore.communication.jms.JmsDestinationResolver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STARTED;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import java.io.Serializable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.jms.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple base class for communication with lookup support
 *
 * @author Joachim Fuchs
 */
public class CommunicationBean extends AbstractAgentBean implements IMessageReceiver, ILookUpListener {
    
    protected Log log = null;
    
    public final static int DEFAULT_THREADPOOL_SIZE = 10;
    
    protected int threadPoolSize = DEFAULT_THREADPOOL_SIZE;
    
    /**
     * If the thread pool size is set to a value < 1, a cached thread pool will
     * be used. Otherwise a fixed thread pool will be used with the given size.
     */
    protected ExecutorService threadPool = null;
    
    protected IDestinationResolver destinationResolver = null;
    
    protected ISendingEndpoint sender = null;
    
    protected IReceivingEndpoint receiver = null;
    
    protected ConnectionFactory connectionFactory = null;
    
    protected String localQueueName = null;
    
    protected String resolverTopicName = null;
    
    public CommunicationBean() {
        
        super(true);
        
    }
    
    public void setLog(Log log) {
        
        this.log = log;
        
    }
    
    public void setThreadPoolSize(int size) {
        
        if (size > 0) {
            
            this.threadPoolSize = size;
            
        }
        
    }
    
    public void setDestinationResolver(IDestinationResolver resolver) {
        
        this.destinationResolver = resolver;
        
    }
    
    public void setSender(ISendingEndpoint sender) {
        
        this.sender = sender;
        
    }
    
    public void setReceiver(IReceivingEndpoint receiver) {
        
        this.receiver = receiver;
        
    }
    
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        
        this.connectionFactory = connectionFactory;
        
    }
    
    public void setLocalQueueName(String name) {
        
        this.localQueueName = name;
        
    }
    
    public void setResolverTopicName(String name) {
        
        this.resolverTopicName = name;
        
    }
    
    public void doInit() throws LifecycleException {
        
//        this.log = thisAgent.getLog(this);
        this.log = LogFactory.getLog(getClass());
        
        if (this.log == null) {
            
            this.log = LogFactory.getLog(getClass());
            
        }
        
        // create the thread pool
        if (threadPoolSize > 0) {
            
            this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
            
        } else {
            
            this.threadPool = Executors.newCachedThreadPool();
            
        }
        
        if (destinationResolver != null) {
            
            if (destinationResolver instanceof ILifecycle) {
                
                ((ILifecycle)destinationResolver).init();
                
            }
            
        } else {
            
            destinationResolver = new JmsDestinationResolver();
            
            ((JmsDestinationResolver)destinationResolver).setConnectionFactory(
                    this.connectionFactory);
            ((JmsDestinationResolver)destinationResolver).setResolverTopicName(
                    this.resolverTopicName);
//            ((JmsDestinationResolver)destinationResolver).setLog(
//                    this.log);
            ((JmsDestinationResolver)destinationResolver).setLookUpListener(
                    this);
            ((JmsDestinationResolver)destinationResolver).init();
            
        }
        
        if (sender == null) {
            
            sender = new ActiveMQSender();
            
            ((ActiveMQSender)sender).setConnectionFactory(connectionFactory);
            ((ActiveMQSender)sender).init();
            
        } 
        
        if (receiver == null) {
            
            receiver = new ActiveMQReceiver();
            
            ((ActiveMQReceiver)receiver).setConnectionFactory(connectionFactory);
            ((ActiveMQReceiver)receiver).setDestinationName(localQueueName);            
            ((ActiveMQReceiver)receiver).init();
            
        } 
        
        receiver.setMessageReceiver(this);
        
//        if (destinationResolver instanceof JmsDestinationResolver) {
//         
//            ((JmsDestinationResolver)destinationResolver).setLog(log);
//            
//        }
//        
//        if (sender instanceof AbstractActiveMQEndpoint) {
//            
//            ((AbstractActiveMQEndpoint)sender).setLog(log);
//            
//        }
//        
//        if (receiver instanceof AbstractActiveMQEndpoint) {
//            
//            ((AbstractActiveMQEndpoint)receiver).setLog(log);
//            
//        }
        
    }
    
    public void doStart() throws LifecycleException {
        
        if (destinationResolver instanceof ILifecycle) {
            
            ((ILifecycle)destinationResolver).start();
            
        }
        
        if (receiver instanceof ILifecycle) {
            
            ((ILifecycle)receiver).start();
            
        }
        
        if (sender instanceof ILifecycle) {
            
            ((ILifecycle)sender).start();
            
        }
        
    }
    
    public void doStop() throws LifecycleException {
        
        if (destinationResolver instanceof ILifecycle) {
            
            ((ILifecycle)destinationResolver).stop();
            
        }
        
        if (sender instanceof ILifecycle) {
            
            ((ILifecycle)sender).stop();
            
        }
        
        if (receiver instanceof ILifecycle) {
            
            ((ILifecycle)receiver).stop();
            
        }
        
    }
    
    public void doCleanup() throws LifecycleException {
        
        if (destinationResolver instanceof ILifecycle) {
            
            ((ILifecycle)destinationResolver).cleanup();
            
        }
        
        threadPool.shutdown();
        
    }
    
    /**
     * Called from the tuple space callback
     *
     */
    public void process(Object object) {
        
        if (log.isDebugEnabled()) {
            
            log.debug("process(" + object + ")");
            
        }
        
        if (!getState().equals(STARTED)) {
            
            log.warn("process called but bean is not started, discarding object");
            return;
            
        }
        
        if (object == null) {
            
            throw new IllegalArgumentException("Argument is null.");
            
        }
        
        final TNGMessage message = createMessageFromObject(object);
        
        if (message != null) {
            
            threadPool.execute(new Runnable() {
                
                public void run() {
                    
                    processMessage(message);
                    
                }
                
            });
            
        }
        
    }
    
    protected TNGMessage createMessageFromObject(Object object) {
        
        TNGMessage result = null;
        
        if (object != null) { // null content is acceptable
            
            if (object instanceof TNGMessage) {
                
                result = (TNGMessage)result;
                
            } else {
                
                result = new TNGMessage();
                if (object instanceof Serializable) {
                    
                    result.setContent((Serializable)object);
                    
                } else {
                    
                    result.setContent(object.toString());
                    
                }
                
            }
            
        } else {
            
            result = new TNGMessage();
            
        }
        
        return result;
        
    }
    
    protected Object createObjectFromMessage(TNGMessage tngMessage) {
        
        return tngMessage.getContent();
        
    }
    
    protected void processMessage(TNGMessage message) {
        
        if (log.isDebugEnabled()) {
            
            log.debug("processMessage(" + message + ")");
            
        }
        
        try {
            
            destinationResolver.resolveDestination(message);
            
        } catch (ResolveException re) {
                    
            handleException(re, message);
            return;
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("sending...");
            
        }
        
        try {
                        
            sender.send(message);
            
        } catch (CommunicationException ce) {
            
            handleException(ce, message);
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("sent!");
            
        }
        
    }
    
    protected void handleException(Exception e, TNGMessage message) {
        
        log.error(message.toString(), e);
        
    }
    
    /**
     * When a message from outside is received, this method is responsible for
     * passing it on to the agent's space.
     */
    public void receiveMessage(TNGMessage message) {
        
        if (log.isDebugEnabled()) {
            
            log.debug("received from bus: " + message);
            
        }                
        
        Object obj = message.getContent();
        
        if (obj instanceof PingObject) {
         // DUMMY
            ((PingObject)obj).setReceivedFromNetwork();
            
        }
        
        memory.write((IFact)obj);
        
    }
    
    /**
     * @todo implement onLookUp()
     */
    public void onLookUp(LookUp lookUp) {
        
        if (log.isDebugEnabled()) {
            
            log.debug("do the lookup for : " + lookUp);
            
        }
        
        // check service name
        
        // check method name
        
        // check input types
        
        // check output type
        
        // -- dummy --
        
        if (log.isDebugEnabled()) {
            
            log.debug("dummy lookup w/ local queue!!!");
            
        }
        lookUp.setDestination(this.localQueueName);
        
    }
    
    public void execute() {
        
        PingObject ping = memory.read(new PingObject(null, false));
        
        if (ping != null) {
            
            ping.setReceivedFromSpace();
            if (log.isDebugEnabled()) {
                
                log.debug("ping received from space");
                
            }
            process(ping);
            
        } 
        
    }
    
}
