/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.AbstractMessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.AbstractMessageTransport.IMessageTransportDelegate;

/**
 * This bean specifies the way an agent communicates. It implements a message-based approach for information
 * exchange and group administration.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class CommunicationBean extends AbstractMethodExposingBean {
    private final class MessageTransportDelegate implements IMessageTransportDelegate {
        public void onAsynchronousException(AbstractMessageTransport source, Exception e) {
            processError(source, e);
        }

        public void onMessage(AbstractMessageTransport source, IJiacMessage message, ICommunicationAddress at, String selector) {
            processMessage(source, message, at, selector);
        }
    }
    
    private final class MemoryDelegationMessageListener implements IJiacMessageListener {
        public void receive(IJiacMessage message, ICommunicationAddress from) {
            memory.write(message);
        }
    }
    
    private final IJiacMessageListener _defaultListener;
    private final IMessageTransportDelegate _defaultDelegate;
    
    private Map<String, AbstractMessageTransport> _transports;
    
    private final Map<String, Set<ListenerContext>> _selectorToListenerMap;
    private final Map<CommunicationAddress, Set<ListenerContext>> _addressToListenerMap;
    
    private Log _log;
    
    private boolean _delayNotification= false;
    
    public CommunicationBean() {
        _defaultListener= new MemoryDelegationMessageListener();
        _defaultDelegate= new MessageTransportDelegate();
        _transports= new HashMap<String, AbstractMessageTransport>();
        _selectorToListenerMap= new HashMap<String, Set<ListenerContext>>();
        _addressToListenerMap= new HashMap<CommunicationAddress, Set<ListenerContext>>();
    }

    // ~ START OF CONFIGURATION AND INITIALISATION STUFF ~ //
    public synchronized void setTransports(Set<AbstractMessageTransport> transports) {
        Set<AbstractMessageTransport> workingCopy;
        if(transports == null) {
            workingCopy= Collections.emptySet();
        } else {
            workingCopy= new HashSet<AbstractMessageTransport>();
            workingCopy.addAll(transports);
        }
        try {
            _delayNotification= true;
            // first remove all existing transports
            if(_transports.size() > 0) {
                Set<AbstractMessageTransport> toRemove= new HashSet<AbstractMessageTransport>();
                toRemove.addAll(_transports.values());
                // only remove transports that are not in the workingCopy
                toRemove.removeAll(workingCopy);
                
                // only add transports that are not yet installed
                workingCopy.removeAll(_transports.values());
                
                for(AbstractMessageTransport transport : toRemove) {
                    removeTransport(transport.getTransportIdentifier());
                }
            }
            
            for(AbstractMessageTransport transport : workingCopy) {
                addTransport(transport);
            }
        } finally {
            _delayNotification= false;
        }
    }
    
    @Override
    public synchronized void doCleanup() throws Exception {
        for(AbstractMessageTransport transport : _transports.values()) {
            try{
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
        
        for(Iterator<AbstractMessageTransport> iter= _transports.values().iterator(); iter.hasNext();) {
            AbstractMessageTransport transport= iter.next();
            transport.setDefaultDelegate(_defaultDelegate);
            try {
                transport.doInit();
            } catch (Exception e) {
                _log.error("transport '" + transport.getTransportIdentifier() + "' did not initialise correctly -> remove it", e);
                try {transport.doCleanup();} catch(Exception x){};
                iter.remove();
            }
        }
        
        if(_transports.size() <= 0) {
            _log.warn("no transports available yet!");
        }
    }
    
    public synchronized void addTransport(AbstractMessageTransport transport) {
        String id= transport.getTransportIdentifier();
        if(_transports.containsKey(id)) {
            throw new IllegalArgumentException("the transport '" + id +  "' already exists");
        }
        
        switch(getState()) {
            case CLEANED_UP: case CLEANING_UP: {
                throw new IllegalStateException("you cannot add any further transport while in destruction state");
            }

            case INITIALIZED: case STARTED: {
                try {
                    transport.setDefaultDelegate(_defaultDelegate);
                    transport.doInit();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // fall through
            }
            default: {
                _transports.put(id, transport);
            }
        }
    }
    
    public synchronized void removeTransport(String transportIdentifier) {
        AbstractMessageTransport transport= _transports.remove(transportIdentifier);
        
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
        
        if(_transports.size() <= 0) {
            throw new CommunicationException("no transport available");
        }
        
//        if(address instanceof IMessageBoxAddress) {
//            // first look which capabilities the communication bean at the specified location has
//        } else {
//            // TODO: Broker capabilities... now we just send the message through all transports
//            for(AbstractMessageTransport transport : _transports.values()) {
//                transport.send(message, address);
//            }
//        }
        
        // TODO: lookup stuff
        for(AbstractMessageTransport transport : _transports.values()) {
            transport.send(message, address);
        }
    }
    
    @Expose
    public synchronized void register(ICommunicationAddress address) throws CommunicationException {
        if(address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        
        internalRegister(_defaultListener, address, null);
    }
    
    @Expose
    public synchronized void unregister(ICommunicationAddress address) throws CommunicationException {
        if(address == null) {
            throw new IllegalArgumentException("address must not be null");
        }
        
        internalUnregister(_defaultListener, address, null);
    }
    // ~ END OF ACTIONS ~ //
    
    // ~ METHODS FOR LISTENER ADMINISTRATION ~ //
    public synchronized void register(IJiacMessageListener listener, ICommunicationAddress address, String selector) throws CommunicationException {
        if(listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        
        if(address == null && selector == null) {
            throw new IllegalArgumentException("either the address or the selector have to non-null");
        }
        
        internalRegister(listener, address, selector);
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
            throw new IllegalArgumentException("either the address or the selector have to non-null");
        }
        
        internalUnregister(listener, address, selector);
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
    
    protected void processMessage(AbstractMessageTransport source, IJiacMessage message, ICommunicationAddress at, String selector) {
        
    }
    
    protected void processError(AbstractMessageTransport source, Exception error) {
        
    }
    
    private void internalRegister(IJiacMessageListener listener, ICommunicationAddress address, String selector) throws CommunicationException {
        ListenerContext context= new ListenerContext(listener, address, selector);
        if(selector != null) {
            Set<ListenerContext> registered= _selectorToListenerMap.get(selector);
            if(registered == null) {
                registered= new HashSet<ListenerContext>();
//                registered
            }
        } else {
            
        }
    }
    
    private void internalUnregister(IJiacMessageListener listener, ICommunicationAddress address, String selector) throws CommunicationException {
        
    }
}

class ListenerContext {
    private final IJiacMessageListener _listener;
    private ICommunicationAddress _address;
    private final String _selector;
    
    ListenerContext(IJiacMessageListener listener, ICommunicationAddress address, String selector) {
        _listener= listener;
        _address= address;
        _selector= selector;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
