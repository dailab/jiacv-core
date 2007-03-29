package de.dailab.jiactng.agentcore.communication;

/**
 * @author Joachim Fuchs
 */
public interface IDestinationResolver {
    
    public void resolveDestination(TNGMessage message) throws ResolveException;
    
}
