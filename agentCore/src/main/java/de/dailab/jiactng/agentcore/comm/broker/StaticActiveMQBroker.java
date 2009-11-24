package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.security.SecureRandom;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.network.NetworkConnector;

import de.dailab.jiac.net.SourceAwareDiscoveryNetworkConnector;

public class StaticActiveMQBroker extends ActiveMQBroker {

	
    public StaticActiveMQBroker() {
//        if(INSTANCE != null) {
//        	
//            throw new IllegalStateException("only one instance of broker per VM is allowed\ninstanceof = " + INSTANCE.getClass());
//        }
        
        INSTANCE= this;
    }

	@Override
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
                
                //network - connect to a static broker via network
                if (amtc.getNetworkURI() != null) {
                	URI networkUri = new URI(amtc.getNetworkURI());
                    NetworkConnector networkConnector = _broker.addNetworkConnector(networkUri);
                    networkConnector.setNetworkTTL(_networkTTL);
                }

                _broker.setPersistent(_persistent);

                //transport - locally listening to port
                if (amtc.getTransportURI() != null) {
	                URI transportUri = new URI(amtc.getTransportURI());
	                TransportConnector connector= _broker.addConnector(transportUri);
                }

            }

        } catch (Exception e) {
            log.error(e.toString());
        }

        _broker.start();
        log.debug("started broker");
	}

    
}
