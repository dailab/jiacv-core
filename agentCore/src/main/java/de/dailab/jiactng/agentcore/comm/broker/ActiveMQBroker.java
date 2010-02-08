package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.security.SecureRandom;
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

/**
 * Implements a message broker as agent node bean based on ActiveMQ technology.
 * @see org.apache.activemq.broker.BrokerService
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 * 
 */
public class ActiveMQBroker extends AbstractAgentNodeBean {
    protected static ActiveMQBroker INSTANCE= null;
    
    /*package*/ static void initialiseProxy(ConnectionFactoryProxy proxy) {
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

    @SuppressWarnings("unchecked")
    public void setNetworkTTL(int networkTTL) throws Exception {
        if(_networkTTL != networkTTL && _broker != null) {
        	List<NetworkConnector> netcons = _broker.getNetworkConnectors();
            for (NetworkConnector net : netcons){
            	_broker.removeNetworkConnector(net);
            	net.setNetworkTTL(networkTTL);
            	_broker.addNetworkConnector(net);
            }
        }
        
        _networkTTL= networkTTL;
    }
    
    // Lifecyclemethods:
    public void doInit() throws Exception {
        log.debug("initializing embedded broker");
        
        _brokerName= agentNode.getName() + getBeanName() + SecureRandom.getInstance("SHA1PRNG").nextLong();
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
        
        try {
            for (ActiveMQTransportConnector amtc : _connectors) {
                log.debug("embedded broker initializing transport:: " + amtc.toString());
                if (amtc.getNetworkURI() != null) {
                	URI networkUri = new URI(amtc.getNetworkURI());
                    NetworkConnector networkConnector = _broker.addNetworkConnector(networkUri);
                    networkConnector.setDuplex(amtc.isDuplex());
                    networkConnector.setNetworkTTL(amtc.getNetworkTTL());
                }

                _broker.setPersistent(_persistent);

            	TransportConnector connector= _broker.addConnector(new URI(amtc.getTransportURI()));
                if (amtc.getDiscoveryURI() != null) {
                    URI uri = new URI(amtc.getDiscoveryURI());
                    URI discoveryURI= new URI(amtc.getDiscoveryURI());
                    connector.setDiscoveryUri(discoveryURI);
//no such method in 5.3 connector.getDiscoveryAgent().setBrokerName(_broker.getBrokerName());
                    NetworkConnector networkConnector= new SourceAwareDiscoveryNetworkConnector(uri);
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

    protected String getBrokerName() {
        if(_brokerName == null) {
            throw new IllegalStateException("broker is not initialised");
        }

        return _brokerName;
    }
}
