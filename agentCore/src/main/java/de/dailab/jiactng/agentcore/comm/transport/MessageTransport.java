/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.transport;

import java.net.URI;

import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.Selector;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class MessageTransport {
    public static interface IMessageTransportDelegate {
        void onAsynchronousException(MessageTransport source, Exception e);
        
        /**
         * Transports <strong>must</strong> ensure to deliver a message <strong>only once</strong>
         * regardless if there are several registrations with and without selectors present. 
         */
        void onMessage(MessageTransport source, IJiacMessage message, ICommunicationAddress at);
    }
    
    private final String _transportIdentifier;
    private IMessageTransportDelegate _delegate;
    
    protected MessageTransport(String transportIdentifier) {
        _transportIdentifier= transportIdentifier.toLowerCase();
    }
    
    public final void setDefaultDelegate(IMessageTransportDelegate delegate) {
        _delegate= delegate;
    }
    
    /**
     * This URI returned by this method, specifies the connection entry point
     * to this message transport.
     * 
     * i.e: if this transport maintains a server socket for TCP communication
     * then the URI might look like <code>tcp://192.168.3.42:4321</code>
     * 
     * By default, this method returns <code>null</code>
     * @return
     */
    public URI getConnectorURI() {
        return null;
    }
    
    public final String getTransportIdentifier() {
        return _transportIdentifier;
    }
    
    @Override
    public final boolean equals(Object obj) {
        return getTransportIdentifier().equals(obj);
    }

    @Override
    public final int hashCode() {
        return getTransportIdentifier().hashCode();
    }

    public abstract void doInit() throws Exception;
    public abstract void doCleanup() throws Exception;
    
    public abstract void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException;
    
    /**
     * Providing a selector enables the transport to optimise the inter-transport communication.
     * But it is <strong>not</strong> a criterion to delegate messages more then once!
     */
    public abstract void listen(ICommunicationAddress address, Selector selector) throws CommunicationException;
    public abstract void stopListen(ICommunicationAddress address, Selector selector) throws CommunicationException;
    
    public final void delegateException(Exception exception) {
        _delegate.onAsynchronousException(this, exception);
    }
    
    public final void delegateMessage(IJiacMessage message, ICommunicationAddress at) {
        _delegate.onMessage(this, message, at);
    }
}
