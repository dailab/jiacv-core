package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.jms.ConnectionFactory;

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
public class ActiveMQBroker extends AbstractAgentNodeBean {
    public static final String PROTOCOL_IP_SEPARATOR = "://";

    public static final char IP_PORT_SEPARATOR = ':';

    /** The embedded broker we use if no other broker is running on our host machine */
    private BrokerService _broker = new BrokerService();
    private Set<String> _connectors = new HashSet<String>();
    private String _discoveryMethod = null;
    private String _discoveryAddress = null;
    private boolean _persistent = false;
    private boolean _jmx = true;

    public ActiveMQBroker() {}

    // Lifecyclemethods:
    public void doInit() throws Exception {
        log.debug("initializing embedded broker");
        _broker = new BrokerService();
        _broker.setBrokerName(getBeanName());
        _broker.setUseJmx(_jmx);
        _broker.setPersistent(_persistent);

        ManagementContext context = new ManagementContext();
        context.setJmxDomainName("de.dailab.jiactng");
        context.setCreateConnector(false);
        _broker.setManagementContext(context);

        try {
            for (String url : _connectors) {
                log.debug("embedded broker initializing url = " + url);
                TransportConnector connector = _broker.addConnector(url);
                if (_discoveryMethod != null && _discoveryAddress != null) {
                    URI uri = new URI(_discoveryMethod + "://" + _discoveryAddress);
                    connector.setDiscoveryUri(uri);
                    connector.getDiscoveryAgent().setBrokerName(_broker.getBrokerName());
                    _broker.addNetworkConnector(uri);
                }
            }

        } catch (Exception e) {
            log.error(e.toString());
        }
        log.debug("embedded broker initialized: Persistent=" + _persistent + ", UseJMX=" + _jmx);

        _broker.start();
        log.debug("started broker");
    }

    public void doCleanup() throws Exception {
        _broker.stop();
        log.debug("stopping broker");
    }

    public void setJmx(boolean jmx) {
        _jmx = jmx;
    }

    public void setPersistent(boolean persistent) {
        _persistent = persistent;
    }

    public void setConnectors(Set<String> urlList) {
        _connectors = urlList;
    }

    public void setDiscoveryMethod(String discoveryMethod) {
        _discoveryMethod = discoveryMethod;
    }

    public void setDiscoveryAddress(String discoveryAddress) {
        _discoveryAddress = discoveryAddress;
    }
    
    // FACTORY METHODS FOR CONNECTOR CREATION
    public ConnectionFactory createConnectionFactory() {
        ActiveMQConnectionFactory factory=  new ActiveMQConnectionFactory("vm://" + getBeanName());
//        factory.setUseAsyncSend(true);
//        factory.setDispatchAsync(true);
        return factory;
    }
}
