/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;


/**
 * This bean specifies the way an agent communicates. It implements a message-based approach for information
 * exchange and group administration.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class CommunicationBean extends AbstractMethodExposingBean {
	/**
	 * a save way to cast from object to targetType
	 * 
	 * @param <T>
	 * @param object
	 * @param targetType
	 * @return
	 */
    private static <T> T saveCast(Object object, Class<T> targetType) {
        if(!targetType.isInstance(object)) {
            throw new IllegalArgumentException("argument is not valid");
        }
        
        return targetType.cast(object);
    }
    
    /**
     * Helpclass used to manage incoming messages and exceptions,
     * routing them to processError or processMessage
     *
     */
    private final class MessageTransportDelegate implements IMessageTransportDelegate {
        public void onAsynchronousException(MessageTransport source, Exception e) {
            processError(source, e);
        }

        public void onMessage(MessageTransport source, IJiacMessage message, ICommunicationAddress at, String selector) {
            processMessage(source, message, saveCast(at, CommunicationAddress.class), selector);
        }
    }
    
    private final class MemoryDelegationMessageListener implements IJiacMessageListener {
        public void receive(IJiacMessage message, ICommunicationAddress from) {
            System.out.println("received message '" + message.toString() + "' at '" + from + "'");
            memory.write(message);
        }
    }
    
    private final IJiacMessageListener _defaultListener;
    private final IMessageTransportDelegate _defaultDelegate;
    private IMessageBoxAddress _defaultMessageBox;
    
    private Map<String, MessageTransport> _transports;
    
    /**
     * This map contains all listeners which where only registered to a selector.
     * They are added to all further communication addresses automatically.
     */
    private final Map<String, WildcardListenerContext> _selectorToListenerMap;
    private final Map<CommunicationAddress, List<ListenerContext>> _addressToListenerMap;
    
    public CommunicationBean() {
        _defaultListener= new MemoryDelegationMessageListener();
        _defaultDelegate= new MessageTransportDelegate();
        _transports= new HashMap<String, MessageTransport>();
        _selectorToListenerMap= new HashMap<String, WildcardListenerContext>();
        _addressToListenerMap= new HashMap<CommunicationAddress, List<ListenerContext>>();
    }

    // ~ START OF CONFIGURATION AND INITIALISATION STUFF ~ //
    /**
     * sets transports to the set given. All transports allready set which are not within the
     * set given as parameter will be removed.
     */
    public synchronized void setTransports(Set<MessageTransport> transports) throws Exception {
        Set<MessageTransport> workingCopy;
        if(transports == null) {
            workingCopy= Collections.emptySet();
        } else {
            workingCopy= new HashSet<MessageTransport>();
            workingCopy.addAll(transports);
        }

        // first remove all existing transports
        if(_transports.size() > 0) {
            Set<MessageTransport> toRemove= new HashSet<MessageTransport>();
            toRemove.addAll(_transports.values());
            // only remove transports that are not in the workingCopy
            toRemove.removeAll(workingCopy);
            
            // only add transports that are not yet installed
            workingCopy.removeAll(_transports.values());
            
            // remove transports other than which we want to set
            for(MessageTransport transport : toRemove) {
                removeTransport(transport.getTransportIdentifier());
            }
        }
        // now add what has to be add
        for(MessageTransport transport : workingCopy) {
            addTransport(transport);
        }
    }
    
    @Override
    public synchronized void doCleanup() throws Exception {
        for(MessageTransport transport : _transports.values()) {
            try {
                transport.doCleanup();
            } catch(Exception e) {
                log.warn("transport '" + transport.getTransportIdentifier() + "' did not cleanup correctly", e);
            }
        }
        // TODO: maybe we should empty the transports map? Or might this bean be reused?
        super.doCleanup();
    }

    @Override
    public synchronized void doInit() throws Exception {
        super.doInit();
        log= thisAgent.getLog(this);
        
        for(Iterator<MessageTransport> iter= _transports.values().iterator(); iter.hasNext();) {
            MessageTransport transport= iter.next();
            transport.setDefaultDelegate(_defaultDelegate);
            try {
                transport.doInit();
            } catch (Exception e) {
                log.error("transport '" + transport.getTransportIdentifier() + "' did not initialised correctly -> will be removed", e);
                try {transport.doCleanup();} catch(Exception x){};
                iter.remove();
            }
        }
        
        // create the default message box for this agent
        establishMessageBox((_defaultMessageBox= CommunicationAddressFactory.createMessageBoxAddress(thisAgent.getAgentName())));
        
        if(_transports.size() <= 0) {
            log.warn("no transports available yet!");
        }
    }
    
    /**
     * Adds transport to the list of transports hold by this CommunicationBean
     * Notes: transport only added when not enlisted yet
     * 
     * @param transport	to add
     * @throws Exception is thrown if transport allready hold by this CommunicationBean
     */
    public synchronized void addTransport(MessageTransport transport) throws Exception {
        String id= transport.getTransportIdentifier();
        if(_transports.containsKey(id)) {
            throw new IllegalArgumentException("the transport '" + id +  "' already exists");
        }
        
        try {
            if(isActive()) {
                transport.setDefaultDelegate(_defaultDelegate);
                transport.doInit();
                registerAllToTransport(transport);
            }
        } finally {
            _transports.put(id, transport);
        }
    }
    
    
    /**
     * removes and cleans up a transport hold by this CommunicationBean
     * 
     * @param transportIdentifier of the transport to remove
     */
    public synchronized void removeTransport(String transportIdentifier) {
        MessageTransport transport= _transports.remove(transportIdentifier);
        
        if(transport == null) {
            return;
        }
   
        switch(getState()) {
            case INITIALIZED: case STARTED: case STOPPED: {
                try {
                    transport.doCleanup();
                } catch(Exception e) {
                    log.warn("transport '" + transportIdentifier + "' did not cleanup correctly", e);
                }
            }
        }
    }
    // ~ END OF CONFIGURATION AND INITIALISATION STUFF ~ //
    
    // ~ START OF ACTIONS ~ //
    
    /**
     * An invocation of this action will associate this agent with a group (a logical destination)
     * 
     * @param group  the group to join
     */
    @Expose
    public synchronized void joinGroup(IGroupAddress group) throws CommunicationException {
        register(group);
    }
    
    /**
     * An invocation of this action will remove this agent from the specified group.
     * 
     * @param group  the group to leave
     */
    @Expose
    public synchronized void leaveGroup(IGroupAddress group) throws CommunicationException {
        unregister(group);
    }
    
    /**
     * This action will create a new message box for this agent. Messages that are sent to it
     * will be received by this agent exclusivly.
     * 
     * @param messageBox    the address of the new message box
     */
    @Expose
    public synchronized void establishMessageBox(IMessageBoxAddress messageBox) throws CommunicationException {
        register(messageBox);
    }
    
    /**
     * This action destroys the message box with the specified address.
     * 
     * @param messageBox    the address to the message box which should be destroyed
     */
    @Expose
    public synchronized void destroyMessageBox(IMessageBoxAddress messageBox) throws CommunicationException {
        unregister(messageBox);
    }
    
    /**
     * This method sends a message to the given destination.
     * 
     * @param message   the message to send
     * @param address   the address to send to 
     */
    @Expose
    public synchronized void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException {
        if(message == null) {
            throw new IllegalArgumentException("message must not be null");
        }
        
        if(address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        
        internalSend(saveCast(message, JiacMessage.class), saveCast(address, CommunicationAddress.class));
    }
    
    /**
     * registers to a given Address and starts to listen to it.
     * @param address
     * @throws CommunicationException
     */
    @Expose
    public synchronized void register(ICommunicationAddress address) throws CommunicationException {
        if(address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        
        internalRegister(_defaultListener, saveCast(address, CommunicationAddress.class), null);
    }
    
    /**
     * stops to listen to communication on a given Address
     * @param address
     * @throws CommunicationException
     */
    @Expose
    public synchronized void unregister(ICommunicationAddress address) throws CommunicationException {
        if(address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        
        internalUnregister(_defaultListener, saveCast(address, CommunicationAddress.class), null);
    }
    // ~ END OF ACTIONS ~ //
    
    // ~ METHODS FOR LISTENER ADMINISTRATION ~ //
    
    /**
     * registers a given listener to an address
     * if all messages shall be received selector == null
     * Notes: Listener and either address or selector must not be null
     */
    public synchronized void register(IJiacMessageListener listener, ICommunicationAddress address, String selector) throws CommunicationException {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        
        if(address == null && selector == null) {
            throw new IllegalArgumentException("either the address or the selector have to be non-null");
        }
        
        internalRegister(listener, saveCast(address, CommunicationAddress.class), selector);
    }
    
    /**
     * register a listener to all addresses the commbean listens
     *
     * @param listener	where the messages should be delegated to
     * @param selector	filterstring for deciding which messages should be delegated
     * 					must not be null!
     * @throws CommunicationException
     */
    public synchronized void register(IJiacMessageListener listener, String selector) throws CommunicationException {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        
        if(selector == null) {
            throw new IllegalArgumentException("selector must not be null");
        }
        
        internalRegister(listener, null, selector);
    }
    
    /**
     * Unregisters a listener either from an address or from all messages 
     * associated with with a given selector
     * Notes: either address or selector must not be null
     * 
     * @param listener	that wants to unregister
     * @param address	The address the listener should stop listen to
     * @param selector	The selector given while the listener was registered (null if none was given)
     * @throws CommunicationException
     */
    public synchronized void unregister(IJiacMessageListener listener, ICommunicationAddress address, String selector) throws CommunicationException {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        
        if(address == null && selector == null) {
            throw new IllegalArgumentException("either the address or the selector have to be non-null");
        }
        
        internalUnregister(listener, saveCast(address, CommunicationAddress.class), selector);
    }
    
    /**
     * Unregisters a listener from listening to messages filtered with a given selector
     * 
     * @param listener the listener that should stop to listen
     * @param selector the selector given while the listener registered
     * @throws CommunicationException
     */
    public synchronized void unregister(IJiacMessageListener listener, String selector) throws CommunicationException {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        
        if(selector == null) {
            throw new IllegalArgumentException("selector must not be null");
        }
        
        internalUnregister(listener, null, selector);
    }
    // ~ END OF METHODS FOR LISTENER ADMINISTRATION ~ //
    
    
    // ~ INTERNAL METHODS ~ //
    /**
     * delegates received messages to the default listener
     */
    protected synchronized void processMessage(MessageTransport source, IJiacMessage message, CommunicationAddress at, String selector) {
        // TODO: built message procession
        try {
            _defaultListener.receive(message, at.bind(source.getTransportIdentifier()));
        } catch (URISyntaxException use) {
            // should not happen
            log.error("could not bind address to '" + source.getTransportIdentifier() + "'", use);
            _defaultListener.receive(message, at);
        }
    }
    
    /**
     * if an error occures....
     * 
     * @param source
     * @param error
     */
    protected void processError(MessageTransport source, Exception error) {
        // TODO: error handling
        log.error("message transport '" + source.getTransportIdentifier() + "' threw an exception", error);
    }
 
    /**
     * Assumes that both the message and the address are valid.
     */
    private synchronized void internalSend(JiacMessage message, CommunicationAddress address) throws CommunicationException {
        if(_transports.size() <= 0) {
            throw new CommunicationException("no transport available");
        }
        
        CommunicationAddress unboundAddress= address.toUnboundAddress();
        
        // first check whether the sender is correct
        if(message.getSender() == null || !_addressToListenerMap.containsKey(unboundAddress)) {
            message.setSender(_defaultMessageBox);
        }
        
        if(address instanceof MessageBoxAddress) {
            // 1:1 communication
            MessageTransport transport= null;
            if(address.isBoundToTransport()) {
                String transportId= address.toURI().getScheme();
                transport= _transports.get(transportId);
            } else {
                // TODO: lookup for transport
                transport= _transports.values().iterator().next();
            }
            
            if(transport != null) {
                transport.send(message, unboundAddress);
            } else {
                throw new CommunicationException("does not have transport for '" + address + "'");
            }
        } else {
            // 1:n communication
            for(MessageTransport transport : _transports.values()) {
                transport.send(message, unboundAddress);
            }
        }
    }
    
    /**
     * Assumes that the listener is non-null and either the address or the selector is non-null.
     */
    private synchronized void internalRegister(IJiacMessageListener listener, CommunicationAddress address, String selector) throws CommunicationException {
        if(address == null) {
            WildcardListenerContext context= _selectorToListenerMap.get(selector);
            if(context != null) {
                return;
            }
            
            context= new WildcardListenerContext(listener, selector);
            if(isActive()) {
                for(ICommunicationAddress registered : _addressToListenerMap.keySet()) {
                    List<ListenerContext> registeredContexts= _addressToListenerMap.get(registered);
                    try {
                        for(MessageTransport transport : _transports.values()) {
                            transport.listen(registered, context.selector);
                        }
                    } finally {
                        registeredContexts.add(context);
                    }
                }
            }
        } else {
            CommunicationAddress unboundAddress= address.toUnboundAddress();
            ListenerContext context= new ListenerContext(listener, selector);
            List<ListenerContext> registeredContexts= _addressToListenerMap.get(unboundAddress);
            
            if(registeredContexts == null) {
                registeredContexts= new LinkedList<ListenerContext>();
                _addressToListenerMap.put(unboundAddress, registeredContexts);
            }
            
            if(registeredContexts.contains(context) && context.listener != listener) {
                return;
            }
            
            registeredContexts.add(context);
            
            if(isActive()) {
                for(MessageTransport transport : _transports.values()) {
                    transport.listen(address, context.selector);
                }
            }
        }
    }
    
    /**
     * registers all addresses the CommunicationBean listens to, on a transport, so
     * that the transport will listen to it.
     * 
     * @param transport	the transport to register the addresses to
     * @throws CommunicationException
     */
    private synchronized void registerAllToTransport(MessageTransport transport) throws CommunicationException {
        for(ICommunicationAddress address : _addressToListenerMap.keySet()) {
            for(ListenerContext context : _addressToListenerMap.get(address)) {
                transport.listen(address, context.selector);
            }
        }
    }
    
    /**
     * Assumse that the listener is non-null and either the address or the selector is non-null.
     */
    private synchronized void internalUnregister(IJiacMessageListener listener, CommunicationAddress address, String selector) throws CommunicationException {
        if(address == null) {
            WildcardListenerContext context= _selectorToListenerMap.get(selector);
            if(context == null) {
                return;
            }
            
            if(isActive()) {
                
            }
        }
    }
    
    private boolean isActive() {
        switch(getState()) {
            case INITIALIZING: case INITIALIZED: case STARTING: case STARTED:
                return true;
            default:
                return false;
        }
    }
}

class ListenerContext {
    public static final Comparator<ListenerContext> COMPARATOR= new Comparator<ListenerContext>() {
        public int compare(ListenerContext o1, ListenerContext o2) {
            // TODO Auto-generated method stub
            return 0;
        }
    };
    
    protected final IJiacMessageListener listener;
    protected final String selector;
    
    ListenerContext(IJiacMessageListener listener, String selector) {
        this.listener= listener;
        this.selector= selector;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        
        ListenerContext other= (ListenerContext) obj;
        return selector == null ? other.selector == null : other.selector == null ? false : selector.equals(other.selector);
    }
}

class WildcardListenerContext extends ListenerContext {
    protected final Set<ICommunicationAddress> blacklist;
    
    WildcardListenerContext(IJiacMessageListener listener, String selector) {
        super(listener, selector);
        blacklist= new HashSet<ICommunicationAddress>();
    }
}
