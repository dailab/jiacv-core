package de.dailab.jiactng.agentcore.comm.wp;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.security.auth.DestroyFailedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sercho.masp.space.SimpleObjectSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper;
import org.sercho.masp.space.event.EventedTupleSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper.SpaceDestroyer;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.ActionData;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.AgentNodeData;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.FactSet;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.MessageOfChange;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.SearchRequest;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.SearchResponse;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * This class is meant to work on the side of the agentnode. It stores a
 * directory based on IFacts so although its meant to store AgentDescriptions
 * it could theoretically also be used for other subclasses of IFact
 * 
 * If an Agent descides to expose any actions within this bean it is assumed that
 * this agent wants to expose all actions contained within the getActions() method,
 * so doing regular updates on actions these will be pulled and exposed too.
 * 
 * Note: To look for entries within this directory the DirectoryAccessBean should be used.
 * 
 * @author Martin Loeffelholz
 *
 */


//TODO: s.u.
/*
 * 1. New AgentNode is connecting -> request to group with own local actions AND agents - flag if cached -> send back local actions of receiving nodes?
 * 		reaction: Every receiving node sends own local entries (actions AND agents) to sending node, if cacheflag is set within message
 * 2. every change has to be forwarded to all other nodes within the group
 * 3. Buffer changes and send them via interval. - Interval has to be configureable
 * 		update own data with received data
 * 4. All agentNodes are sending Pings within 3,5s. Receiving AgentNodes note time of last contact.
 * 		if last contact is older or exactly two pingperiods away erase all entries from that agentNode
 * 5. Every Node sends ping even when cache is deactivated
 * 6. If cache is deactivated, changes will still be sent (over groupaddress). Cacheconfig just decides if actions of other nodes will be noted or not
 * 
 * Konfigurierbar: PingIntervall, Aenderungsintervall, Caching aktivierbar
 */
public class DirectoryAgentNodeBean extends AbstractAgentNodeBean {

	//TODO Possibility to filter own (local) actions - done
	//TODO make groupaddress configureable with spring - done
	//TODO send collected local changes after each _changePropagationInterval - done
	//TODO receive collected global changes and update own data with it - done
	//TODO note last contacts of all AgentNodes from which messages were received and if cacheflag was active
	// cacheflag = will mind other entries so sent them to this one.
	//TODO if last contact is older or exactly two pingperiods away erase all entries from that agentNode 


	/** suffix for addresscreation purposes. Will be added to the UUID of AgentNode to create Beanaddress */
	public final static String SEARCHREQUESTSUFFIX = "DirectoryAgentNodeBean";

	/** Protocol for search Requests */
	public final static String SEARCH_REQUEST_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#SearchRequest";

	/** Protocol for adding of actions to the Directory */
	public final static String ADD_ACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#AddAction";

	/** Protocol for removing of actions to the Directory */
	public final static String REMOVE_ACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#RemoveAction";

	/** Protocol for refreshing of Actions */
	public final static String ACTIONREFRESH_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#ActionRefresh";

	/** Protocol for refreshing Agents */
	public final static String AGENTPING_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#AgentPing";

	/** Protocol for propagating changes on an AgentNode-Directory and to communicate all what is stored within it when a new AgentNode shows up */
	public final static String CHANGE_PROPAGATION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#ChangePropagation";

	/** Address of AgentNodeGroup. Is used to communicate between AgentNodes for purposes like global searches. */
	public final static String AGENTNODESGROUP = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#GroupAddress";

	/**
	 * After this intervall the space will be checked for old actions,
	 * for each of this actions a message will be send to the agent providing it,
	 * which will sent back a message with the actions provided to refresh them.
	 * When the next intervall begins all actions that weren't refreshed will be removed
	 */ 
	private long _refreshingIntervall = 4000;
	private long _firstRefresh = 5000;

	/**
	 * An interval after that a pingmessage is sent to an agent to check if it's still alive
	 * After waiting for one interval all agents that hadn't pinged back are erased from the
	 * directory.
	 */
	private long _agentPingIntervall = 12000;

	/** Interval after which an AgentNode finaly sends a ping message if there weren't sent changes during this period */
	private long _agentNodePingIntervall = 3500;

	/** Interval after which changes are propagated to the other nodes */
	private long _changePropagateInterval = 3000;



	/** Destroyer for the Directory */
	private SpaceDestroyer<IFact> destroyer = null;

	/** The Actual Directory */
	private EventedTupleSpace<IFact> space = null;


	/** The needed direct connection to the outsideworld */
	private MessageTransport _messageTransport = null;

	/** Address of this <code>DirectoryAgentNodeBean</code> */
	private ICommunicationAddress _myAddress = null;

	/** Groupaddress of all <code>AgentNode</code>s used for inter-AgentNode-communication */
	private ICommunicationAddress _otherNodes = null;

	/** Module that handles local and global searchrequests */
	private RequestHandler _searchRequestHandler = null;

	/** Timerobject that schedules update and refreshment activities */
	private Timer _timer;

	/** Module that holds all stored <code>Action</code>s within the Directory up to date */
	private SpaceRefresher _refresher = null; 

	/** Module that regulary ping <code>Agent</code>s to check if they are still alive */
	private AgentPinger _agentPinger = null;

	private MessengerOfChange _changePropagator = null;

	/**
	 * holds the current logic time. 
	 * These are iterational steps marking up-to-dateness of Actions 
	 * and helps to decide if an <code>Action</code> has to be refreshed
	 */
	private long _currentLogicTime = 0;

	/**
	 * holds the current logical time for AgentNodes
	 * this discrete time will be used to decide if another
	 * AgentNode is still alive or not
	 */
	private long _currentAgentNodeTime = 0;
	
	/**
	 * Buffers for additions to and removals from this directory.
	 * These Buffers are used to propagate changes to other nodes
	 * after each changePropagateInterval.
	 */
	private FactSet _additionBuffer = null;
	private FactSet _removalBuffer = null;

	/**
	 * flag if entries from other AgentNodes should be cached localy or
	 * if they should be ignored
	 */
	private boolean _cacheIsActive = true;

	/**
	 * standard constructor method
	 */
	public DirectoryAgentNodeBean() {
		destroyer = EventedSpaceWrapper.getSpaceWithDestroyer(new SimpleObjectSpace<IFact>("WhitePages"));
		space = destroyer.destroybleSpace;
	}

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * add an AgentDescription object when an agent is added to the node.
	 */
	public void addAgentDescription(IAgentDescription agentDescription){
		if (agentDescription != null) {
			synchronized (space) {
				space.write(agentDescription);	
			}
		}
	}

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * remove an AgentDescription object from the directory when an agent
	 * is removed from the node.
	 */
	public void removeAgentDescription(IAgentDescription agentDescription){
		if (agentDescription != null) {
			synchronized (space) {
				space.remove(agentDescription);

				// As an Agent is removed from the directory also remove all his published Actions;
				ActionData agentAction = new ActionData();
				agentAction.setProviderDescription(agentDescription);
				space.removeAll(agentAction);
			}
		}
	}

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * add an Action object when an agent is added to the node.
	 * 
	 * @param <T>
	 * @param action
	 */
	public <T extends IActionDescription> void addAction(T action){
		if (action != null){
			ActionData actionData = new ActionData(_currentLogicTime);
			actionData.setActionDescription(action);
			actionData.setProviderDescription(action.getProviderDescription());
			actionData.setLocal(true);
			synchronized (space) {
				space.write(actionData);	
			}
		}
	}

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * remove an Action object from the directory when an agent
	 * is removed from the node.
	 * 
	 * @param <T>
	 * @param action
	 */
	public <T extends IActionDescription> void removeAction(T action){
		if (action != null) {
			ActionData actionData = new ActionData();
			actionData.setActionDescription(action);
			synchronized (space) {
				space.remove(actionData);
			}
		}
	}

	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to initialize this AgentNodeBean
	 */
	public void doInit(){
		_additionBuffer = new FactSet();
		_removalBuffer = new FactSet();
		_changePropagator = new MessengerOfChange();
		_searchRequestHandler = new RequestHandler();
		_messageTransport.setDefaultDelegate(_searchRequestHandler);
		try {
			_messageTransport.doInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		_refresher = new SpaceRefresher();
		_agentPinger = new AgentPinger();

		//formerly in doStart this has to happen much earlier now
		_myAddress = CommunicationAddressFactory.createMessageBoxAddress(agentNode.getUUID() + SEARCHREQUESTSUFFIX);
		_otherNodes = CommunicationAddressFactory.createGroupAddress(AGENTNODESGROUP);
		try {
			_messageTransport.listen(_myAddress, null);
			_messageTransport.listen(_otherNodes, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

	}


	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to get this AgentNodeBean going
	 */
	public void doStart(){

		FactSet myData = new FactSet(getLocalActions());
		myData.add(getLocalAgents());

		MessageOfChange moc = new MessageOfChange(myData, null);

		JiacMessage helloWorldMessage = new JiacMessage(moc);
		helloWorldMessage.setProtocol(CHANGE_PROPAGATION_PROTOCOL_ID);
		helloWorldMessage.setHeader("HelloWorld", "true");
		helloWorldMessage.setHeader("UUID", agentNode.getUUID());
		helloWorldMessage.setSender(_myAddress);
		try {
			// let the world now what we have to offer
			_messageTransport.send(helloWorldMessage, _otherNodes);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		_timer = new Timer();
		_timer.schedule(_refresher, _firstRefresh, _refreshingIntervall);
		_timer.schedule(_agentPinger, _agentPingIntervall);
		_timer.schedule(_changePropagator, _changePropagateInterval);
	}

	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to stop this AgentNodeBean and hold all activity
	 */
	public void doStop(){
		try {
			_messageTransport.stopListen(_myAddress, null);
			_messageTransport.stopListen(_otherNodes, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		_timer.cancel();
	}

	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to cleanup this AgentNodeBean and give free used memory
	 */
	public void doCleanup(){
		try {
			destroyer.destroy();
		} catch (DestroyFailedException e) {
			e.printStackTrace();
		}
		space = null;
		destroyer = null;
		_timer.purge();
	}

	/**
	 * sets the MessageTransport to use for sending and receiving messages
	 * 
	 * @param mt the MessageTransport mentioned above
	 */
	public void setMessageTransport(MessageTransport mt){
		_messageTransport = mt;
	}

	/**
	 * Sets the refreshing interval. After this interval the space will be checked for old actions,
	 * for each of this actions a message will be send to the agent providing it,
	 * which will sent back a message with the actions provided to refresh them.
	 * When the next interval begins all actions that weren't refreshed will be removed
	 * 
	 * Default: 2000 milliseconds
	 *
	 * @param intervall time in milliseconds
	 */
	public void setRefreshingIntervall(long intervall){
		_refreshingIntervall = intervall;
	}

	/**
	 * gets the Interval in milliseconds after which actionentries will be refreshed
	 * and their presence on the providing agents will be checked
	 * 
	 * Default: 2000 milliseconds
	 * 
	 * @return interval in milliseconds
	 */
	public long getRefrehsingIntervall(){
		return _refreshingIntervall;
	}

	/**
	 * sets the first time (in milliseconds) a refreshment of actions stored 
	 * within the Directory will be commenced. Can be different from 
	 * refreshinginterval given with the other setter
	 * 
	 * Default: 2000 milliseconds
	 * 
	 * @param firstRefresh
	 */
	public void setFirstRefresh(long firstRefresh){
		_firstRefresh = firstRefresh;
	}

	/**
	 * gets the first time (in milliseconds) a refreshment of actions stored 
	 * within the Directory will be commenced. Can be different from 
	 * refreshinginterval given with the other setter
	 * 
	 * Default: 2000 milliseconds
	 * 
	 * @param firstRefresh
	 */
	public long getFirstRefresh(){
		return _firstRefresh;
	}

	/**
	 * sets the interval after which the AgentNodeBean will ping all agents stored within it
	 * to check if they are still alive.
	 * 
	 * Default: 12000 milliseconds
	 * 
	 * @param agentPingIntervall time in milliseconds
	 */
	public void setAgentPingIntervall(long agentPingIntervall){
		_agentPingIntervall = agentPingIntervall;
	}


	/**
	 * sets the interval after which the AgentNodeBean will ping all agents stored within it
	 * to check if they are still alive.
	 * 
	 * Default: 12000 milliseconds
	 * 
	 * @param agentPingIntervall time in milliseconds
	 */
	public long getAgentPingIntervall(){
		return _agentPingIntervall;
	}

	/**
	 * sets the interval after which collected local changes will be propagated to the other AgentNodes
	 * 
	 * Default: 3000 milliseconds
	 * 
	 * @param cpInterval	interval in milliseconds
	 */
	public void setChangePropagateInterval(long cpInterval){
		_changePropagateInterval = cpInterval;
	}

	/**
	 * gets the interval after which collected local changes will be propagated to the other AgentNodes
	 * 
	 * Default: 3000 milliseconds
	 * 
	 */
	public long getChangePropagateInterval(){
		return _changePropagateInterval;
	}


	/**
	 * gets Interval after which a ping will be sent to the other AgentNodes if no changes were sent during that period
	 * so the other AgentNodes will know this one is still alive
	 * 
	 * Default: 3500 milliseconds
	 * 
	 * @return time in milliseconds
	 */
	public long getAgentNodePingIntervall() {
		return _agentNodePingIntervall;
	}

	/**
	 * sets Interval after which a ping will be sent to the other AgentNodes if no changes were sent during that period
	 * so the other AgentNodes will know this one is still alive
	 * 
	 * Default: 3500 milliseconds
	 * 
	 * @param nodePingIntervall time in milliseconds
	 */
	public void setAgentNodePingIntervall(long nodePingIntervall) {
		_agentNodePingIntervall = nodePingIntervall;
	}


	/**
	 * sets the communicationAddress on which all <code>AgentNode</code>s group together and exchange searchRequests and
	 * necessary overhead
	 * 
	 * @param nodes <code>GroupAddress</code> on which all <code>AgentNode</code>s register
	 */
	public void setOtherNodes(ICommunicationAddress nodes) {
		_otherNodes = nodes;
	}

	/**
	 * sets if incoming entries from other AgentNodes will be stored of ignored
	 * 
	 * @param isActive if true, incoming entries will be cached within local directory
	 * Note: Default = true
	 */
	public void setCacheIsActive(boolean isActive){
		_cacheIsActive = isActive;
	}

	/**
	 * gets all Actions that are provided by agents on this AgentNode
	 * 
	 * @return a set of ActionData from all Actions that are provided by local agents
	 */
	private Set<IFact> getLocalActions(){

		ActionData actDat = new ActionData();
		actDat.setLocal(true);
		Set<ActionData> localActionData = space.readAll(actDat);
		// As java generics aren't very cunning we have to make this not very cunning conversion
		Set<IFact> actionDataFacts = new TreeSet<IFact>();
		actionDataFacts.addAll(localActionData);

		return actionDataFacts;
	}

	/**
	 * gets all Agents that are connected to this AgentNode
	 * 
	 * @return a set of IAgentDescriptions that are describing all Agents on the AgentNode this Bean is attached to 
	 */
	private Set<IFact> getLocalAgents(){
		AgentDescription agentDesc = new AgentDescription(null, null, null, null, this.agentNode.getUUID());

		Set<IFact> agentFacts = new HashSet<IFact>();
		agentFacts.addAll(space.readAll(agentDesc));

		return agentFacts;
	}

	/**
	 * 
	 * inner Class to handle the incoming and outgoing searchRequests
	 * it also holds the handling of all incoming messages
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	private class RequestHandler implements IMessageTransportDelegate {

		/**
		 * gets the log of this Bean
		 */
		public Log getLog(String extension){
			//TODO Creating a method within the AgentNode to get a log for AgentNodeBeans and use it here
			return LogFactory.getLog(getClass().getName() + "." + extension);
		}

		/**
		 * receives asynchronous exceptions from the messagetransports and prints them into the console
		 */
		@Override
		public void onAsynchronousException(MessageTransport source, Exception e) {
			e.printStackTrace();
		}


		/**
		 * This method receives and handles <b>all</b> incoming messages to this AgentNodeBean and handles them
		 */
		@Override
		public void onMessage(MessageTransport source, IJiacMessage message,
				ICommunicationAddress at) {

			log.debug("got message " + message);
			if (message.getProtocol().equalsIgnoreCase(SEARCH_REQUEST_PROTOCOL_ID)){
				if (message.getPayload() instanceof SearchRequest){
					SearchRequest request = (SearchRequest) message.getPayload();

					/*
					 * This header will only be set once by the AccessBean so there will be
					 * only one AgentNodeBean that get's a true at this point and so will be
					 * the only AgentNodeBean that sends a request through the AgentNodeGroup
					 * 
					 * isGlobal will be true if headerflag is set and cache is deactivated
					 * If the cache is active there is no need for a true global search!
					 */

					boolean isGlobal = false;
					boolean header = false;
					if (message.getHeader("isGlobal") != null){
						header = message.getHeader("isGlobal").equalsIgnoreCase("true");
						isGlobal = header && !_cacheIsActive;
					}

					System.err.println("SearchREquest! isGlobal=" + isGlobal + " header=" + header + " cacheIsActive=" + _cacheIsActive);
					log.debug("Message is holding SearchRequest");
					if (request.getSearchTemplate() != null){

						// notMe == true, if message is global and coming from me, so I don't have to answer again.
						boolean notMe = false;

						if (message.getHeader("SpareMe") != null){
							notMe = message.getHeader("SpareMe").equalsIgnoreCase(_myAddress.toString());
						}

						IFact template = request.getSearchTemplate();

						log.debug("SearchRequest holds template " + template);
						Set<IFact> result;

						if (!notMe){
							if (template instanceof IActionDescription){
								ActionData actDat = new ActionData();
								actDat.setActionDescription((IActionDescription) template);
								
								if (!header){
									// searchRequest is strictly local!
									actDat.setLocal(true);
								}
								
								Set<ActionData> actDatSet;
								synchronized(space){
									actDatSet = space.readAll(actDat);
									System.err.println("Found: " + actDatSet.toString());
									ActionData bla = new ActionData();
									System.err.println("Actions in Space: " + space.readAll(bla));
								}
								result = new HashSet<IFact>();
								for (ActionData resultData : actDatSet){
									result.add(resultData.getActionDescription());
								}
							} else if (template instanceof IAgentDescription){
								IAgentDescription agentDesc = (IAgentDescription) template;
								
								if(!header){
									// searchRequest is strictly local!
									agentDesc.setAgentNodeUUID(agentNode.getUUID());
								}
								
								result = space.readAll(template);
							} else {
								synchronized(space){
									result = space.readAll(template);
								}
							}

							log.debug("Result to send reads " + result);

							SearchResponse response = new SearchResponse(request, result);

							JiacMessage resultMessage = new JiacMessage(response);
							resultMessage.setProtocol(SEARCH_REQUEST_PROTOCOL_ID);
							resultMessage.setSender(_myAddress);
							try {
								log.debug("AgentNode: sending Message " + resultMessage);
								log.debug("sending it to " + message.getSender());
								_messageTransport.send(resultMessage, message.getSender());
							} catch (CommunicationException e) {
								e.printStackTrace();
							}
							if (isGlobal){
								//GLOBAL SEARCH CALL!!!
								log.debug("SearchRequest was GLOBAL request. Sending searchmessage to otherNodes");
								JiacMessage globalMessage;
								globalMessage = new JiacMessage(request);
								globalMessage.setProtocol(SEARCH_REQUEST_PROTOCOL_ID);
								globalMessage.setHeader("SpareMe", _myAddress.toString());
								globalMessage.setSender(message.getSender());

								try {
									_messageTransport.send(globalMessage, _otherNodes);
								} catch (CommunicationException e) {
									e.printStackTrace();
								}
							}

						}
					} else {
						log.warn("SearchRequest without template received. SearchOperation aborted.");
					}
				} else {
					log.warn("SearchRequest-message received without actual Request in it. SearchOperation aborted.");
				}


			} else if (message.getProtocol().equalsIgnoreCase(ADD_ACTION_PROTOCOL_ID)){
				log.debug("Message is holding Action for storage");
				IActionDescription action = (IActionDescription) message.getPayload();
				ActionData actionData = new ActionData();
				actionData.setActionDescription(action);
				actionData.setProviderDescription(action.getProviderDescription());

				// only the local accessBean uses this protocol, so the action has to be local too
				actionData.setLocal(true);

				log.debug("removing possible obsolete version from directory");
				synchronized (space) {
					space.remove(actionData);
					actionData.setCreationTime(_currentLogicTime + 1);

					log.debug("writing new action to tuplespace");
					space.write(actionData);
					System.err.println("ADDING NEW ACTION: " + actionData);
				}

			} else if (message.getProtocol().equalsIgnoreCase(REMOVE_ACTION_PROTOCOL_ID)){
				log.debug("Message is holding Action for removal");
				IActionDescription action = (IActionDescription) message.getPayload();
				ActionData actionData = new ActionData();
				actionData.setActionDescription(action);

				log.debug("removing action " + action + " from directory");
				synchronized(space){
					space.remove(actionData);
				}

			} else if (message.getProtocol().equalsIgnoreCase(ACTIONREFRESH_PROTOCOL_ID)){
				if (message.getPayload() instanceof IActionDescription){
					IActionDescription actDesc = (IActionDescription) message.getPayload();
					ActionData refreshData = new ActionData();
					refreshData.setActionDescription(actDesc);
					synchronized(space){
						//let's remove the old one
						refreshData = space.remove(refreshData);


						// put the new version into it
						refreshData.setCreationTime(_currentLogicTime + 1);

						space.write(refreshData);
					}
				} else if (message.getPayload() instanceof FactSet){
					FactSet FS = (FactSet) message.getPayload();

					synchronized(space){
						for (IFact fact : FS.getFacts()){
							if (fact instanceof IActionDescription){
								IActionDescription actDesc = (IActionDescription) fact;
								ActionData actDat = new ActionData();
								actDat.setActionDescription(actDesc);

								if ((actDat = space.remove(actDat)) != null){
									actDat.setCreationTime(_currentLogicTime + 1);
									space.write(actDat);
								}
							}
						}
					}
				}

			} else if (message.getProtocol().equalsIgnoreCase(AGENTPING_PROTOCOL_ID)){
				IAgentDescription agentDesc = (IAgentDescription) message.getPayload();
				_agentPinger.removePing(agentDesc);

			} else if (message.getProtocol().equalsIgnoreCase(CHANGE_PROPAGATION_PROTOCOL_ID)){
				
				// this Message can only come from another AgentNode so mark it as alive.
				AgentNodeData otherNode = new AgentNodeData();
				otherNode.setAddress(message.getSender());
				
				AgentNodeData storedData = space.remove(otherNode);
				if(storedData == null){
					otherNode.setCreationTime(_currentAgentNodeTime + 1);
					otherNode.setUUID(message.getHeader("UUID"));
					space.write(otherNode);
					
					// TODO think about the following
					/*
					 * if other node isn't known yet this either has to be a new node - HelloWorld!
					 * of this has to be an old node that was perhaps disconnected with this one 
					 * but not necessarily he has to know that. 
					 */
				} else {
					storedData.setCreationTime(_currentAgentNodeTime + 1);
					space.write(otherNode);
				}
				
				
				
				// Message holds Changes for the global Cache send from another Node
				if (message.getPayload() instanceof MessageOfChange){
					if (message.getPayload() != null){

						MessageOfChange moc = (MessageOfChange) message.getPayload();

						if (_cacheIsActive){
							// So the cache is actually up and running so let's get to work on it.

							// first let's remove what have changed or got obsolete
							if (moc.getRemovals() != null){
								FactSet removals = moc.getRemovals();
								for (IFact fact : removals.getFacts()){
									if (fact instanceof IAgentDescription){
										space.remove(fact);
									} else if (fact instanceof ActionData){
										ActionData actDat = (ActionData) fact;
										actDat.setCreationTime(null);
										actDat.setLocal(false);
										space.remove(actDat);
									}
								}
							}

							// now let's add the newest additions
							if (moc.getAdditions() != null){
								FactSet additions = moc.getAdditions();
								for (IFact fact : additions.getFacts()){
									if (fact instanceof IAgentDescription){
										space.write(fact);
									} else if (fact instanceof ActionData){
										ActionData actDat = (ActionData) fact;
										actDat.setLocal(false);
										actDat.setCreationTime(_currentLogicTime + 1);
										space.write(actDat);
									}
								}
							}
						}
					} 
					if (message.getHeader("HelloWorld") != null){

						FactSet myData = new FactSet(getLocalActions());
						myData.add(getLocalAgents());

						MessageOfChange mocBack = new MessageOfChange(myData, null);

						JiacMessage helloWorldMessage = new JiacMessage(mocBack);
						helloWorldMessage.setProtocol(CHANGE_PROPAGATION_PROTOCOL_ID);
						helloWorldMessage.setHeader("UUID", agentNode.getUUID());
						helloWorldMessage.setSender(_myAddress);
						try {
							// let the world now what we have to offer
							_messageTransport.send(helloWorldMessage, message.getSender());
						} catch (CommunicationException e) {
							e.printStackTrace();
						}
					}
				}


			} else {
				log.warn("Message has unknown protocol " + message.getProtocol());
			}
		}
	}

	/**
	 * Module that pings all stored agents and checks if they are still alive.
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	@SuppressWarnings("serial")
	private class AgentPinger extends TimerTask{

		/**
		 * Set of Pings that got out to the agents but didn't came back yet
		 */
		private Set<IAgentDescription> _ongoingAgentPings = new HashSet<IAgentDescription>();

		/**
		 * Pings all <code>Agent</code>s stored within the Directory and checks if they have replied
		 */
		public void run(){
			// All Agents that haven't ping back are most likely to be non existent anymore
			synchronized (_ongoingAgentPings) {

				synchronized (space) {
					for (IAgentDescription agent : _ongoingAgentPings){
						IAgentDescription spaceAgent = space.remove(agent);
						log.warn("Agent doesn't seem to be present anymore. Probably shutdown. Agent is " + spaceAgent);
					}


					_ongoingAgentPings.clear();

					for (IAgentDescription agent : space.readAll(new AgentDescription())){
						_ongoingAgentPings.add(agent);
						ICommunicationAddress pingAddress = agent.getMessageBoxAddress();
						JiacMessage message = new JiacMessage();
						message.setProtocol(DirectoryAgentNodeBean.AGENTPING_PROTOCOL_ID);
						message.setSender(_myAddress);
						try {
							_messageTransport.send(message, pingAddress);
						} catch (CommunicationException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		/**
		 * if an Agent replies to a ping message this method will be used to make a note of that
		 * 
		 * @param agentDesc the Description of the agent replying
		 */
		public void removePing(IAgentDescription agentDesc){
			synchronized(_ongoingAgentPings){
				_ongoingAgentPings.remove(agentDesc);
			}
		}
	}

	/**
	 * Module that keeps the actions stored within the Directory up to date.
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	@SuppressWarnings("serial")
	private class SpaceRefresher extends TimerTask {

		/**
		 * Method to keep the actions stored within the tuplespace up to date.
		 * within a regular interval this method is called. It checks which actiondata
		 * tends to be obsolete and get's updated informations from each agent that actions
		 * might be 
		 */
		@Override
		public void run() {
			// before checking for new timeouts, let's check for old ones from the last run,
			// that didn't had replys in time to refresh them.

			// During maintenance there must not be any other changes to the tuplespace, so...
			synchronized(space){
				log.debug("Beginning refreshment of stored actions");

//				System.err.println("removing outdated actions");
				ActionData oldAct = new ActionData();
				oldAct.setCreationTime(_currentLogicTime - 1);

				//First let's remove all not refreshed actions
				space.removeAll(oldAct);


				ActionData actionTemplate = new ActionData(_currentLogicTime);


				// Check the Space for timeouts by using the current and now obsolete logical time

//				System.err.println("Actions : " + space.readAll(new ActionData()).size());
//				System.err.println("Timeouts: " + space.readAll(actionTemplate).size());

				Set<IAgentDescription> agentsAllreadyTold = new HashSet<IAgentDescription>();

				for (ActionData actionData : space.readAll(actionTemplate)){
					//as long as timeouts are existing...
					//get the first of them and the action stored within it
					IActionDescription timeoutActionDesc = actionData.getActionDescription();

					IAgentDescription agentDesc = timeoutActionDesc.getProviderDescription();
					if (agentsAllreadyTold.contains(agentDesc)){
						continue;
					} else {
						agentsAllreadyTold.add(agentDesc);
						ICommunicationAddress refreshAddress = agentDesc.getMessageBoxAddress();

						JiacMessage refreshMessage = new JiacMessage(actionData.getActionDescription());
						refreshMessage.setSender(_myAddress);
						refreshMessage.setProtocol(DirectoryAgentNodeBean.ACTIONREFRESH_PROTOCOL_ID);
						try {
							_messageTransport.send(refreshMessage, refreshAddress);
						} catch (CommunicationException e) {
							e.printStackTrace();
						}
					}
				}
			}		

			// finally after processing all timeouts let's give the clock a little nudge
			_currentLogicTime++;
			log.debug("Finished refreshment of stored actions");
		}
	}

	private class MessengerOfChange extends TimerTask {
		@Override
		public void run() {

			if ((!_additionBuffer.isEmpty()) || (!_removalBuffer.isEmpty())){
				MessageOfChange moc = new MessageOfChange(_additionBuffer, _removalBuffer);
				_additionBuffer.clear();
				_removalBuffer.clear();


				JiacMessage changePropagationMessage = new JiacMessage(moc);
				changePropagationMessage.setProtocol(CHANGE_PROPAGATION_PROTOCOL_ID);
				changePropagationMessage.setSender(_myAddress);
				try {
					// let the world now what we have to offer
					_messageTransport.send(changePropagationMessage, _otherNodes);
				} catch (CommunicationException e) {
					e.printStackTrace();
				}

			} else {

				// we don't have actual changes but are still alive. So let's shout it out loud
				JiacMessage stillAliveMessage = new JiacMessage();
				stillAliveMessage.setProtocol(CHANGE_PROPAGATION_PROTOCOL_ID);
				stillAliveMessage.setSender(_myAddress);
				try {
					// let the world now what we have to offer
					_messageTransport.send(stillAliveMessage, _otherNodes);
				} catch (CommunicationException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class AgentNodeWatcher extends TimerTask{
		@Override
		public void run() {
			//TODO Watch over AgentNodes and their last contacttimes
			
			
		}
	}


}
