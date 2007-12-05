package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.util.Set;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;

/**
 * @author Martin Loeffelholz
 * @author Marcel Patzlaff
 * 
 */
public class ActiveMQBroker extends AbstractAgentNodeBean {
    /** The embedded broker we use if no other broker is running on our host machine */
    private String _brokerName= null;
    private BrokerService _broker= null;
    private Set<ActiveMQTransportConnector> _connectors= null;
    private boolean _persistent = false;

    public ActiveMQBroker() {}

    // Lifecyclemethods:
    public void doInit() throws Exception {
        log.debug("initializing embedded broker");
        _broker = new BrokerService();
        _broker.setBrokerName(getBrokerName());
        
        if(agentNode.isManagementEnabled()) {
            _broker.setUseJmx(true);
            ManagementContext context = new ManagementContext();
            context.setJmxDomainName("de.dailab.jiactng");
            context.setCreateConnector(false);
            _broker.setManagementContext(context);
        }
        
        _broker.setPersistent(_persistent);

        try {
            for (ActiveMQTransportConnector amtc : _connectors) {
                log.debug("embedded broker initializing transport:: " + amtc.toString());
                TransportConnector connector = _broker.addConnector(new URI(amtc.getTransportURI()));
                if (amtc.getDiscoveryURI() != null) {
//                    URI uri = new URI(amtc.getDiscoveryURI());
                    connector.setDiscoveryUri(new URI(amtc.getDiscoveryURI()));
//                    connector.getDiscoveryAgent().setBrokerName(_broker.getBrokerName());
//                    _broker.addNetworkConnector(uri);
                }
            }

        } catch (Exception e) {
            log.error(e.toString());
        }

        _broker.start();
        log.debug("started broker");
    }

    public void doCleanup() throws Exception {
        _broker.stop();
        log.debug("stopping broker");
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

    /**
     * Setter for the broker name. The name is only changed when this node bean is not
     * yet initialised. Otherwise the call of this method is ignored.
     * <p>
     * You have to ensure, that the broker name is unique. If the broker name is not set
     * the name of this bean will be taken instead!
     * </p>
     * 
     * @param brokerName    the new name for the broker or <code>null</code> to default it
     *                      to the bean name. 
     */
    public void setBrokerName(String brokerName) {
        if(_broker == null) {
            _brokerName= brokerName;
        }
    }
    
    public String getBrokerName() {
        return _brokerName == null ? getBeanName() : _brokerName;
    }
}
