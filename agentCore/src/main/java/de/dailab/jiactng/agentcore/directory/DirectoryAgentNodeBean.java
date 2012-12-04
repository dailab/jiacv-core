package de.dailab.jiactng.agentcore.directory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
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
 * This agent node bean uses the address "df@dfgroup" to send and receive 
 * alive messages to/from all other agent nodes of the platform at alive 
 * interval. At advertise interval an advertise message will be send with 
 * information about local agents, actions and JMX URLs. If the bean is 
 * stopped, a bye message will be send.
 *  
 * @author Jan Keiser
 * @see de.dailab.jiactng.agentcore.Agent#searchAllActions(IActionDescription)
 * @see de.dailab.jiactng.agentcore.Agent#searchAllAgents(IAgentDescription)
 * @see de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient#getURLsFromMulticast()
 * @see de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient#getURLsFromRegistry(String, int)
 * @see Advertisement
 */
public class DirectoryAgentNodeBean extends AbstractAgentNodeBean implements IDirectory, IMessageTransportDelegate,
    DirectoryAgentNodeBeanMBean {

  private long lastSend;

  private boolean                                    dump              = false;

  /**
   * Name for address-creation purposes. Will be added to the UUID of AgentNode to create the message box for the
   * directory. Example: "df@agentnode"
   */
  public static final String                         ADDRESS_NAME      = "df";

  /** Protocol for saying hello and still there to other agentnodes. */
  public static final String                         ALIVE             = ADDRESS_NAME + ":alive";

  /** Protocol to stop being present. */
  public static final String                         BYE               = ADDRESS_NAME + ":bye";

  /** Protocol to announce my actions. */
  public static final String                         ADVERTISE         = ADDRESS_NAME + ":advertise";

  /** Protocol to request actions of other agentnodes. */
  public static final String                         ALL               = ADDRESS_NAME + ":all";

  /**
   * Interval to send alive message to group. In milliseconds. Default is 2000.
   */
  private long                                       aliveInterval     = 2000;

  /** reference to the service Matcher if it exists */
  private IServiceMatcher                            serviceMatcher    = null;

  /** reference to the ontology storage if it exists */
  private IOntologyStorage                           ontologyStorage   = null;

  /**
   * Interval to send advertise message to group. In milliseconds. Should be greater than aliveInterval to avoid
   * spamming around. Default is 3 minutes.
   */
  private long                                       advertiseInterval = 10800;

  /** The UUID of this agentnode. */
  private String                                     myAgentNode;

  /** The inbox of this bean. */
  ICommunicationAddress                              myAddress;

  /** The group address of all directories. */
  ICommunicationAddress                              groupAddress;

  /** The communication handling for sending and receiving messages. */
  private MessageTransport                           messageTransport;

  // TODO JMX
  /** Stores (node-)local actions. */
  private Set<IActionDescription>                    localActions      = new HashSet<IActionDescription>();

  // TODO JMX
  /**
   * Stores remote actions. Key is the message box address of the node. Value is a set of action that are provided on that node.
   */
  private Hashtable<String, Set<IActionDescription>> remoteActions     = new Hashtable<String, Set<IActionDescription>>();

  // TODO JMX
  /** Stores (node-)local agents. Key is the agent identifier. */
  private Hashtable<String, IAgentDescription>       localAgents       = new Hashtable<String, IAgentDescription>();

  // TODO JMX
  /** Store remote agents. Key is the agent identifier. */
  private Hashtable<String, IAgentDescription>       remoteAgents      = new Hashtable<String, IAgentDescription>();

  // TODO JMX 
  /** Stores all known agentnodes. Key is the message box address of the node. */
  private Hashtable<String, AgentNodeDescription>    nodes             = new Hashtable<String, AgentNodeDescription>();

  /** Timer that schedules alive pings and advertisements. */
  private Timer                                      timer;

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

    myAddress = CommunicationAddressFactory.createMessageBoxAddress(ADDRESS_NAME + "@" + myAgentNode);
    log.info("myAddress=" + myAddress);
    groupAddress = CommunicationAddressFactory.createGroupAddress(ADDRESS_NAME + "@dfgroup");
    log.info("groupAddress=" + groupAddress);

    try {
      messageTransport.listen(myAddress, null);
      messageTransport.listen(groupAddress, null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // find serviceMatcherBean and ontologystorage
    this.serviceMatcher = this.agentNode.findAgentNodeBean(IServiceMatcher.class);
    this.ontologyStorage = this.agentNode.findAgentNodeBean(IOntologyStorage.class);

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
    timer = new Timer();
    timer.schedule(new AgentNodePinger(), aliveInterval, aliveInterval);
  }

  @Override
  public void doStop() throws Exception {
    super.doStop();

    timer.cancel();

    final JiacMessage byeMessage = new JiacMessage();
    byeMessage.setProtocol(BYE);
    sendMessage(byeMessage, groupAddress);

  }

  @Override
  public void doCleanup() throws Exception {
    super.doCleanup();

    this.serviceMatcher = null;
    this.ontologyStorage = null;

    try {
      messageTransport.stopListen(myAddress, null);
      messageTransport.stopListen(groupAddress, null);
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

    if (uuid.equals(myAgentNode)) {
      synchronized(localAgents) {
        localAgents.put(agentDescription.getAid(), agentDescription);
      }
    } else {
      synchronized(remoteAgents) {
        remoteAgents.put(agentDescription.getAid(), agentDescription);
      }
    }
    // dump("registerAgent " + agentDescription.getAid());
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

    synchronized(localAgents) {
      if(localAgents.containsKey(aid)) {
        localAgents.remove(aid);
      } else {
        synchronized(remoteAgents) {
          // TODO deregister actions
          remoteAgents.remove(aid);  
        }
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
    synchronized(localAgents) {
      for (String key : localAgents.keySet()) {
        final IAgentDescription agentDescription = localAgents.get(key);
        if (agentDescription.equals(template)) {
          return agentDescription;
        }
      }
    }
    synchronized(remoteAgents) {
      for (String key : remoteAgents.keySet()) {
        final IAgentDescription agentDescription = remoteAgents.get(key);
        if (agentDescription.equals(template)) {
          return agentDescription;
        }
      }
    }
    log.warn("Cannot find agent:\n" + template.toString());
    return null;
  }

  /**
   * This is inserted for search local agents only. <br>
   * reason: a (IAgentDescription) template with an agent node UUID didn't work. But I need in this case just local
   * agents. used template: <br>
   * <code>new AgentDescription(null, null, null, null, thisAgent.getAgentNode().getUUID())</code> hate mail to mib
   * 
   * @param template
   *          the template for searching local agents
   * @return description of all local agents, which are equal to the template
   */
  public List<IAgentDescription> searchAllLocalAgents(IAgentDescription template) {
    final List<IAgentDescription> agents = new ArrayList<IAgentDescription>();
    synchronized(localAgents) {
      for (String key : localAgents.keySet()) {
        final IAgentDescription agentDescription = localAgents.get(key);
        if (agentDescription.equals(template)) {
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
    synchronized(localAgents) {
      for (String key : localAgents.keySet()) {
        final IAgentDescription agentDescription = localAgents.get(key);
        if (agentDescription.equals(template)) {
          agents.add(agentDescription);
        }
      }
    }
    synchronized(remoteAgents) {
      for (String key : remoteAgents.keySet()) {
        final IAgentDescription agentDescription = remoteAgents.get(key);
        if (agentDescription.equals(template)) {
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
      uuid = actionDescription.getProviderDescription().getAgentNodeUUID();
    } catch (Exception e) {
      log.error("(" + e.getClass().getSimpleName() + ") Cannot register action!\n" + actionDescription.toString());
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
      synchronized(remoteActions) {
        if (!remoteActions.containsKey(ADDRESS_NAME + "@" + uuid)) {
          log.error("UUID unknown. Cannot register action!\n" + actionDescription.toString());
          return;
        }
        final Set<IActionDescription> actions = remoteActions.get(ADDRESS_NAME + "@" + uuid);
        final boolean success = actions.add(actionDescription);
        if (!success) {
          log.warn("Action to register already registered. Substituting with new action:\n"
              + actionDescription.toString());
          actions.remove(actionDescription);
          actions.add(actionDescription);
        }
      }
    }
    log.info("Registered action:\n" + actionDescription.toString());
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
      uuid = actionDescription.getProviderDescription().getAgentNodeUUID();
    } catch (Exception e) {
      log.error("Cannot deregister action!\n" + actionDescription.toString());
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
          log.warn("Cannot deregister action! Unknown action '" + actionDescription.getName() + "'!");
        }
      }
    } else {
      synchronized(remoteActions) {
        final Set<IActionDescription> ad = remoteActions.get(ADDRESS_NAME + "@" + uuid);
        if (ad == null) {
          log.warn("Cannot deregister action! Unknown UUID: " + uuid);
          return;
        }
        if (!ad.remove(actionDescription)) {
          log.warn("Cannot deregister action! Unknown action:\n" + actionDescription.toString());
        }
      }
    }
    dump("deregisterAction " + actionDescription.toString());
  }

  @Override
  public void modifyAction(IActionDescription oldDescription, IActionDescription newDescription) {
    if (oldDescription == null || newDescription == null) {
      log.error("Cannot modify action (old:\n" + oldDescription + "\nnew:\n" + newDescription);
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
      log.error("Provider information incomplete. Missing UUID. Cannot modify action!\n" + oldDescription.toString());
      return;
    }

    if (uuid.equals(myAgentNode)) {
      synchronized (localActions) {
        if (!localActions.remove(oldDescription)) {
          log.warn("Cannot deregister action:\n" + oldDescription.toString());
        }
      }
    } else {
      synchronized(remoteActions) {
        final Set<IActionDescription> ad = remoteActions.get(ADDRESS_NAME + "@" + uuid);
        if (ad == null) {
          log.warn("Cannot deregister action! Unknown UUID: " + uuid);
        } else {
          if (!ad.remove(oldDescription)) {
            log.warn("Cannot deregister action! Unknown action:\n" + oldDescription.toString());
          }
        }
      }
    }

    registerAction(newDescription);
  }

  @Override
  public IActionDescription searchAction(IActionDescription template) {
    if (template == null) {
      log.error("Cannot find action: null!");
      return null;
    }

    // use Matcher for matching if possible
    if (template instanceof IServiceDescription) {
      if (this.serviceMatcher != null) {
        final ArrayList<IServiceDescription> serviceDescList = findAllComplexServices();
        final IServiceDescription matcherResult = this.serviceMatcher.findBestMatch((IServiceDescription) template,
            serviceDescList);

        if ((matcherResult != null)) {
          return matcherResult;
        } else {
          log.warn("Matcher found no result, trying normal template matching...");
        }

      } else {
        log.error("This agentnode has no servicematcher - no complex matching possible!");
      }
    }

    synchronized (localActions) {
      if (localActions.contains(template)) {
        for (IActionDescription ad : localActions) {
          if (ad.equals(template)) {
            return ad;
          }
        }
      }
    }
    synchronized(remoteActions) {
      for (String nodeAddress : remoteActions.keySet()) {
        final Set<IActionDescription> adset = remoteActions.get(nodeAddress);
        if (adset.contains(template)) {
          for (IActionDescription ad : adset) {
            if (ad.equals(template)) {
              return ad;
            }
          }
        }
      }
    }
    log.warn("Cannot find action:\n" + template);
    return null;
  }

  @Override
  public List<IActionDescription> searchAllActions(IActionDescription template) {
    final ArrayList<IActionDescription> actions = new ArrayList<IActionDescription>();

    if (template == null) {
      log.error("Cannot find action: null!");
      return actions;
    }

    // use Matcher for matching if possible
    if (template instanceof IServiceDescription) {
      if (this.serviceMatcher != null) {
        final ArrayList<IServiceDescription> serviceDescList = findAllComplexServices();
        final ArrayList<? extends IActionDescription> matcherResults = this.serviceMatcher.findAllMatches(
            (IServiceDescription) template, serviceDescList);

        if ((matcherResults != null) && (matcherResults.size() > 0)) {
          actions.addAll(matcherResults);
        } else {
          log.warn("Matcher found no result, trying normal template matching...");
        }

      } else {
        log.error("This agentnode has no servicematcher - no complex matching possible!");
      }
    }

    synchronized (localActions) {
      if (localActions.contains(template)) {
        for (IActionDescription actionDescription : localActions) {
          if (actionDescription.equals(template)) {
            actions.add(actionDescription);
          }
        }
      }
    }
    synchronized(remoteActions) {
      for (String nodeAddress : remoteActions.keySet()) {
        final Set<IActionDescription> adset = remoteActions.get(nodeAddress);
        if (adset.contains(template)) {
          for (IActionDescription ad : adset) {
            if (ad.equals(template)) {
              actions.add(ad);
            }
          }
        }
      }
    }
    return actions;
  }

  // ######################################
  // ILifeCycleListener
  // ######################################

  @Override
  public void onEvent(LifecycleEvent event) {
    if (log.isDebugEnabled()) {
      log.debug(myAgentNode + "::" + event.getSource() + " State: " + event.getState());
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
              final List<? extends IActionDescription> actions = effector.getActions();
              if (actions != null && !actions.isEmpty()) {
                for (IActionDescription action : actions) {
                  if ((action.getScope() != null) && action.getScope().contains(ActionScope.NODE)) {
                    action.setProviderDescription(agent.getAgentDescription());
                    registerAction(action);
                  }
                }
              } else {
                log.warn("Effector: " + ab.getBeanName() + " has no Actions?!?");
              }
            }
          }
        } else {
          log.warn("Agent: " + agent.getAgentId() + " has no AgentBeans?!?");
        }
      }
    }

    else if (event.getState() == LifecycleStates.STOPPING) {
      if (event.getSource() instanceof Agent) {
        final String agent = ((Agent) event.getSource()).getAgentId();

        final List<IActionDescription> actions = new ArrayList<IActionDescription>();
        synchronized (localActions) {
          for (IActionDescription action : localActions) {
            if (action.getProviderDescription().getAid().equals(agent)) {
              actions.add(action);
            }
          }
          for (IActionDescription action : actions) {
            localActions.remove(action);
          }
        }

        deregisterAgent(agent);
      }
    }
  }

  // ######################################
  // IMessageTransportDelegate
  // ######################################

  @Override
  public void onMessage(MessageTransport source, IJiacMessage message, ICommunicationAddress at) {

    final ICommunicationAddress senderAddress = message.getSender();

    // own message, do nothing
    if (senderAddress == null || senderAddress.equals(myAddress)) {
      return;
    }

    final String protocol = message.getProtocol();
    if (log.isDebugEnabled()) {
      log.debug("sender=" + senderAddress + " protocol=" + protocol);
    }

    if (protocol.equals(ALIVE)) {
    	synchronized(nodes) {
    		refreshAgentNode(senderAddress);
    	}
      return;
    }

    else if (protocol.equals(BYE)) {
    	synchronized(nodes) {
    		final String nodeAddress = senderAddress.getName();
    		if (nodes.containsKey(nodeAddress)) {
    		  synchronized(remoteActions) {    		  
      			log.warn(nodeAddress + " says bye");
      			removeRemoteAgentOfNode(nodeAddress);
      			remoteActions.remove(nodeAddress);
      			nodes.remove(nodeAddress);
    		  }
    		}
    		dump(nodeAddress + " says bye");
    	}
    	return;
    }

    else if (protocol.equals(ADVERTISE)) {
    	synchronized(nodes) {
    		// refresh agent nodes
    		final Set<JMXServiceURL> connectors = ((Advertisement) message.getPayload()).getJmxURLs();
    		if (connectors != null) {
    			refreshAgentNode(senderAddress, connectors);
    		}
    		else {
    			refreshAgentNode(senderAddress);
    		}

    		// refresh remote actions
    		final Set<IActionDescription> actions = ((Advertisement) message.getPayload()).getActions();
    		if (log.isDebugEnabled()) {
    			log.debug("receive ADVERTISE: " + actions.size());
    		}

    		final Set<IActionDescription> receivedActions = new HashSet<IActionDescription>();
    		for (IActionDescription iad : actions) {

    			if (iad instanceof IServiceDescription) {
    				// special handling for service descriptions
    				final IServiceDescription isd = (IServiceDescription) iad;
    				if (isd.getOntologySource() != null) {
    					IServiceDescription tempService = null;
    					try {
    						tempService = ontologyStorage.deserializeServiceDescription(isd.getOntologySource());

    						if (tempService != null) {
    							// these fields are not filled by the
    							// ontologyStorage, so do it by hand
    							((Action) tempService).setInputTypes(isd.getInputTypes());
    							((Action) tempService).setResultTypes(isd.getResultTypes());
    							((Action) tempService).setProviderDescription(isd.getProviderDescription());
    							((Action) tempService).setScope(isd.getScope());
    						}
    					} catch (Exception ex) {
    						log.error("Caught exception when reading service description: ", ex);
    					}

    					if (tempService != null) {
    						receivedActions.add(tempService);
    					}
    				}

    			} else {
    				// simply add normal actions
    				receivedActions.add(iad);
    			}
    		}

    		remoteActions.put(senderAddress.getName(), receivedActions);

    		// refresh remote agents
    		final Hashtable<String, IAgentDescription> agents = ((Advertisement) message.getPayload()).getAgents();
    		for (IAgentDescription agent : agents.values()) {
    			registerAgent(agent);
    		}

    		dump("ADVERTISE " + senderAddress.getName());
    	}
    }

    else if (protocol.equals(ALL)) {
      sendAdvertisement(senderAddress);
    }
  }

  /**
   * This returns an ICommunicationAddress of an agent node that is known or <code>null</code>, if it is unknown.
   * <br>
   * insert for agent migration (hate mails to mib)
   * 
   * @param uuidOfNode
   *          the UUID of the agent node
   * @return the communication address of the given node, or <code>null</code> if the node is unknown
   */
  public ICommunicationAddress getCommunicationAddressOfANode(String uuidOfNode) {

    if (uuidOfNode == null || uuidOfNode.equals("")) {
      return null;
    }

    ICommunicationAddress ret = null;
    synchronized(nodes) {
      if (nodes.containsKey(ADDRESS_NAME + "@" + uuidOfNode)) {
        final AgentNodeDescription nodeDescription = nodes.get(ADDRESS_NAME + "@" + uuidOfNode);
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
   * @return unmodifiable set with message box address of all known agent nodes.
   */
  public Set<String> getAllKnownAgentNodes() {
    synchronized(nodes) {
      return Collections.unmodifiableSet(nodes.keySet());
    }
  }

  private void refreshAgentNode(ICommunicationAddress node) {
    final String nodeAddress = node.getName();
    synchronized(nodes) {
      if (nodes.containsKey(nodeAddress)) {
      	final long interval = System.currentTimeMillis() - nodes.get(nodeAddress).getAlive();
      	if (interval > 2*aliveInterval) {
      		log.warn("Measured interval of receiving alive message from " + nodeAddress + ": " + interval);
      	}
        nodes.get(nodeAddress).setAlive(System.currentTimeMillis());
      } else {
      	log.warn("New known node " + nodeAddress);
        final AgentNodeDescription description = new AgentNodeDescription(node, System.currentTimeMillis());
        nodes.put(nodeAddress, description);
      }
    }
  }

  private void refreshAgentNode(ICommunicationAddress node, Set<JMXServiceURL> jmxConnectors) {
    final String nodeAddress = node.getName();
    refreshAgentNode(node);
    synchronized(nodes) {
      nodes.get(nodeAddress).setJmxURLs(jmxConnectors);
    }
  }

  @Override
  public Log getLog(String extension) {
    // TODO Creating a method within the AgentNode to get a log for
    // AgentNodeBeans and use it here
    return LogFactory.getLog(getClass().getName() + "." + extension);
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
   *          the communication handler to set
   */
  public void setMessageTransport(MessageTransport newMessageTransport) {
    messageTransport = newMessageTransport;
  }

  /**
   * Returns the time between to pings.
   * 
   * @return the time between to pings
   */
  public long getAliveInterval() {
    return aliveInterval;
  }

  /**
   * Set the time how often the directory will send a ping.
   * 
   * @param interval
   *          the time between pings
   */
  public void setAliveInterval(long interval) {
    this.aliveInterval = interval;
  }

  /**
   * {@inheritDoc}
   */
  public long getAdvertiseInterval() {
    return advertiseInterval;
  }

  /**
   * {@inheritDoc}
   */
  public void setAdvertiseInterval(long interval) {
    advertiseInterval = interval;
  }

  /**
   * Collects all complex services, i.e. entries with an IServiceDescription, from both, the local and the remote
   * services list.
   * 
   * @return a list of all IServiceDescriptions from this directory, containing both, local and remote entries.
   */
  private ArrayList<IServiceDescription> findAllComplexServices() {
    final ArrayList<IServiceDescription> ret = new ArrayList<IServiceDescription>();

    // find serviceDescriptions in local actions
    synchronized (localActions) {    
      for (IActionDescription localAct : localActions) {
        if (localAct instanceof IServiceDescription) {
          ret.add((IServiceDescription) localAct);
        }
      }
    }

    // find serviceDescriptions in remote Actions
    synchronized(remoteActions) {
      for (String nodeAddress : remoteActions.keySet()) {
        final Set<IActionDescription> remoteActSet = remoteActions.get(nodeAddress);
        for (IActionDescription remoteAct : remoteActSet) {
          if (remoteAct instanceof IServiceDescription) {
            ret.add((IServiceDescription) remoteAct);
          }
        }
      }
    }

    return ret;
  }

  // #########################################
  // Communication
  // #########################################

  private void sendMessage(JiacMessage message, ICommunicationAddress address) {
    message.setSender(myAddress);
    message.setHeader("UUID", this.agentNode.getUUID());
    try {
      messageTransport.send(message, address, 0);
      final long interval = System.currentTimeMillis()-lastSend;
      if (interval > 2*aliveInterval) {
    	  log.warn("Measured interval of sending alive message: " + interval);
      }
      lastSend = System.currentTimeMillis();      
    } catch (Exception e) {
      log.error("sendMessage failed. Message:\n" + message.toString(), e);
    }
  }

  /**
   * Send all actions, agents and JMX URLs this agent node is providing.
   */
  private void sendAdvertisement(ICommunicationAddress destination) {
    final JiacMessage adMessage = new JiacMessage();
    adMessage.setProtocol(ADVERTISE);
    // TODO filter global actions

    // find OWL-S ServiceDescriptions and deserialize them by hand
    final Set<IActionDescription> advActions = new HashSet<IActionDescription>();
    synchronized (localActions) {
      for (IActionDescription iad : localActions) {
        if (iad.getScope() != ActionScope.GLOBAL) {
      	  continue;
        }
        if (iad instanceof IServiceDescription) {
          final IServiceDescription isd = (IServiceDescription) iad;
          isd.setOntologySource(ontologyStorage.serializeServiceDescription(isd));
        }
        advActions.add(iad);
      }
    }

    Hashtable<String, IAgentDescription> advAgents = new Hashtable<String, IAgentDescription>();
    synchronized(localAgents) {
      advAgents.putAll(localAgents);
    }

    final Advertisement ad = new Advertisement(advAgents, advActions);
  
    if (agentNode.getJmxConnectors() != null) {
      ad.setJmxURLs(agentNode.getJmxURLs());
    }
    adMessage.setPayload(ad);
    

    // debug
    final Advertisement payload = (Advertisement) adMessage.getPayload();
    if (log.isDebugEnabled()) {
      log.debug("sendAdvertisement: jmxURLs=" + payload.getJmxURLs().size() + " agents=" + payload.getAgents().size()
          + " actions=" + payload.getActions().size());
    }

    sendMessage(adMessage, destination);
    
  }

  class AgentNodePinger extends TimerTask {

    private long counter = advertiseInterval;

    @Override
    public void run() {
      if (counter < advertiseInterval) {
        final JiacMessage aliveMessage = new JiacMessage();
        aliveMessage.setProtocol(ALIVE);
        sendMessage(aliveMessage, groupAddress);
        if (log.isDebugEnabled()) {
          log.debug("Alive Ping: "+System.currentTimeMillis()+" with counter: "+counter);
        }
        counter += aliveInterval;
      } else {
        counter = 0;
        sendAdvertisement(groupAddress);
      }

      // remove dead nodes
      synchronized(nodes) {
    	  final Set<String> deadNodes = new HashSet<String>();
    	  for (String key : nodes.keySet()) {
    		  if (nodes.get(key).getAlive() < (System.currentTimeMillis() - 5 * aliveInterval)) {
    			  deadNodes.add(key);
    		  }
    	  }
    	  if (deadNodes.size() > 0) {
    		  log.warn("Forget nodes due to missing alive message:\n");
    		  synchronized(remoteActions) {
      		  for (String nodeAddress : deadNodes) {
      			  log.warn("\t" + nodeAddress);
      			  remoteActions.remove(nodeAddress);
      			  removeRemoteAgentOfNode(nodeAddress);
      			  nodes.remove(nodeAddress);
      		  }
    		  }
    	  }
      }
    }
  }

  private void removeRemoteAgentOfNode(String nodeAddress) {
    final ArrayList<String> keysToRemove = new ArrayList<String>();
    synchronized(remoteAgents) {
      for (String key : remoteAgents.keySet()) {
        final IAgentDescription agent = remoteAgents.get(key);
        if (nodeAddress.equals(ADDRESS_NAME + "@" + agent.getAgentNodeUUID())) {
          keysToRemove.add(key);
        }
      }
      for (String key : keysToRemove) {
        remoteAgents.remove(key);
      }
    }
  }

  // ######################################
  // Debug
  // ######################################

  private void dump(String cause) {
    if (dump) {
      System.out.println("\n####Dump " + myAgentNode + " " + cause);
      System.out.println("Registered local agents:   " + localAgents.size());
      System.out.println("Registered local actions:  " + localActions.size());
      int actions = 0;
      for (String key : remoteActions.keySet()) {
        actions += remoteActions.get(key).size();
      }
      System.out.println("Registered remote agents:  " + remoteAgents.size());
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
    synchronized(nodes) {
      if (nodes.isEmpty()) {
        return null;
      }
  
      final Set<Map.Entry<String, AgentNodeDescription>> entries = nodes.entrySet();
      try {
        final String[] itemNames = new String[] { "UUID", "description" };
        final CompositeType nodeType = new CompositeType(entries.iterator().next().getClass().getName(),
            "agent node description corresponding to an agent node UUID", itemNames, itemNames, new OpenType<?>[] {
                SimpleType.STRING, entries.iterator().next().getValue().getDescriptionType() });
        final TabularType type = new TabularType(nodes.getClass().getName(), "node descriptions stored in the directory",
            nodeType, new String[] { "UUID" });
        final TabularData data = new TabularDataSupport(type);
        for (Map.Entry<String, AgentNodeDescription> node : entries) {
          final CompositeData nodeData = new CompositeDataSupport(nodeType, itemNames, new Object[] { node.getKey(),
              node.getValue().getDescription() });
          data.put(nodeData);
        }
        return data;
      } catch (OpenDataException e) {
        e.printStackTrace();
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
        final TabularType type = new TabularType(localActions.getClass().getName(),
            "local actions stored in the directory", (CompositeType) localActions.iterator().next().getDescriptionType(),
            new String[] { IActionDescription.ITEMNAME_NAME, IActionDescription.ITEMNAME_INPUTTYPES,
                IActionDescription.ITEMNAME_RESULTTYPES, IActionDescription.ITEMNAME_AGENT });
        final TabularData data = new TabularDataSupport(type);
        for (IActionDescription action : localActions) {
          data.put((CompositeData) action.getDescription());
        }
        return data;
      } catch (OpenDataException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TabularData getLocalAgents() {
    synchronized(localAgents) {
      if (localAgents.isEmpty()) {
        return null;
      }
  
      final Set<Map.Entry<String, IAgentDescription>> entries = localAgents.entrySet();
      try {
        final String[] itemNames = new String[] { "Agent ID", "description" };
        final CompositeType agentType = new CompositeType(entries.iterator().next().getClass().getName(),
            "local agent description corresponding to an agent id", itemNames, itemNames, new OpenType<?>[] {
                SimpleType.STRING, entries.iterator().next().getValue().getDescriptionType() });
        final TabularType type = new TabularType(localAgents.getClass().getName(),
            "local agent descriptions stored in the directory", agentType, new String[] { "Agent ID" });
        final TabularData data = new TabularDataSupport(type);
        for (Map.Entry<String, IAgentDescription> agent : entries) {
          final CompositeData agentData = new CompositeDataSupport(agentType, itemNames, new Object[] { agent.getKey(),
              agent.getValue().getDescription() });
          data.put(agentData);
        }
        return data;
      } catch (OpenDataException e) {
        e.printStackTrace();
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

    final Set<Map.Entry<String, Set<IActionDescription>>> entries = remoteActions.entrySet();
    try {
      final String[] itemNames = new String[] { "agent node UUID", "remote actions" };
      final CompositeType entryType = new CompositeType(entries.iterator().next().getClass().getName(),
          "remote action descriptions corresponding to an agent node UUID", itemNames, itemNames, new OpenType<?>[] {
              SimpleType.STRING, ArrayType.getArrayType(new Action().getDescriptionType()) });
      final TabularType type = new TabularType(remoteActions.getClass().getName(),
          "remote action descriptions stored in the directory", entryType, new String[] { "agent node UUID" });

      final TabularData data = new TabularDataSupport(type);
      for (Map.Entry<String, Set<IActionDescription>> entry : entries) {
        int i = 0;
        final Set<IActionDescription> values = entry.getValue();
        final CompositeData[] actions = new CompositeData[values.size()];
        for (IActionDescription action : values) {
          actions[i++] = (CompositeData) action.getDescription();
        }
        final CompositeData entryData = new CompositeDataSupport(entryType, itemNames, new Object[] { entry.getKey(),
            actions });
        data.put(entryData);
      }
      return data;
    } catch (OpenDataException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TabularData getRemoteAgents() {
    synchronized(remoteAgents) {
      if (remoteAgents.isEmpty()) {
        return null;
      }
  
      final Set<Map.Entry<String, IAgentDescription>> entries = remoteAgents.entrySet();
      try {
        final String[] itemNames = new String[] { "Agent ID", "description" };
        final CompositeType agentType = new CompositeType(entries.iterator().next().getClass().getName(),
            "remote agent description corresponding to an agent id", itemNames, itemNames, new OpenType<?>[] {
                SimpleType.STRING, entries.iterator().next().getValue().getDescriptionType() });
        final TabularType type = new TabularType(remoteAgents.getClass().getName(),
            "remote agent descriptions stored in the directory", agentType, new String[] { "Agent ID" });
        final TabularData data = new TabularDataSupport(type);
        for (Map.Entry<String, IAgentDescription> agent : entries) {
          final CompositeData agentData = new CompositeDataSupport(agentType, itemNames, new Object[] { agent.getKey(),
              agent.getValue().getDescription() });
          data.put(agentData);
        }
        return data;
      } catch (OpenDataException e) {
        e.printStackTrace();
        return null;
      }
    }
  }
}
/*
 * TODO remove* checken bzgl. ConcurrentModificationException 
 * TODO advertise agents ist im Moment etwas stiefmuetterlich behandelt 
 * TODO ausfuehrliches Logging einbauen?!?
 */
