/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
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
import de.dailab.jiactng.agentcore.management.jmx.MessageExchangeNotification;
import de.dailab.jiactng.agentcore.management.jmx.MessageExchangeNotification.MessageExchangeAction;

/**
 * This bean specifies the way an agent communicates. It implements a message-based approach for information exchange
 * and group administration.
 * 
 * @author Marcel Patzlaff
 * @author Martin Loeffelholz
 * @version $Revision$
 */
public final class CommunicationBean extends AbstractMethodExposingBean implements ICommunicationBean, CommunicationBeanMBean {
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
    
    private static IJiacMessage cloneTemplate(IJiacMessage template) {
        if(template == null) {
            return null;
        }
        
        final JiacMessage result= new JiacMessage();
        
        // only header fields are cloned...
        for(String key : template.getHeaderKeys()) {
            result.setHeader(key, template.getHeader(key));
        }
        
        return result;
    }

    /**
     * Helpclass used to manage incoming messages and exceptions, routing them to processError or processMessage
     * 
     */
    private final class MessageTransportDelegate implements IMessageTransportDelegate {
        protected MessageTransportDelegate() {}
        
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

    private final IMessageTransportDelegate defaultDelegate;

    private Map<String, MessageTransport> transports;
    protected final Map<ICommunicationAddress, List<ListenerContext>> addressToListenerMap;

    /**
     * Creates an initial communication bean.
     */
    public CommunicationBean() {
        defaultDelegate = new MessageTransportDelegate();
        transports = new HashMap<String, MessageTransport>();
        addressToListenerMap = new Hashtable<ICommunicationAddress, List<ListenerContext>>();
    }

    // ~ START OF CONFIGURATION AND INITIALISATION STUFF ~ //
    /**
     * Sets transports to the set given. All transports already set which are not within the set given as parameter
     * will be removed.
     * @param newTransports the message transports
     * @throws Exception if a message transport can not be added to this bean
     * @see #addTransport(MessageTransport)
     */
    public synchronized void setTransports(Set<MessageTransport> newTransports) throws Exception {
        if ((log != null) && log.isInfoEnabled()) {
            log.info("CommunicationBean is setting it's transports to: " + newTransports.toString());
        }

        Set<MessageTransport> workingCopy;
        if (newTransports == null) {
            workingCopy = Collections.emptySet();
        } else {
            workingCopy = new HashSet<MessageTransport>();
            workingCopy.addAll(newTransports);
        }

        // first remove all existing transports
        if (transports.size() > 0) {
            final Set<MessageTransport> toRemove = new HashSet<MessageTransport>();
            toRemove.addAll(transports.values());
            // only remove transports that are not in the workingCopy
            toRemove.removeAll(workingCopy);

            // only add transports that are not yet installed
            workingCopy.removeAll(transports.values());

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

    /**
     * Cleanup this communication bean by cleaning up all message transports.
     * @throws Exception if an error occurs during cleanup of this agent bean or a message transport.
     * @see MessageTransport#doCleanup()
     * @see de.dailab.jiactng.agentcore.AbstractAgentBean#doCleanup()
     */
    @Override
    public void doCleanup() throws Exception {
        destroyMessageBox(thisAgent.getAgentDescription().getMessageBoxAddress());
        
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean starts commencing cleanup");
            log.info("Cleaning up transports");
        }
        synchronized (transports) {
            for (MessageTransport transport : transports.values()) {
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

    /**
     * Initialize this communication bean by initializing all specified message transports and establishing the message box.
     * @throws Exception if an error occurs during initialization of this agent bean or a message transport or during establishing the message box.
     * @see MessageTransport#doInit()
     * @see de.dailab.jiactng.agentcore.AbstractAgentBean#doInit()
     * @see #establishMessageBox(IMessageBoxAddress)
     * @see #setTransports(Set)
     */
    @Override
    public synchronized void doInit() throws Exception {
        if ((log != null) && log.isInfoEnabled()) {
            log.info("CommunicationBean begins with initialization");
        }
        super.doInit();

        log.info("initializing Transports");

        for (final Iterator<MessageTransport> iter = transports.values().iterator(); iter.hasNext();) {
            final MessageTransport transport = iter.next();
            transport.setDefaultDelegate(defaultDelegate);
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
                	;
                }
                iter.remove();

                // deregister message transport from management
                deregisterTransport(transport.getTransportIdentifier());
            }
        }

        // create the default message box for this agent
        establishMessageBox(thisAgent.getAgentDescription().getMessageBoxAddress());

        if (transports.size() <= 0) {
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
     *             is thrown if transport already hold by this CommunicationBean
     */
    public synchronized void addTransport(MessageTransport transport) throws Exception {
        if ((log != null) && log.isInfoEnabled()) {
            log.info("Adding Transport '" + transport + "' to CommunicationBean");
        }

        final String id = transport.getTransportIdentifier();
        if (transports.containsKey(id)) {
            throw new IllegalArgumentException("the transport '" + id + "' already exists");
        }

        try {
            if (isActive()) {
                // initialize message transport
                transport.setDefaultDelegate(defaultDelegate);
                transport.doInit();
                registerAllToTransport(transport);
            }
        } finally {
            // add message transport
            transports.put(id, transport);

            // register message transport for management
            registerTransport(transport);
        }
    }

    /**
     * Removes and cleans up a transport hold by this CommunicationBean.
     * 
     * @param transportIdentifier
     *            of the transport to remove
     */
    public synchronized void removeTransport(String transportIdentifier) {
        // remove message transport
        final MessageTransport transport = transports.remove(transportIdentifier);

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
        default:
            // TODO: should not happen         
        }
    }

    // ~ END OF CONFIGURATION AND INITIALISATION STUFF ~ //

    // ~ START OF ACTIONS ~ //
    public synchronized void joinGroup(IGroupAddress group) throws CommunicationException {
        register(group, null);
    }

    public synchronized void leaveGroup(IGroupAddress group) throws CommunicationException {
        unregister(group, null);
    }
    
    public boolean isLocal(IMessageBoxAddress messageBox) {
        final String name= messageBox.getName();
        return name.startsWith(thisAgent.getAgentNode().getUUID());
    }

    // TODO: make this private
    public synchronized void establishMessageBox(IMessageBoxAddress messageBox) throws CommunicationException {
        register(messageBox, null);
    }

    // TODO: make this private
    public synchronized void destroyMessageBox(IMessageBoxAddress messageBox) throws CommunicationException {
        unregister(messageBox, null);
    }

    /**
     * Send a message to a given communication address.
     * @param message the message to send
     * @param address the communication address of the message receiver
     * @throws CommunicationException if the message can not be send
     * @throws IllegalArgumentException if one of the parameters is <code>null</code>
     * @see MessageTransport#send(IJiacMessage, ICommunicationAddress, long)
     * @see #messageExchanged(MessageExchangeNotification.MessageExchangeAction, ICommunicationAddress, IJiacMessage, String)
     */
    public synchronized void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException {
    	send(message, address, 0);
    }

    /**
     * Send a message to a given communication address. If the timeout is reached, the message 
     * will be expired. Please consider that the clocks of different hosts may run asynchronous!
     * @param message the message to send
     * @param address the communication address of the message receiver
     * @param timeToLive the time-to-live of the message in milliseconds or 0 for using timeout specified by the message transport
     * @throws CommunicationException if the message can not be send
     * @throws IllegalArgumentException if one of the parameters is <code>null</code>
     * @see MessageTransport#send(IJiacMessage, ICommunicationAddress, long)
     * @see #messageExchanged(MessageExchangeNotification.MessageExchangeAction, ICommunicationAddress, IJiacMessage, String)
     */
    public synchronized void send(IJiacMessage message, ICommunicationAddress address, long timeToLive) throws CommunicationException {
        if (message == null) {
            throw new IllegalArgumentException("message must not be null");
        }

        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalSend(saveCast(message, JiacMessage.class), saveCast(address, CommunicationAddress.class), timeToLive);
    }

    @Deprecated
    public synchronized void register(ICommunicationAddress address) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean begins to listen at address '" + address + "'");
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalRegister(saveCast(address, CommunicationAddress.class), null);
    }

    @Deprecated
    public synchronized void unregister(ICommunicationAddress address) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean stops to listen at address '" + address + "'");
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalUnregister(saveCast(address, CommunicationAddress.class), null);
    }

    // ~ END OF ACTIONS ~ //

    // ~ METHODS FOR LISTENER ADMINISTRATION ~ //

    // TODO: change the type of the address to IGroupAddress
    public synchronized void register(ICommunicationAddress address, IJiacMessage selectorTemplate) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean begins to listen at address '" + address + "' with Selector '" + selectorTemplate + "'");
        }

        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalRegister(saveCast(address, CommunicationAddress.class), selectorTemplate);
    }

    // TODO: change the type of the address to IGroupAddress
    public synchronized void unregister(ICommunicationAddress address, IJiacMessage selectorTemplate) throws CommunicationException {
        if (log.isInfoEnabled()) {
            log.info("CommunicationBean stops to listen at address '" + address + "' with selector '" + selectorTemplate + "'");
        }
        if (address == null) {
            throw new IllegalArgumentException("address must not be null");
        }

        internalUnregister(saveCast(address, CommunicationAddress.class), selectorTemplate);
    }

    // ~ END OF METHODS FOR LISTENER ADMINISTRATION ~ //

    // ~ INTERNAL METHODS ~ //
    /**
     * Delegates received messages to the memory of the agent
     * @param source the message transport which has received the message
     * @param message the received message
     * @param at the communication address where the message was sent to
     * @see #messageExchanged(MessageExchangeNotification.MessageExchangeAction, ICommunicationAddress, IJiacMessage, String)
     * @see de.dailab.jiactng.agentcore.knowledge.Memory#write(de.dailab.jiactng.agentcore.knowledge.IFact)
     */
    protected void processMessage(MessageTransport source, IJiacMessage message, CommunicationAddress at) {
    	// notification about receiving message
        messageExchanged(MessageExchangeAction.RECEIVE, at, message, source.getTransportIdentifier());
    	
    	if (log.isDebugEnabled()) {
            log.debug("CommunicationBean is receiving Message over transport '" + source.getTransportIdentifier() + "' from '" + at + "'");
            log.debug("received message ' " + message + "' at '" + at + "'");
        }
        
    	// insert by mib
    	// need a header field for information: send-to address
    	message.setHeader(IJiacMessage.Header.SEND_TO, at.getName());
    	
        memory.write(message);

    }

    /**
     * if an error occurs ....
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
    private synchronized void internalSend(JiacMessage message, CommunicationAddress address, long timeToLive)
            throws CommunicationException {
        if (transports.size() <= 0) {
            throw new CommunicationException("no transport available");
        }

        if (log.isDebugEnabled()) {
            log.debug("send message...");
        }
        final CommunicationAddress unboundAddress = address.toUnboundAddress();

        // set the sender of the message
        message.setSender(thisAgent.getAgentDescription().getMessageBoxAddress());

        if (address instanceof MessageBoxAddress) {
            if (log.isDebugEnabled()) {
                log.debug("address is a message box address -> choosing one transport");
            }
            // 1:1 communication
            MessageTransport transport = null;
            if (address.isBoundToTransport()) {
                final String transportId = address.toURI().getScheme();
                transport = transports.get(transportId);
            } else {
                // TODO: lookup for transport
                transport = transports.values().iterator().next();
            }

            if (transport != null) {
                transport.send(message, unboundAddress, timeToLive);
                // notification about sending message
                messageExchanged(MessageExchangeAction.SEND, unboundAddress, message, transport.getTransportIdentifier());
            } else {
                throw new CommunicationException("does not have transport for '" + address + "'");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("address is a group address -> choosing all transports");
            }
            // 1:n communication
            for (MessageTransport transport : transports.values()) {
                transport.send(message, unboundAddress, timeToLive);
                // notification about sending message
                messageExchanged(MessageExchangeAction.SEND, unboundAddress, message, transport.getTransportIdentifier());
            }
        }
    }

    /**
     * Assumes that the listener and the address are non-null.
     */
    private synchronized void internalRegister(CommunicationAddress address, IJiacMessage selectorTemplate) throws CommunicationException {
        final CommunicationAddress unboundAddress = address.toUnboundAddress();
        selectorTemplate= cloneTemplate(selectorTemplate);
        ListenerContext context = new ListenerContext(selectorTemplate);
        List<ListenerContext> registeredContexts = addressToListenerMap.get(unboundAddress);

        // Now there are three cases:
        // 1. The address is already registered and someone is listening with the same selectorTemplate
        // 2. The address is already registered but there are only listeners with other selectorTemplates
        // 3. The address isn't registered and nobody is listening to it.
        if (registeredContexts != null) {
            // we have already some listener registered for this communication address
            final int index = registeredContexts.indexOf(context);

            if (index >= 0) {
                // (1.) there is already another listener with same address and selector registered
                context = registeredContexts.get(index);

                if (log.isInfoEnabled()) {
                	log.info("Another Listener registeres for address '" + address + "' with selectortemplate '" + selectorTemplate + "'");
                }
                
                // check whether the listener is new, otherwise ignore this registration
                context.listeners++; 


                if (log.isDebugEnabled()) {
                    log.debug("added further listener for '" + unboundAddress + "' : '" + selectorTemplate + "'");
                }
                return;
            } 
        }else {
            // (3.) currently there are no listeners registered for this address
            if (log.isDebugEnabled()) {
                log.debug("first listener for '" + unboundAddress + "'");
            }
            registeredContexts= new LinkedList<ListenerContext>();
            addressToListenerMap.put(unboundAddress, registeredContexts);
        }
        // (2.) or (3. (continued))
        context.listeners = 1;
        registeredContexts.add(context);
       

        if (log.isDebugEnabled()) {
        	log.debug("registering new address and listeners on transports");
        }

        if (isActive()) {
        	if (log.isDebugEnabled()) {
        		log.debug("isActive -> registering listeners for new address '" + unboundAddress + "'");
        	}
        	for (MessageTransport transport : transports.values()) {
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
    private synchronized void internalUnregister(CommunicationAddress address, IJiacMessage selectorTemplate) throws CommunicationException {
        final CommunicationAddress unboundAddress = address.toUnboundAddress();
        selectorTemplate= cloneTemplate(selectorTemplate);
        final List<ListenerContext> registeredContexts = addressToListenerMap.remove(unboundAddress);

        if (log.isDebugEnabled()) {
            log.debug("Removing nonWildcardListener with address '" + address + "' and selector '" + selectorTemplate + "'");
        }

        if (registeredContexts == null) {
            if (log.isWarnEnabled()) {
                log.warn("Aborted Unregister: There is no listener registered for this address '" + address + "'");
            }
            return;
        }
        
        ListenerContext context = new ListenerContext(selectorTemplate); // template to find it fast
        final int index = registeredContexts.indexOf(context);

        if(index >= 0) {
        	// this address-selectorTemplate combination is actually registered
            context= registeredContexts.remove(index);
            
            // decrease the number of listeners
            context.listeners--;
            if (context.listeners <= 0){
            	// this was the last one listening to this address with this selectorTemplate
            	if(isActive()) {
                    // remove registration
                    for(MessageTransport transport : transports.values()) {
                        transport.stopListen(unboundAddress, context.selector);
                    }
                }
            } else {
            	// there is indeed someone still listening with this address-selectorTemplate combination
            	registeredContexts.add(context);
                addressToListenerMap.put(unboundAddress, registeredContexts);
            }
            
        } else {
        	// there doesn't exist a registration for this combination 
            if(log.isWarnEnabled()) {
                log.warn("Aborted Unregister: There is no listener registered for this address '" + address + "' and selector '" + selectorTemplate + "'");
            }
        }
        
        if (registeredContexts.size() > 0){
        	// Still someone listening on this address
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
        for (MessageTransport transport : transports.values()) {
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
        for (String transportId : transports.keySet()) {
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
    private void registerTransport(MessageTransport transport) {
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
    private void deregisterTransport(String transportId) {
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

	/**
	 * {@inheritDoc}
	 */
    @SuppressWarnings("unchecked")
    public CompositeData getSelectorsOfAddresses() {
        CompositeData data = null;
        final int size = addressToListenerMap.size();
        final String[] itemNames = new String[size];
        final OpenType[] itemTypes = new OpenType[size];
        final Object[] itemValues = new Object[size];
        final Object[] addresses = addressToListenerMap.keySet().toArray();
        try {
            for (int i = 0; i < size; i++) {
                final ICommunicationAddress address = (ICommunicationAddress) addresses[i];
                itemNames[i] = address.getName();
                itemTypes[i] = new ArrayType(1, SimpleType.STRING);
                final List<ListenerContext> values = addressToListenerMap.get(address);
                final String[] value = new String[values.size()];
                final Iterator<ListenerContext> it = values.iterator();
                int j = 0;
                while (it.hasNext()) {
                    final IJiacMessage selector = it.next().selector;
                    if (selector == null) {
                        value[j] = "null";
                    } else {
                        value[j] = selector.toString();
                    }
                    j++;
                }
                itemValues[i] = value;
            }
            final CompositeType compositeType = new CompositeType(addressToListenerMap.getClass().getName(),
                    "addresses of the communication bean", itemNames, itemNames, itemTypes);
            data = new CompositeDataSupport(compositeType, itemNames, itemValues);
        } catch (OpenDataException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Uses JMX to send notifications that a message was exchanged with another agent.
     *
     * @param action indicates if the message was sent or received.
     * @param receiver the address of the message receiver.
     * @param jiacMessage the exchanged message.
     * @param transport the used message transport mechanism
     */
    public void messageExchanged(MessageExchangeAction action, 
			ICommunicationAddress receiver, IJiacMessage jiacMessage,
			String transport) {
    	String msg;
    	switch (action) {
    	case SEND:
    		msg = "Message sent";
    		break;
    	case RECEIVE:
    		msg = "Message received";
    		break;
    	default:
    		msg = "Message exchanged";
    	}
    	final Notification n =
                new MessageExchangeNotification(this,
                sequenceNumber++,
                System.currentTimeMillis(),
                msg, action, receiver, jiacMessage, transport);
        
        sendNotification(n);
    }
    
    /**
     * Gets information about all notifications this communication bean instance may send.
     * This contains also information about the <code>MessageExchangeNotification</code> 
     * to notify about exchanged messages.
     * @return list of notification information.
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
    	final MBeanNotificationInfo[] parent = super.getNotificationInfo();
    	final int size = parent.length;
    	final MBeanNotificationInfo[] result = new MBeanNotificationInfo[size + 1];
//    	for (int i=0; i<size; i++) {
//    		result[i] = parent[i];
//    	}
    	System.arraycopy(parent, 0, result, 0, size);
    	
        final String[] types = new String[] {
            MessageExchangeNotification.MESSAGE_EXCHANGE
        };
        final String name = MessageExchangeNotification.class.getName();
        final String description = "A message with another agent was exchanged";
        final MBeanNotificationInfo info =
                new MBeanNotificationInfo(types, name, description);
        result[size] = info;
        return result;
    }    
}

class ListenerContext {
    protected int listeners;

    protected final IJiacMessage selector;
    
    ListenerContext(IJiacMessage selectorTemplate){
    	this.selector = selectorTemplate;
    	this.listeners = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof ListenerContext)) {
            return false;
        }

        final ListenerContext other = (ListenerContext) obj;
        return selector == null ? other.selector == null
                                : other.selector == null ? false 
                                                         : selector.equals(other.selector);
    }

    @Override
	public int hashCode() {
        return CommunicationBean.class.hashCode() ^ (selector != null ? selector.hashCode() : 0);
	}

	@Override
    public String toString() {
        return selector == null ? "*" : selector.toString();
    }
}

