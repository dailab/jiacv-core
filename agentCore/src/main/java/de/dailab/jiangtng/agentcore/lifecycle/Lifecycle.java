package de.dailab.jiangtng.agentcore.lifecycle;

/**
 * Basic lifecycle interface.
 *
 * @author Joachim Fuchs
 */
public interface Lifecycle {
    
    /**
     * The states a <code>Lifecycle</code> can have. 
     *
     * UNDEFINED and CLEANED_UP have the same parent state (CREATED),
     * INITIALIZED and STOPPED do as well (READY). The separation allows 
     * better tracking of state transitions. A lifecycle handler in strict mode 
     * would have to notice the equivalence.
     */
    enum LifecycleStates {
        
        /**
         * the freshly created <code>Lifecycle</code>. The super-state is VOID
         */
        UNDEFINED,
        /**
         * state during initialization 
         */
        INITIALIZING,
        /**
         * state after initialization. super-state is READY
         */
        INITIALIZED,
        /**
         * state during startup 
         */
        STARTING,
        /**
         * active state
         */
        STARTED,
        /**
         * state during shutdown 
         */
        STOPPING,
        /**
         * state after being stopped. super-state is READY
         */
        STOPPED,
        /**
         * state while being removed
         */
        CLEANING_UP,
        /**
         * state after being cleaned up. super state is VOID
         */
        CLEANED_UP,
        /**
         * error state. if a lifecycle state transition throws a lifecycle exception,
         * the <code>Lifecycle</code>'s <code>kill()</code> method may be called.
         * this state is equivalent to VOID.
         */
        KILLED;
        
    };
    
    /**
     * Initialize your object, prepare to grab all resources you need for work.
     * To avoid any lenghty processing at object creation time put all your
     * initialization code here.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void init() throws LifecycleException;
    
    /**
     * Start your object, get ready for business. Acquire any resources.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void start() throws LifecycleException;
    
    /**
     * Stop your object. Release resources.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void stop() throws LifecycleException;
    
    /**
     * Prepare for the object's removal.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException 
     */
    public void cleanup() throws LifecycleException;
    
    /**
     * Add a <code>LifecycleListener</code> that is interested in
     * <code>LifecycleEvent</code>s from this object.
     *
     * @param listener the <code>LifecycleListener</code> to add
     */
    public void addLifecycleListener(LifecycleListener listener);
    
    /**
     * Remove the specified <code>LifecycleListener</code>
     *
     * @param listener the <code>LifecycleListener</code> to remove
     */
    public void removeLifecycleListener(LifecycleListener listener);
    
}
