package de.dailab.jiactng.agentcore.lifecycle;

/**
 * This exception will be thrown, if the state of a lifecycle-aware JIAC component can not be changed successful.
 * @author Joachim Fuchs
 */
public class LifecycleException extends Exception {
    private static final long serialVersionUID = 1675899104547457451L;

    /** 
     * Creates a new instance of LifecycleException 
     *
     * @param msg a message describing the exception that occured.
     */
    public LifecycleException(String msg) {
        
        super(msg);
        
    }
    
    /** 
     * Creates a new instance of LifecycleException 
     *
     * @param msg a message describing the exception that occured.
     * @param t an instance of Throwable that was the cause of this exception.
     * @see Throwable  
     */
    public LifecycleException(String msg, Throwable t) {
     
        super(msg, t);
        
    }
    
}
