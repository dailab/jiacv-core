package de.dailab.jiactng.agentcore.communication.jms;

import de.dailab.jiactng.agentcore.communication.CommunicationException;

/**
 * @author Joachim Fuchs
 */
public class JmsCommunicationException extends CommunicationException {
    
    /**
     * Creates a new instance of JmsCommunicationException
     *
     */
    public JmsCommunicationException(String msg) {
        
        super(msg);
        
    }
    
    public JmsCommunicationException(String msg, Throwable t) {
        
        super(msg, t);
        
    }
    
}
