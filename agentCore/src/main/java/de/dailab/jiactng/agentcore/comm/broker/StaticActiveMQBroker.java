package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.security.SecureRandom;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.network.NetworkConnector;

public class StaticActiveMQBroker extends ActiveMQBroker {

   public StaticActiveMQBroker() {
      // if(INSTANCE != null) {
      //
      // throw new IllegalStateException("only one instance of broker per VM is allowed\ninstanceof = " + INSTANCE.getClass());
      // }

	   ActiveMQBroker.instance = this;
   }

   @Override
   public void doInit() throws Exception {
      log.debug("initializing embedded broker");

      _brokerName = agentNode.getName() + getBeanName() + SecureRandom.getInstance("SHA1PRNG").nextLong();
      _broker = new BrokerService();
      _broker.setBrokerName(getBrokerName());

      if (isManagement()) {
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

            // network - connect to a static broker via network
            if (amtc.getNetworkURI() != null) {
               final URI networkUri = new URI(amtc.getNetworkURI());
               //log.debug("adding network connector...");
               final NetworkConnector networkConnector = _broker.addNetworkConnector(networkUri);
               networkConnector.setNetworkTTL(_networkTTL);
               networkConnector.setDuplex(amtc.isDuplex());
               //log.debug("...network connector added");
            }

            _broker.setPersistent(_persistent);

            // transport - locally listening to port
            if (amtc.getTransportURI() != null) {
               //log.debug("adding transport connector...");
               final URI transportUri = new URI(amtc.getTransportURI());
               _broker.addConnector(transportUri);
               //log.debug("...transport connector added");
            }

         }

      } catch (Exception e) {
         log.error(e.toString(),e);
         //e.printStackTrace();
         //System.out.println(e.getStackTrace());
      }

      _broker.start();
      log.debug("started broker");
   }

}
