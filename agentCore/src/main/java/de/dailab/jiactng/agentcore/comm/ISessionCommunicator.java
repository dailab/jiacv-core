/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface ISessionCommunicator {
    void initialise(String sessionId);
    void finish();
    void associate(ICommunicationAddress address);
    void send(IJiacMessage message, ICommunicationAddress address);
    IJiacMessage receive();
    IJiacMessage receiveFrom(ICommunicationAddress address);
}
