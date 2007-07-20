/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public abstract class AbstractMessageTransport {
    public static interface IMessageTransportDelegate {
        void onAsynchronousException(Exception e);
        void onMessage(IJiacMessage message, ICommunicationAddress from, String selector);
    }
    
    private IMessageTransportDelegate _delegate;
    
    protected AbstractMessageTransport() {}
    
    public final void setDefaultDelegate(IMessageTransportDelegate delegate) {
        _delegate= delegate;
    }
    
    public final IMessageTransportDelegate getDefaultDelegate() {
        return _delegate;
    }
    
    public abstract void doInit() throws Exception;
    public abstract void doCleanup() throws Exception;
    
    public abstract void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException;
    public abstract void listen(ICommunicationAddress address, String selector) throws CommunicationException;
    public abstract void stopListen(ICommunicationAddress address, String selector) throws CommunicationException;
}
