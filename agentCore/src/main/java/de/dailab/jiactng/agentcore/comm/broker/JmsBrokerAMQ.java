package de.dailab.jiactng.agentcore.comm.broker;

import java.io.File;
import java.net.URI;

import javax.management.ObjectName;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manageable;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * An embedded AMQ broker. Joachims Broker.. modifiziert.
 * 
 * @todo add configuration support for e.g. multicast
 * @author Joachim Fuchs, Janko Dimitroff
 */
public class JmsBrokerAMQ extends AbstractLifecycle implements Manageable, JmsBrokerAMQMBean {
    /** The logger we use, if it is not set by DI, we create our own */
    protected Log log = LogFactory.getLog(getClass());

    public BrokerValues values = null;

    /** The embedded broker we use if no other broker is running on our host machine */
    protected BrokerService broker = new BrokerService();

	/** Agent node of this message broker */
	protected IAgentNode _agentNode = null;

	/** The manager of the message broker */
	protected Manager _manager = null;

    public void setLog(Log log) {
        this.log = log;
    }

    // ---------------------- Lifecycle
    public void doInit() throws Exception {
    	// create and init new broker
        log.debug("initializing embedded broker");
    	broker = new BrokerService();
        broker.setBrokerName(values.getName());
        broker.setPersistent(values.isPersistent());
		broker.setUseJmx(values.isJmx());
		ManagementContext context = new ManagementContext();
		context.setJmxDomainName("de.dailab.jiactng");
		context.setCreateConnector(false);
		broker.setManagementContext(context);
        try {
        	TransportConnector connector = broker.addConnector(values.getUrl());
            if(values.getDiscoveryMethod() != null && values.getDiscoveryAddress() != null) {
                connector.setDiscoveryUri(new URI(getDiscoveryUri(values.getDiscoveryMethod(), values.getDiscoveryAddress())));
                broker.addNetworkConnector(new URI(getDiscoveryUri(values.getDiscoveryMethod(), values.getDiscoveryAddress())));
                connector.getDiscoveryAgent().setBrokerName(values.getName());
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        log.debug("embedded broker initialized. url = " + values.getUrl());

    	// start broker
        log.debug("starting broker");
        for (Object connector : broker.getTransportConnectors()) {
        	((TransportConnector) connector).start();
        }
        broker.start();
        log.debug("broker started");
     }

    public void doStart() throws Exception {
//      // start broker
//      if (broker != null) {
//          log.debug("starting broker");
//          connector.start();
//          broker.start();
//          log.debug("broker started");
//      } else {
//          log.warn("no broker found to start");
//      }
    }

    public void doStop() throws Exception {
//      // stop broker
//      if (broker != null) {
//          log.debug("stopping broker");
//          connector.stop();
//          broker.stop();
//          log.debug("broker stopped");
//      } else {
//          log.warn("No Broker found to stop");
//      }
    }

    public void doCleanup() throws Exception {
    	// stop broker
        log.debug("stopping broker");
        for (Object connector : broker.getTransportConnectors()) {
          	((TransportConnector) connector).stop();
        }
        broker.stop();
        log.debug("broker stopped");
    }

    /**
     * dummy for tests only
     */
    public void springStart() throws Exception {
        init();
        start();
    }

    /**
     * dummy for tests only
     */
    public void springStop() throws Exception {
        stop();
        try {
            cleanup();
        } catch (LifecycleException le) {
            log.error(le.getCause());
        }
    }

    protected String getDiscoveryUri(String discoveryMethod, String discoveryAddress) {
        return discoveryMethod + "://" + discoveryAddress;
    }

    // ------------------------ Setter/Getter
    public BrokerValues getValues() {
        return values;
    }

    public void setValues(BrokerValues values) {
        this.values = values;
        if (values._url == null){
            values.setUrlFromPortAndProtocol();
        }
    }

	public void setAgentNode(IAgentNode agentNode) {
		_agentNode = agentNode;
	}

	public String getAgentNodeName() {
		return _agentNode.getName();
	}

	/**
     * Registers the broker service for management.
     * @param manager the manager to be used for registration
	 */
	public void enableManagement(Manager manager) {
		// do nothing if management already enabled
		if (isManagementEnabled()) {
			return;
		}
		
		// register broker service for management
		try {
			manager.registerAgentNodeResource(getAgentNodeName(), "BrokerService", this);
		}
		catch (Exception e) {
			System.err.println("WARNING: Unable to register broker service of agent node " + getAgentNodeName() + " as JMX resource.");
			System.err.println(e.getMessage());					
		}

		_manager = manager;
	}
	  
	/**
     * Deregisters the broker service from management.
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}
		
		// deregister broker service from management
		try {
			_manager.unregisterAgentNodeResource(getAgentNodeName(), "BrokerService");
		}
		catch (Exception e) {
			System.err.println("WARNING: Unable to deregister broker service of agent node " + getAgentNodeName() + " as JMX resource.");
			System.err.println(e.getMessage());					
		}
		
		_manager = null;
	}

	/**
	 * Checks wether the management of this object is enabled or not.
	 * @return true if the management is enabled, otherwise false
	 */
	public boolean isManagementEnabled() {
		return _manager != null;
	}

	// Wrapped methods of broker service

	public void addConnector(String bindAddress) {
		try {
			broker.addConnector(bindAddress);
			values.setUrl(bindAddress);
		} catch (Exception e) {}
	}
	
	public void addNetworkConnector(String discoveryAddress) {
		try {
			broker.addNetworkConnector(discoveryAddress);
		} catch (Exception e) {}
	}

	public void addProxyConnector(String bindAddress) {
		try {
			broker.addProxyConnector(bindAddress);
		} catch (Exception e) {}
	}

	public String getMasterConnectorURI() {
		return broker.getMasterConnectorURI();
	}

	public void setMasterConnectorURI(String masterConnectorURI) {
		broker.setMasterConnectorURI(masterConnectorURI);
	}

	public boolean getSlave() {
		return broker.isSlave();
	}

	public boolean getStarted() {
		return broker.isStarted();
	}

	public String getBrokerName() {
		return broker.getBrokerName();
	}

	public void setBrokerName(String brokerName) {
		broker.setBrokerName(brokerName);
		values.setName(brokerName);
	}

	public String getDataDirectory() {
		return broker.getDataDirectory().toString();
	}

	public void setDataDirectory(String dataDirectory) {
		broker.setDataDirectory(new File(dataDirectory));
	}

	public String getTmpDataDirectory() {
		return broker.getTmpDataDirectory().toString();
	}

	public void setTmpDataDirectory(String tmpDataDirectory) {
		broker.setTmpDataDirectory(new File(tmpDataDirectory));
	}

	public boolean getPersistent() {
		return broker.isPersistent();
	}

	public void setPersistent(boolean persistent) {
		broker.setPersistent(persistent);
		values.setPersistent(persistent);
	}

	public boolean getPopulateJMSXUserID() {
		return broker.isPopulateJMSXUserID();
	}

	public void setPopulateJMSXUserID(boolean populateJMSXUserID) {
		broker.setPopulateJMSXUserID(populateJMSXUserID);
	}

	public boolean getUseJmx() {
		return broker.isUseJmx();
	}

	public void setUseJmx(boolean useJmx) {
		broker.setUseJmx(useJmx);
		values.setJmx(useJmx);
	}

	public String getBrokerObjectName() {
		try {
			return broker.getBrokerObjectName().toString();
		} catch (Exception e) {
			return null;
		}
	}

	public void setBrokerObjectName(String brokerObjectName) {
		try {
			broker.setBrokerObjectName(new ObjectName(brokerObjectName));
		} catch (Exception e) {}
	}

	public String[] getNetworkConnectorURIs() {
		return broker.getNetworkConnectorURIs();
	}

	public void setNetworkConnectorURIs(String[] networkConnectorURIs) {
		broker.setNetworkConnectorURIs(networkConnectorURIs);
	}

	public String[] getTransportConnectorURIs() {
		return broker.getTransportConnectorURIs();
	}

	public void setTransportConnectorURIs(String[] transportConnectorURIs) {
		broker.setTransportConnectorURIs(transportConnectorURIs);
	}

	public boolean getUseLoggingForShutdownErrors() {
		return broker.isUseLoggingForShutdownErrors();
	}

	public void setUseLoggingForShutdownErrors(boolean useLoggingForShutdownErrors) {
		broker.setUseLoggingForShutdownErrors(useLoggingForShutdownErrors);
	}

	public boolean getUseShutdownHook() {
		return broker.isUseShutdownHook();
	}

	public void setUseShutdownHook(boolean useShutdownHook) {
		broker.setUseShutdownHook(useShutdownHook);
	}

	public boolean getAdvisorySupport() {
		return broker.isAdvisorySupport();
	}

	public void setAdvisorySupport(boolean advisorySupport) {
		broker.setAdvisorySupport(advisorySupport);
	}

	public void deleteAllMessages() {
		try {
			broker.deleteAllMessages();
		} catch (Exception e) {}
	}

	public boolean getDeleteAllMessagesOnStartup() {
		return broker.isDeleteAllMessagesOnStartup();
	}

	public void setDeleteAllMessagesOnStartup(boolean deleteAllMessagesOnStartup) {
		broker.setDeleteAllMessagesOnStartup(deleteAllMessagesOnStartup);
	}

	public String getVmConnectorURI() {
		return broker.getVmConnectorURI().toString();
	}

	public void setVmConnectorURI(String vmConnectorURI) {
		try {
			broker.setVmConnectorURI(new URI(vmConnectorURI));
		} catch (Exception e) {}
	}

	public boolean getShutdownOnMasterFailure() {
		return broker.isShutdownOnMasterFailure();
	}

	public void setShutdownOnMasterFailure(boolean shutdownOnMasterFailure) {
		broker.setShutdownOnMasterFailure(shutdownOnMasterFailure);
	}

	public boolean getKeepDurableSubsActive() {
		return broker.isKeepDurableSubsActive();
	}

	public void setKeepDurableSubsActive(boolean keepDurableSubsActive) {
		broker.setKeepDurableSubsActive(keepDurableSubsActive);
	}

	public boolean getUseVirtualTopics() {
		return broker.isUseVirtualTopics();
	}

	public void setUseVirtualTopics(boolean useVirtualTopics) {
		broker.setUseVirtualTopics(useVirtualTopics);
	}

	public int getPersistenceThreadPriority() {
		return broker.getPersistenceThreadPriority();
	}

	public void setPersistenceThreadPriority(int persistenceThreadPriority) {
		broker.setPersistenceThreadPriority(persistenceThreadPriority);
	}
}
