/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.util.ReferenceEqualityCheckSet;

/**
 * This bean specifies the way an agent communicates. It implements a message-based approach for information exchange
 * and group administration.
 * 
 * @author Marcel Patzlaff
 * @author Martin Loeffelholz
 * @version $Revision$
 */
public class CommunicationBean extends AbstractMethodExposingBean implements CommunicationBeanMBean {
    /**
     * a save way to cast from object to targetType
     * 
     * @param <T>
     * @param object
     * @param targetType
     * @return
     */
    private static <T> T saveCast(Object object, Class<T> targetType) {
        if (!targetType.isInstance(object)) {
            throw new IllegalArgumentException("argument is not valid");
        }

        return targetType.cast(object);
    }

    /**
     * Helpclass used to manage incoming messages and exceptions, routing them to processError or processMessage
     * 
     */
    private final class MessageTransportDelegate implements IMessageTransportDelegate {
        public void onAsynchronousException(MessageTransport source, Exception e) {
            processError(source, e);
        }

        public void onMessage(MessageTransport source, IJiacMessage message, ICommunicationAddress at) {
            processMessage(source, message, saveCast(at.toUnboundAddress(), CommunicationAddress.class));
        }

        public Log getLog(String extension) {
            return thisAgent.getLog(CommunicationBean.this, extension);
        }
    }

    private final class MemoryDelegationMessageListener implements IJiacMessageListener {
        public void receive(IJiacMessage message, ICommunicationAddress from) {
            if (log.isDebugEnabled()) {
                log.debug("writing message '" + message.toString() + "' at '" + from + "' into memory");
            }
            memory.write(message);
        }
    }

    private final IJiacMessageListener _defaultListener;
    private final IMessageTransportDelegate _defaultDelegate;

    private IMessageBoxAddress _defaultMessageBox;

    private Map<String, MessageTransport> _transports;
    protected final Map<ICommunicationAddress, List<ListenerContext>> addressToListenerMap;

    public CommunicationBean() {
        _defaultListener = new MemoryDelegationMessageListener();
        _defaultDelegate = new MessageTransportDelegate();
        _transports = new HashMap<String, MessageTransport>();
        addressToListenerMap = new Hashtable<ICommunicationAddress, List<ListenerContext>>();
    }

    // ~ START OF CONFIGURATION AND INITIALISATION STUFF ~ //
    /**
     * sets transports to the set given. All transports allready set which are not within the set given as parameter
     * will be removed.
     */
    public synchronized void setTransports(Set<MessageTransport> transports) throws Exception {
        if ((log != null) && log.isInfoEnabled()) {
            log.info("CommunicationBean is setting it's transports to: " + transports.toString());
        }

        Set<MessageTransport> workingCopy;
        if (transports == null) {
            workingCopy = Collections.emptySet();
        } else {
            workingCopy = new HashSet<MessageTransport>();
            workingCopy.addAll(transports);
        }

        // first remove all existing transports
        if (_transports.size() > 0) {
            Set<MessageTransport> toRemove = new HashSet<MessageTransport>();
            toRemove.addAll(_transports.values());
            // only remove transports that are not in the workingCopy
            toRemove.removeAll(workingCopy);

            // only add transports that are not yet installed
            workingCopy.removeAll(_transports.values());

            // remove transports other than which we want to set
            for (MessageTransport transport : toRemove) {
                removeTransport(transport.getTransportIdentifier());
            }
        }
        // now add what has to be add
        for (MessageTransport transport : workingCopy) {
            addTransport(transport);
        }
    }

    @Override
    public void doCleanup() throws Exception {
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean starts commencing cleanup");
            log.info("Cleaning up transports");
        }
        synchronized (_transports) {
            for (MessageTransport transport : _transports.values()) {
                try {
                    transport.doCleanup();
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("transport '" + transport.getTransportIdentifier() + "' did not cleanup correctly", e);
                    }
                }
            }
        }
        // TODO: maybe we should empty the transports map? Or might this bean be reused?
        super.doCleanup();
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean commenced cleanup");
        }
    }

    @Override
    public synchronized void doInit() throws Exception {
        if ((log != null) && log.isInfoEnabled()) {
            log.info("CommunicationBean begins with initialization");
        }
        super.doInit();

        log.info("initializing Transports");

        for (Iterator<MessageTransport> iter = _transports.values().iterator(); iter.hasNext();) {
            MessageTransport transport = iter.next();
            transport.setDefaultDelegate(_defaultDelegate);
            try {
                transport.doInit();
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("transport '" + transport.getTransportIdentifier()
                            + "' did not initialised correctly -> will be removed", e);
                }
                try {
                    transport.doCleanup();
                } catch (Exception x) {
                }
                ;
                iter.remove();

                // deregister message transport from management
                deregisterTransport(transport.getTransportIdentifier());
            }
        }

        // create the default message box for this agent
        establishMessageBox((_defaultMessageBox = CommunicationAddressFactory.createMessageBoxAddress(thisAgent
                .getAgentName())));

        if (_transports.size() <= 0) {
            log.warn("no transports available yet!");
        }

        log.info("CommunicationBean has finished it's initialization");

    }

    /**
     * Adds transport to the list of transports hold by this CommunicationBean Notes: transport only added when not
     * enlisted yet
     * 
     * @param transport
     *            to add
     * @throws Exception
     *             is thrown if transport allready hold by this CommunicationBean
     */
    public synchronized void addTransport(MessageTransport transport) throws Exception {
        if ((log != null) && log.isInfoEnabled()) {
            log.info("Adding Transport '" + transport + "' to CommunicationBean");
        }

        String id = transport.getTransportIdentifier();
        if (_transports.containsKey(id)) {
            throw new IllegalArgumentException("the transport '" + id + "' already exists");
        }

        try {
            if (isActive()) {
                // init message transport
                transport.setDefaultDelegate(_defaultDelegate);
                transport.doInit();
                registerAllToTransport(transport);
            }
        } finally {
            // add message transport
            _transports.put(id, transport);

            // register message transport for management
            registerTransport(transport);
        }
    }

    /**
     * removes and cleans up a transport hold by this CommunicationBean
     * 
     * @param transportIdentifier
     *            of the transport to remove
     */
    public synchronized void removeTransport(String transportIdentifier) {
        // remove message transport
        MessageTransport transport = _transports.remove(transportIdentifier);

        if (transport == null) {
            if (log.isWarnEnabled()) {
                log.warn("Transport '" + transportIdentifier + "' not found to remove from CommunicationBean");
            }
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("Transport '" + transportIdentifier + "' removed from CommunicationBean");
        }

        // deregister message transport from management
        deregisterTransport(transportIdentifier);

        // cleanup message transport
        switch (getState()) {
        case INITIALIZED:
        case STARTED:
        case STOPPED: {
            try {
                transport.doCleanup();
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("transport '" + transportIdentifier + "' did not cleanup correctly", e);
                }
            }
        }
        }
    }

    // ~ END OF CONFIGURATION AND INITIALISATION STUFF ~ //

    // ~ START OF ACTIONS ~ //
    @Expose
    public synchronized IMessageBoxAddress getDefaultMessageBoxAddress() {
        return _defaultMessageBox;
    }

    /**
     * An invocation of this action will associate this agent with a group (a logical destination)
     * 
     * @param group         the group to join
     */
    @Expose
    public synchronized void joinGroup(IGroupAddress group) throws CommunicationException {
        register(group);
    }

    /**
     * An invocation of this action will remove this agent from the specified group.
     * 
     * @param group         the group to leave
     */
    @Expose
    public synchronized void leaveGroup(IGroupAddress group) throws CommunicationException {
        unregister(group);
    }

    /**
     * This action will create a new message box for this agent. Messages that are sent to it will be received by this
     * agent exclusivly.
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
     * @param message       the message to send
     * @param address       the address to send to
     */
    @Expose
    public synchronized void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }

        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalSend(saveCast(message, JiacMessage.class), saveCast(address, CommunicationAddress.class));
    }

    /**
     * registers to a given Address and starts to listen to it.
     * 
     * @param address
     * @throws CommunicationException
     */
    @Expose
    public synchronized void register(ICommunicationAddress address) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean begins to listen at address '" + address + "'");
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalRegister(_defaultListener, saveCast(address, CommunicationAddress.class), null);
    }

    /**
     * stops to listen to communication on a given Address
     * 
     * @param address
     * @throws CommunicationException
     */
    @Expose
    public synchronized void unregister(ICommunicationAddress address) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean stops to listen at address '" + address + "'");
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalUnregister(_defaultListener, saveCast(address, CommunicationAddress.class), null);
    }

    // ~ END OF ACTIONS ~ //

    // ~ METHODS FOR LISTENER ADMINISTRATION ~ //

    /**
     * registers a given listener to an address if all messages shall be received selector == null Notes: Listener and
     * either address or selector must not be null
     */
    public synchronized void register(IJiacMessageListener listener, ICommunicationAddress address, Selector selector) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("Listener '" + listener + "' begins to listen at address '" + address + "' with Selector '" + selector + "'");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }

        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalRegister(listener, saveCast(address, CommunicationAddress.class), selector);
    }

    /**
     * Unregisters a listener either from an address or from all messages associated with with a given selector Notes:
     * either address or selector must not be null
     * 
     * @param listener
     *            that wants to unregister
     * @param address
     *            The address the listener should stop listen to
     * @param selector
     *            The selector given while the listener was registered (null if none was given)
     * @throws CommunicationException
     */
    public synchronized void unregister(IJiacMessageListener listener, ICommunicationAddress address, Selector selector) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("Listener '" + listener + "' stops to listen at address '" + address + "' with selector '" + selector + "'");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }

        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalUnregister(listener, saveCast(address, CommunicationAddress.class), selector);
    }

    // ~ END OF METHODS FOR LISTENER ADMINISTRATION ~ //

    // ~ INTERNAL METHODS ~ //
    /**
     * delegates received messages to the default listener
     */
    protected void processMessage(MessageTransport source, IJiacMessage message, CommunicationAddress at) {
        if (log.isDebugEnabled()) {
            log.debug("CommunicationBean is receiving Message over transport '" + source.getTransportIdentifier() + "' from '" + at + "'");
        }

        if (log.isDebugEnabled()) {
            log.debug("received message ' " + message + "' at '" + at + "'");
        }
        Set<IJiacMessageListener> notified = new ReferenceEqualityCheckSet<IJiacMessageListener>();
        CommunicationAddress boundAddress = null;
        try {
            boundAddress = at.bind(source.getTransportIdentifier());
        } catch (URISyntaxException use) {
            if (log.isWarnEnabled()) {
                log.warn("could not bound address '" + at + "' to transport '" + source.getTransportIdentifier() + "'", use);
            }
            boundAddress = at;
        }

        synchronized (addressToListenerMap) {
            for (ListenerContext context : addressToListenerMap.get(at)) {
                boolean notify = false;

                if (context.selector != null) {
                    String header = message.getHeader(context.selector.getKey());

                    if (header != null && header.equals(context.selector.getValue())) {
                        notify = true;
                    }
                } else {
                    // we have a generic listener with no selector...
                    notify = true;
                }
                if (notify) {
                    for (IJiacMessageListener current : context.listeners) {
                        if (current != _defaultListener && notified.add(current)) {
                            try {
                                current.receive(message, boundAddress);
                            } catch (RuntimeException re) {
                                if (log.isWarnEnabled()) {
                                    log.warn("listener threw a runtime exception", re);
                                }
                            }
                        }
                    }
                }
            }
        }

        // if no appropriate listener was found
        if (notified.size() <= 0) {
            _defaultListener.receive(message, boundAddress);
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
        if (log.isErrorEnabled()) {
            log.error("message transport '" + source.getTransportIdentifier() + "' threw an exception", error);
        }
    }

    /**
     * Assumes that both the message and the address are valid.
     */
    private synchronized void internalSend(JiacMessage message, CommunicationAddress address)
            throws CommunicationException {
        if (_transports.size() <= 0) {
            throw new CommunicationException("no transport available");
        }

        if (log.isDebugEnabled()) {
            log.debug("send message...");
        }
        CommunicationAddress unboundAddress = address.toUnboundAddress();

        // first check whether the sender is correct
        if (message.getSender() == null || !addressToListenerMap.containsKey(unboundAddress)) {
            message.setSender(_defaultMessageBox);
        }

        if (address instanceof MessageBoxAddress) {
            if (log.isDebugEnabled()) {
                log.debug("address is a message box address -> choosing one transport");
            }
            // 1:1 communication
            MessageTransport transport = null;
            if (address.isBoundToTransport()) {
                String transportId = address.toURI().getScheme();
                transport = _transports.get(transportId);
            } else {
                // TODO: lookup for transport
                transport = _transports.values().iterator().next();
            }

            if (transport != null) {
                transport.send(message, unboundAddress);
            } else {
                throw new CommunicationException("does not have transport for '" + address + "'");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("address is a group address -> choosing all transports");
            }
            // 1:n communication
            for (MessageTransport transport : _transports.values()) {
                transport.send(message, unboundAddress);
            }
        }
    }

    /**
     * Assumes that the listener and the address are non-null.
     */
    private synchronized void internalRegister(IJiacMessageListener listener, CommunicationAddress address, Selector selector) throws CommunicationException {
        CommunicationAddress unboundAddress = address.toUnboundAddress();
        ListenerContext context = new ListenerContext(selector);
        List<ListenerContext> registeredContexts = addressToListenerMap.get(unboundAddress);

        if (registeredContexts != null) {
            // we have already some listener registered for this communication address
            int index = registeredContexts.indexOf(context);

            if (index >= 0) {
                // there is already another listener with same address and selector registered
                context = registeredContexts.get(index);

                // check whether the listener is new, otherwise ignore this registration
                if(context.listeners.contains(listener)) {
                    if (log.isWarnEnabled()) {
                        log.warn("listener is already registered for '" + unboundAddress + "' : '" + selector + "'");
                    }
                    return;
                }

                if (log.isDebugEnabled()) {
                    log.debug("added further listener for '" + unboundAddress + "' : '" + selector + "'");
                }
                
                // transports are already notified so we just add the new listener into the list
                context.listeners.add(listener);
                return;
            }
        }
        
        /*
         * here we have two cases:
         * 1) there are listeners for the address but non with the specified selector
         * 2) there are no listeners for the address
         */
        if(registeredContexts == null) {
            // currently there are no listeners registered for this address - selector combination
            if (log.isDebugEnabled()) {
                log.debug("first listener for '" + unboundAddress + "'");
            }
            registeredContexts= new LinkedList<ListenerContext>();
            addressToListenerMap.put(unboundAddress, registeredContexts);
        }
        
        registeredContexts.add(context);
        context.listeners.add(listener);

        if (log.isDebugEnabled()) {
            log.debug("registering new address and listeners on transports");
        }

        // first give our new listener something to listen to
        if (isActive()) {
            if (log.isDebugEnabled()) {
                log.debug("isActive -> registering listeners for new address '" + unboundAddress + "'");
            }
            for (MessageTransport transport : _transports.values()) {
                transport.listen(address, context.selector);
            }
        }
    }

    /**
     * registers all addresses the CommunicationBean listens to, on a transport, so that the transport will listen to
     * it.
     * 
     * @param transport
     *            the transport to register the addresses to
     * @throws CommunicationException
     */
    private synchronized void registerAllToTransport(MessageTransport transport) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("Registering all addresses and listeners on transport '" + transport.getTransportIdentifier()
                    + "'");
        }
        for (ICommunicationAddress address : addressToListenerMap.keySet()) {
            for (ListenerContext context : addressToListenerMap.get(address)) {
                transport.listen(address, context.selector);
            }
        }
    }

    /**
     * Assumes that the listener and the address are non-null.
     */
    private synchronized void internalUnregister(IJiacMessageListener listener, CommunicationAddress address, Selector selector) throws CommunicationException {
        CommunicationAddress unboundAddress = address.toUnboundAddress();
        List<ListenerContext> registeredContexts = addressToListenerMap.remove(unboundAddress);

        if (log.isDebugEnabled()) {
            log.debug("Removing nonWildcardListener with address '" + address + "' and selector '" + selector + "'");
        }

        if (registeredContexts == null) {
            if (log.isWarnEnabled()) {
                log.warn("Aborted Unregister: There is no listener registered for this address '" + address + "'");
            }
            return;
        }
        
        ListenerContext context = new ListenerContext(selector); // template to find it fast
        int index = registeredContexts.indexOf(context);

        if(index >= 0) {
            context= registeredContexts.remove(index);
            // remove listener from list
            if(!context.listeners.remove(listener)) {
                if(log.isWarnEnabled()) {
                    log.warn("Aborted Unregister: Listener was not registered for '" + unboundAddress + "' : '" + selector + "'");
                }
                return;
            }
        } else {
            if(log.isWarnEnabled()) {
                log.warn("Aborted Unregister: There is no listener registered for this address '" + address + "' and selector '" + selector + "'");
            }
            return;
        }
        
        if(context.listeners.size() <= 0) {
            if(isActive()) {
                // remove registration
                for(MessageTransport transport : _transports.values()) {
                    transport.stopListen(unboundAddress, context.selector);
                }
            }
        } else {
            // re-add context because it is still not empty
            registeredContexts.add(context);
        }
        
        if(registeredContexts.size() > 0) {
            // re-add context list because it is still not empty
            addressToListenerMap.put(unboundAddress, registeredContexts);
        }
    }

    private boolean isActive() {
        switch (getState()) {
        case INITIALIZING:
        case INITIALIZED:
        case STARTING:
        case STARTED:
            return true;
        default:
            return false;
        }
    }

    /**
     * Registers the communication bean and all message transports for management
     * 
     * @param manager
     *            the manager to be used for registration
     */
    public void enableManagement(Manager manager) {
        // do nothing if management already enabled
        if (isManagementEnabled()) {
            return;
        }

        // register communication bean
        super.enableManagement(manager);

        // register all message transports for management
        for (MessageTransport transport : _transports.values()) {
            registerTransport(transport);
        }
    }

    /**
     * Deregisters the communication bean and all message transports from management
     */
    public void disableManagement() {
        // do nothing if management already disabled
        if (!isManagementEnabled()) {
            return;
        }

        // deregister all message transports from management
        for (String transportId : _transports.keySet()) {
            deregisterTransport(transportId);
        }

        super.disableManagement();
    }

    /**
     * Registers a message transport for management.
     * 
     * @param transport
     *            the message transport to be registered
     */
    private final void registerTransport(MessageTransport transport) {
        // do nothing if management is not enabled
        if (!isManagementEnabled()) {
            return;
        }

        // register message transport for management
        try {
            _manager.registerAgentBeanResource(this, thisAgent, "MessageTransport", transport.getTransportIdentifier(),
                    transport);
        } catch (Exception e) {
            if ((log != null) && (log.isErrorEnabled())) {
                log.error("WARNING: Unable to register message transport " + transport.getTransportIdentifier()
                        + " of agent bean " + beanName + " of agent " + thisAgent.getAgentName() + " of agent node "
                        + thisAgent.getAgentNode().getName() + " as JMX resource.");
                log.error(e.getMessage());
            } else {
                System.err.println("WARNING: Unable to register message transport "
                        + transport.getTransportIdentifier() + " of agent bean " + beanName + " of agent "
                        + thisAgent.getAgentName() + " of agent node " + thisAgent.getAgentNode().getName()
                        + " as JMX resource.");
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Deregisters a message transport from management.
     * 
     * @param transportId
     *            the identifier of the message transport
     */
    private final void deregisterTransport(String transportId) {
        // do nothing if management is not enabled
        if (!isManagementEnabled()) {
            return;
        }

        // deregister message transport from management
        try {
            _manager.unregisterAgentBeanResource(this, thisAgent, "MessageTransport", transportId);
        } catch (Exception e) {
            if ((log != null) && (log.isErrorEnabled())) {
                log.error("WARNING: Unable to deregister message transport " + transportId + " of agent bean "
                        + beanName + " of agent " + thisAgent.getAgentName() + " of agent node "
                        + thisAgent.getAgentNode().getName() + " as JMX resource.");
                log.error(e.getMessage());
            } else {
                System.err.println("WARNING: Unable to deregister message transport " + transportId + " of agent bean "
                        + beanName + " of agent " + thisAgent.getAgentName() + " of agent node "
                        + thisAgent.getAgentNode().getName() + " as JMX resource.");
                System.err.println(e.getMessage());
            }
        }
    }

    public CompositeData getSelectorsOfAddresses() {
        CompositeData data = null;
        int size = addressToListenerMap.size();
        String[] itemNames = new String[size];
        OpenType[] itemTypes = new OpenType[size];
        Object[] itemValues = new Object[size];
        Object[] addresses = addressToListenerMap.keySet().toArray();
        try {
            for (int i = 0; i < size; i++) {
                ICommunicationAddress address = (ICommunicationAddress) addresses[i];
                itemNames[i] = address.getName();
                itemTypes[i] = new ArrayType(1, SimpleType.STRING);
                List<ListenerContext> values = addressToListenerMap.get(address);
                String[] value = new String[values.size()];
                Iterator<ListenerContext> it = values.iterator();
                int j = 0;
                while (it.hasNext()) {
                    Selector selector = it.next().selector;
                    if (selector == null) {
                        value[j] = "null";
                    } else {
                        value[j] = selector.toString();
                    }
                    j++;
                }
                itemValues[i] = value;
            }
            CompositeType compositeType = new CompositeType(addressToListenerMap.getClass().getName(),
                    "addresses of the communication bean", itemNames, itemNames, itemTypes);
            data = new CompositeDataSupport(compositeType, itemNames, itemValues);
        } catch (OpenDataException e) {
            e.printStackTrace();
        }
        return data;
    }
}

class ListenerContext {
    protected final ReferenceEqualityCheckSet<IJiacMessageListener> listeners;

    protected final Selector selector;

    ListenerContext(Selector selector) {
        this.selector = selector;
        listeners = new ReferenceEqualityCheckSet<IJiacMessageListener>();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ListenerContext)) {
            return false;
        }

        ListenerContext other = (ListenerContext) obj;
        return selector == null ? other.selector == null
                                : other.selector == null ? false 
                                                         : selector.equals(other.selector);
    }

    @Override
    public String toString() {
        return selector == null ? "*" : selector.toString();
    }
}

