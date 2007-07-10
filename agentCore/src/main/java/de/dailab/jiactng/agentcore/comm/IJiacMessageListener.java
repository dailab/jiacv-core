/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IJiacMessageListener {
    void receive(IJiacMessage message, ICommunicationAddress from);
}
