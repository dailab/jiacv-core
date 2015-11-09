package de.dailab.jiactng.agentcore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.timer.Timer;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.net.SocketAppender;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.Log4jConfigurer;

import de.dailab.jiactng.Version;
import de.dailab.jiactng.agentcore.comm.broker.ActiveMQBroker;
import de.dailab.jiactng.agentcore.comm.broker.ActiveMQTransportConnector;
import de.dailab.jiactng.agentcore.conf.GenericAgentProperties;
import de.dailab.jiactng.agentcore.directory.IDirectory;
import de.dailab.jiactng.agentcore.group.IAgentGroup;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.management.jmx.JmxConnector;
import de.dailab.jiactng.agentcore.management.jmx.JmxManager;
import de.dailab.jiactng.agentcore.ontology.AgentGroupDescription;
import de.dailab.jiactng.agentcore.util.IdFactory;
import de.dailab.jiactng.agentcore.util.jar.JARClassLoader;
import de.dailab.jiactng.agentcore.util.jar.JARMemory;

/**
 * Simple agent node implementation.
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public class SimpleAgentNode extends AbstractLifecycle implements IAgentNode, InitializingBean, BeanNameAware,
																				SimpleAgentNodeMBean {

	private static final String SSL_KEYSTORE_TYPE_JKS = "JKS";

	/** SSL identifier */
	public static final String SSL_USAGE_IDENTIFIER = "de.dailab.jiactng.agentcore.sslInUse";
	public static final String SSL_LIMITED_CIPHER_SUITES = "de.dailab.jiactng.agentcore.limitedCipherSuites";

	/** The threadPool object */
	private ExecutorService threadPool = null;

	/**
	 * A customized logging configuration will be used instead of the default configuration.
	 */
	private String loggingConfig;

	/** The unique ID of this agent node */
	protected String uuid = null;

	/** The name of the agent node. */
	protected String name = null;

	/** The name of the agent node owner (per default, the system user) */
	private String owner = null;

	/** The list of agent node beans. */
	private final ArrayList<IAgentNodeBean> agentNodeBeans;

	/** The list of agents. */
	private final ArrayList<IAgent> agents;

	/** The list of all agent groups. */
	private final ArrayList<IAgentGroup> groups;

	/** Storage for the agentFutures. Used to stop/cancel agent threads. */
	private HashMap<String, Future<?>> agentFutures = null;

	/** Configuration of a set of JMX connector server. */
	private Set<JmxConnector> jmxConnectors = null;

	/** Optional: IDirectory that manages white and yellow pages. */
	private IDirectory directory = null;

	/** A timer for being informed about dates. */
	private Timer timer = null;

	/** For SSL use: path to keystore (if desired for use) */
	private String keyStore = null;

	/** For SSL use: keystore password (if desired for use) */
	private String keyStorePassword = null;

	/** For SSL use: keystore type (standard type: "JKS") (if desired for use) */
	private String keyStoreType = "JKS";

	/** For SSL use: path to truststore (if desired for use) */
	private String trustStore = null;

	/** For SSL use: truststore password (if desired for use) */
	private String trustStorePassword = null;

	/** For SSL use: truststore type (standard type: "JKS") (if desired for use) */
	private String trustStoreType = "JKS";

	/** For SSL use: use ssl (standard: false) */
	private boolean sslInUse = false;

	/** For SSL use: comma delimeted list of ciphers to use (standard: null = all available) */
	private String cipherSuitesToUse = null;

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
	 * Property for overwriting the discoveryURI this is a convenience property that overwrites the standard discoveryURI
	 * in the TransportConnectors of the message broker
	 */
	private String overwriteDiscoveryURI = null;

	/**
	 * Main method for starting JIAC-TNG. Loads a spring-configuration file denoted by the first argument and uses a
	 * ClassPathXmlApplicationContext to instantiate its contents
	 * 
	 * @param args the first argument is interpreted as a classpath-relative name of a spring configurations file. 
	 *             The second argument, if present, is interpreted as the name of the log4j.properties file to use.
	 * @see org.springframework.context.support.ClassPathXmlApplicationContext
	 */
	public static void main(String... args) {
		if (args == null || args.length == 0) {
			throw new IllegalArgumentException("String[] args must hold the node configuration file "
					+ "and (optionally) the log4j properties file.");
		}
		if (args.length > 1) {
			System.setProperty("log4j.configuration", args[1]);
		} else {
			System.setProperty("log4j.configuration", "jiactng_log4j.properties");
		}
		System.setProperty("spring.rootconfigfile", args[0]);
		new ClassPathXmlApplicationContext(args[0]);
	}

	/**
	 * -------------Jetzt geht's lo'hos -----------------------------------------
	 */

	/** Constructor. Creates the UUID for the agent node. */
	public SimpleAgentNode() {
		uuid = IdFactory.createAgentNodeId(this.hashCode());
		setLog(LogFactory.getLog(uuid));
		agentNodeBeans = new ArrayList<IAgentNodeBean>();
		agents = new ArrayList<IAgent>();
		groups = new ArrayList<IAgentGroup>();
		owner = System.getProperty("user.name");

		// start timer
		timer = new Timer();
		timer.start();

		// make sure JIAC version appears on console/logging
		log.warn("JIAC version: " + getJiacVersion() + " (" + getJiacVendor() + ")");
		System.err.println("JIAC version: " + getJiacVersion() + " (" + getJiacVendor() + ")");
	}

	/**
	 * Configuration of a set of JMX connector server used for remote management.
	 * 
	 * @param newJmxConnectors the set of connectors.
	 */
	public final void setJmxConnectors(Set<JmxConnector> newJmxConnectors) {
		jmxConnectors = newJmxConnectors;
	}

	/**
	 * Gets the configuration of the JMX connector servers used for remote management.
	 * 
	 * @return the set of connectors.
	 */
	public final Set<JmxConnector> getJmxConnectors() {
		return jmxConnectors;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setAgents(List<IAgent> newAgents) {
		if (newAgents == null) {
			throw new IllegalArgumentException("Cannot set agentlist to null! Use remove if you want to delete the list.");
		}

		for (IAgent agent : newAgents) {
			if (agent == null) {
				log.warn("Found null-entry in agentlist for setAgents. This entry is ignored.");
				continue;
			}

			if (directory != null) {
				agent.addLifecycleListener(directory);
			}
		}

		synchronized (agents) {
			if (agents.size() > 0) {
				// If the agent list is not empty, overwriting it may lead to agent objects that are no longer known to the
				// node.
				// This should not happen.
				log.error("The old list of agents is not empty! Overwriting this list is probably not a good idea!");
				agents.clear();
			}

			// refresh agent list
			agents.addAll(newAgents);
		}
	}

	/**
	 * Sets a list of groups.
	 * 
	 * @param groups the list of groups to set
	 */
	public void setGroups(List<IAgentGroup> groups) {
		this.groups.addAll(groups);
	}

	/**
	 * adds multiple unique agents from a single GenericAgentProperties description to this AgentNode at once<br/>
	 * this method is called by Spring when using the genericAgents property, see the JUnit test
	 * GenericAgentPropertiesInjection for an example
	 * 
	 * @param agentProps a List of GenericAgentProperties which contain the number of Agents to be added, the Agent
	 *          configuration, the AgentBeanName and a pattern from which the Agent name will be generated TODO maybe
	 *          simplify this by adding some sort of cloning mechanism the the Agent class using reflection. having this,
	 *          any agent could create a new instance of himself.
	 */
	public final void setGenericAgents(List<GenericAgentProperties> agentProps) {
		// long start = System.currentTimeMillis();
		if (agentProps != null && agentProps.size() > 0) {
			final ArrayList<IAgent> genericAgents = new ArrayList<IAgent>();
			for (GenericAgentProperties gap : agentProps) {
				final ClassPathXmlApplicationContext cpxac = new ClassPathXmlApplicationContext(gap.getAgentConfig());
				if (cpxac != null && cpxac.containsBeanDefinition(gap.getAgentBeanName())) {
					for (int i = 1; i <= gap.getCount(); i++) {
						final IAgent newAgent = (IAgent) cpxac.getBean(gap.getAgentBeanName());
						newAgent.setAgentName(gap.createAgentName(i));
						// log does not work in this context somehow: using system instead
						// System.out.println("adding agent: " + newAgent.getAgentName() + " with ID: " + newAgent.getAgentId());
						genericAgents.add(newAgent);
					}
				} else {
					System.out.println(new StringBuffer("could not create xmlapplicationcontext from ")
																									.append(gap.getAgentConfig())
																									.append(" or agentbean definition does not exist, beanname is: ")
																									.append(gap.getAgentBeanName()));
				}
			}
			agents.addAll(genericAgents);
		} else {
			System.out.println("nothing to do - GenericAgentProperties list is null or empty");
		}
		// System.out.println("creation took " + (System.currentTimeMillis() - start) + " milliseconds");
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addAgent(IAgent agent) {
		if (agent == null) {
			throw new IllegalArgumentException("Argument for addAgent must not be null!");
		}

		agent.setAgentNode(this);

		// TODO: state changes?
		final List<String> oldAgentList = getAgents();
		synchronized (agents) {
			agents.add(agent);
		}
		agent.addLifecycleListener(lifecycle.createLifecycleListener());

		if (directory != null) {
			agent.addLifecycleListener(directory);
			((Agent) agent).setDirectory(directory);
		}

		// register agent for management
		agent.enableManagement(_manager);

		agentListChanged(oldAgentList, getAgents());
	}

	/**
	 * {@inheritDoc}
	 */

	public final void removeAgent(IAgent agent) {
		// TODO: state changes?

		if (agent == null) {
			throw new IllegalArgumentException("Argument for removeAgent must not be null!");
		}

		synchronized (agents) {
			if (!agents.contains(agent)) {
				log.error("Agent could not be found on this agentnode and therefore cannot be removed!");
				return; // TODO: exception?
			}
		}

		if (!LifecycleStates.CLEANED_UP.equals(agent.getState())) {
			log.warn("Removing agent "
																							+ agent.getAgentName()
																							+ "("
																							+ agent.getAgentId()
																							+ ") while it is not yet cleaned up. Make sure you try to change the state first.");
		}

		// unregister agent from management
		agent.disableManagement();

		// remove agent
		final List<String> oldAgentList = getAgents();
		synchronized (agents) {
			agents.remove(agent);
		}
		agentListChanged(oldAgentList, getAgents());
	}

	/**
	 * Uses JMX to send notifications that the attribute "Agents" of the managed agent node has been changed (e.g. added
	 * or removed agent).
	 * 
	 * @param oldAgentList the old list of agent names
	 * @param newAgentList the new list of agent names
	 */
	private void agentListChanged(List<String> oldAgentList, List<String> newAgentList) {
		final Notification n = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
																						"Agents changed", "Agents", "java.util.ArrayList<java.lang.String>",
																						oldAgentList, newAgentList);
		sendNotification(n);
	}

	/**
	 * {@inheritDoc}
	 */

	public final List<IAgent> findAgents() {
		synchronized (agents) {
			return Collections.unmodifiableList(agents);
		}
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

	public final String getUUID() {
		return uuid;
	}

	/**
	 * {@inheritDoc}
	 */

	public final String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */

	public final void setName(String newName) {
		name = newName;
	}

	/**
	 * Returns the name of localhost.
	 * 
	 * @return name of localhost
	 * @throws UnknownHostException if no IP address for the local host could be found.
	 * @see InetAddress#toString()
	 */
	public final String getHost() throws UnknownHostException {
		return InetAddress.getLocalHost().getCanonicalHostName();
	}

	/**
	 * Returns the agent node owner. The default owner name is the system's user account.
	 * 
	 * @return owner name
	 */
	public String getOwner() {
		return this.owner;
	}

	/**
	 * Sets the agent node's owner name.
	 * 
	 * @param owner owner name
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * Gets the name of the agent platform, which is defined by address and group of the discovery URI. Only agent nodes
	 * which belongs to the same agent platform are able to communicate.
	 * 
	 * @return the platform name or <code>null</code> if no network connector exist or the discovery URI of the network
	 *         connector does not belong to the protocol "smartmulticast".
	 */
	public String getPlatformName() {
		// TODO: consider multiple brokers with different discovery URIs
		final String discoveryprefix = "smartmulticast://";
		final String networkprefix = "static:(";
		final String failoverprefix = "failover:(";
		for (IAgentNodeBean agentNodeBean : this.getAgentNodeBeans()) {
			if (agentNodeBean instanceof ActiveMQBroker) {
				Set<ActiveMQTransportConnector> connectors = ((ActiveMQBroker) agentNodeBean).getConnectors();
				for (ActiveMQTransportConnector conn : connectors) {
					final String discoveryUri = conn.getDiscoveryURI();
					if ((discoveryUri != null) && discoveryUri.startsWith(discoveryprefix)) {
						return discoveryUri.substring(discoveryprefix.length());
					}
					final String networkUri = conn.getNetworkURI();
					if ((networkUri != null) && networkUri.startsWith(networkprefix)) {
						String uri = networkUri.substring(networkprefix.length(), networkUri.length() - 1);
						return (uri.startsWith(failoverprefix)) ? uri.substring(failoverprefix.length(), uri.length() - 1) : uri;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the unique identifiers of agents which reside on this agent node.
	 * 
	 * @return list of unique agent identifiers
	 */
	public final List<String> getAgents() {
		final ArrayList<String> result = new ArrayList<String>();
		synchronized (agents) {
			for (IAgent a : agents) {
				result.add(a.getAgentId());
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<String> addAgents(byte[] configuration, List<JARMemory> libraries, String owner) throws Exception {
		// create class loader for the new agents
		final JARClassLoader cl = new JARClassLoader();
		for (JARMemory jar : libraries) {
			cl.addJAR(jar);
		}

		// create spring application context
		final GenericApplicationContext appContext = new GenericApplicationContext();
		appContext.setClassLoader(cl);
		final XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appContext);
		xmlReader.loadBeanDefinitions(new ByteArrayResource(configuration));
		appContext.refresh();

		// add and start all created agents
		final List<String> agentIds = new ArrayList<String>();
		final Collection<?> newAgents = appContext.getBeansOfType(IAgent.class).values();
		for (Object a : newAgents) {
			final IAgent agent = (IAgent) a;
			agent.setOwner(owner);
			agent.setSpringConfigXml(configuration);
			agent.setClassLoader(cl);

			addAgent(agent);
			agentIds.add(agent.getAgentId());
			/* try { agent.init(); agent.start(); } catch (LifecycleException e) { // TODO: e.printStackTrace(); } */
		}

		return agentIds;
	}

	/**
	 * {@inheritDoc}
	 */

	public void setBeanName(String newName) {
		setName(newName);
	}

	/**
	 * Handles the change of life-cycle states of agents on this agent node.
	 * 
	 * @param evt the life-cycle event
	 */
	public void onEvent(LifecycleEvent evt) {
		final Object source = evt.getSource();
		synchronized (agents) {
			if (agents.contains(source)) {
				final IAgent agent = (IAgent) source;
				switch (evt.getState()) {
					case STARTED:
						final Future<?> f1 = threadPool.submit(agent);
						agentFutures.put(agent.getAgentName(), f1);
						break;
					case STOPPED:
						final Future<?> f2 = agentFutures.get(agent.getAgentName());
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
					default:
						// do nothing
				}
			}
		}
	}

	/**
	 * Initialization-method. This method is called by Spring after startup (through the InitializingBean-Interface) and
	 * is used to start the agent node after all beans haven been instantiated by Spring. Currently only creates the JMX
	 * connector servers, registers the agent node as JMX resource and calls the init() and start()-methods from
	 * ILifefycle for this.
	 * 
	 * @throws Exception if the agent node can not be initialized and started.
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		log.warn("Agentnode is: " + this.getName() + " (" + this.getUUID() + ") with owner: " + this.getOwner());

		System.setProperty(SSL_USAGE_IDENTIFIER, "" + sslInUse);
		if (cipherSuitesToUse != null) {
			cipherSuitesToUse = cipherSuitesToUse.replace(" ", "");
			System.setProperty(SSL_LIMITED_CIPHER_SUITES, "" + cipherSuitesToUse);
		}
		if (sslInUse) {
			setSslContext();
		}

		// create shutdown hook for graceful termination
		Runtime.getRuntime().addShutdownHook(shutdownhook);
		// set references for all agents
		addLifecycleListener(this);
		synchronized (agents) {
			// owner of agent node is also owner of the initially created agents
			final String owner = getOwner();

			for (IAgentGroup group : groups) {
				System.out.println("group: " + group.getName() + " members.size: " + group.getMembers().size());
				this.agents.addAll(group.getMembers());
				System.out.println("agents.size: " + agents.size());
			}

			for (IAgent a : agents) {
				if (a.getOwner() == null) {
					a.setOwner(owner);
				}
				a.setAgentNode(this);
				a.addLifecycleListener(this.lifecycle.createLifecycleListener());
			}
		}

		synchronized (agentNodeBeans) {
			for (IAgentNodeBean nodeBean : agentNodeBeans) {
				nodeBean.addLifecycleListener(this.lifecycle.createLifecycleListener());
			}
		}

		// enable management of agent node and all its resources
		enableManagement(new JmxManager());

		// start agent node
		init();
		start();
	}

	private void setSslContext() throws Exception {

		log.info("Initializing ssl context (V0.2)");

		boolean acceptJdkKeystores = false;

		// Keystore
		File ks = null;
		KeyManagerFactory kmf = null;
		if (keyStore != null && keyStorePassword != null) {
			System.setProperty("javax.net.ssl.keyStore", keyStore);
			System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
			System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
			ks = new File(keyStore);
			if (!ks.exists() && !ks.canRead()) {
				// log.warn("Could not find keystore file! Using system environment variables only.");
				throw new IOException("Could not load desired keystore file (" + ks.getAbsolutePath() + ")!");
			}
			// ks file readable
			InputStream inKs = null;
			try {
				inKs = new FileInputStream(ks);
				KeyStore keystore = KeyStore.getInstance(keyStoreType);
				keystore.load(inKs, keyStorePassword.toCharArray());
				kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				kmf.init(keystore, keyStorePassword.toCharArray());
				log.info("Keystore initiated (" + ks.getAbsolutePath() + ")");
			} catch (Exception e) {
				log.error("Could not read keystore. Please check exception message: " + e.getLocalizedMessage(), e);
				closeStream(inKs);
				throw e;
			}
			closeStream(inKs);
		} else {
			// keystore settings not set. use empty instead.
			if (!acceptJdkKeystores) {
				throw new IOException("Either path to keystore or keystore password not set!");
			}
			kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(null, null);
			log.info("Empty keystore initiated");
		}

		// Truststore
		File ts = null;
		TrustManagerFactory tmf = null;
		if (trustStore != null && trustStorePassword != null) {
			System.setProperty("javax.net.ssl.trustStore", trustStore);
			System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
			System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
			ts = new File(trustStore);
			if (!ts.exists() && !ts.canRead()) {
				// log.warn("Could not find truststore file! Trying to set system environment variables.");
				throw new IOException("Could not load desired truststore file (" + ts.getAbsolutePath() + ")!");
			}
			InputStream inTs = null;
			try {
				inTs = new FileInputStream(ts);
				KeyStore truststore = KeyStore.getInstance(trustStoreType);
				truststore.load(inTs, trustStorePassword.toCharArray());
				tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(truststore);
				log.info("Truststore initiated (" + ts.getAbsolutePath() + ")");
			} catch (Exception e) {
				log.error("Could not read truststore. Please check exception message: " + e.getLocalizedMessage(), e);
				closeStream(inTs);
				throw e;
			}
			closeStream(inTs);
		} else {
			// truststore settings not set. use standard java truststore instead.
			if (!acceptJdkKeystores) {
				throw new IOException("Either path to truststore or truststore password not set!");
			}
			tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore keyStore = null;
			tmf.init(keyStore);
			log.info("Standard truststore initiated");
		}

		SSLContext sslContext = SSLContext.getInstance("TLS");
		// initiate with keystore and truststore items
		sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		SSLContext.setDefault(sslContext);

		log.info("Set ssl context");

	}

	private void closeStream(InputStream in) {
		if (in != null) {
			try {
				in.close();
			} catch (Exception e) {}
		}
	}

	/**
	 * Shuts down the managed agent node and all its agents (incl. unregister as JMX resource) before stopping all JMX
	 * connector servers.
	 * 
	 * @throws LifecycleException if an error occurs during stop and cleanup of this agent node.
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
		synchronized (agents) {
			while (!agents.isEmpty()) {
				removeAgent(agents.get(agents.size() - 1));
			}
		}

		// disable management of agent node and all its resources
		disableManagement();

		synchronized (agentNodeBeans) {
			// TODO: anything left to do for the nodeBeans?
			agentNodeBeans.clear();
		}

		// stop timer
		if (timer != null) {
			timer.stop();
			timer = null;
		}

		log.info("AgentNode " + getName() + " has been closed.");
	}

	/**
	 * {@inheritDoc}
	 */

	public void doInit() {
		threadPool = Executors.newCachedThreadPool();
		agentFutures = new HashMap<String, Future<?>>();

		// initialize all beans of the agent nodes
		// TODO testing
		synchronized (agentNodeBeans) {
			HashSet<Class<?>> anbClassSet = new HashSet<Class<?>>();

			for (IAgentNodeBean anb : agentNodeBeans) {
				if (anbClassSet.contains(anb.getClass())) {
					log.warn("Agentnode already contains bean of class " + anb.getClass() + " - skipping!");
					continue;
				} else {
					anbClassSet.add(anb.getClass());
				}

				// initialize node bean
				try {
					log.info("Trying to initialize agentnodebean: " + anb.getBeanName());
					anb.init();
				} catch (LifecycleException lce) {
					// TODO
					log.error("Failure when initializing agentnodebean: " + anb.getBeanName(), lce);
				}

				// check for directory
				if ((directory == null) && (anb instanceof IDirectory)) {
					directory = (IDirectory) anb;
				}
			}
		}

		// initialize and set references for all agents if any
		synchronized (agents) {
			for (IAgent a : agents) {

				if (directory != null) {
					a.addLifecycleListener(directory);
					if (a instanceof Agent) {
						((Agent) a).setDirectory(directory);
					}
				}

				log.info("Initializing agent: " + a.getAgentName());
				try {
					log.info("Trying to initialize agent: " + a.getAgentName());
					a.init();
				} catch (LifecycleException e) {
					// TODO:
					log.error("Failure when initializing agent: " + a.getAgentName(), e);
				}
			}

			for (IAgentGroup group : groups) {
				for (IAgent agent : group.getMembers()) {
					AgentGroupDescription groupdescr = new AgentGroupDescription();
					groupdescr.setName(group.getName());
					((Agent) agent).getMemory().write(groupdescr);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */

	public void doStart() {
		// call start on all beans of the agent node
		// TODO testing
		synchronized (agentNodeBeans) {
			for (IAgentNodeBean anb : agentNodeBeans) {
				try {
					log.info("Trying to start agentnodebean: " + anb.getBeanName());
					anb.start();
				} catch (LifecycleException lce) {
					// TODO
					lce.printStackTrace();
				}
			}
		}

		synchronized (agents) {
			// call start() and instantiate Threads for all agents if any
			for (IAgent a : agents) {
				try {
					if (((Agent) a).getStartTime() == null) {
						log.info("Trying to start agent: " + a.getAgentName());
						a.start();
					} else {
						log.info("Agent has a startTime and is not started now: " + a.getAgentName());
					}
				} catch (Exception ex) {
					// TODO
					ex.printStackTrace();
				}
			}
			log.info("AgentNode " + getName() + " started with " + agents.size() + " agents");
		}

	}

	/**
	 * {@inheritDoc}
	 */

	public void doStop() {
		// call stop() for all agents if any
		synchronized (agents) {
			for (IAgent a : agents) {
				try {
					log.info("Trying to stop agent: " + a.getAgentName());
					a.stop();
				} catch (LifecycleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// call stop on all beans of the agent node in reverse order
		// TODO testing
		synchronized (agentNodeBeans) {
			for (int i = agentNodeBeans.size() - 1; i >= 0; i--) {
				final IAgentNodeBean anb = agentNodeBeans.get(i);
				try {
					log.info("Trying to stop agentnodebean: " + anb.getBeanName());
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
		synchronized (agents) {
			for (IAgent a : agents) {
				try {
					log.info("Trying to cleanup agent: " + a.getAgentName());
					a.cleanup();
				} catch (LifecycleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		// call cleanup on all beans of the agent node in reverse order
		// TODO testing
		synchronized (agentNodeBeans) {
			for (int i = agentNodeBeans.size() - 1; i >= 0; i--) {
				final IAgentNodeBean anb = agentNodeBeans.get(i);
				try {
					log.info("Trying to cleanup agentnodebean: " + anb.getBeanName());
					anb.cleanup();
				} catch (LifecycleException lce) {
					// TODO
					lce.printStackTrace();
				}
			}
		}

		if (threadPool != null) {
			threadPool.shutdown();
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

	public final ExecutorService getThreadPool() {
		return threadPool;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<IAgentNodeBean> getAgentNodeBeans() {
		synchronized (agentNodeBeans) {
			return Collections.unmodifiableList(agentNodeBeans);
		}
	}

	/**
	 * Getter for attribute "AgentNodeBeanClasses" of the managed agent node.
	 * 
	 * @return the class of agent beans running in this agent node
	 */
	public final List<String> getAgentNodeBeanClasses() {
		final ArrayList<String> ret = new ArrayList<String>();
		for (ILifecycle bean : getAgentNodeBeans()) {
			ret.add(bean.getClass().getName());
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setAgentNodeBeans(List<IAgentNodeBean> agentnodebeans) {
		this.agentNodeBeans.clear();

		for (IAgentNodeBean anb : agentnodebeans) {
			this.agentNodeBeans.add(anb);
			anb.setAgentNode(this);
		}
	}

	/**
	 * @return the loggingConfig
	 */
	public final String getLoggingConfig() {
		return loggingConfig;
	}

	/**
	 * @param newLoggingConfig the loggingConfig to set
	 */
	public final void setLoggingConfig(String newLoggingConfig) {
		loggingConfig = newLoggingConfig;
		// overwriting logging config from main method
		System.setProperty("log4j.configuration", newLoggingConfig);
		try {
			Log4jConfigurer.initLogging(newLoggingConfig);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}

	/**
	 * Activates the java security policy defined in the given policy file by setting the system property
	 * <code>java.security.policy</code> and activating the default security manager.
	 * 
	 * @param filename the name of the policy file
	 * @see System#setSecurityManager(SecurityManager)
	 */
	public void setAuthorizationPolicyFilename(String filename) {
		System.setProperty("java.security.policy", filename);
		System.setSecurityManager(new SecurityManager());
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addLog4JSocketAppender(String address, int port) {
		System.out.println("Add socket appender for " + address + ":" + port + " ...");

		// add appender for logger of the agent node
		final SocketAppender appender = new SocketAppender(address, port);
		appender.setName(address + ":" + port);
		((Log4JLogger) log).getLogger().addAppender(appender);

		System.out.println("Socket appender added.");
	}

	/**
	 * {@inheritDoc}
	 */
	public final InetAddress addLog4JSocketAppender(Set<InetAddress> addresses, int port) {
		for (InetAddress address : addresses) {
			try {
				if (address.isReachable(3000)) {
					addLog4JSocketAppender(address.getHostAddress(), port);
					return address;
				}
			} catch (IOException e) {}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeLog4JSocketAppender(String address, int port) {
		System.out.println("Remove socket appender for " + address + ":" + port + " ...");

		// remove appender from logger of the agent node
		((Log4JLogger) log).getLogger().removeAppender(address + ":" + port);

		System.out.println("Socket appender removed.");
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getDirectoryName() {
		if (directory instanceof AbstractAgentNodeBean) {
			return ((AbstractAgentNodeBean) directory).getBeanName();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Set<JMXServiceURL> getJmxURLs() {
		if (_manager == null) {
			return null;
		}

		Set<JMXServiceURL> jmxURLs = new HashSet<JMXServiceURL>();
		try {
			ObjectName query = ((JmxManager) _manager).getMgmtNameOfAgentNodeResource(uuid,
																							JmxManager.CATEGORY_JMX_CONNECTOR_SERVER, "*");
			for (ObjectName o : ManagementFactory.getPlatformMBeanServer().queryNames(query, null)) {
				String url = o.getKeyProperty("resource");
				url = url.substring(1, url.length() - 1);
				try {
					jmxURLs.add(new JMXServiceURL(url));
				} catch (MalformedURLException mue) {
					mue.printStackTrace();
				}
			}
			return jmxURLs;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Registers the agent node and all its resources for management
	 * 
	 * @param manager the manager to be used for enabling management of this agent node.
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
			System.err.println("WARNING: Unable to register agent node " + getName() + " as JMX resource.");
			System.err.println(e.getMessage());
		}

		// register agent node timer for management
		try {
			manager.registerAgentNodeResource(this, "Timer", timer);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("WARNING: Unable to register timer of agent node " + getName() + " as JMX resource.");
			System.err.println(e.getMessage());
		}

		// register agent node beans for management
		if (agentNodeBeans != null) {
			for (IAgentNodeBean anb : agentNodeBeans) {
				anb.enableManagement(manager);
			}
		}

		// register agents for management
		synchronized (agents) {
			for (IAgent a : agents) {
				a.enableManagement(manager);
			}
		}

		// enable remote management
		if ((jmxConnectors != null) && (manager instanceof JmxManager)) {
			((JmxManager) manager).enableRemoteManagement(this);
		}

		super.enableManagement(manager);
	}

	/**
	 * Unregisters the agent node and all its resources from management
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}

		// disable remote management
		_manager.disableRemoteManagement(this);

		// unregister agents from management
		synchronized (agents) {
			for (IAgent a : agents) {
				a.disableManagement();
			}
		}

		// unregister agent node beans from management
		for (IAgentNodeBean anb : agentNodeBeans) {
			anb.disableManagement();
		}

		// unregister agent node timer from management
		try {
			_manager.unregisterAgentNodeResource(this, "Timer");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("WARNING: Unable to deregister timer of agent node " + getName() + " as JMX resource.");
			System.err.println(e.getMessage());
		}

		// unregister agent node from management
		try {
			_manager.unregisterAgentNode(this);
		} catch (Exception e) {
			System.err.println("WARNING: Unable to deregister agent node " + getName() + " as JMX resource.");
			System.err.println(e.getMessage());
		}

		super.disableManagement();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOverwriteDiscoveryURI() {
		return overwriteDiscoveryURI;
	}

	/**
	 * Sets the discovery URI to be used by all brokers of the agent node. Changes after initialization of the agent node
	 * will be ignored.
	 * 
	 * @param newOverwriteDiscoveryURI the discovery URI
	 */
	public void setOverwriteDiscoveryURI(String newOverwriteDiscoveryURI) {
		overwriteDiscoveryURI = newOverwriteDiscoveryURI;
	}

	/**
	 * @return the keyStore
	 */
	public String getKeyStore() {
		return keyStore;
	}

	/**
	 * @param keyStore the keyStore to set
	 */
	public void setKeyStore(String keyStore) {
		this.keyStore = keyStore;
	}

	/**
	 * @return the keyStorePassword
	 */
	public String getKeyStorePassword() {
		return keyStorePassword;
	}

	/**
	 * @param keyStorePassword the keyStorePassword to set
	 */
	public void setKeyStorePassword(String keyStorePassword) {
		this.keyStorePassword = keyStorePassword;
	}

	/**
	 * @return the keyStoreType
	 */
	public String getKeyStoreType() {
		return keyStoreType;
	}

	/**
	 * @param keyStoreType the keyStoreType to set
	 */
	public void setKeyStoreType(String keyStoreType) {
		this.keyStoreType = keyStoreType;
	}

	/**
	 * @return the trustStore
	 */
	public String getTrustStore() {
		return trustStore;
	}

	/**
	 * @param trustStore the trustStore to set
	 */
	public void setTrustStore(String trustStore) {
		this.trustStore = trustStore;
	}

	/**
	 * @return the trustStorePassword
	 */
	public String getTrustStorePassword() {
		return trustStorePassword;
	}

	/**
	 * @param trustStorePassword the trustStorePassword to set
	 */
	public void setTrustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
	}

	/**
	 * @return the trustStoreType
	 */
	public String getTrustStoreType() {
		return trustStoreType;
	}

	/**
	 * @param trustStoreType the trustStoreType to set
	 */
	public void setTrustStoreType(String trustStoreType) {
		this.trustStoreType = trustStoreType;
	}

	/**
	 * @return the sslInUse
	 */
	public boolean isSslInUse() {
		return sslInUse;
	}

	/**
	 * @param sslInUse the sslInUse to set
	 */
	public void setSslInUse(boolean sslInUse) {
		this.sslInUse = sslInUse;
	}

	/**
	 * @return the cipherSuitesToUse
	 */
	public String getCipherSuitesToUse() {
		return cipherSuitesToUse;
	}

	/**
	 * @param cipherSuitesToUse the cipherSuitesToUse to set
	 */
	public void setCipherSuitesToUse(String cipherSuitesToUse) {
		this.cipherSuitesToUse = cipherSuitesToUse;
	}

	/**
	 * {@inheritDoc}
	 */
	public final <T> T findAgentNodeBean(Class<T> type) {
		if (type == null) {
			throw new IllegalArgumentException("Cannot find AgentNodeBean for null-type");
		}
		IAgentNodeBean ret = null;
		synchronized (this.agentNodeBeans) {
			for (IAgentNodeBean ianb : this.agentNodeBeans) {
				if (type.isInstance(ianb)) {
					ret = ianb;
					break;
				}
			}
		}

		return type.cast(ret);
	}

	/**
	 * {@inheritDoc}
	 */
	public void loadClass(String className) throws ClassNotFoundException {
		getClass().getClassLoader().loadClass(className);
	}

}
