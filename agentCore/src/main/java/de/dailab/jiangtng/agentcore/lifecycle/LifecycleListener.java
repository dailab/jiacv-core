package de.dailab.jiangtng.agentcore.lifecycle;

/**
 * Basic listener for lifecycle events.
 *
 * @author Joachim Fuchs
 */
public interface LifecycleListener {

    /**
     * Delivers a <code>LifecycleEvent</code><br/>
     * <b>Note: </b>implementations should process the events in a non-blocking
     * fashion and return quickly.
     *
     * @param evt the event to deliver
     */
    public void onEvent(LifecycleEvent evt);
    
}
