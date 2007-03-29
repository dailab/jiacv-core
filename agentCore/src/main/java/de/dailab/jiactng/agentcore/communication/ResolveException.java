package de.dailab.jiactng.agentcore.communication;

/**
 * @author Joachim Fuchs
 */
public class ResolveException extends CommunicationException {
    
    /**
     * Creates a new instance of ResolveException
     *
     */
    public ResolveException(String msg) {
        
        super(msg);
        
    }
    
    public ResolveException(String msg, Throwable t) {
        
        super(msg, t);
        
    }
    
}
