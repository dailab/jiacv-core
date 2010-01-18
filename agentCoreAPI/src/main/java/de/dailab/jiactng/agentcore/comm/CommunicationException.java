/*
 * $Id: CommunicationException.java 16539 2008-02-27 14:38:25Z marcel $ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * This class represents an exception during message transfer between agents.
 * @author Marcel Patzlaff
 * @version $Revision: 16539 $
 */
public class CommunicationException extends Exception {
    private static final long serialVersionUID = -5293677860889166406L;

    /**
     * Constructor for a communication exception.
     * @param message a description
     * @param cause the reason
     */
    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for a communication exception.
     * @param message a description
     */
    public CommunicationException(String message) {
        super(message);
    }

    /**
     * Constructor for a communication exception.
     * @param cause the reason
     */
    public CommunicationException(Throwable cause) {
        super(cause);
    }
}
