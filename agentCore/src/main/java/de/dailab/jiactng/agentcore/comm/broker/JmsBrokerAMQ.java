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
		log.debug("initializing embedded broker");
		
		values.setUrlFromPortAndProtocol();
		
		broker = new BrokerService();
		broker.setBrokerName(values.getName());
		broker.setUseJmx(values.isJmx());
		broker.setPersistent(values.isPersistent());
		try {
		
			connector = broker.addConnector(values.getUrl());
			connector.setDiscoveryUri(new URI(getDiscoveryUri(values.getDiscoveryMethod(), values.getDiscoveryAddress())));
			broker.addNetworkConnector(new URI(getDiscoveryUri(values.getDiscoveryMethod(), values.getDiscoveryAddress())));
			connector.getDiscoveryAgent().setBrokerName(values.getName());
		} catch (BindException be) {
			// address is in use already
		}
		log.debug("embedded broker initialized. url = " + values.getUrl());
	}

	public void doStart() throws Exception {
		log.debug("starting broker");
		// start broker
		if (broker != null) {
			connector.start();
			broker.start();
			log.debug("broker started");
		} else {
			log.warn("no broker found to start");
		}
	}

	public void doStop() throws Exception {
		log.debug("stopping broker");
		// stop broker
		if (broker != null) {
			connector.stop();
			broker.stop();
			log.debug("broker stopped");
		}
	}

	public void doCleanup() throws Exception {
		log.debug("cleaning up broker");
		log.debug("broker cleaned up");
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
			le.printStackTrace();
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
