package de.dailab.jiactng.agentcore.lifecycle;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.*;

import java.util.ArrayList;
import java.util.List;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

/**
 * Basic implementation for a default <code>LifecycleHandler</code>. It manages 
 * the propagation of lifecycle events on behalf of the managed <code>ILifecycle/code>.
 * The <code>ILifecycle</code> implementation may call <code>beforeXXX()</code>
 * and <code>afterXXX()</code> when entering or leaving the lifecycle method 
 * <code>xxx()</code>.
 *
 * @author Joachim Fuchs
 */
public class DefaultLifecycleHandler {
    
    /**
     * Indicates wether this <code>Lifecycle</code> may accept only transitions
     * following the state graph
     */
    protected boolean strict = false;
    
    /**
     * The <code>Lifecycle</code>'s current state
     */
    protected LifecycleStates state = UNDEFINED;
    
    /**
     * The <code>Lifecycle</code> this handler takes care of
     */
    protected ILifecycle lifecycle = null;
    
    /**
     * The list of registered <code>LifecycleListeners</code>
     */
    protected List<ILifecycleListener> listeners = new ArrayList<ILifecycleListener>();
    
    /**
     * The number of the next notification.
     */
    private long sequenceNumber = 1; 

    /**
     * Creates a new instance of DefaultLifecycleHandler
     *
     * @param lifecycle the <code>Lifecycle</code> this handler takes care of
     */
    public DefaultLifecycleHandler(ILifecycle lifecycle) {
        
        this(lifecycle, false);
        
    }
    
    /**
     * Creates a new instance of DefaultLifecycleHandler
     *
     * @param lifecycle the <code>Lifecycle</code> this handler takes care of
     * @param strict    determines wether this handler is suppposed to enforce
     *                  the lifecycle graph
     */
    public DefaultLifecycleHandler(ILifecycle lifecycle, boolean strict) {
        
        this.lifecycle = lifecycle;
        this.strict = strict;
        
    }

    /**
     * Returns the current state of the managed <code>Lifecycle</code>
     *
     * @return the liefcycle's state
     */
    public LifecycleStates getState() {
     
        return state;
        
    }
        
    /**
     * Returns an anonymous lifecyle listener that allows the top most
     * listener in a hierarchy to receive lifecyle events from its 'grandchildren'
     * @return a freshly created <code>LifecycleListener</code>
     */
    public ILifecycleListener createLifecycleListener() {
        
        return new AnonymousLifecycleListener(this);
        
    }
    
    /**
     * Fires the supplied <code>LifecycleEvent</code> to all registered
     * <code>LifecycleListeners</code> in a synchronous and blocking fashion.
     *
     * @param evt the <code>LifecycleEvent</code> to fire
     */
    public void fireLifecycleEvent(LifecycleEvent evt) {
        
        synchronized (listeners) {
            
            for (ILifecycleListener ll : listeners) {
                
                // @todo fire event unblocking
                ll.onEvent(evt);
                
            }
            
        }
        
    }
    
    /**
     * Adds the specified <code>LifecycleListener</code> to the list
     * of listeners
     *
     * @param listener the <code>LifecycleListener</code> to add
     */
    public void addLifecycleListener(ILifecycleListener listener) {
        
        if (listener instanceof AnonymousLifecycleListener) {
            
            if (((AnonymousLifecycleListener)listener).getCreator() == this) {
                
                throw new IllegalArgumentException(
                        "You cannot add a listener to the handler that created it");
                
            }
            
        }
        
        if (!listeners.contains(listener)) {
            
            listeners.add(listener);
            
        }
        
    }
    
    /**
     * Removes the specified <code>LifecycleListener</code> from the list
     * of listeners
     *
     * @param listener the <code>LifecycleListener</code> to remove
     */
    public void removeLifecycleListener(ILifecycleListener listener) {
        
        if (listeners.contains(listener)) {
            
            listeners.remove(listener);
            
        }
        
    }
    
    /**
     * Uses JMX to send notifications that the attribute "LifecycleState" 
     * of the managed lifecycle (e.g. agent) has been changed. 
     * 
     * @param oldState the old state of the lifecycle
     * @param newState the new state of the lifecycle
     */
    protected void sendStateChangedNotification(String newState) {
    	Notification n = 
    		new AttributeChangeNotification(lifecycle, 
    				sequenceNumber++, 
				    System.currentTimeMillis(), 
				    "LifecycleState changed", 
				    "LifecycleState", 
				    "java.lang.String", 
				    "", 
				    newState); 

    	((NotificationBroadcasterSupport)lifecycle).sendNotification(n);
    }

    /**
     * Call this method when entering <code>init()</code>.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void beforeInit() throws LifecycleException {
        
        if (strict) {
            
            /*
             * check if transition is allowed
             *
             */
            
        }
        
        state = INITIALIZING;
        
        sendStateChangedNotification("initializing");
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, INITIALIZING));
        
    }
    
    /**
     * Call this method when leaving <CODE>init()</CODE>.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void afterInit() throws LifecycleException {
        
        if (strict) {
            
            /*
             * check if transition is allowed
             *
             */
            
        }
        
        state = INITIALIZED;
        
        sendStateChangedNotification("initialized");
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, INITIALIZED));
        
    }
    
    /**
     * Call this method when entering <CODE>start()</CODE>.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void beforeStart() throws LifecycleException {
        
        if (strict) {
            
            /*
             * check if transition is allowed
             *
             */
            
        }
        
        state = STARTING;
        
        sendStateChangedNotification("starting");
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, STARTING));
        
    }
    
    /**
     * Call this method when leaving <CODE>start()</CODE>.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void afterStart() throws LifecycleException {
        
        if (strict) {
            
            /*
             * check if transition is allowed
             *
             */
            
        }
        
        state = STARTED;
        
        sendStateChangedNotification("started");
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, STARTED));
        
    }
    
    /**
     * Call this method when entering <CODE>stop()</CODE>.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void beforeStop() throws LifecycleException {
        
        if (strict) {
            
            /*
             * check if transition is allowed
             *
             */
            
        }
        
        state = STOPPING;
        
        sendStateChangedNotification("stopping");
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, STOPPING));
        
    }
    
    /**
     * Call this method when leaving <CODE>stop()</CODE>.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void afterStop() throws LifecycleException {
        
        if (strict) {
            
            /*
             * check if transition is allowed
             *
             */
            
        }
        
        state = STOPPED;
        
        sendStateChangedNotification("stopped");
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, STOPPED));
        
    }
    
    /**
     * Call this method when entering <CODE>cleanup()</CODE>.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void beforeCleanup() throws LifecycleException {
        
        if (strict) {
            
            /*
             * check if transition is allowed
             *
             */
            
        }
        
        state = CLEANING_UP;
        
        sendStateChangedNotification("cleaning up");
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, CLEANING_UP));
        
    }
    
    /**
     * Call this method when leaving <CODE>cleanup()</CODE>.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void afterCleanup() throws LifecycleException {
        
        if (strict) {
            
            /*
             * check if transition is allowed
             *
             */
            
        }
        
        state = CLEANED_UP;
        
        sendStateChangedNotification("cleaned up");
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, CLEANED_UP));
        
    }        
    
    /**
     * Allows for creating listener hierarchies.
     */
    protected class AnonymousLifecycleListener implements ILifecycleListener {
        
        /**
         * The handler that created this listener. Required to make sure we 
         * do not build a cyclic hierarchy.
         */
        DefaultLifecycleHandler creator = null;
        
        /**
         * Creates an <code>AnonymousLifecycleListener</code>.
         * 
         * 
         * @param creator 
         */
        AnonymousLifecycleListener(DefaultLifecycleHandler creator) {
            
            this.creator = creator;
            
        }
        
        /**
         * Propagates the event to the lifecycle handler that created this listener
         *
         * @param evt the <code>LifecycleEvent</code> to propagate
         */
        public void onEvent(LifecycleEvent evt) {
            
            creator.fireLifecycleEvent(evt);
            
        }
        
        /**
         * Returns this listener's creator. Allows to prevent cyclic propagation
         * hierarchies
         *
         * @return the creator of this listener
         */
        protected DefaultLifecycleHandler getCreator() {
            
            return this.creator;
            
        }
        
    }
}
