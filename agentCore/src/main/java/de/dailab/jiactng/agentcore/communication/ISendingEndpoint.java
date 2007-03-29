package de.dailab.jiactng.agentcore.communication;

/**
 * @author Joachim Fuchs
 */
public interface ISendingEndpoint {
    
    public void send(TNGMessage message) throws CommunicationException;
    
}
