package de.dailab.jiactng.agentcore;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.comm.JmsBrokerAMQ;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * Simple platform implementation
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public class SimpleAgentNode extends AbstractLifecycle implements IAgentNode, InitializingBean, SimpleAgentNodeMBean {

	/** The threadPool object */
	private ExecutorService _threadPool = null;

	/** Log-instance for the agentnode */
	protected Log log = null;

	/** this one's fake */
	protected String _uuid = null;

	/** The name of the agentnode. */
	protected String _name = null;

	/** The list of agents. */
	private ArrayList<IAgent> _agents = null;

	/** Storage for the agentFutures. Used to stop/cancel agentthreads. */
	private HashMap<String, Future> agentFutures = null;

	/** The list of JMX connector servers for remote management. */
	private ArrayList<JMXConnectorServer> _connectorServer = new ArrayList<JMXConnectorServer>();

	/** Comma separated list of provided JMX connector protocols. */
	private String _jmxConnectors = "";

	JmsBrokerAMQ _embeddedBroker = null;

	/** Constructur. Creates the uuid for the agentnode. */
	public SimpleAgentNode() {
		_uuid = new String("p:" + Long.toHexString(System.currentTimeMillis() + this.hashCode()));
	}

	public void setEmbeddedBroker(JmsBrokerAMQ embeddedBroker) {
		_embeddedBroker = embeddedBroker;
	}

	/**
	 * Setter for comma separated list of JMX connector protocols.
	 */
	public void setJmxConnectors(String jmxConnectors) {
		this._jmxConnectors = jmxConnectors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#setAgents(java.util.ArrayList)
	 */
	public void setAgents(ArrayList<IAgent> agents) {
		_agents = agents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#addAgent(de.dailab.jiactng.agentcore.IAgent)
	 */
	public void addAgent(IAgent agent) {
		// TODO: statechanges?
		ArrayList<String> oldAgentList = getAgents();
		_agents.add(agent);
		agent.addLifecycleListener(this.lifecycle.createLifecycleListener());
		agentListChanged(oldAgentList, getAgents());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#removeAgent(de.dailab.jiactng.agentcore.IAgent)
	 */
	public void removeAgent(IAgent agent) {
		// TODO: statechanges?
		ArrayList<String> oldAgentList = getAgents();
		_agents.remove(agent);
		agentListChanged(oldAgentList, getAgents());
	}

	/**
	 * Uses JMX to send notifications that the attribute "Agents" of the managed agent node has been changed (e.g. added
	 * or removed agent).
	 * 
	 * @param oldAgentList the old list of agent names
	 * @param newAgentList the new list of agent names
	 */
	private void agentListChanged(ArrayList<String> oldAgentList, ArrayList<String> newAgentList) {
		Notification n = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
																						"Agents changed", "Agents", "java.util.ArrayList<java.lang.String>",
																						oldAgentList, newAgentList);
		sendNotification(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#findAgents()
	 */
	public ArrayList<IAgent> findAgents() {
		// TODO: Security must decide whether the lifelist should be returned or
		// not.
		return _agents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#getLog(de.dailab.jiactng.agentcore.IAgent)
	 */
	public Log getLog(IAgent agent) {
		return LogFactory.getLog(getName() + ":" + agent.getAgentName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#getLog(de.dailab.jiactng.agentcore.IAgent,
	 *      de.dailab.jiactng.agentcore.AbstractAgentBean)
	 */
	public Log getLog(IAgent agent, IAgentBean bean) {
		return LogFactory.getLog(getName() + ":" + agent.getAgentName() + ":" + bean.getBeanName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#getUUID()
	 */
	public String getUUID() {
		return this._uuid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#getName()
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Returns the name of localhost.
	 * 
	 * @return name of localhost
	 * @see InetAddress#toString()
	 */
	public String getHost() throws UnknownHostException {
		return InetAddress.getLocalHost().toString();
	}

	/**
	 * Returns the user's account name.
	 * 
	 * @return user's account name
	 * @see System#getProperties()
	 */
	public String getOwner() {
		return System.getProperty("user.name");
	}

	/**
	 * Returns the names of agents which reside on this agent node.
	 * 
	 * @return list of agent names
	 */
	public ArrayList<String> getAgents() {
		ArrayList<String> result = new ArrayList<String>();
		for (IAgent a : _agents) {
			result.add(a.getAgentName());
		}
		return result;
	}

	/**
	 * Deploys and starts new agents on this agent node.
	 * 
	 * @param name of the XML file which contains the spring configuration of the agents
	 */
	public void addAgents(String configFile) {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext(new String[] { configFile + ".xml" });
		Collection newAgents = appContext.getBeansOfType(IAgent.class).values();
		for (Object a : newAgents) {
			IAgent agent = (IAgent) a;
			agent.setAgentNode(this);
			addAgent(agent);
			try {
				agent.init();
				agent.start();
			} catch (LifecycleException e) {
				// TODO:
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name) {
		_name = name;
	}

	/**
	 * Handles the change of lifecycle states of agents on this agent node.
	 * 
	 * @param evt the lifecycle event
	 */
	public void onEvent(LifecycleEvent evt) {
		Object source = evt.getSource();
		if (_agents.contains(source)) {
			IAgent agent = (IAgent) source;
			switch (evt.getState()) {
				case STARTED:
					Future f1 = _threadPool.submit(agent);
					agentFutures.put(agent.getAgentName(), f1);
					break;
				case STOPPED:
					Future f2 = agentFutures.get(agent.getAgentName());
					if (f2 == null) {
						(new LifecycleException("Agentfuture not found")).printStackTrace();
					} else {
						// if soft-cancel fails, do a force-cancel.
						if (!f2.cancel(false) && !f2.isDone()) {
							log.warn("Agent " + agent.getAgentName() + " did not respond then stopping. Thread is forcecanceled.");
							f2.cancel(true);
						}
					}
					break;
			}
		}
	}

	/**
	 * Initialisation-method. This method is called by Spring after startup (through the InitializingBean-Interface) and
	 * is used to start the agentnode after all beans haven been instantiated by Spring. Currently only creates the JMX
	 * connector servers, registers the agent node as JMX resource and calls the init() and start()-methods from
	 * ILifefycle for this.
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		// Enable remote management
		StringTokenizer st = new StringTokenizer(_jmxConnectors, ",");
		if (st.countTokens() > 0) {
			System.setProperty("com.sun.management.jmxremote", "");
		}
		// Create and register all specified JMX connector servers
		while (st.hasMoreTokens()) {
			// get parameters of connector server
			String protocol = st.nextToken().trim().toLowerCase();
			int port = 0;
			String path = null;
			if (protocol.equals("soap")) {
				port = 8080;
				path = "/jmxconnector";
			}

			// create connector server
			JMXServiceURL jurl = new JMXServiceURL(protocol, null, port, path);
			System.out.println("Creating Connector: " + jurl);
			JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(jurl, null, mbs);
			cs.start();
			_connectorServer.add(cs);

			// register connector server
			JMXServiceURL address = cs.getAddress();
			System.out.println("Registering URL for agent node: " + address);
			// TODO register the connector server
			System.out.println("Registered URL: " + address);
			System.out.println("Service URL successfully registered");
		}

		// register agent node as JMX resource
		try {
			ObjectName name = new ObjectName("de.dailab.jiactng.agentcore:type=SimpleAgentNode,name=" + _name);
			mbs.registerMBean(this, name);
			System.out.println("Agent node " + _name + " registered as JMX resource.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// set references for all agents
		addLifecycleListener(this);
		for (IAgent a : _agents) {
			a.setAgentNode(this);
			a.addLifecycleListener(this.lifecycle.createLifecycleListener());
		}

		// start agent node
		init();
		start();
	}

	/**
	 * Shuts down the managed agent node and all its agents (incl. deregistration as JMX resource) before stopping all JMX
	 * connector servers.
	 * 
	 * @throws de.dailab.jiactng.agentcore.lifecycle.LifecycleException
	 */
	public void shutdown() throws LifecycleException {
		// clean up agent node
		stop();
		cleanup();

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		// deregister all agents as JMX resource
		for (IAgent a : _agents) {
			try {
				ObjectName name = new ObjectName("de.dailab.jiactng.agentcore:type=Agent,name=" + a.getAgentName());
				if (mbs.isRegistered(name)) {
					mbs.unregisterMBean(name);
				}
				System.out.println("Agent " + a.getAgentName() + " deregistered as JMX resource.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		_agents = null;

		// unregister agent node as JMX resource
		try {
			ObjectName name = new ObjectName("de.dailab.jiactng.agentcore:type=SimpleAgentNode,name=" + _name);
			if (mbs.isRegistered(name)) {
				mbs.unregisterMBean(name);
			}
			System.out.println("Agent node " + _name + " deregistered as JMX resource.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Stop all connector servers
		for (JMXConnectorServer cs : this._connectorServer) {
			System.out.println("Stop the connector server: " + cs.getAddress().toString());
			try {
				cs.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this._connectorServer = null;

		log.warn("AgentNode " + getName() + " has been closed.");
	}

	/*
	 * (non-Javadoc) init all registered agents
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doInit()
	 */
	public void doInit() {
		log = LogFactory.getLog(getName());
		_threadPool = Executors.newCachedThreadPool();
		agentFutures = new HashMap<String, Future>();

		// call init and set references for all agents
		for (IAgent a : _agents) {
			log.warn("Initializing agent: " + a.getAgentName());
			try {
				a.init();
			} catch (LifecycleException e) {
				// TODO:
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStart()
	 */
	public void doStart() {
		// call start() and instantiate Threads for all agents
		for (IAgent a : _agents) {
			try {
				a.start();
			} catch (Exception ex) {
				// TODO
				ex.printStackTrace();
			}
		}

		log.warn("AgentNode " + getName() + " started with " + _agents.size() + " agents");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStop()
	 */
	public void doStop() {
		// call stop() for all agents
		for (IAgent a : _agents) {
			try {
				a.stop();
			} catch (LifecycleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doCleanup()
	 */
	public void doCleanup() {
		// call cleanup for all agents
		for (IAgent a : _agents) {
			try {
				a.cleanup();
			} catch (LifecycleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		_threadPool.shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#getThreadPool()
	 */
	public ExecutorService getThreadPool() {
		return _threadPool;
	}

}
