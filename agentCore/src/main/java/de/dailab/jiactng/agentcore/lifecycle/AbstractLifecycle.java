package de.dailab.jiactng.agentcore.lifecycle;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import org.springframework.context.ApplicationContext;

/**
 * Abstract base class for <code>ILifecycle</code> implementations.
 * <b>Not threadsafe</b>
 *
 * @author Joachim Fuchs
 */
public abstract class AbstractLifecycle extends NotificationBroadcasterSupport implements ILifecycle, AbstractLifecycleMBean {
    
    /**
     * The lifecycle handler that is used internally.
     */
    protected DefaultLifecycleHandler lifecycle = null;

    /**
     * Default constructor that creates an internally used lifecycle handler for the default mode.
     */
    public AbstractLifecycle() {
        
        lifecycle = new DefaultLifecycleHandler(this);
        
    }

    /**
     * Constructor that creates an internally used lifecycle handler for the given mode.
     * @param strict <code>true</code> for strict mode and <code>false</code> for loose mode
     */
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
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public void init() throws LifecycleException {
        try {
        	lifecycle.beforeInit();
        }
        catch (IllegalStateException e) {
        	switch (getState()) {
        		case INITIALIZED:
        		case STOPPED:
        		case STARTED:
        			// already initialized => do nothing
            		return;
            	default:
                	throw new LifecycleException("Initialization not allowed in state " + getState(), e);
        	}
        }
        
        try {
            
            doInit();
            
        } catch (Throwable t) {
            if (t instanceof LifecycleException) {
                throw (LifecycleException)t;
            }
            
            throw new LifecycleException("Failed to initialize", t);
        }
        
        lifecycle.afterInit();
        
    }
    
    /**
     * You may override this method to change the lifecycle event propagation behavior.
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public void start() throws LifecycleException {
        try {
        	lifecycle.beforeStart();
        }
        catch (IllegalStateException e) {
        	switch (getState()) {
        		case STARTED:
        			// already started => do nothing
        			return;
        		default:
        			throw new LifecycleException("Starting not allowed in state " + getState(), e);
        	}
        }
        
        try {
            
            doStart();
            
        } catch (Throwable t) {
            if (t instanceof LifecycleException) {
                throw (LifecycleException)t;
            }
            
            throw new LifecycleException("Failed to start", t);
        }
        
        lifecycle.afterStart();
        
    }
    
    /**
     * You may override this method to change the lifecycle event propagation behavior.
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public void stop() throws LifecycleException {
        try {
        	lifecycle.beforeStop();
        }
        catch (IllegalStateException e) {
        	switch (getState()) {
        		case INITIALIZED:
        		case STOPPED:
        		case UNDEFINED:
        		case CLEANED_UP:
        			// already stopped => do nothing
            		return;
            	default:
                	throw new LifecycleException("Stopping not allowed in state " + getState(), e);
        	}
        }
        
        try {
            
            doStop();
            
        } catch (Throwable t) {
            if (t instanceof LifecycleException) {
                throw (LifecycleException)t;
            }

            throw new LifecycleException("Failed to stop", t);
        }
        
        lifecycle.afterStop();
        
    }
    
    /**
     * You may override this method to change the lifecycle event propagation behavior.
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public void cleanup() throws LifecycleException {
        try {
        	lifecycle.beforeCleanup();
        }
        catch (IllegalStateException e) {
        	switch (getState()) {
        		case UNDEFINED:
        		case CLEANED_UP:
        			// already cleaned up => do nothing
            		return;
            	default:
                    throw new LifecycleException("Cleaning up not allowed in state " + getState(), e);
        	}
        }
        
        try {
            
            doCleanup();
            
        } catch (Throwable t) {
            if (t instanceof LifecycleException) {
                throw (LifecycleException)t;
            }
            
            throw new LifecycleException("Failed to clean up", t);
        }
        
        lifecycle.afterCleanup();
        
    }
    
    /**
     * Registers the supplied <code>ILifecycleListener</code>.
     * @param listener the lifecycle listener.
     */
    public void addLifecycleListener(ILifecycleListener listener) {
        
        lifecycle.addLifecycleListener(listener);
        
    }
    
    /**
     * Unregisters the supplied <code>ILifecycleListener</code>.
     * @param listener the lifecycle listener.
     */
    public void removeLifecycleListener(ILifecycleListener listener) {
        
        lifecycle.removeLifecycleListener(listener);
        
    }
    
    /**
     * Returns the current lifecycle state of this <code>ILifecycle</code>.
     *
     * @return the current lifecycle state.
     */
    public LifecycleStates getState() {
        
        return lifecycle.getState();
        
    }
    
    /**
	 * {@inheritDoc}
	 */
    public String getLifecycleState() {
        return getState().toString();
    }
    
	/**
	 * Getter for attribute "Strict" of the managed resource.
	 * @return the lifecycle mode of this resource
	 */
	public boolean isStrict() {
		return lifecycle.isStrict();
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

    /**
     * Returns information about the <code>AttributeChangeNotification</code> 
     * this lifecycle instance may send to notify about the changed state.
     * @return list with only one notification information.
     */
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
     * Put your initialization code here.
     * @throws Exception if this lifecycle instance can not be initialized.
     */
    public abstract void doInit() throws Exception ;
    
    /**
     * Put your start code here.
     * @throws Exception if this lifecycle instance can not be started.
     */
    public abstract void doStart() throws Exception ;
    
    /**
     * Put your stop code here.
     * @throws Exception if this lifecycle instance can not be stopped.
     */
    public abstract void doStop() throws Exception ;
    
    /**
     * Put your clean up code here.
     * @throws Exception if this lifecycle instance can not be cleaned up.
     */
    public abstract void doCleanup() throws Exception ;
    
}
