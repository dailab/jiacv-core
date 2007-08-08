package de.dailab.jiactng.agentcore;

//import de.dailab.jiactng.agentcore.comm.protocolenabler.AbstractProtocolEnabler;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;

import de.dailab.jiactng.Version;
import de.dailab.jiactng.agentcore.comm.broker.BrokerValues;
import de.dailab.jiactng.agentcore.comm.broker.JmsBrokerAMQ;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.servicediscovery.IServiceDescription;
import de.dailab.jiactng.agentcore.servicediscovery.ServiceDirectory;
import de.dailab.jiactng.agentcore.util.IdFactory;

/**
 * Simple agent node implementation
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public class SimpleAgentNode extends AbstractLifecycle implements IAgentNode, InitializingBean, SimpleAgentNodeMBean {

	/** The threadPool object */
	private ExecutorService _threadPool = null;

	/** Log-instance for the agentnode */
	protected Log log = null;
	
	/** A customized logging configuration will be used instead of the default configuration. */
	private String loggingConfig;

	/** this one's fake */
	protected String _uuid = null;

	/** The name of the agentnode. */
	protected String _name = null;

	/** The agentnodes one beans. */
	private ArrayList<ILifecycle> agentNodeBeans;

	/** The list of agents. */
	private ArrayList<IAgent> _agents = null;

	/** Storage for the agentFutures. Used to stop/cancel agentthreads. */
	private HashMap<String, Future> agentFutures = null;

	/** The list of JMX connector servers for remote management. */
	private ArrayList<JMXConnectorServer> _connectorServer = new ArrayList<JMXConnectorServer>();

	/** Configuration of a set of JMX connector server. */
	private Set<Map> _jmxConnectors = null;

	/** Der eingebettete JMSBroker zur Inter-AgentNode-Kommunikation */
	JmsBrokerAMQ _embeddedBroker = null;

	/** Das ServiceDirectory des AgentNodes */
	private ServiceDirectory _serviceDirectory = null;

	/** Shutdown thread to be started when JVM was killed */
	private Thread shutdownhook = new Thread() {
		public void run() {
			System.out.println("\nShutting down agent node ...");
			try {
				shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/** -------------Jetzt geht's lo'hos ----------------------------------------- */

	/** Constructur. Creates the uuid for the agentnode. */
	public SimpleAgentNode() {
		// _uuid = new String("p:" + Long.toHexString(System.currentTimeMillis() + this.hashCode()));
		_uuid = IdFactory.createAgentNodeId(this.hashCode());
	}

	public void setEmbeddedBroker(JmsBrokerAMQ embeddedBroker) {
		_embeddedBroker = embeddedBroker;
	}

	/**
	 * Configuration of a set of JMX connector server.
	 */
	public void setJmxConnectors(Set<Map> jmxConnectors) {
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
		return LogFactory.getLog(getName() + "." + agent.getAgentName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentNode#getLog(de.dailab.jiactng.agentcore.IAgent,
	 *      de.dailab.jiactng.agentcore.AbstractAgentBean)
	 */
	public Log getLog(IAgent agent, IAgentBean bean) {
		return LogFactory.getLog(getName() + "." + agent.getAgentName() + "." + bean.getBeanName());
	}
    
    public Log getLog(IAgent agent, IAgentBean bean, String extension) {
        return LogFactory.getLog(getName() + "." + agent.getAgentName() + "." + bean.getBeanName() + "." + extension);
    }

	/**
	 * Getter for attribute "JiacVersion" of the managed agent node.
	 * @return the version of JIAC TNG
	 */
	public String getJiacVersion() {
		return "JIAC TNG " + Version.getName() + 
			" version " + Version.getNumber() + 
			" (" + Version.getTimestamp() + ")";
	}

	/**
	 * Getter for attribute "JiacVendor" of the managed agent node.
	 * @return the vendor of JIAC TNG
	 */
	public String getJiacVendor() {
		return "DAI-Labor, TU Berlin";
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
		if (_agents != null) {
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
		if (source == _serviceDirectory) {
			switch (evt.getState()) {
				case STARTED:
					Future f1 = _threadPool.submit(_serviceDirectory);
					agentFutures.put(_serviceDirectory.getFutureName(getName()), f1);
					break;
				case STOPPED:
					Future f2 = agentFutures.get(_serviceDirectory.getFutureName(getName()));
					if (f2 == null) {
						(new LifecycleException("ServiceDirectory future not found")).printStackTrace();
					} else {
						// if soft-cancel fails, do a force-cancel.
						if (!f2.cancel(false) && !f2.isDone()) {
							log.warn("ServiceDirectory " + _serviceDirectory.getFutureName(getName()) + " did not respond to stopping. Thread is forcecanceled.");
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

		// create shutdown hook for graceful termination
		Runtime.getRuntime().addShutdownHook(shutdownhook);

		// create all connector server for remote management
		if (_jmxConnectors != null) {
			// Enable remote management
			if (!_jmxConnectors.isEmpty()) {
				System.setProperty("com.sun.management.jmxremote", "");
			}
			// Create and register all specified JMX connector servers
			for (Map conf : _jmxConnectors) {
				// get parameters of connector server
				String protocol = (String) conf.get("protocol");
				if (protocol == null) {
					System.out.println("WARNING: No protocol specified for a JMX connector server");
					continue;
				}
				String portStr = (String) conf.get("port");
				int port = 0;
				if (portStr != null) {
					try {
						port = Integer.parseInt(portStr);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				String path = (String) conf.get("path");
				String authenticator = (String) conf.get("authenticator");

				if (protocol.equals("rmi")) {
					// check use of RMI registry
					String registryPort = (String) conf.get("registryPort");
					String registryHost = (String) conf.get("registryHost");
					if ((registryPort != null) || (registryHost != null)) {
						path = "/jndi/rmi://" + ((registryHost == null) ? "localhost" : registryHost)
																										+ ((registryPort == null) ? "" : ":" + registryPort) + "/" + _name;
					}
				}

				// create connector server
				JMXServiceURL jurl = new JMXServiceURL(protocol, null, port, path);
				HashMap<String,Object> env = new HashMap<String,Object>();
				if (authenticator != null) {
					try {
						Object auth = Class.forName(authenticator).newInstance();
						env.put(JMXConnectorServer.AUTHENTICATOR, auth);
					}
					catch (Exception e) {
						System.err.println("WARNING: Initialisation of JMX authentication failed, because can not create instance of " + authenticator);
					}
				}
				System.out.println("Creating Connector: " + jurl);
				JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(jurl, env, mbs);
				cs.start();
				_connectorServer.add(cs);

				// register connector server
				JMXServiceURL address = cs.getAddress();
				System.out.println("Registering URL for agent node: " + address);
				// TODO register the connector server
				System.out.println("Registered URL: " + address);
				System.out.println("Service URL successfully registered");
			}
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
		if (_agents != null) {
			for (IAgent a : _agents) {
				a.setAgentNode(this);
				a.addLifecycleListener(this.lifecycle.createLifecycleListener());
			}
		}

		// listener am servicedrirectory setzen 
		if (_serviceDirectory != null) {
			_serviceDirectory.addLifecycleListener(this.lifecycle.createLifecycleListener());
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
		// remove shutdown hook
		if (!shutdownhook.isAlive()) {
			Runtime.getRuntime().removeShutdownHook(shutdownhook);
		}

		// clean up agent node
		stop();
		cleanup();

		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

		// deregister all agents as JMX resource
		if (_agents != null) {
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

		if (log != null) {
			log.warn("AgentNode " + getName() + " has been closed.");
		}
	}

	/*
	 * (non-Javadoc) init all registered agents
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doInit()
	 */
	public void doInit() {
		log = LogFactory.getLog(getName());
                if (_embeddedBroker != null) {
                    try {
                        _embeddedBroker.init();
                    } catch (LifecycleException le) {
                        // @todo exception handling
                        le.printStackTrace();
                    }
                }
		_threadPool = Executors.newCachedThreadPool();
		agentFutures = new HashMap<String, Future>();

		// call init on all beans of the agentnodes
		// TODO testing
		if (agentNodeBeans != null) {
			for (ILifecycle anb : agentNodeBeans) {
				log.info("Initializing agentnode bean: " + anb.getClass());
				try {
					anb.init();
				} catch (LifecycleException lce) {
					// TODO
					lce.printStackTrace();
				}
			}
		}

		// call init and set references for all agents if any
		if (_agents != null) {
			for (IAgent a : _agents) {
				log.info("Initializing agent: " + a.getAgentName());
				try {
					a.init();
				} catch (LifecycleException e) {
					// TODO:
					e.printStackTrace();
				}
			}
		}
		
		// das servicedirectory separat initialisieren - is auch ne AgentBean
		try {
			if (_serviceDirectory != null) {
				_serviceDirectory.setAgentNodeName(getName());
				_serviceDirectory.init();
			}
		} catch (LifecycleException e) {
			// TODO:
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStart()
	 */
	public void doStart() {
            if (_embeddedBroker != null) {
                    try {
                        _embeddedBroker.start();
                    } catch (LifecycleException le) {
                        // @todo exception handling
                        le.printStackTrace();
                    }
                }
		// call start on all beans of the agentnode
		// TODO testing
		if (agentNodeBeans != null) {
			for (ILifecycle anb : agentNodeBeans) {
				try {
					anb.start();
				} catch (LifecycleException lce) {
					// TODO
					lce.printStackTrace();
				}
			}
		}

		// call start() and instantiate Threads for all agents if any
		if (_agents != null) {
			for (IAgent a : _agents) {
				try {
					a.start();
				} catch (Exception ex) {
					// TODO
					ex.printStackTrace();
				}
			}
		}

		// das servicedirectory separat starten - is auch ne AgentBean
		try {
			if (_serviceDirectory != null) {
				_serviceDirectory.start();
			}
		} catch (LifecycleException e) {
			// TODO:
			e.printStackTrace();
		}

		log.info("AgentNode " + getName() + " started with " + (_agents!=null?_agents.size():"0") + " agents");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStop()
	 */
	public void doStop() {
		// call stop() for all agents if any
		if (_agents != null) {
			for (IAgent a : _agents) {
				try {
					a.stop();
				} catch (LifecycleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// call stop on all beans of the agentnode
		// TODO testing
		if (agentNodeBeans != null) {
			for (ILifecycle anb : agentNodeBeans) {
				try {
					anb.stop();
				} catch (LifecycleException lce) {
					// TODO
					lce.printStackTrace();
				}
			}
		}
		// das servicedirectory separat stoppen - is auch ne AgentBean
		try {
			if (_serviceDirectory != null) {
				_serviceDirectory.stop();
			}
		} catch (LifecycleException e) {
			// TODO:
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doCleanup()
	 */
	public void doCleanup() {
		// call cleanup for all agents if any
		if (_agents != null) {
			for (IAgent a : _agents) {
				try {
					a.cleanup();
				} catch (LifecycleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// call cleanup on all beans of the agentnode
		// TODO testing
		if (agentNodeBeans != null) {
			for (ILifecycle anb : agentNodeBeans) {
				try {
					anb.cleanup();
				} catch (LifecycleException lce) {
					// TODO
					lce.printStackTrace();
				}
			}
		}
		if (_threadPool != null) {
			_threadPool.shutdown();
		}
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

	/**
	 * Main method for starting JIAC-TNG. Loads a spring-configuration file denoted by the first argument and uses a
	 * ClassPathXmlApplicationContext to instantiate its contents
	 * 
	 * @param args the first argument is interpreted as a classpathrelative name of a spring configurations file. Other
	 *          arguments are ignored.
	 * @see org.springframework.context.support.ClassPathXmlApplicationContext
	 */
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext(args[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public ArrayList<ILifecycle> getAgentNodeBeans() {
		return this.agentNodeBeans;
	}

	/**
	 * Getter for attribute "AgentNodeBeanClasses" of the managed agent node.
	 * @return the class of agent beans running in this agent node
	 */
	public ArrayList<String> getAgentNodeBeanClasses() {
		if (getAgentNodeBeans() == null) {
			return null;
		}
		ArrayList<String> ret = new ArrayList<String>();
		for (ILifecycle bean : getAgentNodeBeans()) {
			ret.add(bean.getClass().getName());
		}
		return ret;		
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAgentNodeBeans(ArrayList<ILifecycle> agentnodebeans) {
		this.agentNodeBeans = agentnodebeans;
	}

	public ServiceDirectory getServiceDirectory() {
		return _serviceDirectory;
	}

	public void setServiceDirectory(ServiceDirectory serviceDirectory) {
		_serviceDirectory = serviceDirectory;
	}

	/**
	 * @return the loggingConfig
	 */
	public String getLoggingConfig() {
		return loggingConfig;
	}

	/**
	 * @param loggingConfig the loggingConfig to set
	 */
	public void setLoggingConfig(String loggingConfig) {
		this.loggingConfig = loggingConfig;
		
		try {
			Log4jConfigurer.initLogging(loggingConfig);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}

	/**
	 * Getter for attribute "Logger" of the managed agent node.
	 * @return information about class and levels of the logger of this agent node
	 */
	public CompositeData getLogger() {
		if (log == null) {
			return null;
		}
		String[] itemNames = new String[] {"class", "debug", "error", "fatal", "info", "trace", "warn"};
		try {
			CompositeType type = new CompositeType("javax.management.openmbean.CompositeDataSupport", "Logger information", itemNames, new String[] {"Implementation of the logger instance", "debug", "error", "fatal", "info", "trace", "warn"}, new OpenType[] {SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN});
			return new CompositeDataSupport(type, itemNames, new Object[] {log.getClass().getName(), log.isDebugEnabled(), log.isErrorEnabled(), log.isFatalEnabled(), log.isInfoEnabled(), log.isTraceEnabled(), log.isWarnEnabled()});
		}
		catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Getter for attribute "AmqBroker" of the managed agent node.
	 * @return the configuration of the embedded ActiveMQ broker of this agent node
	 */
	public CompositeData getAmqBrokerValues() {
		if ((_embeddedBroker == null) || (_embeddedBroker.getValues() == null)) {
			return null;
		}
		BrokerValues values = _embeddedBroker.getValues();
		String[] itemNames = new String[] {"DiscoveryAddress", "DiscoveryMethod", "Name", "Url", "Jmx", "Persistent", "Port", "Protocol"};
		try {
			CompositeType type = new CompositeType("javax.management.openmbean.CompositeDataSupport", "ActiveMQ broker values", itemNames, new String[] {"DiscoveryAddress", "DiscoveryMethod", "Name", "Url", "Jmx", "Persistent", "Port", "Protocol"}, new OpenType[] {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.STRING, SimpleType.STRING});
			return new CompositeDataSupport(type, itemNames, new Object[] {values.getDiscoveryAddress(), values.getDiscoveryMethod(), values.getName(), values.getUrl(), values.isJmx(), values.isPersistent(), values.getPort(), values.getProtocol()});
		}
		catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}	
	}

	/**
	 * Getter for attribute "ServiceDirectoryData" of the managed agent node.
	 * @return information about the service directory of this agent node
	 */
	public CompositeData getServiceDirectoryData() {
		if (_serviceDirectory == null) {
			return null;
		}

		// create commBean data
//		String[] commBeanItemNames = new String[] {"UniversalId", "LocalId", "QueueReceiverQueueName", "QueueSenderDestinationName", "TopicReceiverTopicName", "TopicSenderTopicName", "DefaultQueueName", "DefaultTopicName", "ProtocolType"};
		String[] commBeanItemNames = new String[] {"ConnectorURI", "TransportIdentifier"};
		CompositeDataSupport commBeanData = null;
		CompositeType commBeanType = null;
		try {
//			commBeanType = new CompositeType("javax.management.openmbean.CompositeDataSupport", "Communication bean data", commBeanItemNames, new String[] {"UniversalId", "LocalId", "QueueReceiverQueueName", "QueueSenderDestinationName", "TopicReceiverTopicName", "TopicSenderTopicName", "DefaultQueueName", "DefaultTopicName", "ProtocolType"}, new OpenType[] {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING, SimpleType.STRING});
			commBeanType = new CompositeType("javax.management.openmbean.CompositeDataSupport", "Message transport data", commBeanItemNames, new String[] {"ConnectorURI", "TransportIdentifier"}, new OpenType[] {SimpleType.STRING, SimpleType.STRING});
//			CommBean commBean = _serviceDirectory.getCommBean();
			MessageTransport commBean = _serviceDirectory.getCommBean();
			if (commBean != null) {
//				Object[] commBeanValues = new Object[] {null, null, null, null, null, null, commBean.getDefaultQueueName(), commBean.getDefaultTopicName(), commBean.getProtocolType()};
				Object[] commBeanValues = new Object[] {null, commBean.getTransportIdentifier()};
				if (commBean.getConnectorURI() != null) {
					commBeanValues[0] = commBean.getConnectorURI().toString();
				}
//				if (commBean.getAddress() != null) {
//					commBeanValues[0] = commBean.getAddress().getUniversalId();
//					commBeanValues[1] = commBean.getAddress().getLocalId();
//				}
//				if (commBean.getCommunicator() != null) {
//					if (commBean.getCommunicator().getReceiver() != null) {
//						commBeanValues[2] = commBean.getCommunicator().getReceiver().getQueueName();						
//					}
//					if (commBean.getCommunicator().getSender() != null) {
//						commBeanValues[3] = commBean.getCommunicator().getSender().getDestinationName();						
//					}
//				}
//				if (commBean.getTopicCommunicator() != null) {
//					if (commBean.getTopicCommunicator().getReceiver() != null) {
//						commBeanValues[4] = commBean.getTopicCommunicator().getReceiver().getTopicName();						
//					}
//					if (commBean.getTopicCommunicator().getSender() != null) {
//						commBeanValues[5] = commBean.getTopicCommunicator().getSender().getTopicName();						
//					}
//				}
//				commBeanData = new CompositeDataSupport(commBeanType, commBeanItemNames, commBeanValues);
				commBeanData = new CompositeDataSupport(commBeanType, commBeanItemNames, commBeanValues);
			}
		}
		catch (OpenDataException e) {
			e.printStackTrace();
		}
	
		// get name of all services
		List<IServiceDescription> allServices = _serviceDirectory.getAllServices();
		int size = allServices.size();
		String[] allServiceNames = new String[size];
		for (int i=0; i<size; i++) {
			allServiceNames[i] = allServices.get(i).getName();
		}
		
		// get name of all web services
		List<IServiceDescription> allWebServices = _serviceDirectory.getAllWebServices();
		size = allWebServices.size();
		String[] allWebServiceNames = new String[size];
		for (int i=0; i<size; i++) {
			allWebServiceNames[i] = allWebServices.get(i).getName();
		}
		
		// create service directory data
		String[] itemNames = new String[] {"PublishTimer", "ServiceNumber", "AllWebServiceNames", "AllServiceNames", "MessageTransport"};
		try {
			CompositeType type = new CompositeType("javax.management.openmbean.CompositeDataSupport", "Service directory data", itemNames, new String[] {"PublishTimer", "ServiceNumber", "AllWebServiceNames", "AllServiceNames", "MessageTransport"}, new OpenType[] {SimpleType.INTEGER, SimpleType.INTEGER, new ArrayType(1, SimpleType.STRING), new ArrayType(1, SimpleType.STRING), commBeanType});
			return new CompositeDataSupport(type, itemNames, new Object[] {_serviceDirectory.getPublishTimer(), _serviceDirectory.getServiceNumber(), allWebServiceNames, allServiceNames, commBeanData});
		}
		catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}	
	}

	/**
	 * Activates the java security policy defined in the given policy file by setting 
	 * the system property <code>java.security.policy</code> and activating the 
	 * default security manager.
	 * @param filename the name of the policy file
	 * @see System#setSecurityManager(SecurityManager)
	 */
	public void setAuthorizationPolicyFilename(String filename) {
		System.setProperty("java.security.policy", filename);
		System.setSecurityManager(new SecurityManager());
	}
}
