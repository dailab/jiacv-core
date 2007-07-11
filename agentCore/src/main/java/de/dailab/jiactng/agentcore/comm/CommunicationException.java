/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class CommunicationException extends Exception {
    public CommunicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(Throwable cause) {
        super(cause);
    }
}
