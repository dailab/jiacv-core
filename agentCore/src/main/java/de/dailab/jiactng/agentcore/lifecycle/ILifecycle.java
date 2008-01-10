package de.dailab.jiactng.agentcore.lifecycle;

/**
 * Basic lifecycle interface.
 *
 * @author Joachim Fuchs
 */
public interface ILifecycle {

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

    };

    /**
     * Initialize your object, prepare to grab all resources you need for work.
     * To avoid any lenghty processing at object creation time put all your
     * initialization code here.
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public void init() throws LifecycleException;

    /**
     * Start your object, get ready for business. Acquire any resources.
     * 
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public void start() throws LifecycleException;

    /**
     * Stop your object. Release resources.
     * 
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public void stop() throws LifecycleException;

    /**
     * Prepare for the object's removal.
     * 
     * @throws LifecycleException if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     */
    public void cleanup() throws LifecycleException;

    /**
     * Add a <code>LifecycleListener</code> that is interested in
     * <code>LifecycleEvent</code>s from this object.
     *
     * @param listener the <code>LifecycleListener</code> to add
     */
    public void addLifecycleListener(ILifecycleListener listener);

    /**
     * Remove the specified <code>LifecycleListener</code>
     *
     * @param listener the <code>LifecycleListener</code> to remove
     */
    public void removeLifecycleListener(ILifecycleListener listener);

    /**
     * Returns the current lifecycle state.
     *
     * @return the <code>Lifecycle</code>'s state
     */
    public LifecycleStates getState();

    /**
     * Called when the lifecycle state has been changed.
     * 
     * @param oldState the old lifecycle state
     * @param newState the new lifecycle state
     */
    public void stateChanged(LifecycleStates oldState, LifecycleStates newState);
}
