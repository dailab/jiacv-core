/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.transport;

import java.net.URI;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * The MessageTransport specifies the commonalities of a client from
 * a message bus.
 * 
 * <p>
 * All client implementations must provide operations to un-/register
 * to specific logical destinations and to deliver message to the bus.
 * Life-cycle methods are optional and should be implemented whenever
 * initialization/cleanup code is needed.
 * </p>
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class MessageTransport implements MessageTransportMBean {
    public static interface IMessageTransportDelegate {
        /**
         * This method is called from a transport when a communication 
         * or administration exception occurred.
         * 
         * @param source        the transport that reports the exception
         * @param e             the specific exception that was caught
         */
        void onAsynchronousException(MessageTransport source, Exception e);
        
        /**
         * Transports <strong>must</strong> ensure to deliver a message <strong>only once</strong>
         * regardless if there are several registrations with and without selectors present.
         * 
         * @param source        the transport that received the message
         * @param message       the message object
         * @param at            the destination at which the message was received
         */
        void onMessage(MessageTransport source, IJiacMessage message, ICommunicationAddress at);
        
        /**
         * This method is used from all transports to acquire a named instance of
         * {@link Log}
         * 
         * @param extension     the name extension for the logger
         * @return the named logger
         */
        Log getLog(String extension);
    }
    
    private final String transportIdentifier;
    private IMessageTransportDelegate delegate;
    
    protected Log log;

	/* 0 means no timeout */
	public static final long DEFAULT_TIMEOUT = 0;
	protected long timeToLive = DEFAULT_TIMEOUT;

    protected MessageTransport(String transportIdentifier) {
        this.transportIdentifier= transportIdentifier.toLowerCase();
    }
    
    /**
     * This method set the handler for this transport. It will be informed
     * about all new messages, according the contract specified in
     * {@link IMessageTransportDelegate#onMessage(MessageTransport, IJiacMessage, ICommunicationAddress)},
     * and also about exceptions from the communication infrastructure.
     * 
     * @param newDelegate      the handle this transport should delegate information to
     */
    public final void setDefaultDelegate(IMessageTransportDelegate newDelegate) {
        delegate = newDelegate;
        
        if (delegate.getLog(transportIdentifier) == null) {
      	  log.error("IMessageTransportDelegate.getLog() returned null! This can cause exceptions! ");
        }
        log = delegate.getLog(transportIdentifier);
        
    }
    
    /**
     * This URI returned by this method, specifies the connection entry point
     * to this message transport.
     * 
     * i.e: if this transport maintains a server socket for TCP communication
     * then the URI might look like <code>tcp://192.168.3.42:4321</code>
     * 
     * By default, this method returns <code>null</code>
     * @return the connection entry point or <code>null</code>
     * 
     * @deprecated
     * TODO: check whether this method is still of use
     */
    public URI getConnectorURI() {
        return null;
    }
    
    /**
     * This method returns the identifier for this transport. The identifier
     * should map to specific client implementations. Furthermore it
     * is usable in {@link URI URIs}.
     * 
     * @return      the identifier of this transport
     */
    public final String getTransportIdentifier() {
        return transportIdentifier;
    }
    
    /**
     * Overrides the default implementation in {@link Object} and
     * only checks the equality of the identifiers.
     * @param obj the other message transport
     * @return the result of equality check
     * @see #getTransportIdentifier()
     */
    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof MessageTransport) {
            return getTransportIdentifier().equals(((MessageTransport)obj).getTransportIdentifier());
        }
        return false;
    }

    /**
     * Overrides the default implementation in {@link Object} and
     * returns the hashCode of the identifier.
     * @return the hash code
     * @see #getTransportIdentifier()
     */
    @Override
    public final int hashCode() {
        return getTransportIdentifier().hashCode();
    }

    /**
     * Initialization could can be placed here. The default
     * implementation of this method does nothing.
     * 
     * @throws Exception    if the initialization process fails
     */
    public void doInit() throws Exception {};
    
    /**
     * Whenever the transport requires resources they have to be realized
     * in the implementation of this method. The default implementation
     * does nothing
     * 
     * @throws Exception    if the cleanup process fails
     */
    public void doCleanup() throws Exception {};
    
    /**
     * This method issues the delivery of the specified message to the specified destination.
     * If the timeout is reached, the message will be expired. Please consider that the clocks 
     * of different hosts may run asynchronous!
     * 
     * @param message       the message to be send
     * @param address       the destination the message should be send to
     * @param timeToLive	the time-to-live of the message in milliseconds or 0 for using timeout specified by the message transport
     * 
     * @throws CommunicationException       when the delivery of the message to the bus fails
     * @throws IllegalArgumentException     if one of the arguments is <code>null</code>
     */
    public abstract void send(IJiacMessage message, ICommunicationAddress address, long timeToLive) throws CommunicationException;
    
    /**
     * This method queries the transport to open an incoming channel to the message bus and
     * to delegate messages that match the given receiving destination and the selector. Messages
     * that are received but do not match any of the queries are silently dropped.
     * <p>
     * Providing a selector enables the transport to optimize the message bus communication.
     * But it is <strong>not</strong> a criterion to delegate messages more then once!
     * </p>
     * 
     * @param address the communication address
     * @param selector the selector
     * @throws CommunicationException       when the registration to the message bus fails
     * @throws IllegalArgumentException     if the address is <code>null</code>
     */
    public abstract void listen(ICommunicationAddress address, IJiacMessage selector) throws CommunicationException;
    
    /**
     * The same as {@link #listen(ICommunicationAddress, IJiacMessage)} in reverse ;-)
     * 
     * @param address the communication address
     * @param selector the selector
     * @throws CommunicationException       when the deregistration to the message bus fails
     * @throws IllegalArgumentException     if the address is <code>null</code>
     */
    public abstract void stopListen(ICommunicationAddress address, IJiacMessage selector) throws CommunicationException;
    
    // TODO: refactor this because it is not part of the user API
    public final void delegateException(Exception exception) {
        delegate.onAsynchronousException(this, exception);
    }
    
    // TODO: refactor this because it is not part of the user API
    public final void delegateMessage(IJiacMessage message, ICommunicationAddress at) {
        delegate.onMessage(this, message, at);
    }
    
    protected Log createChildLog(String name) {
        return delegate.getLog(transportIdentifier + "." + name);
    }

	/**
	 * {@inheritDoc}
	 */
	public final long getTimeToLive() {
		return timeToLive;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setTimeToLive(long newTimeToLive) {
		timeToLive = newTimeToLive;
	}
}
