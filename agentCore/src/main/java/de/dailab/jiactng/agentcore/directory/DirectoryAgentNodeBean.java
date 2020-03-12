package de.dailab.jiactng.agentcore.directory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.ontology.AgentNodeDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;
import de.dailab.jiactng.agentcore.ontology.IServiceDescription;

/**
 * This agent node bean is used to enable search for agents or actions of all
 * agent nodes of the platform. It also allows to get the JMX URLs of remote
 * agent nodes.
 * 
 * This agent node bean uses the default address "df@dfgroup" or corresponding 
 * addresses of the defined groups to send and receive alive messages to/from 
 * all other agent nodes of these groups at the given alive interval for the 
 * specific group. At advertise interval an advertise message will be send to 
 * each group with information about local agents, actions and JMX URLs. 
 * An amendment with information about the started or stopped agent will be 
 * send immediately after the list of locally started agents has been changed. 
 * If the bean is stopped, a bye message will be send.
 * 
 * @author Jan Keiser
 * @see de.dailab.jiactng.agentcore.Agent#searchAllActions(IActionDescription)
 * @see de.dailab.jiactng.agentcore.Agent#searchAllAgents(IAgentDescription)
 * @see de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient#getURLsFromMulticast()
 * @see de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient#getURLsFromRegistry(String,
 *      int)
 * @see Advertisement
 */
public class DirectoryAgentNodeBean extends AbstractAgentNodeBean implements
		IDirectory, IMessageTransportDelegate, DirectoryAgentNodeBeanMBean {

	private long lastSend;

	private boolean dump = false;

	/**
	 * Name for address-creation purposes. Will be added to the UUID of
	 * AgentNode to create the message box for the directory. Example:
	 * "df@agentnode"
	 */
	public static final String ADDRESS_NAME = "df";

	/** Protocol for saying hello and still there to other agent nodes. */
	public static final String ALIVE = ADDRESS_NAME + ":alive";

	/** Protocol to stop being present. */
	public static final String BYE = ADDRESS_NAME + ":bye";

	/** Protocol to announce my actions and agents. */
	public static final String ADVERTISE = ADDRESS_NAME + ":advertise";

	/** Protocol to amend my actions and agents. */
	public static final String AMEND = ADDRESS_NAME + ":amend";

	/** Protocol to request actions and agents of other agent nodes. */
	public static final String ALL = ADDRESS_NAME + ":all";

	/**
	 * Interval for group address to send alive message to group if only one
	 * group address exists. In milliseconds. Default is 15000.
	 */
	private long aliveInterval = 15000;

	/**
	 * Interval for group addresses to send alive message to group. In
	 * milliseconds. Default is 15000.
	 */
	private HashMap<String, Long> aliveIntervals = null;

	/** 
	 * Exceeding this delay for sending or receiving alive messages leads to 
	 * a warning log message. In milliseconds. Default is 10000.
	 */
	private long allowedAliveDelay = 10000;

	/** 
	 * Exceeding this delay for receiving alive messages leads to a removal of 
	 * the remote node from the directory. In milliseconds. Default is 20000.*/
	private long maxAliveDelay = 20000;

	/** reference to the service Matcher if it exists */
	private IServiceMatcher serviceMatcher = null;

	/** reference to the ontology storage if it exists */
	private IOntologyStorage ontologyStorage = null;

	/**
	 * Interval to send advertise message to group. In milliseconds. Should be
	 * greater than aliveInterval to avoid spamming around. Default is 3
	 * minutes.
	 */
	private long advertiseInterval = 180000;

	/**
	 * Interval for every group address to send advertise message to group. In
	 * milliseconds. Should be greater than aliveInterval to avoid spamming
	 * around. Default is 3 minutes.
	 */
	private HashMap<String, Long> advertiseIntervals = null;

	/** The UUID of this agent node. */
	private String myAgentNode;

	/** The inbox of this bean. */
	ICommunicationAddress myAddress;

	private List<String> groupAddressNames;

	/** The group address of all directories. */
	Set<ICommunicationAddress> groupAddresses;

	/** The communication handling for sending and receiving messages. */
	private MessageTransport messageTransport;

	// TODO JMX
	/** Stores (node-)local actions. */
	private Set<IActionDescription> localActions = new HashSet<IActionDescription>();

	// TODO JMX
	/**
	 * Stores remote actions. Key is the message box address of the node. Value
	 * is a set of action that are provided on that node.
	 */
	private Hashtable<String, Set<IActionDescription>> remoteActions = new Hashtable<String, Set<IActionDescription>>();

	// TODO JMX
	/** Stores (node-)local agents. Key is the agent identifier. */
	private Hashtable<String, IAgentDescription> localAgents = new Hashtable<String, IAgentDescription>();

	// TODO JMX
	/** Store remote agents. Key is the agent identifier. */
	private Hashtable<String, IAgentDescription> remoteAgents = new Hashtable<String, IAgentDescription>();

	// TODO JMX
	/** Stores all known agent nodes. Key is the message box address of the node. */
	private Hashtable<String, AgentNodeDescription> nodes = new Hashtable<String, AgentNodeDescription>();

	/** Stores the message box address of all missing nodes */
	private Set<String> missingNodes = new HashSet<String>();

	/** Timer that schedules alive pings and advertisements. */
	private Timer timer;

	// ######################################
	// AbstractLifecycle
	// ######################################

	@Override
	public void doInit() throws Exception {
		super.doInit();
		myAgentNode = this.agentNode.getUUID();

		messageTransport.setDefaultDelegate(this);
		try {
			messageTransport.doInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		myAddress = CommunicationAddressFactory
				.createMessageBoxAddress(ADDRESS_NAME + "@" + myAgentNode);
		log.info("myAddress=" + myAddress);
		groupAddresses = new HashSet<ICommunicationAddress>();

		if (aliveIntervals == null) {
			aliveIntervals = new HashMap<String, Long>();
		}
		if (advertiseIntervals == null) {
			advertiseIntervals = new HashMap<String, Long>();
		}

		if (groupAddressNames == null) {
			ICommunicationAddress groupAddress = CommunicationAddressFactory
					.createGroupAddress(ADDRESS_NAME + "@dfgroup");
			groupAddresses.add(groupAddress);
			aliveIntervals.put(groupAddress.toUnboundAddress().getName(),
					aliveInterval);
			advertiseIntervals.put(groupAddress.toUnboundAddress().getName(),
					advertiseInterval);
		} else {
			for (String name : groupAddressNames) {
				ICommunicationAddress groupAddress = CommunicationAddressFactory
						.createGroupAddress(ADDRESS_NAME + "@" + name);
				groupAddresses.add(groupAddress);
				log.info("groupAddress=" + groupAddress);
				if (aliveIntervals.get(name) == null) {
					aliveIntervals.put(groupAddress.toUnboundAddress().getName(),
							aliveInterval);
				}
				if (advertiseIntervals.get(name) == null) {
					advertiseIntervals.put(groupAddress.toUnboundAddress().getName(),
							advertiseInterval);
				}
			}
		}

		try {
			messageTransport.listen(myAddress, null);
			for (ICommunicationAddress groupAddress : groupAddresses) {
				messageTransport.listen(groupAddress, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// find serviceMatcherBean and ontologystorage
		this.serviceMatcher = this.agentNode
				.findAgentNodeBean(IServiceMatcher.class);
		this.ontologyStorage = this.agentNode
				.findAgentNodeBean(IOntologyStorage.class);

		if (this.serviceMatcher != null) {
			log.info("Found a ServiceMatcher for this agentnode");
		} else {
			log.info("No ServiceMatcher was found on this agentode, complex matching will not be possible!");
		}

		if (this.ontologyStorage != null) {
			log.info("Found an OntologyStorage for this agentnode");
		} else {
			log.info("No OntologyStorage was found on this agentode, ontology-handling will not be possible!");
		}
	}

	@Override
	public void doStart() throws Exception {
		super.doStart();
		lastSend = System.currentTimeMillis();

		// start timer for each DF-group to send ALIVE and ADVERTISE messages periodically
		timer = new Timer();
		for (ICommunicationAddress groupAddress : groupAddresses) {
			long aliveInterval = aliveIntervals.get(groupAddress
					.toUnboundAddress().getName());
			long advertiseInterval = advertiseIntervals.get(groupAddress
					.toUnboundAddress().getName());
			timer.schedule(new AgentNodePinger(groupAddress, aliveInterval,
					advertiseInterval), 3000, aliveInterval);
		}
	}

	@Override
	public void doStop() throws Exception {
		super.doStop();

		timer.cancel();

		final JiacMessage byeMessage = new JiacMessage();
		byeMessage.setProtocol(BYE);
		for (ICommunicationAddress groupAddress : groupAddresses) {
			sendMessage(byeMessage, groupAddress);
		}
	}

	@Override
	public void doCleanup() throws Exception {
		super.doCleanup();

		this.serviceMatcher = null;
		this.ontologyStorage = null;

		try {
			messageTransport.stopListen(myAddress, null);
			for (ICommunicationAddress groupAddress : groupAddresses) {
				messageTransport.stopListen(groupAddress, null);
			}
			messageTransport.doCleanup();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	// ######################################
	// WHITE PAGES
	// ######################################

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerAgent(IAgentDescription agentDescription) {
		if (agentDescription == null) {
			log.error("Cannot register agent: null!");
			return;
		}

		String uuid = null;
		try {
			uuid = agentDescription.getAgentNodeUUID();
		} catch (Exception e) {
			log.error("Cannot register agent! No UUID!");
			return;
		}
		if (uuid == null) {
			log.error("Cannot register agent! No UUID!");
			return;
		}

		String aid = agentDescription.getAid();
		IAgentDescription old = null;
		if (uuid.equals(myAgentNode)) {
			synchronized (localAgents) {
				old = localAgents.put(aid, agentDescription);
			}
		} else {
			synchronized (remoteAgents) {
				old = remoteAgents.put(aid, agentDescription);
			}
		}
		if (old == null) {
			log.info("Registered new agent:\n" + agentDescription.toString());
		} else if (! old.equals(agentDescription)) {
			log.info("Registered updated agent:\n" + agentDescription.toString());
		} else {
			log.debug("Registered known agent:\n" + agentDescription.toString());
		}
		dump("registerAgent " + agentDescription.getAid());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deregisterAgent(String aid) {
		if (aid == null) {
			log.error("Cannot deregister agent: null!");
			return;
		}

		synchronized (localAgents) {
			IAgentDescription old = null;
			if (localAgents.containsKey(aid)) {
				old = localAgents.remove(aid);
			} else {
				synchronized (remoteAgents) {
					// TODO deregister actions
					old = remoteAgents.remove(aid);
				}
			}
			if (old != null) {
				log.info("Deregistered agent:\n" + aid);
			}
		}
		dump("deregisterAgent " + aid);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void modifyAgent(IAgentDescription agentDescription) {
		if (agentDescription == null) {
			log.error("Cannot modify agent: null!");
			return;
		}
		registerAgent(agentDescription);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IAgentDescription searchAgent(IAgentDescription template) {
		if (template == null) {
			log.error("Cannot find agent: null!");
			return null;
		}
		synchronized (localAgents) {
			for (IAgentDescription agentDescription : localAgents.values()) {
				if (agentDescription.matches(template)) {
					return agentDescription;
				}
			}
		}
		synchronized (remoteAgents) {
			for (IAgentDescription agentDescription : remoteAgents.values()) {
				if (agentDescription.matches(template)) {
					return agentDescription;
				}
			}
		}
		log.warn("Cannot find agent:\n" + template.toString());
		return null;
	}

	/**
	 * This is inserted for search local agents only. <br>
	 * reason: a (IAgentDescription) template with an agent node UUID didn't
	 * work. But I need in this case just local agents. used template: <br>
	 * <code>new AgentDescription(null, null, null, null, thisAgent.getAgentNode().getUUID())</code>
	 * hate mail to mib
	 * 
	 * @param template
	 *            the template for searching local agents
	 * @return description of all local agents, which are equal to the template
	 */
	public List<IAgentDescription> searchAllLocalAgents(IAgentDescription template) {
		final List<IAgentDescription> agents = new ArrayList<IAgentDescription>();
		synchronized (localAgents) {
			for (IAgentDescription agentDescription : localAgents.values()) {
				if (agentDescription.matches(template)) {
					agents.add(agentDescription);
				}
			}
		}
		return agents;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<IAgentDescription> searchAllAgents(IAgentDescription template) {
		final List<IAgentDescription> agents = new ArrayList<IAgentDescription>();
		synchronized (localAgents) {
			for (IAgentDescription agentDescription : localAgents.values()) {
				if (agentDescription.matches(template)) {
					agents.add(agentDescription);
				}
			}
		}
		synchronized (remoteAgents) {
			for (IAgentDescription agentDescription : remoteAgents.values()) {
				if (agentDescription.matches(template)) {
					agents.add(agentDescription);
				}
			}
		}
		return agents;
	}

	// ######################################
	// YELLOW PAGES
	// ######################################

	@Override
	public void registerAction(IActionDescription actionDescription) {
		if (actionDescription == null) {
			log.error("Cannot register action: null!");
			return;
		}

		String uuid = null;
		try {
			uuid = actionDescription.getProviderDescription()
					.getAgentNodeUUID();
		} catch (Exception e) {
			log.error("(" + e.getClass().getSimpleName()
					+ ") Cannot register action!\n"
					+ actionDescription.toString());
			return;
		}
		if (uuid == null) {
			log.error("Provider information incomplete. Missing UUID. Cannot register action!\n"
					+ actionDescription.toString());
			return;
		}

		if (myAgentNode.equals(uuid)) {
			synchronized (localActions) {
				final boolean success = localActions.add(actionDescription);
				if (!success) {
					log.warn("Action to register already registered. Substituting with new action:\n"
							+ actionDescription.toString());
					localActions.remove(actionDescription);
					localActions.add(actionDescription);
				}
			}
		}

		else {
			synchronized (remoteActions) {
				if (!remoteActions.containsKey(ADDRESS_NAME + "@" + uuid)) {
					log.error("UUID unknown. Cannot register action!\n"
							+ actionDescription.toString());
					return;
				}
				final Set<IActionDescription> actions = remoteActions
						.get(ADDRESS_NAME + "@" + uuid);
				final boolean success = actions.add(actionDescription);
				if (!success) {
					log.warn("Action to register already registered. Substituting with new action:\n"
							+ actionDescription.toString());
					actions.remove(actionDescription);
					actions.add(actionDescription);
				}
			}
		}
		if (log.isInfoEnabled()) {
			log.info("Registered action:\n" + actionDescription.toString());
		}
		dump("registerAction " + actionDescription.getName());

		// TODO register agents that provides this action
	}

	@Override
	public void deregisterAction(IActionDescription actionDescription) {
		if (actionDescription == null) {
			log.error("Cannot remove action: null!");
			return;
		}

		String uuid = null;
		try {
			uuid = actionDescription.getProviderDescription()
					.getAgentNodeUUID();
		} catch (Exception e) {
			log.error("Cannot deregister action!\n"
					+ actionDescription.toString());
			return;
		}
		if (uuid == null) {
			log.error("Provider information incomplete. Missing UUID. Cannot deregister action!\n"
					+ actionDescription.toString());
			return;
		}

		if (uuid.equals(myAgentNode)) {
			synchronized (localActions) {
				if (!localActions.remove(actionDescription)) {
					log.warn("Cannot deregister action! Unknown action '"
							+ actionDescription.getName() + "'!");
				}
			}
		} else {
			synchronized (remoteActions) {
				final Set<IActionDescription> ad = remoteActions
						.get(ADDRESS_NAME + "@" + uuid);
				if (ad == null) {
					log.warn("Cannot deregister action! Unknown UUID: " + uuid);
					return;
				}
				if (!ad.remove(actionDescription)) {
					log.warn("Cannot deregister action! Unknown action:\n"
							+ actionDescription.toString());
				}
			}
		}
		dump("deregisterAction " + actionDescription.toString());
	}

	@Override
	public void modifyAction(IActionDescription oldDescription,
			IActionDescription newDescription) {
		if (oldDescription == null || newDescription == null) {
			log.error("Cannot modify action (old:\n" + oldDescription
					+ "\nnew:\n" + newDescription);
			return;
		}

		String uuid = null;
		try {
			uuid = oldDescription.getProviderDescription().getAgentNodeUUID();
		} catch (Exception e) {
			log.error("Cannot modify action!\n" + oldDescription.toString());
			return;
		}
		if (uuid == null) {
			log.error("Provider information incomplete. Missing UUID. Cannot modify action!\n"
					+ oldDescription.toString());
			return;
		}

		if (uuid.equals(myAgentNode)) {
			synchronized (localActions) {
				if (!localActions.remove(oldDescription)) {
					log.warn("Cannot deregister action:\n"
							+ oldDescription.toString());
				}
			}
		} else {
			synchronized (remoteActions) {
				final Set<IActionDescription> ad = remoteActions
						.get(ADDRESS_NAME + "@" + uuid);
				if (ad == null) {
					log.warn("Cannot deregister action! Unknown UUID: " + uuid);
				} else {
					if (!ad.remove(oldDescription)) {
						log.warn("Cannot deregister action! Unknown action:\n"
								+ oldDescription.toString());
					}
				}
			}
		}

		registerAction(newDescription);
	}

	@Override
	public IActionDescription searchAction(IActionDescription template) {
		if (template == null) {
			log.error("Cannot find action: action template is null!");
			return null;
		}

		// use Matcher for matching if possible
		if (hasSemanticIRI(template)) {
			IServiceDescription templateSD = createTemplateSD(template);
			IServiceDescription matcherResult = searchAction(templateSD);
			if (matcherResult != null) {
				// XXX what is this step for, and why only here, but not in searchAllActions?
				IActionDescription iad = this.findActionDescription(matcherResult);
				if (iad != null){
					return iad;
				} else {
					log.warn("Matcher found result, however the respective action was not found afterwards. Trying normal template matching.");
				}
			} else {
				log.warn("Matcher found no result, trying normal template matching...");
			}
		}

		synchronized (localActions) {
			for (IActionDescription ad : localActions) {
				if (ad.matches(template)) {
					return ad;
				}
			}
		}
		synchronized (remoteActions) {
			for (Set<IActionDescription> adset : remoteActions.values()) {
				for (IActionDescription ad : adset) {
					if (ad.matches(template)) {
						return ad;
					}
				}
			}
		}
		log.warn("Cannot find action:\n" + template);
		return null;
	}
	
	@Override
	public IServiceDescription searchAction(IServiceDescription template) {
		if (template == null) {
			log.error("Cannot find action: service template is null!");
			return null;
		}

		if (hasServiceMatcher()) {
			final ArrayList<IServiceDescription> serviceDescList = findAllComplexServices();
			final IServiceDescription matcherResult = this.serviceMatcher
					.findBestMatch(template, serviceDescList);
			return matcherResult;

		} else {
			log.error("This agentnode has no servicematcher and / or ontology storage - no complex matching possible!");
		}
		log.warn("Cannot find action:\n" + template);
		return null;
	}

	@Override
	public List<IActionDescription> searchAllActions(IActionDescription template) {
		if (template == null) {
			log.error("Cannot find actions: action template is null!");
			return Collections.emptyList();
		}

		// use Matcher for matching if possible
		if (hasSemanticIRI(template)) {
			IServiceDescription templateSD = createTemplateSD(template);
			List<IActionDescription> matcherResults = searchAllActions(templateSD);
			if (! matcherResults.isEmpty()) {
				return matcherResults;
			} else {
				log.warn("Matcher found no result, trying normal template matching...");
			}
		}

		final ArrayList<IActionDescription> actions = new ArrayList<IActionDescription>();
		synchronized (localActions) {
			for (IActionDescription actionDescription : localActions) {
				if (actionDescription.matches(template)) {
					actions.add(actionDescription);
				}
			}
		}
		synchronized (remoteActions) {
			for (Set<IActionDescription> adset : remoteActions.values()) {
				for (IActionDescription ad : adset) {
					if (ad.matches(template)) {
						actions.add(ad);
					}
				}
			}
		}
		return actions;
	}
	
	@Override
	public List<IActionDescription> searchAllActions(IServiceDescription template) {
		if (template == null) {
			log.error("Cannot find actions: service template is null!");
			return Collections.emptyList();
		}

		if (hasServiceMatcher()) {
			final ArrayList<IServiceDescription> serviceDescList = findAllComplexServices();
			final ArrayList<? extends IActionDescription> matcherResults = this.serviceMatcher
					.findAllMatches(template, serviceDescList);
			if (matcherResults != null) {
				return new ArrayList<>(matcherResults);
			}
			
		} else {
			log.error("This agentnode has no servicematcher and / or ontology storage - no complex matching possible!");
		}
		return Collections.emptyList();
	}

	/**
	 * Create template ServiceDescription for ActionDescription with URI.
	 *
	 * Since template URIs might be used multiple times with different
	 * content, we remove the old one before matching; otherwise there would
	 * be no update on the service request for the matcher.
	 *
	 * @param template	some template action that has a semantic URI
	 * @return			service description created from the OWL-S at the URI, or null
	 */
	private IServiceDescription createTemplateSD(IActionDescription template) {
		if (! hasServiceMatcher()) {
			log.warn("Trying to Create Semantic Service Description without a Service Matcher!");
			return null;
		}
		try {
			this.ontologyStorage.removeOntology(new URI(template.getSemanticServiceDescriptionIRI()));
			return this.ontologyStorage.
					loadServiceDescriptionFromOntology(new URI(template.getSemanticServiceDescriptionIRI()));
		} catch (URISyntaxException e) {
			log.error("Semantic IRI of action " + template.getName()
					+ " incorrect: " + template.getSemanticServiceDescriptionIRI());
			return null;
		}
	}

	/**
	 * @return whether service matcher and ontology storage are present.
	 */
	private boolean hasServiceMatcher() {
		return this.serviceMatcher != null && this.ontologyStorage != null;
	}

	/**
	 * @param template	some action template
	 * @return			whether the template has a non-empty semantic service IRI
	 */
	private boolean hasSemanticIRI(IActionDescription template) {
		return template.getSemanticServiceDescriptionIRI() != null && ! template.getSemanticServiceDescriptionIRI().isEmpty();
	}


	// ######################################
	// ILifeCycleListener
	// ######################################

	@Override
	public void onEvent(LifecycleEvent event) {
		if (log.isDebugEnabled()) {
			log.debug(myAgentNode + "::" + event.getSource() + " State: "
					+ event.getState());
		}

		if (event.getState() == LifecycleStates.STARTING) {
			if (event.getSource() instanceof Agent) {
				final Agent agent = (Agent) event.getSource();
				registerAgent(agent.getAgentDescription());

				// register actions
				final List<IAgentBean> beans = agent.getAgentBeans();
				if (beans != null && !beans.isEmpty()) {
					for (IAgentBean ab : beans) {
						if (ab instanceof IEffector) {
							final IEffector effector = (IEffector) ab;
							final List<? extends IActionDescription> actions = effector
									.getActions();
							if (actions != null && !actions.isEmpty()) {
								for (IActionDescription action : actions) {
									if ((action.getScope() != null)
											&& action.getScope().contains(
													ActionScope.NODE)) {
										action.setProviderDescription(agent
												.getAgentDescription());
										registerAction(action);
									}
								}
							} else {
								log.warn("Effector: " + ab.getBeanName()
										+ " has no Actions?!?");
							}
						}
					}
				} else {
					log.warn("Agent: " + agent.getAgentId()
							+ " has no AgentBeans?!?");
				}

				// send advertisement with description of a single started agent
				if (agentNode.getState() == LifecycleStates.STARTED) {
					sendAmendment(agent.getAgentId(), true);
				}
			}
		}

		else if (event.getState() == LifecycleStates.STOPPING) {
			if (event.getSource() instanceof Agent) {
				final String agent = ((Agent) event.getSource()).getAgentId();

				final List<IActionDescription> actions = new ArrayList<IActionDescription>();
				synchronized (localActions) {
					for (IActionDescription action : localActions) {
						if (action.getProviderDescription().getAid()
								.equals(agent)) {
							actions.add(action);
						}
					}
					for (IActionDescription action : actions) {
						localActions.remove(action);
					}
				}

				deregisterAgent(agent);

				// send advertisement with ID of a single stopped agent
				if (agentNode.getState() == LifecycleStates.STARTED) {
					sendAmendment(agent, false);
				}
			}
		}
	}

	// ######################################
	// IMessageTransportDelegate
	// ######################################

	@Override
	public void onMessage(MessageTransport source, IJiacMessage message,
			ICommunicationAddress at) {

		final ICommunicationAddress senderAddress = message.getSender();
		//final String groupAddress = message.getGroup();
		final String groupAddress = (at instanceof IGroupAddress)? at.toUnboundAddress().getName() : null;

		// own message, do nothing
		if (senderAddress == null || senderAddress.equals(myAddress)) {
			return;
		}

		final String protocol = message.getProtocol();
		if (log.isDebugEnabled()) {
			log.debug("Received message with protocol " + protocol + " from " + senderAddress);
		}

		if (protocol.equals(ALIVE)) {
			synchronized (nodes) {
				refreshAgentNode(senderAddress, groupAddress, false);
			}
			return;
		}

		else if (protocol.equals(BYE)) {
			synchronized (nodes) {
				final String nodeAddress = senderAddress.getName();
				if (nodes.containsKey(nodeAddress)) {
					synchronized (remoteActions) {
						log.info(nodeAddress + " says bye");
						removeRemoteAgentOfNode(nodeAddress);
						remoteActions.remove(nodeAddress);
						nodes.remove(nodeAddress);
						log.info(nodeAddress + " removed");
					}
				}
				dump(nodeAddress + " says bye");
			}
			return;
		}

		else if (protocol.equals(ADVERTISE)) {
			synchronized (nodes) {
				// refresh agent node
				final Advertisement payload = (Advertisement) message.getPayload();
				refreshAgentNode(senderAddress, groupAddress, payload.getJmxURLs(), payload.getAliveInterval());

				// refresh remote actions
				final Set<IActionDescription> actions = ((Advertisement) message
						.getPayload()).getActions();
				if (log.isDebugEnabled()) {
					log.debug("receive ADVERTISE: " + actions.size());
				}

				final Set<IActionDescription> receivedActions = new HashSet<IActionDescription>();
				
				receivedActions.addAll(actions);

				remoteActions.put(senderAddress.getName(), receivedActions);

				// refresh remote agents
				final Hashtable<String, IAgentDescription> agents = ((Advertisement) message
						.getPayload()).getAgents();
				for (IAgentDescription agent : agents.values()) {
					registerAgent(agent);
				}

				// remove killed remote agents
				synchronized (remoteAgents) {
					Iterator<IAgentDescription> i = remoteAgents.values().iterator();
					while (i.hasNext()) {
						final IAgentDescription a = i.next();
						if (!agents.containsKey(a.getAid()) && senderAddress.getName().equals( ADDRESS_NAME + "@" + a.getAgentNodeUUID())) {
							// remote agent belongs to the advertised node but is not included in the advertisement anymore
							// => remote agent was killed, so its description must be removed from directory
							i.remove();
							log.info("Deregistered agent:\n" + a.getAid());
						}
					}
				}

				dump("ADVERTISE " + senderAddress.getName());
			}
		}

		else if (protocol.equals(AMEND)) {
			final Amendment am = (Amendment) message.getPayload();
			if (am instanceof AgentStarted) {
				final AgentStarted started = (AgentStarted)am;

				// add started remote agent
				final IAgentDescription agent = started.getAgent();
				synchronized (remoteAgents) {
					if (remoteAgents.put(agent.getAid(), agent) == null) {
						log.info("Registered remote agent: " + agent.getAid());
					}
					else {
						// started agent was already registered
						log.warn("Updated remote agent: " + agent.getAid());						
					}
				}

				// add actions of the started remote agent
				synchronized (remoteActions) {
					Set<IActionDescription> actions = remoteActions.get(senderAddress.getName());
					if (actions == null) {
						remoteActions.put(senderAddress.getName(), started.getActions());
					}
					else {
						// replace action descriptions of the started agent
						Iterator<IActionDescription> i = actions.iterator();
						while (i.hasNext()) {
							final IAgentDescription provider = i.next().getProviderDescription();
							if (provider.getAid().equals(agent.getAid())) {
								i.remove();
							}
						}
						actions.addAll(started.getActions());
					}

					if (log.isDebugEnabled()) {
						log.debug("Registered actions of remote agent: " + started.getActions().size());
					}
				}
			}
			else if (am instanceof AgentStopped) {
				final AgentStopped stopped = (AgentStopped)am;

				// remove actions of the stopped remote agent
				synchronized (remoteActions) {
					Set<IActionDescription> actions = remoteActions.get(senderAddress.getName());
					if (actions != null) {
						int counter = 0;
						Iterator<IActionDescription> i = actions.iterator();
						while (i.hasNext()) {
							final IAgentDescription provider = i.next().getProviderDescription();
							if (provider.getAid().equals(stopped.getAgentId())) {
								i.remove();
								counter++;
							}
						}

						if (log.isDebugEnabled()) {
							log.debug("Deregistered actions of remote agent: " + counter);
						}
					}
				}

				// remove stopped remote agent
				synchronized (remoteAgents) {
					if (remoteAgents.remove(stopped.getAgentId()) == null) {
						// stopped agent was not registered
						log.warn("Remote agent already deregistered: " + stopped.getAgentId());						
					}
					else {
						log.info("Deregistered remote agent: " + stopped.getAgentId());						
					}
				}
			}

			dump("AMEND " + senderAddress.getName());
		}

		else if (protocol.equals(ALL)) {
			sendAdvertisement(senderAddress, getAliveInterval(message.getGroup()));
		}
	}

	/**
	 * This returns an ICommunicationAddress of an agent node that is known or
	 * <code>null</code>, if it is unknown. <br>
	 * insert for agent migration (hate mails to mib)
	 * 
	 * @param uuidOfNode
	 *            the UUID of the agent node
	 * @return the communication address of the given node, or <code>null</code>
	 *         if the node is unknown
	 */
	public ICommunicationAddress getCommunicationAddressOfANode(
			String uuidOfNode) {

		if (uuidOfNode == null || uuidOfNode.equals("")) {
			return null;
		}

		ICommunicationAddress ret = null;
		synchronized (nodes) {
			if (nodes.containsKey(ADDRESS_NAME + "@" + uuidOfNode)) {
				final AgentNodeDescription nodeDescription = nodes
						.get(ADDRESS_NAME + "@" + uuidOfNode);
				ret = nodeDescription.getAddress();
			}
		}
		return ret;

	}

	/**
	 * Returns the message box address of all known and reachable agent nodes.
	 * The unmodifiable set is backed by the directory, so changes in the
	 * directory will affect this set.
	 * 
	 * @return unmodifiable set with message box address of all known agent
	 *         nodes.
	 */
	public Set<String> getAllKnownAgentNodes() {
		synchronized (nodes) {
			return Collections.unmodifiableSet(nodes.keySet());
		}
	}

	private void refreshAgentNode(ICommunicationAddress node,
			String groupAddress, boolean advertisement) {
		final String nodeAddress = node.getName();
		synchronized (nodes) {
			final AgentNodeDescription description = nodes.get(nodeAddress);
			if (description != null) {
				// node is already known
				if (groupAddress != null) {
					// check delay of periodical message
					final long interval = System.currentTimeMillis()
							- description.getAlive();
					if (interval > (description.getAliveInterval() + allowedAliveDelay)) {
						log.warn("Measured interval of receiving alive message from "
								+ nodeAddress + ": " + interval + "ms instead of " + description.getAliveInterval() + "ms");
					}
				}
				description.setAlive(System.currentTimeMillis());
			} else {
				// node was still unknown or forgotten due to excessive delay of periodical message
				if (advertisement) {
					final AgentNodeDescription newDescription = new AgentNodeDescription(
						node, System.currentTimeMillis());
					nodes.put(nodeAddress, newDescription);
					if (missingNodes.contains(nodeAddress)) {
						// got advertisement of forgotten node => node is known again
						log.warn("Got new information from missing node " + nodeAddress);
						missingNodes.remove(nodeAddress);
					}
					else {
						// node was unknown so far, because the remote or the local node is new
						log.info("New known node " + nodeAddress);
						if (groupAddress != null) {
							// got advertisement from a new node => send back an advertisement immediately
							sendAdvertisement(node, getAliveInterval(groupAddress));
						}
					}
				}
				else {
					if (missingNodes.contains(nodeAddress)) {
						// got alive message from a forgotten node => request for advertisement
						final JiacMessage allMessage = new JiacMessage();
						allMessage.setProtocol(ALL);
						allMessage.setGroup(groupAddress);
						sendMessage(allMessage, node);
					}
				}
			}
		}
	}

	private void refreshAgentNode(ICommunicationAddress node,
			String groupAddress, Set<JMXServiceURL> jmxConnectors, long aliveInterval) {
		final String nodeAddress = node.getName();
		refreshAgentNode(node, groupAddress, true);
		synchronized (nodes) {
			final AgentNodeDescription description = nodes.get(nodeAddress);
			description.setJmxURLs(jmxConnectors);
			description.setAliveInterval(aliveInterval);
		}
	}

	@Override
	public Logger getLog(String extension) {
		if (agentNode == null) {
			return null;
		}
		return agentNode.getLog(this, extension);
	}

	@Override
	public void onAsynchronousException(MessageTransport source, Exception e) {
		e.printStackTrace();
	}

	// ######################################
	// Getter/Setter
	// ######################################

	/**
	 * Returns the communication handler.
	 * 
	 * @return the communication handler
	 */
	public MessageTransport getMessageTransport() {
		return messageTransport;
	}

	/**
	 * Sets the communication handler.
	 * 
	 * @param newMessageTransport
	 *            the communication handler to set
	 */
	public void setMessageTransport(MessageTransport newMessageTransport) {
		messageTransport = newMessageTransport;
	}

	/**
	 * Set the time how often the directory will send a ping.
	 * 
	 * @param interval
	 *            the time between pings
	 */
	public void setAliveInterval(long interval) {
		this.aliveInterval = interval;
	}

	/**
	 * Set the time how often the directory will send a ping.
	 * 
	 * @param aliveIntervals
	 *            the time between pings
	 */
	public void setAliveIntervals(HashMap<String, Long> aliveIntervals) {
		this.aliveIntervals = new HashMap<String, Long>();
		for (Entry<String, Long> entry : aliveIntervals.entrySet()) {
			this.aliveIntervals.put("df@" + entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @deprecated
	 */
	public long getAliveInterval() {
		return aliveInterval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Long> getAliveIntervals() {
		return aliveIntervals;
	}

	/**
	 * Returns the time between to pings.
	 * 
	 * @return the time between to pings
	 */
	public long getAliveInterval(String groupName) {
		return aliveIntervals.get(groupName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAdvertiseInterval(long interval) {
		advertiseInterval = interval;
	}

	public void setAdvertiseIntervals(HashMap<String, Long> advertiseIntervals) {
		this.advertiseIntervals = new HashMap<String, Long>();
		for (Entry<String, Long> entry : advertiseIntervals.entrySet()) {
			this.advertiseIntervals.put("df@" + entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @deprecated
	 */
	public long getAdvertiseInterval() {
		return advertiseInterval;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Long> getAdvertiseIntervals() {
		return advertiseIntervals;
	}

	public long getAdvertiseInterval(String groupName) {
		return advertiseIntervals.get(groupName);
	}

	public void setGroup(List<String> groupAddressNames) {
		this.groupAddressNames = groupAddressNames;
	}

	public List<String> getGroupNames() {
		return groupAddressNames;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getAllowedAliveDelay() {
		return allowedAliveDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setAllowedAliveDelay(long allowedAliveDelay) {
		this.allowedAliveDelay = allowedAliveDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getMaxAliveDelay() {
		return maxAliveDelay;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMaxAliveDelay(long maxAliveDelay) {
		this.maxAliveDelay = maxAliveDelay;
	}

	
	/**
	 * Searches for the respective ActionDescription for a given ServiceDescription. The 
	 * ActionDescription can then be used for the invocation part. This ensures that the provider 
	 * side does not have to include the ServiceDescription libraries within the JIAC environment. 
	 * 
	 * 
	 * @param isd The ServiceDescription for which the respective ActionDescription is missing 
	 * @return The respective ActionDescription, if still running. Otherwise null.
	 */
	private IActionDescription findActionDescription(IServiceDescription isd){
		// find serviceDescription in local actions
		synchronized (localActions) {
			for (IActionDescription localAct : localActions) {
				if (compare(localAct, isd)){
					return localAct;
				}
			}
		}
		
		// find serviceDescription in remote Actions
		synchronized (remoteActions) {
			for (Set<IActionDescription> remoteActSet : remoteActions.values()) {
				for (IActionDescription remoteAct : remoteActSet) {
				    if (compare(remoteAct, isd)){
				    	return remoteAct;
				    }
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Compares two actions with each other
	 * 
	 * @param iad ActionDescription
	 * @param isd ServiceDescription
	 * @return
	 */
	private boolean compare(IActionDescription iad, IServiceDescription isd) {
		
		if(iad.getName().equals(isd.getName()) &&
				iad.getSemanticServiceDescriptionIRI().equals(isd.getSemanticServiceDescriptionIRI()) &&
				iad.getProviderDescription().equals(isd.getProviderDescription()) &&
				iad.getScope().equals(isd.getScope())){
		
			return true;
		
		}
		
		return false;
	}

	/**
	 * Collects all complex services, i.e. entries with an IServiceDescription,
	 * from both, the local and the remote services list.
	 * 
	 * @return a list of all IServiceDescriptions from this directory,
	 *         containing both, local and remote entries.
	 */
	private ArrayList<IServiceDescription> findAllComplexServices() {
		final ArrayList<IServiceDescription> ret = new ArrayList<IServiceDescription>();

		// find serviceDescriptions in local actions
		synchronized (localActions) {
			for (IActionDescription localAct : localActions) {
			    if (localAct.getSemanticServiceDescriptionIRI() != null && ! localAct.getSemanticServiceDescriptionIRI().isEmpty()){
					try {
						IServiceDescription sd = ontologyStorage.
								loadServiceDescriptionFromOntology(new URI(localAct.getSemanticServiceDescriptionIRI()));
						sd = this.setActionParametersOnServiceDescription(sd, localAct);
						ret.add(sd);
					} catch (URISyntaxException e) {
						log.error("Semantic IRI of action " + localAct.getName() 
								+ " incorrect: " + localAct.getSemanticServiceDescriptionIRI());
					} catch (Exception e) {
						log.error("Semantic service description for action " + localAct.getName() 
								+ " could not be loaded. Semantic URI: " + localAct.getSemanticServiceDescriptionIRI());
					}
				}
				
			}
		}

		// find serviceDescriptions in remote Actions
		synchronized (remoteActions) {
			for (Set<IActionDescription> remoteActSet : remoteActions.values()) {
				for (IActionDescription remoteAct : remoteActSet) {
				    if (remoteAct.getSemanticServiceDescriptionIRI() != null && ! remoteAct.getSemanticServiceDescriptionIRI().isEmpty()){
						try {
							IServiceDescription sd = ontologyStorage.
									loadServiceDescriptionFromOntology(new URI(remoteAct.getSemanticServiceDescriptionIRI()));
							sd = this.setActionParametersOnServiceDescription(sd, remoteAct);
							ret.add(sd);
						} catch (URISyntaxException e) {
							log.error("Semantic IRI of action " + remoteAct.getName() 
									+ " incorrect: " + remoteAct.getSemanticServiceDescriptionIRI());
						} catch (Exception e) {
							log.error("Semantic service description for action " + remoteAct.getName() 
									+ " could not be loaded. Semantic URI: " + remoteAct.getSemanticServiceDescriptionIRI());
							log.error(e.getMessage());
							
						}
					}
				}
			}
		}

		return ret;
	}
	
	private IServiceDescription setActionParametersOnServiceDescription(IServiceDescription isd, IActionDescription iad) {
		
		isd.setName(iad.getName());
		isd.setActionType(iad.getActionType());
		
		isd.setInputTypeNames(iad.getInputTypeNames());
		isd.setResultTypeNames(iad.getResultTypeNames());
		
		try {
			isd.setInputTypes(iad.getInputTypes());
			isd.setResultTypes(iad.getResultTypes());
		} catch (ClassNotFoundException e) {
			// actions from different node might use classes not on the class path
			log.warn("Input/Result Types of action " + iad.getName() + " could not be loaded: " + e.getMessage());
		}
		
		isd.setSemanticServiceDescriptionURI(iad.getSemanticServiceDescriptionIRI());
		
		isd.setProviderBean(iad.getProviderBean());
		isd.setProviderDescription(iad.getProviderDescription());
				
		isd.setScope(iad.getScope());
		
		return isd;
	}
	


	// #########################################
	// Communication
	// #########################################

	protected void sendMessage(JiacMessage message,
			ICommunicationAddress address) {
		message.setSender(myAddress);
		if (address instanceof IGroupAddress) {
			message.setGroup(address.toUnboundAddress().getName());
		}
		message.setHeader("UUID", this.agentNode.getUUID());
		try {
			messageTransport.send(message, address, 0);
			if (log.isDebugEnabled()) {
				log.debug("Sent message with protocol " + message.getProtocol() + " to " + address.toUnboundAddress().getName());
			}
			if (address instanceof IGroupAddress) {
				final long interval = System.currentTimeMillis() - lastSend;
				if (interval > (getAliveInterval(address.toUnboundAddress().getName()) + allowedAliveDelay)) {
					log.warn("Measured interval of sending alive message: "
							+ interval + "ms instead of " + getAliveInterval(address.toUnboundAddress().getName()) + "ms");
				}
				lastSend = System.currentTimeMillis();
			}
		} catch (Exception e) {
			log.error("sendMessage failed. Message:\n" + message.toString(), e);
		}
	}

	/**
	 * Send all actions, agents and JMX URLs this agent node is providing.
	 */
	private void sendAdvertisement(ICommunicationAddress destination, long aliveInterval) {
		final JiacMessage adMessage = new JiacMessage();
		adMessage.setProtocol(ADVERTISE);

		// collect all local actions with scope GLOBAL or WEBSERVICE for the advertisement
		final Set<IActionDescription> advActions = new HashSet<IActionDescription>();
		synchronized (localActions) {
			for (IActionDescription localAction : localActions) {
				if (localAction.getScope().contains(ActionScope.GLOBAL)) {
					advActions.add(localAction);
				}
			}
		}

		// collect all local agents for the advertisement
		Hashtable<String, IAgentDescription> advAgents = new Hashtable<String, IAgentDescription>();
		synchronized (localAgents) {
			advAgents.putAll(localAgents);
		}

		final Advertisement ad = new Advertisement(advAgents, advActions, aliveInterval);

		// add JMX service URLs of the agent node to the advertisement
		if (agentNode.getJmxConnectors() != null) {
			ad.setJmxURLs(agentNode.getJmxURLs());
		}
		adMessage.setPayload(ad);

		// debug
		final Advertisement payload = (Advertisement) adMessage.getPayload();
		if (log.isDebugEnabled()) {
			log.debug("sendAdvertisement: jmxURLs="
					+ payload.getJmxURLs().size() + " agents="
					+ payload.getAgents().size() + " actions="
					+ payload.getActions().size());
		}

		sendMessage(adMessage, destination);

	}

	/**
	 * Send description of the started agent or ID of the stopped agent.
	 */
	private void sendAmendment(String agentId, boolean started) {
		final JiacMessage amMessage = new JiacMessage();
		amMessage.setProtocol(AMEND);

		if (started) {
			// collect all actions of the started agent with scope GLOBAL or WEBSERVICE for the amendment
			final Set<IActionDescription> amActions = new HashSet<IActionDescription>();
			synchronized (localActions) {
				for (IActionDescription localAction : localActions) {
					IAgentDescription provider = localAction.getProviderDescription();
					if (localAction.getScope().contains(ActionScope.GLOBAL) && (provider != null) && provider.getAid().equals(agentId)) {
						amActions.add(localAction);
					}
				}
			}

			final Amendment am = new AgentStarted(localAgents.get(agentId), amActions);
			amMessage.setPayload(am);

			// debug
			final Amendment payload = (Amendment) amMessage.getPayload();
			if (log.isDebugEnabled()) {
				log.debug("sendAmendment: agent="
						+ ((AgentStarted)payload).getAgent().getAid() + " actions="
						+ ((AgentStarted)payload).getActions().size());
			}
		}

		else {
			final Amendment am = new AgentStopped(agentId);
			amMessage.setPayload(am);

			// debug
			final Amendment payload = (Amendment) amMessage.getPayload();
			if (log.isDebugEnabled()) {
				log.debug("sendAmendment: agent="
						+ ((AgentStopped)payload).getAgentId());
			}

		}

		for (ICommunicationAddress groupAddress : groupAddresses) {
			sendMessage(amMessage, groupAddress);
		}
	}

	class AgentNodePinger extends TimerTask {

		/**
		 * counter of agent node pinger
		 */
		private long counter;

		/**
		 * alive interval for agent node pinger
		 */
		private long aliveInterval;

		/**
		 * advertise interval for agent node pinger
		 */
		private long advertiseInterval;

		private ICommunicationAddress groupAddress;

		public AgentNodePinger(ICommunicationAddress groupAddress,
				long aliveInterval, long advertiseInterval) {
			this.aliveInterval = aliveInterval;
			this.counter = advertiseInterval;
			this.advertiseInterval = advertiseInterval;
			this.groupAddress = groupAddress;
		}

		@Override
		public void run() {
			if (counter < advertiseInterval) {
				final JiacMessage aliveMessage = new JiacMessage();
				aliveMessage.setProtocol(ALIVE);
				sendMessage(aliveMessage, groupAddress);
				if (log.isDebugEnabled()) {
					log.debug("Alive Ping: " + System.currentTimeMillis()
							+ " with counter: " + counter);
				}
				counter += aliveInterval;
			} else {
				counter -= advertiseInterval;
				sendAdvertisement(groupAddress, aliveInterval);
			}

			// remove dead nodes
			synchronized (nodes) {
				final Set<String> deadNodes = new HashSet<String>();
				for (String key : nodes.keySet()) {
					final AgentNodeDescription description = nodes.get(key);
					if (description.getAlive() < (System.currentTimeMillis() - (description.getAliveInterval() + maxAliveDelay))) {
						deadNodes.add(key);
					}
				}
				if (deadNodes.size() > 0) {
					log.warn("Forget nodes due to missing alive message:\n");
					synchronized (remoteActions) {
						for (String nodeAddress : deadNodes) {
							log.warn("\t" + nodeAddress);
							remoteActions.remove(nodeAddress);
							removeRemoteAgentOfNode(nodeAddress);
							nodes.remove(nodeAddress);
							missingNodes.add(nodeAddress);
						}
					}
				}
			}
		}
	}

	private void removeRemoteAgentOfNode(String nodeAddress) {
		synchronized (remoteAgents) {
			for (final Iterator<IAgentDescription> entries = remoteAgents.values().iterator(); entries.hasNext(); ) {
				final IAgentDescription agent = entries.next();
				if (nodeAddress.equals(ADDRESS_NAME + "@" + agent.getAgentNodeUUID())) {
					entries.remove();
				}
			}
		}
	}

	// ######################################
	// Debug
	// ######################################

	private void dump(String cause) {
		if (dump) {
			System.out.println("\n####Dump " + myAgentNode + " " + cause);
			System.out.println("Registered local agents:   "
					+ localAgents.size());
			System.out.println("Registered local actions:  "
					+ localActions.size());
			int actions = 0;
			for (Set<IActionDescription> actionSet : remoteActions.values()) {
				actions += actionSet.size();
			}
			System.out.println("Registered remote agents:  "
					+ remoteAgents.size());
			System.out.println("Registered remote actions: " + actions);
			System.out.println("Known agentnodes:          " + nodes.size());
			System.out.println("####End dump\n");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDump() {
		return dump;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDump(boolean newDump) {
		dump = newDump;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TabularData getKnownNodes() {
		synchronized (nodes) {
			if (nodes.isEmpty()) {
				return null;
			}

			final Set<Map.Entry<String, AgentNodeDescription>> entries = nodes
					.entrySet();
			try {
				final String[] itemNames = new String[] { "UUID", "description" };
				final CompositeType nodeType = new CompositeType(
						entries.iterator().next().getClass().getName(),
						"agent node description corresponding to an agent node UUID",
						itemNames, itemNames, new OpenType<?>[] {
								SimpleType.STRING,
								entries.iterator().next().getValue()
										.getDescriptionType() });
				final TabularType type = new TabularType(nodes.getClass()
						.getName(),
						"node descriptions stored in the directory", nodeType,
						new String[] { "UUID" });
				final TabularData data = new TabularDataSupport(type);
				for (Map.Entry<String, AgentNodeDescription> node : entries) {
					final CompositeData nodeData = new CompositeDataSupport(
							nodeType, itemNames, new Object[] { node.getKey(),
									node.getValue().getDescription() });
					data.put(nodeData);
				}
				return data;
			} catch (OpenDataException e) {
				log.error("Unable to create open data format for known nodes.", e);
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TabularData getLocalActions() {
		synchronized (localActions) {
			if (localActions.isEmpty()) {
				return null;
			}

			try {
				final TabularType type = new TabularType(localActions
						.getClass().getName(),
						"local actions stored in the directory",
						(CompositeType) localActions.iterator().next()
								.getDescriptionType(), IActionDescription.getItemNames());
				final TabularData data = new TabularDataSupport(type);
				for (IActionDescription action : localActions) {
					data.put((CompositeData) action.getDescription());
				}
				return data;
			} catch (OpenDataException e) {
				log.error("Unable to create open data format for local actions.", e);
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TabularData getLocalAgents() {
		synchronized (localAgents) {
			if (localAgents.isEmpty()) {
				return null;
			}

			final Set<Map.Entry<String, IAgentDescription>> entries = localAgents
					.entrySet();
			try {
				final String[] itemNames = new String[] { "Agent ID",
						"description" };
				final CompositeType agentType = new CompositeType(entries
						.iterator().next().getClass().getName(),
						"local agent description corresponding to an agent id",
						itemNames, itemNames, new OpenType<?>[] {
								SimpleType.STRING,
								entries.iterator().next().getValue()
										.getDescriptionType() });
				final TabularType type = new TabularType(localAgents.getClass()
						.getName(),
						"local agent descriptions stored in the directory",
						agentType, new String[] { "Agent ID" });
				final TabularData data = new TabularDataSupport(type);
				for (Map.Entry<String, IAgentDescription> agent : entries) {
					final CompositeData agentData = new CompositeDataSupport(
							agentType, itemNames, new Object[] {
									agent.getKey(),
									agent.getValue().getDescription() });
					data.put(agentData);
				}
				return data;
			} catch (OpenDataException e) {
				log.error("Unable to create open data format for local agents.", e);
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TabularData getRemoteActions() {
		if (remoteActions.isEmpty()) {
			return null;
		}

		final Set<Map.Entry<String, Set<IActionDescription>>> entries = remoteActions
				.entrySet();
		try {
			final String[] itemNames = new String[] { "agent node UUID",
					"remote actions" };
			final CompositeType entryType = new CompositeType(
					entries.iterator().next().getClass().getName(),
					"remote action descriptions corresponding to an agent node UUID",
					itemNames, itemNames, new OpenType<?>[] {
							SimpleType.STRING,
							ArrayType.getArrayType(new Action()
									.getDescriptionType()) });
			final TabularType type = new TabularType(remoteActions.getClass()
					.getName(),
					"remote action descriptions stored in the directory",
					entryType, new String[] { "agent node UUID" });

			final TabularData data = new TabularDataSupport(type);
			for (Map.Entry<String, Set<IActionDescription>> entry : entries) {
				int i = 0;
				final Set<IActionDescription> values = entry.getValue();
				final CompositeData[] actions = new CompositeData[values.size()];
				for (IActionDescription action : values) {
					actions[i++] = (CompositeData) action.getDescription();
				}
				final CompositeData entryData = new CompositeDataSupport(
						entryType, itemNames, new Object[] { entry.getKey(),
								actions });
				data.put(entryData);
			}
			return data;
		} catch (OpenDataException e) {
			log.error("Unable to create open data format for remote actions.", e);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TabularData getRemoteAgents() {
		synchronized (remoteAgents) {
			if (remoteAgents.isEmpty()) {
				return null;
			}

			final Set<Map.Entry<String, IAgentDescription>> entries = remoteAgents
					.entrySet();
			try {
				final String[] itemNames = new String[] { "Agent ID",
						"description" };
				final CompositeType agentType = new CompositeType(
						entries.iterator().next().getClass().getName(),
						"remote agent description corresponding to an agent id",
						itemNames, itemNames, new OpenType<?>[] {
								SimpleType.STRING,
								entries.iterator().next().getValue()
										.getDescriptionType() });
				final TabularType type = new TabularType(remoteAgents
						.getClass().getName(),
						"remote agent descriptions stored in the directory",
						agentType, new String[] { "Agent ID" });
				final TabularData data = new TabularDataSupport(type);
				for (Map.Entry<String, IAgentDescription> agent : entries) {
					final CompositeData agentData = new CompositeDataSupport(
							agentType, itemNames, new Object[] {
									agent.getKey(),
									agent.getValue().getDescription() });
					data.put(agentData);
				}
				return data;
			} catch (OpenDataException e) {
				log.error("Unable to create open data format for remote agents.", e);
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getMissingNodes() {
		return missingNodes;
	}

}
