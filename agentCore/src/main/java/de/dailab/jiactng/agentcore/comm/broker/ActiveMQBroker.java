package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.network.DiscoveryNetworkConnector;
import org.apache.activemq.network.NetworkConnector;
import org.apache.log4j.Level;

import de.dailab.jiac.net.SourceAwareDiscoveryNetworkConnector;
import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * Implements a message broker as agent node bean based on ActiveMQ technology.
 *
 * @see org.apache.activemq.broker.BrokerService
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 */
public class ActiveMQBroker extends AbstractAgentNodeBean implements ActiveMQBrokerMBean {

	/** The ActiveMQ broker used by all agent nodes of the local JVM. */
	protected static ActiveMQBroker instance = null;

	/**
	 * Initializes the given connection factory proxy with a new ActiveMQ connection factory using the name of the broker
	 * of this JVM.
	 * 
	 * @param proxy the connection factory proxy
	 * @see ConnectionFactoryProxy#connectionFactory
	 * @see ActiveMQConnectionFactory#ActiveMQConnectionFactory(String)
	 * @throws IllegalStateException if no broker is running in this JVM
	 */
	static void initialiseProxy(final ConnectionFactoryProxy proxy) {
		if (instance == null) {
			throw new IllegalStateException("no broker is running");
		}
		// since a new broker is created upon the first connection, we need to set
		// the persistence flag here too, otherwise kahadb is always used for this
		// broker
		if (proxy.isPersistent()) {
			proxy.connectionFactory = new ActiveMQConnectionFactory("vm://" + instance.getBrokerName());
		} else {
			proxy.connectionFactory = new ActiveMQConnectionFactory("vm://" + instance.getBrokerName()
																							+ "?broker.persistent=false");
		}
	}

	protected String _brokerName = null;
	protected BrokerService _broker = null;
	protected Set<ActiveMQTransportConnector> _connectors = new HashSet<ActiveMQTransportConnector>();
	protected boolean _persistent = false;
	protected boolean _management = true;
	protected int _networkTTL = 1;
	protected String _dataDirectory = null;

	/**
	 * Creates an empty ActiveMQ broker and initializes the static variable <code>INSTANCE</code> with this broker if not
	 * yet set.
	 */
	public ActiveMQBroker() {
		synchronized (ActiveMQBroker.class) {
			// if(INSTANCE != null) {
			// throw new IllegalStateException("only on instance per VM is allowed");
			// }
			//
			if (instance == null) {
				instance = this;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNetworkTTL() {
		return this._networkTTL;
	}

	/**
	 * {@inheritDoc}
	 */
	// @SuppressWarnings("unchecked")
	@Override
	public void setNetworkTTL(final int networkTTL) throws Exception {
		if ((this._networkTTL != networkTTL) && (this._broker != null)) {
			final List<NetworkConnector> netcons = this._broker.getNetworkConnectors();
			for (NetworkConnector net : netcons) {
				this._broker.removeNetworkConnector(net);
				net.setNetworkTTL(networkTTL);
				this._broker.addNetworkConnector(net);
			}
		}

		this._networkTTL = networkTTL;
	}

	// Lifecyclemethods:

	/**
	 * Initialize this broker by instantiating and starting the ActiveMQ broker service. Three connectors (network URI,
	 * transport URI, discovery URI) are added to the broker service for each specified transport connector. The discovery
	 * URI defined by the agent node will be preferred.
	 * 
	 * @throws Exception if an error occurs during initialization
	 * @see BrokerService
	 * @see #setConnectors(Set)
	 */
	@Override
	public void doInit() throws Exception {
		this.log.debug("initializing embedded broker");

		boolean useSsl = false;
		try {
			useSsl = Boolean.parseBoolean(System.getProperty(SimpleAgentNode.SSL_USAGE_IDENTIFIER));
		} catch (Exception e) {
			log.warn("Could not read ssl state!", e);
			useSsl = false;
		}

		if (useSsl && _connectors != null) {
			String cipherSuite = null;
			try {
				cipherSuite = System.getProperty(SimpleAgentNode.SSL_LIMITED_CIPHER_SUITES);
			} catch (Exception e) {
				log.warn("Could not read ssl cipherSuite state!", e);
				cipherSuite = null;
			}
			// change transport connectors from tcp to ssl
			Iterator<ActiveMQTransportConnector> it = _connectors.iterator();
			while (it.hasNext()) {
				ActiveMQTransportConnector c = it.next();
				String transportUri = c.getTransportURI();
				if (transportUri.toLowerCase().startsWith("tcp:")) {
					transportUri = "ssl:" + transportUri.substring(4);
					if (cipherSuite != null) {
						// add cipher suite limitation to transport uri
						int index = transportUri.lastIndexOf('?');
						if (index < 0) {
							transportUri += "?transport.enabledCipherSuites=" + cipherSuite;
						} else {
							transportUri += "&transport.enabledCipherSuites=" + cipherSuite;
						}
					}
					c.setTransportURI(transportUri);
				}
			}
		}

		this._brokerName = this.agentNode.getUUID() + this.getBeanName();
		this._broker = new BrokerService();
		this._broker.setBrokerName(this.getBrokerName());
		this._broker.setPersistent(this._persistent);
		if (this._dataDirectory != null) {
			this._broker.setDataDirectory(this.getDataDirectory());
		}

		if (!this._persistent) {
			this._broker.setDeleteAllMessagesOnStartup(true);
			this._broker.setEnableStatistics(false);
		}

		if (this.isManagement()) {
			this._broker.setUseJmx(true);
			final ManagementContext context = new ManagementContext();
			context.setJmxDomainName("de.dailab.jiactng");
			context.setCreateConnector(false);
			this._broker.setManagementContext(context);
		} else {
			this._broker.setUseJmx(false);
		}

		try {
			for (ActiveMQTransportConnector amtc : this._connectors) {
				if (this.agentNode.getOverwriteDiscoveryURI() != null) {
					amtc.setDiscoveryURI(this.agentNode.getOverwriteDiscoveryURI());
				}
				this.log.debug("embedded broker initializing transport:: " + amtc.toString());

				if (amtc.getNetworkURI() != null) {
					final URI networkUri = new URI(amtc.getNetworkURI());
					NetworkConnector networkConnector = new DiscoveryNetworkConnector(networkUri);
					networkConnector.setName(amtc.getName());
					networkConnector.setDuplex(amtc.isDuplex());
					networkConnector.setNetworkTTL(amtc.getNetworkTTL());
					this._broker.addNetworkConnector(networkConnector);
				}
				if (amtc.getTransportURI() != null) {
					final TransportConnector connector = this._broker.addConnector(new URI(amtc.getTransportURI()));
					if (amtc.getDiscoveryURI() != null) {
						final URI uri = new URI(amtc.getDiscoveryURI());
						final URI discoveryURI = new URI(amtc.getDiscoveryURI());
						connector.setDiscoveryUri(discoveryURI);
						// no such method in 5.3
						// connector.getDiscoveryAgent().setBrokerName(_broker.getBrokerName());
						if (this._broker.getNetworkConnectorByName("SourceAwareDiscoveryNetworkConnector:" + amtc.getDiscoveryURI()) == null) {
							final NetworkConnector networkConnector = new SourceAwareDiscoveryNetworkConnector(uri);
							networkConnector.setNetworkTTL(this._networkTTL);
							this._broker.addNetworkConnector(networkConnector);
						}
					}
				}
			}

		} catch (Exception e) {
			this.log.error(e.toString(), e);
		}

		this._broker.start();
		this.log.debug("started broker");
	}

	/**
	 * Cleanup this broker by stopping the ActiveMQ broker service.
	 * 
	 * @throws Exception if an error occurs during stop of the broker service
	 * @see BrokerService#stop()
	 */
	@Override
	public void doCleanup() throws Exception {
		this.log.debug("stopping broker");
		this._broker.stop();
		// _broker.waitUntilStopped();
		this.log.debug("stopping broker done");
		this._broker = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getPersistent() {
		return this._persistent;
	}

	/**
	 * Indicates whether messages should be stored in a data base or not.<br/>
	 * as default ActiveMQ uses KahaDB to store message. KahaDB is included in the activeMQ dependency
	 * 
	 * @param persistent <code>true</code> to store messages and <code>false</code> otherwise.
	 */
	public void setPersistent(final boolean persistent) {
		this._persistent = persistent;
	}

	public boolean isManagement() {
		return this._management;
	}

	public void setManagement(final boolean management) {
		this._management = management;
	}

	/**
	 * Get the location of the activeMQ data directory used as persistence store the directory applies only when a
	 * persistency adapter other than MemoryAdapter is used, e.g. when persistency is switched on
	 * 
	 * @return the location of the message store as a String
	 */
	public String getDataDirectory() {
		return this._dataDirectory;
	}

	/**
	 * sets the location of the Message store, e.g. the activeMQ persistency data base<br/>
	 * the location is expected to be a folder represented as String<br/>
	 * the default location is /$userhome/activemq-data/
	 * 
	 * @param _dataDirectory the location of the message store as a String
	 */
	public void setDataDirectory(final String _dataDirectory) {
		this._dataDirectory = _dataDirectory;
	}

	/**
	 * Setter for the set of connectors. Connectors are entry points to the broker that accept remote connections. By
	 * default, every broker has a logical vm-connector which permits the inner-vm-message exchange.
	 * <p>
	 * This method should be called before this bean is initialised!
	 * </p>
	 * 
	 * @param connectors the set of connectors
	 */
	public void setConnectors(final Set<ActiveMQTransportConnector> connectors) {
		this._connectors = connectors;
	}

	public Set<ActiveMQTransportConnector> getConnectors() {
		return Collections.unmodifiableSet(this._connectors);
	}

	/**
	 * Get the name of the ActiveMQ broker service.
	 * 
	 * @return the name
	 * @throws IllegalStateException if the broker service is not initialized
	 */
	protected String getBrokerName() {
		if (this._brokerName == null) {
			throw new IllegalStateException("broker is not initialised");
		}

		return this._brokerName;
	}

	/**
	 * Register the broker and all transport connectors for management
	 * 
	 * @param manager the manager to be used for registration
	 */
	@Override
	public void enableManagement(final Manager manager) {
		// do nothing if management is already enabled or management is disabled for
		// this bean
		if (!this.isManagement() && this.isManagementEnabled()) {
			return;
		}

		// register broker
		super.enableManagement(manager);

		// register all transport connectors for management
		for (ActiveMQTransportConnector connector : this._connectors) {
			this.registerConnector(connector);
		}
	}

	/**
	 * Unregister the broker and all transport connectors from management
	 */
	@Override
	public void disableManagement() {
		// do nothing if management is already disabled or management is disabled
		// for this bean
		if (!this.isManagement() && !this.isManagementEnabled()) {
			return;
		}

		// unregister all transport connectors from management
		for (ActiveMQTransportConnector connector : this._connectors) {
			this.unregisterConnector(connector);
		}

		super.disableManagement();
	}

	/**
	 * Register a transport connector for management.
	 * 
	 * @param connector the transport connector to be registered
	 */
	private void registerConnector(final ActiveMQTransportConnector connector) {
		// do nothing if management is not enabled
		if (!(this.isManagement() && this.isManagementEnabled())) {
			return;
		}

		// register message transport for management
		try {
			this._manager.registerAgentNodeBeanResource(this, this.getAgentNode(),
																							ActiveMQTransportConnectorMBean.RESOURCE_TYPE,
																							"\"" + connector.getName() + ":" + connector.getTransportURI() + "\"",
																							connector);
		} catch (Exception e) {
			if ((this.log != null) && (this.log.isEnabledFor(Level.ERROR))) {
				this.log.error("WARNING: Unable to register transport connector " + connector.getName() + ":"
																								+ connector.getTransportURI() + " of the broker of agent node "
																								+ this.getAgentNode().getName() + " as JMX resource.", e);
				this.log.error(e.getMessage());
			} else {
				System.err.println("WARNING: Unable to register transport connector " + connector.getName() + ":"
																								+ connector.getTransportURI() + " of the broker of agent node "
																								+ this.getAgentNode().getName() + " as JMX resource.");
				System.err.println(e.getMessage());
			}
		}
	}

	/**
	 * Unregister a transport connector from management.
	 * 
	 * @param connector the transport connector to be unregistered
	 */
	private void unregisterConnector(final ActiveMQTransportConnector connector) {
		// do nothing if management is not enabled
		if (!this.isManagementEnabled()) {
			return;
		}

		// unregister transport connector from management
		try {
			this._manager.unregisterAgentNodeBeanResource(this, this.getAgentNode(),
																							ActiveMQTransportConnectorMBean.RESOURCE_TYPE,
																							"\"" + connector.getName() + ":" + connector.getTransportURI() + "\"");
		} catch (Exception e) {
			if ((this.log != null) && (this.log.isEnabledFor(Level.ERROR))) {
				this.log.error("WARNING: Unable to deregister transport connector " + connector.getName() + ":"
																								+ connector.getTransportURI() + " of the broker of agent node "
																								+ this.getAgentNode().getName() + " as JMX resource.");
				this.log.error(e.getMessage());
			} else {
				System.err.println("WARNING: Unable to deregister transport connector " + connector.getName() + ":"
																								+ connector.getTransportURI() + " of the broker of agent node "
																								+ this.getAgentNode().getName() + " as JMX resource.");
				System.err.println(e.getMessage());
			}
		}
	}

}
