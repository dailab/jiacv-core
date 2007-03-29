package de.dailab.jiactng.agentcore.communication;

/**
 * @author Joachim Fuchs
 */
public class CommunicationException extends Exception {
    
    /**
     * Creates a new instance of CommunicationException
     *
     */
    public CommunicationException(String msg) {
        
        super(msg);
        
    }
    
    public CommunicationException(String msg, Throwable t) {
        
        super(msg, t);
        
    }
    
}
