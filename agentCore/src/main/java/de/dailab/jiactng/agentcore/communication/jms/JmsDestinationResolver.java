package de.dailab.jiactng.agentcore.communication.jms;

import de.dailab.jiactng.agentcore.communication.IDestinationResolver;
import de.dailab.jiactng.agentcore.communication.ILookUpListener;
import de.dailab.jiactng.agentcore.communication.LookUp;
import de.dailab.jiactng.agentcore.communication.ResolveException;
import de.dailab.jiactng.agentcore.communication.TNGMessage;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TemporaryTopic;
import org.apache.activemq.ConnectionClosedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Joachim Fuchs
 */
public class JmsDestinationResolver extends AbstractLifecycle
        implements IDestinationResolver, MessageListener, ExceptionListener {
    
    /**
     * Default timout for lookup requests in milliseconds
     */
    public final static int DEFAULT_TIMEOUT = 5000;
    
    public final static String DEFAULT_RESOLVER_TOPIC_NAME = "gossip-default";
    
    protected static long idCounter = 0;
    
    protected Log log = LogFactory.getLog(getClass());
    
    protected String resolverTopicName = DEFAULT_RESOLVER_TOPIC_NAME;
    
    protected int timeout = DEFAULT_TIMEOUT;
    
    protected ConnectionFactory connectionFactory = null;
    
    protected Connection connection = null;
    
    protected Session session = null;
    
    protected Destination resolverTopic = null;
    
    protected String username = null;
    
    protected String password = null;
    
    protected boolean transacted = false;
    
    protected int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
    
    protected MessageProducer producer = null;
    
    protected MessageConsumer consumer = null;
    
    protected final Object WAIT_LOCK = new Object();
    
    protected Map<Long, LookUp> lookUpMap = new HashMap<Long, LookUp>();
    
    protected ILookUpListener lookUpListener = null;
    
    protected boolean started = false;
    
    public void setLog(Log log) {
        
        this.log = log;
        
    }
    
    public void setResolverTopicName(String name) {
        
        this.resolverTopicName = name;
        
    }
    
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        
        this.connectionFactory = connectionFactory;
        
    }
    
    public void setUsername(String username) {
        
        this.username = username;
        
    }
    
    public void setPassword(String password) {
        
        this.password = password;
        
    }
    
    public void setTransacted(boolean b) {
        
        this.transacted = b;
        
    }
    
    public void setAcknowledgeMode(int mode) {
        
        this.acknowledgeMode = mode;
        
    }
    
    public void resolveDestination(TNGMessage message) throws ResolveException {
        
        if (log.isDebugEnabled()) {
            
            log.debug("do lookup for " + message);
            
        }
        
        // 1. create and populate lookup
        
        LookUp lookUp = new LookUp( ++idCounter );
        
        TemporaryTopic replyTopic = null;
        MessageConsumer replyConsumer = null;
        
        Timer timer = new Timer();
        
        try {
            
            if (log.isDebugEnabled()) {
                
                log.debug("resolvertopic = " + resolverTopic);
                
            }
            
//            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            MessageProducer mp = session.createProducer(resolverTopic);
            
            ObjectMessage request = session.createObjectMessage(lookUp);
//            request.setJMSDestination(resolverTopic);
            replyTopic = session.createTemporaryTopic();
            request.setJMSReplyTo(replyTopic);
            
            replyConsumer = session.createConsumer(replyTopic);
            replyConsumer.setMessageListener(this);
            
            lookUpMap.put(lookUp.getId(), lookUp);
            
            if (log.isDebugEnabled()) {
                
                log.debug("sending lookup");
                
            }
            
            mp.send(request);
            
            if (log.isDebugEnabled()) {
                
                log.debug("lookup sent");
                
            }
            
            timer.schedule(new TimeoutTask(), timeout);
            
            if (log.isDebugEnabled()) {
                
                log.debug("timer set");
                
            }
            
        } catch (JMSException je) {
            
            throw new ResolveException("Failed to send resolve request", je);
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("lookup sent and timer is set, wait now");
            
        }
        
        while (!hasResponse(lookUp) && !hasTimeout(lookUp)) {
            
            synchronized (WAIT_LOCK) {
                
                try {
                    
                    if (log.isDebugEnabled()) {
                        
                        log.debug("waiting...");
                        
                    }
                    
                    WAIT_LOCK.wait();
                    
                    if (log.isDebugEnabled()) {
                        
                        log.debug("woke up!");
                        
                    }
                    
                } catch (InterruptedException ie) {
                    
                    throw new ResolveException("Interrupted during wait.");
                    
                }
                
            }
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("after wait() loop");
            
        }
        
        lookUpMap.remove(lookUp.getId());
        timer.cancel();
        
        try {
            
            if (log.isDebugEnabled()) {
                
                log.debug("remove reply stuff...");
                
            }
            
            replyConsumer.close();
            replyTopic.delete();
            
        } catch (ConnectionClosedException cce) {
            
            log.warn(cce);
            
        } catch (JMSException je) {
            
            throw new ResolveException("Failed to clean up", je);
            
        }
        
        if (lookUp.getDestination() != null) {
            
            if (log.isDebugEnabled()) {
                
                log.debug("lookup success? " + (lookUp.getDestination() != null));
                
            }
            
            message.setDestination(lookUp.getDestination());
            
        } else {
            
            throw new ResolveException("Nothing found.");
            
        }
        
    }
    
    public void onMessage(Message msg) {
        
        if (log.isDebugEnabled()) {
            
            log.debug("onMessage(): " + msg);
            
        }
        
        if (msg instanceof ObjectMessage) {
            
            ObjectMessage message = (ObjectMessage)msg;
            
            try {
                
                Object content = message.getObject();
                
                if (content instanceof LookUp) {
                    
                    LookUp lookUp = (LookUp)content;
                    
                    if (this.resolverTopic.equals(msg.getJMSDestination())) {
                        
                        handleLookUp(msg, lookUp);
                        
                    } else {
                        
                        handleReply(lookUp);
                        
                    }
                    
                }
                
            } catch (JMSException je) {
                
                log.error(je);
                
            }
            
        }
        
//        if (msg instanceof ObjectMessage) {
//
//            Object content = null;
//            ObjectMessage objMessage = (ObjectMessage) msg;
//            Destination messageTargetDestination = null;
//
//            try {
//                content = objMessage.getObject();
//                messageTargetDestination = objMessage.getJMSDestination();
//            } catch (JMSException e) {
//                e.printStackTrace();
//            }
//
//            if (content instanceof LookUp) {
//
//                LookUp lookUp = (LookUp) content;
//                long lookUpId = lookUp.getId();
//
//                if (messageTargetDestination.equals(resolverTopic)) {
        
        // listener calls setDestination() on lookUp
//                    lookUpListener.onLookUp(lookUp);
        
//                     build a replyMessage
//                    try {
        
//                        ObjectMessage reply = session.createObjectMessage();
//                        reply.setJMSDestination(objMessage.getJMSReplyTo());
//                        reply.setObject(lookUp);
//                        MessageProducer replyProducer = session.createProducer(
//                                objMessage.getJMSReplyTo());
//                        replyProducer.setDeliveryMode(
//                                DeliveryMode.NON_PERSISTENT);
//
//                        // try to send the reply and catch the exception in case
//                        //of the resolverTopic isn't valid anymore
//                        try {
//                            replyProducer.send(reply);
//
////                            session.commit();
//
//                        } catch (InvalidDestinationException ide) {
//                            ide.printStackTrace();
//                            // the temptopic was deleted due to timeout, so...
//                            // there is to do nothing about this error ...
//                            // or at least shouldn't be.
//                        }
//                    } catch (JMSException e) {
//                        e.printStackTrace();
//                    }
        
//                } else if ( (isOneOfMyLookups(lookUp))){
//// so if it doesn't came through gossip and is one of our own..
//// take a look if an response is still needed and if the response is useful
//                    if ( (!hasResponse(lookUp) ) &&
//                            (lookUp.getDestination() != null) ) {
//                        lookUpMap.put(lookUpId, lookUp);
//
//                        synchronized (WAIT_LOCK) {
//
//                            WAIT_LOCK.notifyAll();
//
//                        }
//
//                    }
//                }
        
//                    // if it didn't came over gossip nor is one we wait
//                    //for ignore it and do nothing.
//                    // most propable it's only one message that came through
//                    //late during begin of cleanup-procedures
//
//                }  // end of LookUp handling
//
//            }  // end of ObjectMessage handling
        
//            if (log.isDebugEnabled()) {
//
//                log.debug("onMessage() DONE.");
//
//            }
        
    }
    
    protected void handleLookUp(Message message, LookUp lookUp) {
        
        if (log.isDebugEnabled()) {
            
            log.debug("lookup to handle: " + lookUp);
            
        }
        
        this.lookUpListener.onLookUp(lookUp);
        
        if (lookUp.getDestination() != null) {
            
            try {
                
                MessageProducer mp = session.createProducer(
                        message.getJMSReplyTo());
                
                mp.send(message.getJMSReplyTo(),
                        session.createObjectMessage(lookUp));
                
                if (log.isDebugEnabled()) {
                    
                    log.debug("reply successfully sent");
                    
                }
                
            } catch (JMSException je) {
                
                log.warn("Failed to reply", je);
                
            }
            
        } else {
            
            if (log.isDebugEnabled()) {
                
                log.debug("not replying (unsuccsessful lookup)");
                
            }
            
        }
        
    }
    
    protected void handleReply(LookUp lookUp) {
        
        if (log.isDebugEnabled()) {
            
            log.debug("REPLY");
            
        }
        
        LookUp myRequest = lookUpMap.get(lookUp.getId());
        
        if (myRequest != null) {
            
            if (log.isDebugEnabled()) {
                
                log.debug("reply matches " + myRequest);
                
            }
            
            myRequest.setDestination(lookUp.getDestination());
            
            synchronized (WAIT_LOCK) {
                
                WAIT_LOCK.notifyAll();
                
            }
            
        } else {
            
            if (log.isDebugEnabled()) {
                
                log.debug("reply does not match any of my lookups");
                
            }
            
        }
        
    }
    
    public boolean isOneOfMyLookups(LookUp lookUp){
        
        return lookUpMap.containsKey(lookUp.getId());
        
    }
    
    public boolean hasResponse(LookUp lookUp){
        
        return (lookUpMap.get(lookUp.getId()).getDestination() != null);
        
    }
    
    public void setLookUpListener(ILookUpListener listener) {
        
        this.lookUpListener = listener;
        
    }
    
    public void doInit() throws LifecycleException {
        // nothing to do here
    }
    
    public void doStart() throws LifecycleException {
        
        if (log == null) {
            
            log = LogFactory.getLog(getClass());
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("starting destination resolver.");
            
        }
        
        if (connectionFactory == null) {
            
            throw new LifecycleException("No connection factory found.");
            
        }
        
        started = true;
        
        try {
            
            connection = connectionFactory.createConnection();
            connection.start();
            connection.setExceptionListener(this);
            
            session = connection.createSession(transacted, acknowledgeMode);
//            session.setMessageListener(this);
            
            resolverTopic = session.createTopic(resolverTopicName);
            
//            producer = session.createProducer(resolverTopic);
//            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            
            MessageConsumer consumer = session.createConsumer(resolverTopic);
            consumer.setMessageListener(this);
            
        } catch (JMSException je) {
            
            throw new LifecycleException("Failed to start resolver", je);
            
        }
        
        if (log.isDebugEnabled()) {
            
            log.debug("destination resolver started listening for topic: '" + resolverTopic + "' ");
            
        }
        
    }
    
    public void doStop() throws LifecycleException {
        
        if (log.isDebugEnabled()) {
            
            log.debug("stopping destination resolver");
            
        }
        
        started = false;
        
        try {
            
            if (consumer != null) {
                
                consumer.close();
                
            }
            
            if (producer != null) {
                
                producer.close();
                
            }
            
            if (session != null) {
                
                session.close();
                
            }
            
            if (connection != null) {
                
                connection.close();
                
            }
            
        } catch (ConnectionClosedException cce) {
            
            log.warn(cce);
            
        } catch (JMSException je) {
            
            throw new LifecycleException("Failed to top resolver", je);
            
        }
        
    }
    
    public void doCleanup() throws LifecycleException {
        // do nothing here
    }
    
    private boolean hasTimeout(LookUp lookUp) {
        
        long startTime = lookUpMap.get(lookUp.getId()).getStartTime();
        
        return ( startTime <= (System.currentTimeMillis() - DEFAULT_TIMEOUT) );
        
    }
    
    public void stateChanged(ILifecycle.LifecycleStates oldState, ILifecycle.LifecycleStates newState) {
    }
    
    public void onException(JMSException jMSException) {
        
        log.error(jMSException);
        
    }
    
    private class TimeoutTask extends TimerTask {
        
        public void run() {
            
            synchronized (WAIT_LOCK) {
                
                WAIT_LOCK.notifyAll();
                
            }
            
        }
        
    }
    
}
