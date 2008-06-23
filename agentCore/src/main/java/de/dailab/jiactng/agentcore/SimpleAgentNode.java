package de.dailab.jiactng.agentcore;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.net.SocketAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.Log4jConfigurer;

import de.dailab.jiactng.Version;
import de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean;
import de.dailab.jiactng.agentcore.conf.GenericAgentProperties;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.management.jmx.JmxManager;
import de.dailab.jiactng.agentcore.util.IdFactory;
import de.dailab.jiactng.agentcore.util.jar.JARClassLoader;
import de.dailab.jiactng.agentcore.util.jar.JARMemory;

/**
 * Simple agent node implementation
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public class SimpleAgentNode extends AbstractLifecycle implements IAgentNode, InitializingBean, SimpleAgentNodeMBean {

	/** The threadPool object */
	private ExecutorService _threadPool = null;

	/**
	 * A customized logging configuration will be used instead of the default
	 * configuration.
	 */
	private String loggingConfig;

	/** this one's fake */
	protected String _uuid = null;

	/** The name of the agentnode. */
	protected String _name = null;

	/** The agentnodes one beans. */
	private final ArrayList<IAgentNodeBean> _agentNodeBeans;

	/** The list of agents. */
	private final ArrayList<IAgent> _agents;

	/** Storage for the agentFutures. Used to stop/cancel agentthreads. */
	private HashMap<String, Future<?>> agentFutures = null;

	/** Configuration of a set of JMX connector server. */
	private Set<Map<String, Object>> _jmxConnectors = null;

	/** The manager of the agent node */
	private Manager _manager = null;
	
	/** Optional: DirectoryAgentNodeBean to add local Agents to */
	private DirectoryAgentNodeBean _directory = null;

	/** Shutdown thread to be started when JVM was killed */
	private Thread shutdownhook = new Thread() {
		public void run() {
			System.out.println("\nShutting down agent node '" + SimpleAgentNode.this.getName() + "'...");
			try {
				shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * Main method for starting JIAC-TNG. Loads a spring-configuration file
	 * denoted by the first argument and uses a ClassPathXmlApplicationContext
	 * to instantiate its contents
	 * 
	 * @param args
	 *            the first argument is interpreted as a classpathrelative name
	 *            of a spring configurations file. Other arguments are ignored.
	 * @see org.springframework.context.support.ClassPathXmlApplicationContext
	 */
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext(args[0]);
	}

	/**
	 * -------------Jetzt geht's lo'hos
	 * -----------------------------------------
	 */

	/** Constructor. Creates the uuid for the agentnode. */
	public SimpleAgentNode() {
		_uuid = IdFactory.createAgentNodeId(this.hashCode());
		setLog(LogFactory.getLog(_uuid));
		_agentNodeBeans = new ArrayList<IAgentNodeBean>();
		_agents = new ArrayList<IAgent>();
	}

	/**
	 * Configuration of a set of JMX connector server.
	 * 
	 * @param jmxConnectors
	 *            the set of connectors.
	 */
	public void setJmxConnectors(Set<Map<String, Object>> jmxConnectors) {
		this._jmxConnectors = jmxConnectors;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAgents(List<IAgent> agents) {
		// owner of agent node is also owner of the initially created agents
		String owner = getOwner();
		for (IAgent agent : agents) {
			agent.setOwner(owner);
		}

		// refresh agent list
		_agents.clear();// TODO is this really necessary???
		_agents.addAll(agents);
	}

	/**
	 * adds multiple unique agents from a single GenericAgentProperties description
	 * to this AgentNode at once<br/>
	 * this method is called by Spring when using the genericAgents property, see the JUnit test GenericAgentPropertiesInjection for an example
	 * 
	 * @param agentProps
	 *            a List of GenericAgentProperties which contain the number of
	 *            Agents to be added, the Agent configuration, the AgentBeanName and a pattern from
	 *            which the Agent name will be generated
	 *            
	 *  TODO maybe simplify this by adding some sort of cloning mechanism the the Agent class using reflection. having this, any agent could create a new instance of himself. 
	 *   
	 */
	public void setGenericAgents(List<GenericAgentProperties> agentProps) {
		//long start  = System.currentTimeMillis();		
		if (agentProps != null && agentProps.size() > 0) {
			ArrayList<IAgent> agents = new ArrayList<IAgent>();
			for (GenericAgentProperties gap : agentProps) {
				ClassPathXmlApplicationContext cpxac = new ClassPathXmlApplicationContext(gap.getAgentConfig());
				if (cpxac != null && cpxac.containsBeanDefinition(gap.getAgentBeanName())) {
					for (int i = 1; i <= gap.getCount(); i++) {
						IAgent newAgent = (IAgent) cpxac.getBean(gap.getAgentBeanName());
						newAgent.setAgentName(gap.createAgentName(i));
						//log does not work in this context somehow: using system instead
						//System.out.println("adding agent: " + newAgent.getAgentName() + " with ID: " + newAgent.getAgentId());
						agents.add(newAgent);
					}
				} else System.out.println(new StringBuffer("could not create xmlapplicationcontext from ")
				.append(gap.getAgentConfig())
				.append(" or agentbean definition does not exist, beanname is: ")
				.append(gap.getAgentBeanName()));
			}
			_agents.addAll(agents);
		} else {
			System.out.println("nothing to do - GenericAgentProperties list is null or empty");
		}
		//System.out.println("creation took " + (System.currentTimeMillis() - start) + " milliseconds");
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAgent(IAgent agent) {
		agent.setAgentNode(this);

		// TODO: statechanges?
		List<String> oldAgentList = getAgents();
		synchronized(_agents) {
			_agents.add(agent);
		}
		agent.addLifecycleListener(this.lifecycle.createLifecycleListener());
		agentListChanged(oldAgentList, getAgents());

		if (_directory != null){
			agent.addLifecycleListener(_directory);
		}
		// register agent for management
		agent.enableManagement(_manager);
		
	}

	/**
	 * {@inheritDoc}
	 */

	public void removeAgent(IAgent agent) {
		// TODO: statechanges?

		// deregister agent from management
		agent.disableManagement();

		// remove agent
		List<String> oldAgentList = getAgents();
		_agents.remove(agent);
		agentListChanged(oldAgentList, getAgents());
	}

	/**
	 * Uses JMX to send notifications that the attribute "Agents" of the managed
	 * agent node has been changed (e.g. added or removed agent).
	 * 
	 * @param oldAgentList
	 *            the old list of agent names
	 * @param newAgentList
	 *            the new list of agent names
	 */
	private void agentListChanged(List<String> oldAgentList, List<String> newAgentList) {
		Notification n = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(), "Agents changed", "Agents", "java.util.ArrayList<java.lang.String>", oldAgentList,
				newAgentList);
		sendNotification(n);
	}

	/**
	 * {@inheritDoc}
	 */

	public List<IAgent> findAgents() {
		return Collections.unmodifiableList(_agents);
	}

	/**
	 * {@inheritDoc}
	 */

	public Log getLog(IAgentNodeBean nodeBean) {
		return LogFactory.getLog(getUUID() + "." + nodeBean.getBeanName());
	}

	/**
	 * {@inheritDoc}
	 */

	public Log getLog(IAgent agent) {
		return LogFactory.getLog(getUUID() + "." + agent.getAgentId());
	}

	/**
	 * {@inheritDoc}
	 */

	public Log getLog(IAgent agent, IAgentBean bean) {
		return LogFactory.getLog(getUUID() + "." + agent.getAgentId() + "." + bean.getBeanName());
	}

	/**
	 * {@inheritDoc}
	 */

	public Log getLog(IAgent agent, IAgentBean bean, String extension) {
		return LogFactory.getLog(getUUID() + "." + agent.getAgentId() + "." + bean.getBeanName() + "." + extension);
	}

	/**
	 * Getter for attribute "JiacVersion" of the managed agent node.
	 * 
	 * @return the version of JIAC TNG
	 */
	public String getJiacVersion() {
		return "JIAC TNG " + Version.getName() + " version " + Version.getNumber() + " (" + Version.getTimestamp() + ")";
	}

	/**
	 * Getter for attribute "JiacVendor" of the managed agent node.
	 * 
	 * @return the vendor of JIAC TNG
	 */
	public String getJiacVendor() {
		return "DAI-Labor, TU Berlin";
	}

	/**
	 * {@inheritDoc}
	 */

	public String getUUID() {
		return this._uuid;
	}

	/**
	 * {@inheritDoc}
	 */

	public String getName() {
		return _name;
	}

	/**
	 * Returns the name of localhost.
	 * 
	 * @return name of localhost
	 * @throws UnknownHostException
	 *             if no IP address for the local host could be found.
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
	 * Returns the unique identifiers of agents which reside on this agent node.
	 * 
	 * @return list of unique agent identifiers
	 */
	public List<String> getAgents() {
		ArrayList<String> result = new ArrayList<String>();
		for (IAgent a : _agents) {
			result.add(a.getAgentId());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAgents(byte[] configuration, List<JARMemory> libraries, String owner) throws Exception {
		// create classloader for the new agents
		JARClassLoader cl = new JARClassLoader();
		for (JARMemory jar: libraries) {
			cl.addJAR(jar);
		}

		// create spring application context
		GenericApplicationContext appContext = new GenericApplicationContext();
		appContext.setClassLoader(cl);
		XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
		xmlReader.loadBeanDefinitions(new ByteArrayResource(configuration));
		appContext.refresh();

		// add and start all created agents
		Collection<?> newAgents = appContext.getBeansOfType(IAgent.class).values();
		for (Object a : newAgents) {
			IAgent agent = (IAgent) a;
			agent.setOwner(owner);
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

	/**
	 * {@inheritDoc}
	 */

	public void setBeanName(String name) {
		if (isManagementEnabled()) {
			Manager manager = _manager;
			disableManagement();
			_name = name;
			enableManagement(manager);
		} else {
			_name = name;
		}
	}

	/**
	 * Handles the change of lifecycle states of agents on this agent node.
	 * 
	 * @param evt
	 *            the lifecycle event
	 */
	public void onEvent(LifecycleEvent evt) {
		Object source = evt.getSource();
		if (_agents.contains(source)) {
			IAgent agent = (IAgent) source;
			switch (evt.getState()) {
			case STARTED:
				Future<?> f1 = _threadPool.submit(agent);
				agentFutures.put(agent.getAgentName(), f1);
				break;
			case STOPPED:
				Future<?> f2 = agentFutures.get(agent.getAgentName());
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
	 * Initialisation-method. This method is called by Spring after startup
	 * (through the InitializingBean-Interface) and is used to start the
	 * agentnode after all beans haven been instantiated by Spring. Currently
	 * only creates the JMX connector servers, registers the agent node as JMX
	 * resource and calls the init() and start()-methods from ILifefycle for
	 * this.
	 * 
	 * @throws Exception
	 *             if the agent node can not be initialized and started.
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		// create shutdown hook for graceful termination
		Runtime.getRuntime().addShutdownHook(shutdownhook);

		// set references for all agents
		addLifecycleListener(this);
		for (IAgent a : _agents) {
			a.setAgentNode(this);
			a.addLifecycleListener(this.lifecycle.createLifecycleListener());
		}

		for (IAgentNodeBean nodeBean : _agentNodeBeans) {
			nodeBean.setAgentNode(this);
			nodeBean.addLifecycleListener(this.lifecycle.createLifecycleListener());
		}

		// enable management of agent node and all its resources
		enableManagement(new JmxManager());

		// start agent node
		init();
		start();
	}

	/**
	 * Shuts down the managed agent node and all its agents (incl.
	 * deregistration as JMX resource) before stopping all JMX connector
	 * servers.
	 * 
	 * @throws LifecycleException
	 *             if an error occurs during stop and cleanup of this agent
	 *             node.
	 */
	public void shutdown() throws LifecycleException {
		// remove shutdown hook
		if (!shutdownhook.isAlive()) {
			Runtime.getRuntime().removeShutdownHook(shutdownhook);
		}

		// clean up agent node
		stop();
		cleanup();

		// remove agents
		while (!_agents.isEmpty()) {
			removeAgent(_agents.get(_agents.size()-1));
		}

		// disable management of agent node and all its resources
		disableManagement();

		_agentNodeBeans.clear();

		if (log != null) {
			log.info("AgentNode " + getName() + " has been closed.");
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public void doInit() {
		_threadPool = Executors.newCachedThreadPool();
		agentFutures = new HashMap<String, Future<?>>();

		// call init on all beans of the agentnodes
		// TODO testing
		if (_agentNodeBeans != null) {
			for (IAgentNodeBean anb : _agentNodeBeans) {
				try {
					if (log != null && log.isInfoEnabled()) {
						log.info("Trying to initialize agentnodebean: " + anb.getBeanName());
					}
					anb.init();
				} catch (LifecycleException lce) {
					// TODO
					if(log != null) {
						log.error("Failure when initializing agentnodebean: "+anb.getBeanName(),lce);
					} else {
						System.err.println("Failure when initializing agentnodebean: "+anb.getBeanName());
						lce.printStackTrace();
					}
				}
			}
		}

		// call init and set references for all agents if any
		if (_agents != null) {
			for (IAgent a : _agents) {
				log.info("Initializing agent: " + a.getAgentName());
				try {
					if (log != null && log.isInfoEnabled()) {
						log.info("Trying to initialize agent: " + a.getAgentName());
					}
					a.init();				
				} catch (LifecycleException e) {
					// TODO:
					if(log != null) {
						log.error("Failure when initializing agent: "+a.getAgentName(),e);
					} else {
						System.err.println("Failure when initializing agent: "+a.getAgentName());
						e.printStackTrace();
					}
				}

				if (_directory == null){
					
					// Check if White Pages bean is present
					for (IAgentNodeBean agentNodeBean : this.getAgentNodeBeans()) {
						if (agentNodeBean instanceof DirectoryAgentNodeBean) {
							// directory is present so add agentdescription to it.
							_directory = (DirectoryAgentNodeBean) agentNodeBean;
							break;// we found what we were looking for so we can stop searching
						}
					}
					if(_directory!= null) {
						_directory.addAgentDescription(a.getAgentDescription());
					}
				} else {
					_directory.addAgentDescription(a.getAgentDescription());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public void doStart() {
		// call start on all beans of the agentnode
		// TODO testing
		if (_agentNodeBeans != null) {
			for (IAgentNodeBean anb : _agentNodeBeans) {
				try {
					if (log != null) {
						log.info("Trying to start agentnodebean: " + anb.getBeanName());
					}
					anb.start();
				} catch (LifecycleException lce) {
					// TODO
					lce.printStackTrace();
				}
			}
		}

		synchronized(_agents) {
			// call start() and instantiate Threads for all agents if any
			if (_agents != null) {
				for (IAgent a : _agents) {
					try {
						if (log != null) {
							log.info("Trying to start agent: " + a.getAgentName());
						}
						a.start();
					} catch (Exception ex) {
						// TODO
						ex.printStackTrace();
					}
				}
			}
		}

		log.info("AgentNode " + getName() + " started with " + (_agents != null ? _agents.size() : "0") + " agents");
	}

	/**
	 * {@inheritDoc}
	 */

	public void doStop() {
		// call stop() for all agents if any
		if (_agents != null) {
			for (IAgent a : _agents) {
				try {
					if (log != null) {
						log.info("Trying to stop agent: " + a.getAgentName());
					}
					a.stop();
				} catch (LifecycleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// call stop on all beans of the agentnode in reverse order
		// TODO testing
		if (_agentNodeBeans != null) {
			for (int i=_agentNodeBeans.size()-1; i>=0; i--) {
				IAgentNodeBean anb = _agentNodeBeans.get(i);
				try {
					if (log != null) {
						log.info("Trying to stop agentnodebean: " + anb.getBeanName());
					}
					anb.stop();
				} catch (LifecycleException lce) {
					// TODO
					lce.printStackTrace();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void doCleanup() {
		// call cleanup for all agents if any
		if (_agents != null) {
			for (IAgent a : _agents) {
				try {
					if (log != null) {
						log.info("Trying to cleanup agent: " + a.getAgentName());
					}
					a.cleanup();
				} catch (LifecycleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// call cleanup on all beans of the agentnode in reverse order
		// TODO testing
		if (_agentNodeBeans != null) {
			for (int i=_agentNodeBeans.size()-1; i>=0; i--) {
				IAgentNodeBean anb = _agentNodeBeans.get(i);
				try {
					if (log != null) {
						log.info("Trying to cleanup agentnodebean: " + anb.getBeanName());
					}
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

	/**
	 * {@inheritDoc}
	 */

	public String toString() {
		return getName();
	}

	/**
	 * {@inheritDoc}
	 */

	public ExecutorService getThreadPool() {
		return _threadPool;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IAgentNodeBean> getAgentNodeBeans() {
		return Collections.unmodifiableList(_agentNodeBeans);
	}

	/**
	 * Getter for attribute "AgentNodeBeanClasses" of the managed agent node.
	 * 
	 * @return the class of agent beans running in this agent node
	 */
	public List<String> getAgentNodeBeanClasses() {
		ArrayList<String> ret = new ArrayList<String>();
		for (ILifecycle bean : getAgentNodeBeans()) {
			ret.add(bean.getClass().getName());
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAgentNodeBeans(List<IAgentNodeBean> agentnodebeans) {
		_agentNodeBeans.clear();
		_agentNodeBeans.addAll(agentnodebeans);
	}

	/**
	 * @return the loggingConfig
	 */
	public String getLoggingConfig() {
		return loggingConfig;
	}

	/**
	 * @param loggingConfig
	 *            the loggingConfig to set
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
	 * Activates the java security policy defined in the given policy file by
	 * setting the system property <code>java.security.policy</code> and
	 * activating the default security manager.
	 * 
	 * @param filename
	 *            the name of the policy file
	 * @see System#setSecurityManager(SecurityManager)
	 */
	public void setAuthorizationPolicyFilename(String filename) {
		System.setProperty("java.security.policy", filename);
		System.setSecurityManager(new SecurityManager());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addLog4JSocketAppender(String address, int port) {
		System.out.println("Add socket appender for " + address + ":" + port + " ...");

		// add appender for logger of the agent node
		SocketAppender appender = new SocketAppender(address, port);
		appender.setName(address+":"+port);
		((Log4JLogger)log).getLogger().addAppender(appender);

		System.out.println("Socket appender added.");
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeLog4JSocketAppender(String address, int port) {
		System.out.println("Remove socket appender for " + address + ":" + port + " ...");

		// remove appender from logger of the agent node
		((Log4JLogger)log).getLogger().removeAppender(address+":"+port);

		System.out.println("Socket appender removed.");
	}

	/**
	 * Registers the agent node and all its resources for management
	 * 
	 * @param manager
	 */
	public void enableManagement(Manager manager) {
		// do nothing if management already enabled
		if (isManagementEnabled()) {
			return;
		}

		// register agent node for management
		try {
			manager.registerAgentNode(this);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("WARNING: Unable to register agent node " + this.getName() + " as JMX resource.");
			System.err.println(e.getMessage());
		}

		// register agent node beans for management
		if (_agentNodeBeans != null) {
			for (IAgentNodeBean anb : this._agentNodeBeans) {
				anb.enableManagement(manager);
			}
		}

		// register agents for management
		if (_agents != null) {
			for (IAgent a : this._agents) {
				a.enableManagement(manager);
			}
		}

		// enable remote management
		if (_jmxConnectors != null) {
			manager.enableRemoteManagement(_uuid, _name, _jmxConnectors);
		}

		_manager = manager;
	}

	/**
	 * Deregisters the agent node and all its resources from management
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}

		// disable remote management
		_manager.disableRemoteManagement(_name);

		// deregister agents from management
		for (IAgent a : this._agents) {
			a.disableManagement();
		}

		// deregister agent node beans from management
		for (IAgentNodeBean anb : this._agentNodeBeans) {
			anb.disableManagement();
		}

		// deregister agent node from management
		try {
			_manager.unregisterAgentNode(this);
		} catch (Exception e) {
			System.err.println("WARNING: Unable to deregister agent node " + this.getName() + " as JMX resource.");
			System.err.println(e.getMessage());
		}

		_manager = null;
	}

	/**
	 * Checks wether the management of this object is enabled or not.
	 * 
	 * @return true if the management is enabled, otherwise false
	 */
	public boolean isManagementEnabled() {
		return _manager != null;
	}

}
