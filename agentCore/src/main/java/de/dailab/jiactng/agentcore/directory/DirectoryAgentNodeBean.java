package de.dailab.jiactng.agentcore.directory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNodeBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.broker.ActiveMQBroker;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.ontology.AgentNodeDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class DirectoryAgentNodeBean extends AbstractAgentNodeBean implements
		IDirectory, IMessageTransportDelegate {

	/** 
	 * Name for address-creation purposes. Will be added to the UUID
	 * of AgentNode to create the message box for the directory.
	 * Example: "df@agentnode"  
	 */
	public final static String ADDRESS_NAME = "df";
	
	/** Protocol for saying hello and still there to other agentnodes. */
	public final static String ALIVE = ADDRESS_NAME + ":alive";
	
	/** Protocol to stop being present. */
	public final static String BYE = ADDRESS_NAME + ":bye";
	
	/** Protocol to announce my actions. */
	public final static String ADVERTISE = ADDRESS_NAME + ":advertise";
	
	/** Protocol to request actions of other agentnodes. */
	public final static String ALL = ADDRESS_NAME + ":all";
	
	/** Interval to send alive message to group. In milliseconds. Default is 2000.*/
	private long aliveInterval = 2000;
	
	/** 
	 * Interval to send advertise message to group. In milliseconds.
	 * Should be greater than aliveInterval to avoid spamming around. 
	 * Default is 3 minutes.
	 */
	private long advertiseInterval = 10800; 
	
	/** The UUID of this agentnode. */
	private String myAgentNode;
	
	/** The inbox of this bean. */
	ICommunicationAddress myAddress;
	
	/** The group address of all directories. */
	ICommunicationAddress groupAddress;
	
	/** The communication handling for sending and receiving messages.*/
	private MessageTransport messageTransport;
	
	/** Stores (node-)local actions.*/
	private Set<IActionDescription> localActions = new HashSet<IActionDescription>();
	
	/** 
	 * Stores remote actions. Key is the UUID of the node. 
	 * Value is a set of action that are provided on that node.
	 */
	private Hashtable<String, Set<IActionDescription>> remoteActions = new Hashtable<String, Set<IActionDescription>>();
	
	/** Stores (node-)local agents. Key is the agent identifier. */
	private Hashtable<String, IAgentDescription> localAgents = new Hashtable<String, IAgentDescription>();
	
	/** Store remote agents. Key is the agent identifier.*/
	private Hashtable<String, IAgentDescription> remoteAgents = new Hashtable<String, IAgentDescription>();
	
	/** Stores all known agentnodes. Key is the UUID of the node.*/
	private Hashtable<String, AgentNodeDescription> nodes = new Hashtable<String, AgentNodeDescription>();
	
	
	/** Timer that schedules alive pings and advertisements. */
	private Timer timer;

	
//######################################
//		AbstractLifecycle 
//######################################	
		
	@Override
	public void doInit() throws Exception {
		super.doInit();
		myAgentNode  = this.agentNode.getUUID();
		
		messageTransport.setDefaultDelegate(this);
		try {
			messageTransport.doInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		myAddress = CommunicationAddressFactory.createMessageBoxAddress(ADDRESS_NAME + "@" + myAgentNode);
		System.out.println("myAddress=" + myAddress);
		groupAddress = CommunicationAddressFactory.createGroupAddress(ADDRESS_NAME + "@dfgroup");
		System.out.println("groupAddress=" + groupAddress);
		
		try {
			messageTransport.listen(myAddress, null);
			messageTransport.listen(groupAddress, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void doStart() throws Exception {
		super.doStart();
		timer = new Timer();
		timer.schedule(new AgentNodePinger(), aliveInterval, aliveInterval);
	}

	@Override
	public void doStop() throws Exception {
		super.doStop();

		timer.cancel();

		JiacMessage byeMessage = new JiacMessage();
		byeMessage.setProtocol(BYE);
		sendMessage(byeMessage, groupAddress);

	}

	@Override
	public void doCleanup() throws Exception {
		super.doCleanup();
		
		try {
			messageTransport.stopListen(myAddress, null);
			messageTransport.stopListen(groupAddress, null);
			messageTransport.doCleanup();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

//######################################
//		WHITE PAGES 
//######################################	

	
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
			localAgents.put(agentDescription.getAid(), agentDescription);
		} else {
			remoteAgents.put(agentDescription.getAid(), agentDescription);
		}
		dump("registerAgent " + agentDescription.getAid());
	}

	@Override
	public void deregisterAgent(String aid) {
		if (aid == null) {
			log.error("Cannot deregister agent: null!");
			return;
		}
		
		Object agent = localAgents.remove(aid);
		if (agent == null) {
			remoteAgents.remove(aid);
			//TODO deregister actions
		}
		dump("deregisterAgent " + aid);
	}

	@Override
	public void modifyAgent(IAgentDescription agentDescription) {
		if (agentDescription == null) {
			log.error("Cannot modify agent: null!");
			return;
		}
		registerAgent(agentDescription);
	}

	@Override
	public IAgentDescription searchAgent(IAgentDescription template) {
		if (template == null) {
			log.error("Cannot find agent: null!");
			return null;
		}
		for (String key:localAgents.keySet()) {
			IAgentDescription agentDescription = localAgents.get(key);
			if (agentDescription.equals(template)) {
				return agentDescription;
			}
		}
		for (String key:remoteAgents.keySet()) {
			IAgentDescription agentDescription = remoteAgents.get(key);
			if (agentDescription.equals(template)) {
				return agentDescription;
			}
		}
		log.warn("Cannot find agent:\n" + template.toString());
		return null;
	}

	@Override
	public List<IAgentDescription> searchAllAgents(IAgentDescription template) {
		List<IAgentDescription> agents = new ArrayList<IAgentDescription>();
		for (String key:localAgents.keySet()) {
			IAgentDescription agentDescription = localAgents.get(key);
			if (agentDescription.equals(template)) {
				agents.add(agentDescription);
			}
		}
		for (String key:remoteAgents.keySet()) {
			IAgentDescription agentDescription = remoteAgents.get(key);
			if (agentDescription.equals(template)) {
				agents.add(agentDescription);
			}
		}
		return agents;
	}


//######################################
//	YELLOW PAGES 
//######################################	

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
			log.error("Cannot register action!\n" + actionDescription.toString());
			return;
		}
		if (uuid == null) {
			log.error("Provider information incomplete. Missing UUID. Cannot register action!\n" + actionDescription.toString());
			return;
		}
		
		if (myAgentNode.equals(uuid)) {
			boolean success = localActions.add(actionDescription);
			if (!success) {
				log.warn("Action to register already registered. Substituting with new action:\n" + actionDescription.toString());
				localActions.remove(actionDescription);
				localActions.add(actionDescription);
			}
		} 
		
		else {
			if (!remoteActions.containsKey(uuid)) {
				log.error("UUID unknown. Cannot register action!\n" + actionDescription.toString());
				return;
			}
			Set<IActionDescription> actions = remoteActions.get(uuid);
			boolean success = actions.add(actionDescription);
			if (!success) {
				log.warn("Action to register already registered. Substituting with new action:\n" + actionDescription.toString());
				actions.remove(actionDescription);
				actions.add(actionDescription);
			}
		}
		log.info("Registered action:\n" + actionDescription.toString());
		dump("registerAction " + actionDescription.getName());
		
		//TODO register agents that provides this action
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
			log.error("Provider information incomplete. Missing UUID. Cannot deregister action!\n" + actionDescription.toString());
			return;
		}
		
		if (uuid.equals(myAgentNode)) {
			if (!localActions.remove(actionDescription)) {
				log.warn("Cannot deregister action! Unknown action!");
			}
		} else {
			Set<IActionDescription> ad = remoteActions.get(uuid);
			if (ad == null) {
				log.warn("Cannot deregister action! Unknown UUID: " + uuid);
				return;
			}
			if (!ad.remove(actionDescription)) {
				log.warn("Cannot deregister action! Unknown action:\n" + actionDescription.toString());
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
			if (!localActions.remove(oldDescription)) {
				log.warn("Cannot deregister action:\n" + oldDescription.toString());
			}
		} else {
			Set<IActionDescription> ad = remoteActions.get(uuid);
			if (ad == null) {
				log.warn("Cannot deregister action! Unknown UUID: " + uuid);
			} else {
				if (!ad.remove(oldDescription)) {
					log.warn("Cannot deregister action! Unknown action:\n" + oldDescription.toString());
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
		
		if (localActions.contains(template)) {
			for (IActionDescription ad:localActions) {
				if (ad.equals(template)) {
					return ad;
				}
			}
		}
		for (String key:remoteActions.keySet()) {
			Set<IActionDescription> adset = remoteActions.get(key);
			if (adset.contains(template)) {
				for (IActionDescription ad:adset) {
					if (ad.equals(template)) {
						return ad;
					}
				}
			}
		}
		log.warn("Cannot find action:\n" + template);
		return null;
	}

	@Override
	public List<IActionDescription> searchAllActions(IActionDescription template) {
		ArrayList<IActionDescription> actions = new ArrayList<IActionDescription>();

		if (template == null) {
			log.error("Cannot find action: null!");
			return actions;
		}

		if (localActions.contains(template)) {
			for (IActionDescription actionDescription:localActions) {
				if (actionDescription.equals(template)) {
					actions.add(actionDescription);
				}
			}
		}
		for (String key:remoteActions.keySet()) {
			Set<IActionDescription> adset = remoteActions.get(key);
			if (adset.contains(template)) {
				for (IActionDescription ad:adset) {
					if (ad.equals(template)) {
						actions.add(ad);
					}
				}
			}
		}
		return actions;
	}

//######################################
//	ILifeCycleListener
//######################################	

	@Override
	public void onEvent(LifecycleEvent event) {
		log.debug(myAgentNode + "::" + event.getSource() + " State: " + event.getState());
		
		if (event.getState() == LifecycleStates.INITIALIZED) {
			if (event.getSource() instanceof Agent) {
				Agent agent = (Agent)event.getSource();
				registerAgent(agent.getAgentDescription());

				//register actions
				List<IAgentBean> beans = agent.getAgentBeans();
				if (beans != null && !beans.isEmpty()) {
					for (IAgentBean ab:beans) {
						if (ab instanceof IEffector) {
							IEffector effector = (IEffector)ab;
							List<? extends Action> actions = effector.getActions();
							if (actions != null && !actions.isEmpty()) {
								for (Action action:actions) {
									if (action.getScope().contains(ActionScope.NODE)) {
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
				String agent = ((Agent)event.getSource()).getAgentId();
				
				List<IActionDescription> actions = new ArrayList<IActionDescription>();
				for (IActionDescription action:localActions) {
					if (action.getProviderDescription().getAid().equals(agent)) {
						actions.add(action);
					}
				}
				for (IActionDescription action:actions) {
					localActions.remove(action);
				}
				
				deregisterAgent(agent);
			}
		}
	}

//######################################
//	IMessageTransportDelegate
//######################################	

	@Override
	public void onMessage(MessageTransport source, IJiacMessage message,
			ICommunicationAddress at) {
		
		ICommunicationAddress senderAddress = message.getSender();
		
		//own message, do nothing
		if (senderAddress.equals(myAddress)) {
			return;
		}
		
		String protocol = message.getProtocol();
		System.out.println("sender=" + senderAddress + " protocol=" + protocol);

		if (protocol.equals(ALIVE)) {
			refreshAgentNode(senderAddress);
			return;
		}
		
		else if (protocol.equals(BYE)) {
			String uuid = senderAddress.getName();
			if (nodes.containsKey(uuid)) {
				//TODO remove its agents
				remoteActions.remove(uuid);
				nodes.remove(uuid);
			}
			dump(uuid + " says bye");
			return;
		}
		
		else if (protocol.equals(ADVERTISE)) {
			refreshAgentNode(senderAddress);
			Set<IActionDescription> actions = ((Advertisement)message.getPayload()).getActions();
			System.out.println("receive ADVERTISE: " + actions.size());
			remoteActions.put(senderAddress.getName(), actions);
			dump("ADVERTISE " + senderAddress.getName());
		}
		
		else if (protocol.equals(ALL)) {
			sendAdvertisement(senderAddress);
		}
	}
	
	private void refreshAgentNode(ICommunicationAddress node) {
		String uuid = node.getName();
		if (nodes.containsKey(uuid)) {
			nodes.get(uuid).setAlive(System.currentTimeMillis());
		} else {
			AgentNodeDescription description = new AgentNodeDescription(node, System.currentTimeMillis());
			nodes.put(uuid, description);
		}
	}

	@Override
	public Log getLog(String extension) {
		// TODO Creating a method within the AgentNode to get a log for AgentNodeBeans and use it here
		return LogFactory.getLog(getClass().getName() + "." + extension);
	}

	@Override
	public void onAsynchronousException(MessageTransport source, Exception e) {
		e.printStackTrace();
	}

//######################################
//	Getter/Setter
//######################################	


	/**
	 * Returns the communication handler.
	 * @return the communication handler
	 */
	public MessageTransport getMessageTransport() {
		return messageTransport;
	}

	/**
	 * Sets the communication handler.
	 * @param messageTransport the communication handler to set
	 */
	public void setMessageTransport(MessageTransport messageTransport) {
		this.messageTransport = messageTransport;
	}

	/**
	 * Returns the time between to pings.
	 * @return the time between to pings
	 */
	public long getAliveInterval() {
		return aliveInterval;
	}
	
	/**
	 * Set the time how often the directory will send a ping.
	 * @param interval the time between pings
	 */
	public void setAliveInterval(long interval) {
		this.aliveInterval = interval;
	}
	
//#########################################
// Communication	
//#########################################

	private void sendMessage(JiacMessage message, ICommunicationAddress address){
		message.setSender(myAddress);
		message.setHeader("UUID", this.agentNode.getUUID());
		try {
			messageTransport.send(message, address);
		} catch (Exception e) {
			log.error("sendMessage failed. Message:\n" + message.toString(), e);
		}
	}
	
	/**
	 * Send all actions this node is providing.
	 */
	private void sendAdvertisement(ICommunicationAddress destination) {
		JiacMessage adMessage = new JiacMessage();
		adMessage.setSender(myAddress);
		adMessage.setHeader("UUID", this.agentNode.getUUID());
		adMessage.setProtocol(ADVERTISE);
		//TODO filter global actions
		adMessage.setPayload(new Advertisement(localActions));
		
		//debug
		Set<IActionDescription> payloadActions = ((Advertisement)adMessage.getPayload()).getActions();
		System.out.println("sendAdvertisement: " + payloadActions.size());
		
		try {
			messageTransport.send(adMessage, destination);
		} catch (Exception e) {
			log.error("sendMessage failed. Message:\n" + adMessage.toString(), e);
		}
		
	}
	
	class AgentNodePinger extends TimerTask {
		
		private long counter = 0;

		@Override
		public void run() {
			if (counter < advertiseInterval) {
				JiacMessage aliveMessage = new JiacMessage();
				aliveMessage.setProtocol(ALIVE);
				sendMessage(aliveMessage, groupAddress);
				counter += aliveInterval;
			} else {
				counter = 0;
				sendAdvertisement(groupAddress);
			}
			
			//remove dead nodes
			Set<String> deadNodes = new HashSet<String>();
			for (String key:nodes.keySet()) {
				if (nodes.get(key).getAlive() < (System.currentTimeMillis() - 5 * aliveInterval)) {
					deadNodes.add(key);
				}
			}
			if (deadNodes.size() > 0) {
				log.warn("removing node:\n");
				for (String key:deadNodes) {
					log.warn("\t" + key);
					remoteActions.remove(key);
					//TODO remove agents
					nodes.remove(key);
				}
			}
		}
	}
	
//######################################
//	Debug
//######################################	

	private void dump(String cause) {
		System.out.println("\n####Dump " + myAgentNode + " " + cause);
		System.out.println("Registered local agents:   " + localAgents.size());
		System.out.println("Registered local actions:  " + localActions.size());
		int actions = 0;
		for (String key:remoteActions.keySet()) {
			actions += remoteActions.get(key).size();
		}
		System.out.println("Registered remote actions: " + actions);
		System.out.println("Known agentnodes:          " + nodes.size());
		System.out.println("####End dump\n");
	}
}
/*
 * TODO remove* checken bzgl. ConcurrentModificationException
 * TODO advertise agents ist im Moment etwas stiefmuetterlich behandelt
 * TODO ausfuehrliches Logging einbauen?!?
 */
