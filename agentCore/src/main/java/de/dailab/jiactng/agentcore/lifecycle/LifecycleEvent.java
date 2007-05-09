package de.dailab.jiactng.agentcore.lifecycle;

import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.UNDEFINED;

import java.util.EventObject;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;

/**
 * Basic lifecycle event object.
 *
 * @author Joachim Fuchs
 */
public class LifecycleEvent extends EventObject {
            
    /**
     * The source object's lifecycle state represented by this event.
     */
    private LifecycleStates state = UNDEFINED;            
    
    /**
     * 
     * Creates a new instance of LifecycleEvent 
     * 
     * @param state 
     * @param source the <code>Lifecycle</code> instance that this event 
     *               originates from
     */
    public LifecycleEvent(ILifecycle source, LifecycleStates state) {
        
        super(source);
        this.state = state;
        
    }
    
    /**
     * Returns the source object's lifecycle state represented by this event.
     * The possible states are defined in <code>Lifecycle</code>
     *
     * @return the lifecycle state
     */
    public LifecycleStates getState() {
     
        return this.state;
        
    }
       
}
