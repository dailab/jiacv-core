package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;

/**
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 * 
 */
public final class ActiveMQBroker extends AbstractAgentNodeBean {
    private static ActiveMQBroker INSTANCE= null;
    
    /*package*/ static void initialiseProxy(ConnectionFactoryProxy proxy) {
        if(INSTANCE == null) {
            throw new IllegalStateException("no broker is running");
        }
        
        proxy.connectionFactory= new ActiveMQConnectionFactory("vm://" + INSTANCE.getBrokerName());
    }
    
    private String _brokerName= null;
    private BrokerService _broker= null;
    private Set<ActiveMQTransportConnector> _connectors= new HashSet<ActiveMQTransportConnector>();
    private boolean _persistent = false;

    public ActiveMQBroker() {
        synchronized (ActiveMQBroker.class) {
            if(INSTANCE != null) {
                throw new IllegalStateException("only on instance per VM is allowed");
            }
            
            INSTANCE= this;
        }
    }

    // Lifecyclemethods:
    public void doInit() throws Exception {
        log.debug("initializing embedded broker");
        
        // TODO: ensure that there are no dots, underscores and so on!
        _brokerName= agentNode.getName() + getBeanName() + hashCode();
        _broker = new BrokerService();
        _broker.setBrokerName(getBrokerName());
        
        if(agentNode.isManagementEnabled()) {
            _broker.setUseJmx(true);
            ManagementContext context = new ManagementContext();
            context.setJmxDomainName("de.dailab.jiactng");
            context.setCreateConnector(false);
            _broker.setManagementContext(context);
        } else {
            _broker.setUseJmx(false);
        }
        
        _broker.setPersistent(_persistent);

        try {
            for (ActiveMQTransportConnector amtc : _connectors) {
                log.debug("embedded broker initializing transport:: " + amtc.toString());
                TransportConnector connector = _broker.addConnector(new URI(amtc.getTransportURI()));
                if (amtc.getDiscoveryURI() != null) {
                    URI uri = new URI(amtc.getDiscoveryURI());
                    URI discoveryURI= new URI(amtc.getDiscoveryURI());
                    connector.setDiscoveryUri(discoveryURI);
                    connector.getDiscoveryAgent().setBrokerName(_broker.getBrokerName());
                    _broker.addNetworkConnector(uri);
                }
            }

        } catch (Exception e) {
            log.error(e.toString());
        }

        _broker.start();
        log.debug("started broker");
    }

    public void doCleanup() throws Exception {
    	log.debug("stopping broker");
    	_broker.stop();
    	log.debug("stopping broker done");
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

    private String getBrokerName() {
        if(_brokerName == null) {
            throw new IllegalStateException("broker is not initialised");
        }

        return _brokerName;
    }
}
