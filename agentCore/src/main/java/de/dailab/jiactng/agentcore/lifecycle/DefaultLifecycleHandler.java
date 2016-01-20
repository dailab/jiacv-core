package de.dailab.jiactng.agentcore.lifecycle;

import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.CLEANED_UP;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.CLEANING_UP;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.INITIALIZED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.INITIALIZING;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STARTED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STARTING;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STOPPED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STOPPING;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.UNDEFINED;

import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;

/**
 * Basic implementation for a default <code>LifecycleHandler</code>. It manages
 * the propagation of lifecycle events on behalf of the managed <code>ILifecycle</code>.
 * The <code>ILifecycle</code> implementation may call <code>beforeXXX()</code>
 * and <code>afterXXX()</code> when entering or leaving the life-cycle method
 * <code>xxx()</code>.
 *
 * @author Joachim Fuchs
 */
public class DefaultLifecycleHandler {
    
//    /**
//     * Indicates wether this <code>Lifecycle</code> may accept only transitions
//     * following the state graph
//     */
//    protected boolean strict = true;
       
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
     * Creates a new instance of DefaultLifecycleHandler
     *
     * @param lifecycle the <code>Lifecycle</code> this handler takes care of
     */
    public DefaultLifecycleHandler(ILifecycle lifecycle) {
        
        // this(lifecycle, false);
    	this.lifecycle = lifecycle;
        
    }
    
//    /**
//     * Creates a new instance of DefaultLifecycleHandler
//     *
//     * @param lifecycle the <code>Lifecycle</code> this handler takes care of
//     * @param strict    determines wether this handler is suppposed to enforce
//     *                  the lifecycle graph
//     */
//    public DefaultLifecycleHandler(ILifecycle lifecycle, boolean strict) {
//        
//        this.lifecycle = lifecycle;
//        this.strict = strict;
//        
//    }
    
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
        
        if (listener instanceof AnonymousLifecycleListener &&
                ((AnonymousLifecycleListener)listener).getCreator() == this) {
            
            throw new IllegalArgumentException(
                    "You cannot add a listener to the handler that created it");
            
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
     * Call this method when entering <code>init()</code>. It sets the state to
     * INITIALIZING and informs all listener.
     * @throws IllegalStateException if strict mode and the current state is not VOID
     * @see LifecycleStates#INITIALIZING
     * @see ILifecycle#stateChanged(LifecycleStates, LifecycleStates)
     * @see #fireLifecycleEvent(LifecycleEvent)
     */
    public void beforeInit() throws IllegalStateException {
        
        /*
         * in strict mode the init transition is allowed only from VOID state
         */
//    	if (isStrict()) {
    		if (!(state.equals(UNDEFINED) || state.equals(CLEANED_UP))) {
    			throw new IllegalStateException("Lifecycle is not in state UNDEFINED or CLEANED_UP.");
    		}
//    	}
            
        final LifecycleStates oldState = state;
        state = INITIALIZING;
        
        lifecycle.stateChanged(oldState, state);
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, state));
        
    }
    
    /**
     * Call this method when leaving <CODE>init()</CODE>. It sets the state to INITIALIZED
     * in case of success or UNDEFINED in case of failure and informs all listener.
     * @param success the success of the initialization
     * @throws IllegalStateException never
     * @see LifecycleStates#INITIALIZED
     * @see ILifecycle#stateChanged(LifecycleStates, LifecycleStates)
     * @see #fireLifecycleEvent(LifecycleEvent)
     */
    public void afterInit(boolean success) throws IllegalStateException {
        
        final LifecycleStates oldState = state;
        if (success) {
        	state = INITIALIZED;
        } else {
        	state = UNDEFINED;
        }
        
        lifecycle.stateChanged(oldState, state);
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, state));
        
    }
    
    /**
     * Call this method when entering <CODE>start()</CODE>. It sets the state to
     * STARTING and informs all listener. In strict mode the transition 
     * <CODE>init()</CODE> will be done before if the current state is VOID.
     * @throws IllegalStateException if strict mode and the current state is not READY or VOID
     * @see LifecycleStates#STARTING
     * @see ILifecycle#stateChanged(LifecycleStates, LifecycleStates)
     * @see #fireLifecycleEvent(LifecycleEvent)
     */
    public void beforeStart() throws IllegalStateException {
        
        /*
         * In strict mode the start transition is allowed only from READY or VOID state
         * In case of VOID an init transition will be automatically done before. 
         */
//    	if (isStrict()) {
    		switch (state) {
    			case INITIALIZED:
    			case STOPPED:
    				break;
    			case UNDEFINED:
    			case CLEANED_UP:
    				try {
    					lifecycle.init();
    					break;
    				} catch (Exception e) {
						// do nothing, because prepare start also if a previous init failed
    					// but we should at least propagate the exception instead of stating that we are in state STARTED, which we aren't
    					throw new IllegalStateException("Initialization failed due to Exception in init()", e);
    				}
    			case STARTED:
    				throw new IllegalStateException("Lifecycle is already in state STARTED.");
    			default: 
    				// transitional states, like INITIALIZING, STARTING, etc. ... 
    				throw new IllegalStateException("Can not start when in state " + getState());
    		}
//    	}

        final LifecycleStates oldState = state;
        state = STARTING;
        
        lifecycle.stateChanged(oldState, state);
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, state));
        
    }
    
    /**
     * Call this method when leaving <CODE>start()</CODE>. It sets the state to STARTED
     * in case of success or INITIALIZED in case of failure and informs all listener.
     * @param success the success of the initialization
     * @throws IllegalStateException never
     * @see LifecycleStates#STARTED
     * @see ILifecycle#stateChanged(LifecycleStates, LifecycleStates)
     * @see #fireLifecycleEvent(LifecycleEvent)
     */
    public void afterStart(boolean success) throws IllegalStateException {
        
        final LifecycleStates oldState = state;
        if (success) {
        	state = STARTED;
        } else {
        	state = INITIALIZED;
        }
        
        lifecycle.stateChanged(oldState, state);
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, state));
        
    }
    
    /**
     * Call this method when entering <CODE>stop()</CODE>. It sets the state to
     * STOPPING and informs all listener.
     * @throws IllegalStateException if strict mode and the current state is not STARTED
     * @see LifecycleStates#STOPPING
     * @see ILifecycle#stateChanged(LifecycleStates, LifecycleStates)
     * @see #fireLifecycleEvent(LifecycleEvent)
     */
    public void beforeStop() throws IllegalStateException {
        
        /*
         * in strict mode the stop transition is allowed only from STARTED state
         */
//    	if (isStrict()) {
    		if (!state.equals(STARTED)) {
    			throw new IllegalStateException("Lifecycle is not in state STARTED.");
    		}
//    	}
            
        final LifecycleStates oldState = state;
        state = STOPPING;
        
        lifecycle.stateChanged(oldState, state);
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, state));
        
    }
    
    /**
     * Call this method when leaving <CODE>stop()</CODE>. It sets the state to
     * STOPPED and informs all listener.
     * @throws IllegalStateException never
     * @see LifecycleStates#STOPPED
     * @see ILifecycle#stateChanged(LifecycleStates, LifecycleStates)
     * @see #fireLifecycleEvent(LifecycleEvent)
     */
    public void afterStop() throws IllegalStateException {
        
        final LifecycleStates oldState = state;
        state = STOPPED;
        
        lifecycle.stateChanged(oldState, state);
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, state));
        
    }
    
    /**
     * Call this method when entering <CODE>cleanup()</CODE>. It sets the state to
     * CLEANING_UP and informs all listener. In strict mode the transition 
     * <CODE>stop()</CODE> will be done before if the current state is STARTED.
     * @throws IllegalStateException if strict mode and the current state is not READY or STARTED
     * @see LifecycleStates#CLEANING_UP
     * @see ILifecycle#stateChanged(LifecycleStates, LifecycleStates)
     * @see #fireLifecycleEvent(LifecycleEvent)
     */
    public void beforeCleanup() throws IllegalStateException {
        
        /*
         * In strict mode the cleanup transition is allowed only from READY or STARTED state.
         * In case of STARTED a stop transition will be automatically done before. 
         */
//    	if (isStrict()) {
    		switch (state) {
				case INITIALIZED:
				case STOPPED:
					break;
				case STARTED:
					try {
						lifecycle.stop();
						break;
					} catch (Exception e) {
						// do nothing, because prepare cleanup also if a previous stop failed 
					}
				default: throw new IllegalStateException("Lifecycle is already in state CLEANED_UP or UNDEFINED.");
    		}
//    	}
        
        final LifecycleStates oldState = state;
        state = CLEANING_UP;
        
        lifecycle.stateChanged(oldState, state);
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, state));
        
    }
    
    /**
     * Call this method when leaving <CODE>cleanup()</CODE>. It sets the state to
     * CLEANED_UP and informs all listener. 
     * @throws IllegalStateException never
     * @see LifecycleStates#CLEANED_UP
     * @see ILifecycle#stateChanged(LifecycleStates, LifecycleStates)
     * @see #fireLifecycleEvent(LifecycleEvent)
     */
    public void afterCleanup() throws IllegalStateException {
        
        final LifecycleStates oldState = state;
        state = CLEANED_UP;
        
        lifecycle.stateChanged(oldState, state);
        fireLifecycleEvent(
                new LifecycleEvent(lifecycle, state));
        
    }
    
    protected void setState(@SuppressWarnings("unused") LifecycleStates targetState) {
        throw new UnsupportedOperationException("Not yet implemented");
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

//    /**
//     * Checks if the lifecycle mode is strict.
//     * @return <code>true</code>, if the mode is strict.
//     */
//    public boolean isStrict() {
//        
//        return strict;
//        
//    }
    
}
