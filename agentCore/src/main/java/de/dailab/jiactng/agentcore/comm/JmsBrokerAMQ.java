package de.dailab.jiactng.agentcore.comm;

import java.net.BindException;

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
	protected Log log = null;

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
		if (this.log == null) {
			this.log = LogFactory.getLog(getClass());
		}
		if (log.isDebugEnabled()) {
			log.debug("initializing embedded broker");
		}

		broker = new BrokerService();
		broker.setBrokerName(values.getName());
		broker.setUseJmx(values.isJmx());
		broker.setPersistent(values.isPersistent());
		try {
			connector = broker.addConnector(values.getUrl());
		} catch (BindException be) {
			// address is in use already 
		}

		if (log.isDebugEnabled()) {
			log.debug("embedded broker initialized. url = " + values.getUrl());
		}
	}

	public void doStart() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("starting broker");
		}
		// start broker
		if (broker != null) {
			connector.start();
			broker.start();

			if (log.isDebugEnabled()) {
				log.debug("broker started");
			}
		} else {
			log.warn("no broker found to start");
		}
	}

	public void doStop() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("stopping broker");
		}
		// stop broker
		if (broker != null) {
			connector.stop();
			broker.stop();
			if (log.isDebugEnabled()) {
				log.debug("broker stopped");
			}
		}
	}

	public void doCleanup() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("cleaning up broker");
		}
		// if it is stopped, it doesnt have to be removed
		// if (broker != null && connector != null) {
		// broker.removeConnector(connector);
		// } broker = null;
		if (log.isDebugEnabled()) {
			log.debug("broker cleaned up");
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
			le.printStackTrace();
		}
	}

	// ------------------------ Setter/Getter
	public BrokerValues getValues() {
		return values;
	}

	public void setValues(BrokerValues values) {
		this.values = values;
	}

}
