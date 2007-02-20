package de.dailab.jiactng.agentcore.lifecycle;

/**
 * @author Joachim Fuchs
 */
public class LifecycleException extends Exception {
    
    /** 
     * Creates a new instance of LifecycleException 
     *
     * @param msg 
     */
    public LifecycleException(String msg) {
        
        super(msg);
        
    }
    
    /** 
     * Creates a new instance of LifecycleException 
     *
     * @param msg 
     * @param t   
     */
    public LifecycleException(String msg, Throwable t) {
     
        super(msg, t);
        
    }
    
}
