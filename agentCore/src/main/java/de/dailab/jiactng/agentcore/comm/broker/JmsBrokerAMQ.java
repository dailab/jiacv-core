package de.dailab.jiactng.agentcore.comm.broker;

import java.net.BindException;
import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * An embedded AMQ broker. Joachims Broker.. modifiziert.
 * 
 * @todo add configuration support for e.g. multicast
 * @author Joachim Fuchs, Janko Dimitroff
 */
public class JmsBrokerAMQ extends AbstractLifecycle {
	/** The logger we use, if it is not set by DI, we create our own */
	protected Log log = LogFactory.getLog(getClass());

	public BrokerValues values = null;

	/** The embedded broker we use if no other broker is running on our host machine */
	protected BrokerService broker = null;

	/** The connector we use to connect to the broker */
	protected TransportConnector connector = null;

	public void setLog(Log log) {
		this.log = log;
	}

	// ---------------------- Lifecycle
	public void doInit() throws Exception {
		if (log == null){
			System.err.println("No log set for Embedded Broker -> init aborted!");
			return;
		}
		
		if (log.isInfoEnabled()){
			log.info("initializing embedded broker");
		}
		
		if (values._url == null){
			values.setUrlFromPortAndProtocol();
		}
		
		broker = new BrokerService();
		broker.setBrokerName(values.getName());
		broker.setUseJmx(false); // values.isJmx()
		broker.setPersistent(values.isPersistent());
		try {
		
			connector = broker.addConnector(values.getUrl());
			connector.setDiscoveryUri(new URI(getDiscoveryUri(values.getDiscoveryMethod(), values.getDiscoveryAddress())));
			broker.addNetworkConnector(new URI(getDiscoveryUri(values.getDiscoveryMethod(), values.getDiscoveryAddress())));
			connector.getDiscoveryAgent().setBrokerName(values.getName());
		} catch (BindException be) {
			log.error(be.getCause().toString());
			log.warn("No Broker will be started");
			broker = null;
		}
		if(log.isInfoEnabled()) {
			log.info("embedded broker initialized. url = " + values.getUrl());
		}
	}

	public void doStart() throws Exception {
		
		// start broker
		if (broker != null) {
			if (log.isInfoEnabled()){
				log.info("EmbeddedBroker is starting...");
			}
			connector.start();
			broker.start();
			if(log.isInfoEnabled()){
				log.info("broker started");
			}
		} else {
			if (log.isErrorEnabled()){
				log.error("no broker found to start!");
			}
		}
	}

	public void doStop() throws Exception {
		// stop broker
		if (broker != null) {
			if (log.isInfoEnabled()){
				log.info("stopping embedded broker");
			}
			connector.stop();
			broker.stop();
			
			if(log.isInfoEnabled()){
				log.debug("embedded broker stopped");
			}
		} else {
			if (log.isWarnEnabled()){
				log.warn("No Broker found to Stop");
			}
		}
	}

	public void doCleanup() throws Exception {
		if (broker != null){
			if (log.isInfoEnabled()){
				log.info("cleaning up broker");
				log.info("broker cleaned up");
			}
			
		} else {
			if (log.isWarnEnabled()){
				log.warn("No Broker to cleanup");
			}
		}
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
	}

}
