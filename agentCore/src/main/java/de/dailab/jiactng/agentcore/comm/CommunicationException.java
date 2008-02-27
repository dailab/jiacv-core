/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class CommunicationException extends Exception {
    private static final long serialVersionUID = -5293677860889166406L;

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
