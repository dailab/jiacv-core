/*
 * $Id: CommunicationException.java 16539 2008-02-27 14:38:25Z marcel $ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * @author Marcel Patzlaff
 * @version $Revision: 16539 $
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
