package de.dailab.jiactng.agentcore.lifecycle;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Abstract base class for <code>ILifecycle</code> implementations.
 * <b>Not threadsafe</b>
 *
 * @author Joachim Fuchs
 */
public abstract class AbstractLifecycle extends NotificationBroadcasterSupport implements ILifecycle, ApplicationContextAware {

    /**
     * The lifecycle handler that is used internally.
     */
    protected DefaultLifecycleHandler lifecycle = null;

    public AbstractLifecycle() {
     
        lifecycle = new DefaultLifecycleHandler(this);
        
    }
    
    public AbstractLifecycle(boolean strict) {
     
        lifecycle = new DefaultLifecycleHandler(this, strict);
        
    }
    
    /**
     * The number of the next JMX compliant notification.
     */
    protected long sequenceNumber = 1; 

    /**
     * Reference to the spring application context
     */
    protected ApplicationContext applicationContext = null;

    /**
     * You may override this method to change the lifecycle event propagation behavior.
     */
    public void init() throws LifecycleException {

        lifecycle.beforeInit();

        try {

            doInit();

        } catch (Throwable t) {

            throw new LifecycleException("Failed to initialize", t);

        }

        lifecycle.afterInit();

    }

    /**
     * You may override this method to change the lifecycle event propagation behavior.
     */
    public void start() throws LifecycleException {

        lifecycle.beforeStart();

        try {

            doStart();

        } catch (Throwable t) {

            throw new LifecycleException("Failed to start", t);

        }

        lifecycle.afterStart();

    }

    /**
     * You may override this method to change the lifecycle event propagation behavior.
     */
    public void stop() throws LifecycleException {

        lifecycle.beforeStop();

        try {

            doStop();

        } catch (Throwable t) {

            throw new LifecycleException("Failed to stop", t);

        }

        lifecycle.afterStop();

    }

    /**
     * You may override this method to change the lifecycle event propagation behavior.
     */
    public void cleanup() throws LifecycleException {

        lifecycle.beforeCleanup();

        try {

            doCleanup();

        } catch (Throwable t) {

            throw new LifecycleException("Failed to clean up", t);

        }

        lifecycle.afterCleanup();

    }

    /**
     * Registers the supplied <code>ILifecycleListener</code>.
     */
    public void addLifecycleListener(ILifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }

    /**
     * Unregisters the supplied <code>ILifecycleListener</code>.
     */
    public void removeLifecycleListener(ILifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Returns the current lifecycle state of this <code>ILifecycle</code>.
     *
     * @return the current lifecycle state
     */
    public LifecycleStates getState() {

        return lifecycle.getState();

    }

    /**
     * @see de.dailab.jiactng.agentcore.AgentMBean#getLifecycleState()
     */
    public String getLifecycleState() {
  	  System.out.println("Get LifecycleState ...");
  	  return getState().toString();
    }
  	
    /**
     * Uses JMX to send notifications that the attribute "LifecycleState" 
     * of the managed lifecycle (e.g. agent) has been changed. 
     * 
     * @param oldState the old state of the lifecycle
     * @param newState the new state of the lifecycle
     */
    public void stateChanged(LifecycleStates oldState, LifecycleStates newState) {
    	Notification n = 
    		new AttributeChangeNotification(this, 
    				sequenceNumber++, 
				    System.currentTimeMillis(), 
				    "LifecycleState changed", 
				    "LifecycleState", 
				    "java.lang.String", 
				    oldState.toString(), 
				    newState.toString()); 

    	sendNotification(n);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[] {
            AttributeChangeNotification.ATTRIBUTE_CHANGE
        };
        String name = AttributeChangeNotification.class.getName();
        String description = "An attribute of this MBean has changed";
        MBeanNotificationInfo info =
            new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[] {info};
    }

    /**
     * @see org
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
  	  this.applicationContext = applicationContext;
    }
    
    /**
     * Put your initialization code here.
     */
    public abstract void doInit() throws LifecycleException ;

    /**
     * Put your start code here.
     */
    public abstract void doStart() throws LifecycleException ;

    /**
     * Put your stop code here.
     */
    public abstract void doStop() throws LifecycleException ;

    /**
     * Put your clean up code here.
     */
    public abstract void doCleanup() throws LifecycleException ;

}
