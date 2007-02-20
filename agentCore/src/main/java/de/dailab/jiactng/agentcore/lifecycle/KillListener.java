package de.dailab.jiactng.agentcore.lifecycle;

/**
 * Callback interface that gets notified if a <code>Lifecycle</code> gets reset
 * to VOID state due to an error.
 *
 * @author Joachim Fuchs
 */
public interface KillListener {

    /**
     * Indicates that the supplied <code>Lifecycle</code> has been killed.
     * 
     * @param lifecycle the killed <code>Lifecycle</code>.
     */
    public void onKill(Lifecycle lifecycle);
    
}
