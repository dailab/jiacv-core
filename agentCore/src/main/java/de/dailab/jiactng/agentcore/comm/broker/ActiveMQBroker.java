package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.network.NetworkConnector;

import de.dailab.jiac.net.SourceAwareDiscoveryNetworkConnector;
import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * Implements a message broker as agent node bean based on ActiveMQ technology.
 * @see org.apache.activemq.broker.BrokerService
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 * 
 */
public class ActiveMQBroker extends AbstractAgentNodeBean implements ActiveMQBrokerMBean {

	/** The ActiveMQ broker used by all agent nodes of the local JVM. */
	protected static ActiveMQBroker INSTANCE= null;

	/**
	 * Initializes the given connection factory proxy with a new ActiveMQ connection factory 
	 * using the name of the broker of this JVM.
	 * @param proxy the connection factory proxy
	 * @see ConnectionFactoryProxy#connectionFactory
	 * @see ActiveMQConnectionFactory#ActiveMQConnectionFactory(String)
	 * @throws IllegalStateException if no broker is running in this JVM
	 */
    static void initialiseProxy(ConnectionFactoryProxy proxy) {
        if(INSTANCE == null) {
            throw new IllegalStateException("no broker is running");
        }
        
        proxy.connectionFactory= new ActiveMQConnectionFactory("vm://" + INSTANCE.getBrokerName());
    }
    
    protected String _brokerName= null;
    protected BrokerService _broker= null;
    protected Set<ActiveMQTransportConnector> _connectors= new HashSet<ActiveMQTransportConnector>();
    protected boolean _persistent = false;
    protected int _networkTTL = 1;

    /**
     * Creates an empty ActiveMQ broker and initializes the static variable
     * <code>INSTANCE</code> with this broker if not yet set.
     */
    public ActiveMQBroker() {
        synchronized (ActiveMQBroker.class) {
//            if(INSTANCE != null) {
//                throw new IllegalStateException("only on instance per VM is allowed");
//            }
//            
        	if (INSTANCE == null) {
        		INSTANCE = this;
        	}
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getNetworkTTL() {
    	return _networkTTL;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void setNetworkTTL(int networkTTL) throws Exception {
        if(_networkTTL != networkTTL && _broker != null) {
        	final List<NetworkConnector> netcons = _broker.getNetworkConnectors();
            for (NetworkConnector net : netcons){
            	_broker.removeNetworkConnector(net);
            	net.setNetworkTTL(networkTTL);
            	_broker.addNetworkConnector(net);
            }
        }
        
        _networkTTL= networkTTL;
    }
    
    // Lifecyclemethods:

    /**
     * Initialize this broker by instantiating and starting the ActiveMQ broker service. Three connectors
     * (network URI, transport URI, discovery URI) are added to the broker service for each specified transport connector.
     * @throws Exception if an error occurs during initialization
     * @see BrokerService
     * @see #setConnectors(Set)
     */
    public void doInit() throws Exception {
        log.debug("initializing embedded broker");
        
        _brokerName= agentNode.getUUID() + getBeanName();
        _broker = new BrokerService();
        _broker.setBrokerName(getBrokerName());
        
        if(agentNode.isManagementEnabled()) {
            _broker.setUseJmx(true);
            final ManagementContext context = new ManagementContext();
            context.setJmxDomainName("de.dailab.jiactng");
            context.setCreateConnector(false);
            _broker.setManagementContext(context);
        } else {
            _broker.setUseJmx(false);
        }
        
        try {
            for (ActiveMQTransportConnector amtc : _connectors) {
                log.debug("embedded broker initializing transport:: " + amtc.toString());
                if (amtc.getNetworkURI() != null) {
                	final URI networkUri = new URI(amtc.getNetworkURI());
                    final NetworkConnector networkConnector = _broker.addNetworkConnector(networkUri);
                    networkConnector.setDuplex(amtc.isDuplex());
                    networkConnector.setNetworkTTL(amtc.getNetworkTTL());
                }

                _broker.setPersistent(_persistent);

            	final TransportConnector connector= _broker.addConnector(new URI(amtc.getTransportURI()));
                if (amtc.getDiscoveryURI() != null) {
                    final URI uri = new URI(amtc.getDiscoveryURI());
                    final URI discoveryURI= new URI(amtc.getDiscoveryURI());
                    connector.setDiscoveryUri(discoveryURI);
//no such method in 5.3 connector.getDiscoveryAgent().setBrokerName(_broker.getBrokerName());
                    final NetworkConnector networkConnector= new SourceAwareDiscoveryNetworkConnector(uri);
                    networkConnector.setNetworkTTL(_networkTTL);
                    _broker.addNetworkConnector(networkConnector);
                } 
            }

        } catch (Exception e) {
            log.error(e.toString());
        }

        _broker.start();
        log.debug("started broker");
    }

    /**
     * Cleanup this broker by stopping the ActiveMQ broker service.
     * @throws Exception if an error occurs during stop of the broker service
     * @see BrokerService#stop()
     */
    public void doCleanup() throws Exception {
    	log.debug("stopping broker");
    	_broker.stop();
    	log.debug("stopping broker done");
    }

    /**
     * {@inheritDoc}
     */
    public boolean getPersistent() {
    	return _persistent;
    }

    /**
     * Indicates whether messages should be stored in a data base or not.
     * ActiveMQ uses Derby to store message.
     * You must include this dependency explicitly in your project as it is
     * not associated with agentCore.
     * 
     * @param persistent    <code>true</code> to store messages and <code>false</code> otherwise.
     */
    public void setPersistent(boolean persistent) {
        _persistent = persistent;
    }

    /**
     * Setter for the set of connectors.
     * Connectors are entry points to the broker that accept remote connections.
     * By default, every broker has a logical vm-connector which permits the
     * inner-vm-message exchange.
     * 
     * <p>
     * This method should be called before this bean is initialised!
     * </p>
     * 
     * @param connectors    the set of connectors
     */
    public void setConnectors(Set<ActiveMQTransportConnector> connectors) {
        _connectors = connectors;
    }

    public Set<ActiveMQTransportConnector> getConnectors() {
      return Collections.unmodifiableSet(_connectors);
    }

    
    /**
     * Get the name of the ActiveMQ broker service.
     * @return the name
     * @throws IllegalStateException if the broker service is not initialized
     */
    protected String getBrokerName() {
        if(_brokerName == null) {
            throw new IllegalStateException("broker is not initialised");
        }

        return _brokerName;
    }

    /**
     * Register the broker and all transport connectors for management
     * 
     * @param manager
     *            the manager to be used for registration
     */
    public void enableManagement(Manager manager) {
        // do nothing if management already enabled
        if (isManagementEnabled()) {
            return;
        }

        // register broker
        super.enableManagement(manager);

        // register all transport connectors for management
        for (ActiveMQTransportConnector connector : _connectors) {
            registerConnector(connector);
        }
    }

    /**
     * Unregister the broker and all transport connectors from management
     */
    public void disableManagement() {
        // do nothing if management already disabled
        if (!isManagementEnabled()) {
            return;
        }

        // unregister all transport connectors from management
        for (ActiveMQTransportConnector connector : _connectors) {
            unregisterConnector(connector);
        }

        super.disableManagement();
    }

    /**
     * Register a transport connector for management.
     * 
     * @param connector
     *            the transport connector to be registered
     */
    private void registerConnector(ActiveMQTransportConnector connector) {
        // do nothing if management is not enabled
        if (!isManagementEnabled()) {
            return;
        }

        // register message transport for management
        try {
            _manager.registerAgentNodeBeanResource(this, getAgentNode(), "TransportConnector", "\"" + connector.getTransportURI() + "\"",
            		connector);
        } catch (Exception e) {
            if ((log != null) && (log.isErrorEnabled())) {
                log.error("WARNING: Unable to register transport connector " + connector.getTransportURI()
                        + " of the broker of agent node "
                        + getAgentNode().getName() + " as JMX resource.");
                log.error(e.getMessage());
            } else {
                System.err.println("WARNING: Unable to register transport connector "
                        + connector.getTransportURI() + " of the broker of agent node "
                        + getAgentNode().getName() + " as JMX resource.");
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Unregister a transport connector from management.
     * 
     * @param connector
     *            the transport connector to be unregistered
     */
    private void unregisterConnector(ActiveMQTransportConnector connector) {
        // do nothing if management is not enabled
        if (!isManagementEnabled()) {
            return;
        }

        // unregister transport connector from management
        try {
            _manager.unregisterAgentNodeBeanResource(this, getAgentNode(), "TransportConnector", "\"" + connector.getTransportURI() + "\"");
        } catch (Exception e) {
            if ((log != null) && (log.isErrorEnabled())) {
                log.error("WARNING: Unable to deregister transport connector " + connector.getTransportURI() 
                		+ " of the broker of agent node "
                        + getAgentNode().getName() + " as JMX resource.");
                log.error(e.getMessage());
            } else {
                System.err.println("WARNING: Unable to deregister transport connector " + connector.getTransportURI() 
                		+ " of the broker of agent node "
                        + getAgentNode().getName() + " as JMX resource.");
                System.err.println(e.getMessage());
            }
        }
    }

}
