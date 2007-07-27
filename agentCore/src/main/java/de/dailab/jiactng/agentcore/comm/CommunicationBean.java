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

import org.apache.commons.logging.Log;

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
    private static <T> T saveCast(Object object, Class<T> targetType) {
        if(!targetType.isInstance(object)) {
            throw new IllegalArgumentException("argument is not valid");
        }
        
        return targetType.cast(object);
    }
    
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
    
    private Log _log;
    
    public CommunicationBean() {
        _defaultListener= new MemoryDelegationMessageListener();
        _defaultDelegate= new MessageTransportDelegate();
        _transports= new HashMap<String, MessageTransport>();
        _selectorToListenerMap= new HashMap<String, WildcardListenerContext>();
        _addressToListenerMap= new HashMap<CommunicationAddress, List<ListenerContext>>();
    }

    // ~ START OF CONFIGURATION AND INITIALISATION STUFF ~ //
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
            
            for(MessageTransport transport : toRemove) {
                removeTransport(transport.getTransportIdentifier());
            }
        }
        
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
                _log.warn("transport '" + transport.getTransportIdentifier() + "' did not cleanup correctly", e);
            }
        }
        // TODO: maybe we should empty the transports map? Or might this bean be reused?
        super.doCleanup();
    }

    @Override
    public synchronized void doInit() throws Exception {
        super.doInit();
        _log= thisAgent.getLog(this);
        
        for(Iterator<MessageTransport> iter= _transports.values().iterator(); iter.hasNext();) {
            MessageTransport transport= iter.next();
            transport.setDefaultDelegate(_defaultDelegate);
            try {
                transport.doInit();
            } catch (Exception e) {
                _log.error("transport '" + transport.getTransportIdentifier() + "' did not initialise correctly -> remove it", e);
                try {transport.doCleanup();} catch(Exception x){};
                iter.remove();
            }
        }
        
        // create the default message box for this agent
        establishMessageBox((_defaultMessageBox= CommunicationAddressFactory.createMessageBoxAddress(thisAgent.getAgentName())));
        
        if(_transports.size() <= 0) {
            _log.warn("no transports available yet!");
        }
    }
    
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
                    _log.warn("transport '" + transportIdentifier + "' did not cleanup correctly", e);
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
     * @param address   the address which points 
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
    
    @Expose
    public synchronized void register(ICommunicationAddress address) throws CommunicationException {
        if(address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        
        internalRegister(_defaultListener, saveCast(address, CommunicationAddress.class), null);
    }
    
    @Expose
    public synchronized void unregister(ICommunicationAddress address) throws CommunicationException {
        if(address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        
        internalUnregister(_defaultListener, saveCast(address, CommunicationAddress.class), null);
    }
    // ~ END OF ACTIONS ~ //
    
    // ~ METHODS FOR LISTENER ADMINISTRATION ~ //
    public synchronized void register(IJiacMessageListener listener, ICommunicationAddress address, String selector) throws CommunicationException {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        
        if(address == null && selector == null) {
            throw new IllegalArgumentException("either the address or the selector have to be non-null");
        }
        
        internalRegister(listener, saveCast(address, CommunicationAddress.class), selector);
    }
    
    
    public synchronized void register(IJiacMessageListener listener, String selector) throws CommunicationException {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        
        if(selector == null) {
            throw new IllegalArgumentException("selector must not be null");
        }
        
        internalRegister(listener, null, selector);
    }
    
    public synchronized void unregister(IJiacMessageListener listener, ICommunicationAddress address, String selector) throws CommunicationException {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        
        if(address == null && selector == null) {
            throw new IllegalArgumentException("either the address or the selector have to be non-null");
        }
        
        internalUnregister(listener, saveCast(address, CommunicationAddress.class), selector);
    }
    
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
    protected synchronized void processMessage(MessageTransport source, IJiacMessage message, CommunicationAddress at, String selector) {
        // TODO: built message procession
        try {
            _defaultListener.receive(message, at.bind(source.getTransportIdentifier()));
        } catch (URISyntaxException use) {
            // should not happen
            _log.error("could not bind address to '" + source.getTransportIdentifier() + "'", use);
            _defaultListener.receive(message, at);
        }
    }
    
    protected void processError(MessageTransport source, Exception error) {
        // TODO: error handling
        _log.error("message transport '" + source.getTransportIdentifier() + "' threw an exception", error);
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
