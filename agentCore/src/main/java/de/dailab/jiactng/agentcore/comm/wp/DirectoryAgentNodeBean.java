package de.dailab.jiactng.agentcore.comm.wp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import javax.security.auth.DestroyFailedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sercho.masp.space.SimpleObjectSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper;
import org.sercho.masp.space.event.EventedTupleSpace;
import org.sercho.masp.space.event.EventedSpaceWrapper.SpaceDestroyer;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentNodeBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.broker.ActiveMQBroker;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.ActionData;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.AgentNodeData;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.AgentNodeDataBase;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.FactSet;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.MessageOfChange;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.SearchRequest;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.SearchResponse;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * This class is meant to work on the side of the AgentNode. It stores a
 * directory based on IFacts so although its meant to store AgentDescriptions
 * it could theoretically also be used for other subclasses of IFact
 * 
 * If an Agent decides to expose any actions within this bean it is assumed that
 * this agent wants to expose all actions contained within the getActions() method,
 * so doing regular updates on actions these will be pulled and exposed too.
 * 
 * Note: To look for entries within this directory the DirectoryAccessBean should be used.
 * 
 * @author Martin Loeffelholz
 *
 */

public class DirectoryAgentNodeBean extends AbstractAgentNodeBean implements IMessageTransportDelegate, ILifecycleListener, DirectoryAgentNodeBeanMBean {

	/** suffix for address-creation purposes. Will be added to the UUID of AgentNode to create Beanaddress */
	public final static String SEARCHREQUESTSUFFIX = "DirectoryAgentNodeBean";

	/** Protocol for search Requests */
	public final static String SEARCH_REQUEST_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#SearchRequest";

	/** Protocol for adding of actions to the Directory */
	public final static String ADD_ACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#AddAction";

	/** Protocol for removing of actions to the Directory */
	public final static String REMOVE_ACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#RemoveAction";

	/** Protocol for refreshing of Actions */
	public final static String REFRESH_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#ActionRefresh";

	/** Protocol for propagating changes on an AgentNode-Directory and to communicate all what is stored within it when a new AgentNode shows up */
	public final static String CHANGE_PROPAGATION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#ChangePropagation";

	/** Address of AgentNodeGroup. Is used to communicate between AgentNodes for purposes like global searches. */
	public final static String AGENTNODESGROUP = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#GroupAddress";
	
	/** broker on this agentNode. This reference will be used to dynamicly set networkTTL on the broker to realy get all other nodes that
	 * are connected with this broker through a network of brokers
	 */ 
	private ActiveMQBroker _broker = null;
	
	/**
	 * determines if the DirectoryAgentNodeBean should change the networkTTL property of the networkconnector of the broker
	 * dynamicly based upon the number of other AgentNodes it knows. The networkTTL property determines how many hops a
	 * message may make over other brokers to get to all consumers within the network and so should be either changed dynamicly
	 * or set to a number equal or higher than all AgentNodes/Brokers connected.
	 */
	private boolean _dynamicNetworkTTL = true;

	/**
	 * After this interval the space will be checked for old actions,
	 * for each of this actions a message will be send to the agent providing it,
	 * which will sent back a message with the actions provided to refresh them.
	 * When the next interval begins all actions that weren't refreshed will be removed
	 */ 
	private long _refreshingInterval = 4000;

	/**
	 * Time after which the first refreshment of stored actions will be initiated
	 */
	private long _firstRefresh = 5000;

	/**
	 * An interval after that a ping-message is sent to an agent to check if it's still alive
	 * After waiting for one interval all agents that hadn't pinged back are erased from the
	 * directory.
	 */
	private long _agentPingInterval = 12000;

	/** Interval after which changes are propagated to the other nodes 
	 *  This Interval is used for Alive-detection of other AgentNodes too. If there will be no message from another
	 *  AgentNode within two times this interval the AgentNode will be removed from this Directory with all entries
	 *  of Agents or Actions from it. */
	private long _changePropagateInterval = 3500;


	/** Destroyer for the Directory */
	private SpaceDestroyer<IFact> destroyer = null;

	/** The Actual Directory */
	private EventedTupleSpace<IFact> space = null;


	/** The needed direct connection to the outside world */
	private MessageTransport _messageTransport = null;
	private boolean _messageTransportIsActive = false;

	/** Address of this <code>DirectoryAgentNodeBean</code> */
	private ICommunicationAddress _myAddress = null;

	/** Groupaddress of all <code>AgentNode</code>s used for inter-AgentNode-communication */
	private ICommunicationAddress _otherNodes = null;
	
	/** Name of the group of AgentNodes this AgentNode is associated with.*/
	private String _groupName = AGENTNODESGROUP;

	/** Timerobject that schedules update and refreshment activities */
	private Timer _timer;

	/** decides if timer should be stopped */
	private boolean _timerStop = false;

	/** Module that holds all stored local <code>Action</code>s within the Directory up to date */
	private SpaceRefresher _refresher = null; 

	/** Module that regulary ping <code>Agent</code>s to check if they are still alive */
	private AgentRefresher _agentPinger = null;

	/** Module that manages messages from other AgentNodes and updates global entries within this AgentNodes Directory */
	private MessengerOfChange _changePropagator = null;

	/** TimerTask checks every second if AgentNodes weren't signaling something within 2 * _changePropagateInterval.
	 * if an AgentNode with such a timeout is found it will be removed. All agents and their actions associated with it too.
	 */ 
	private AgentNodeWatcher _agentNodeWatcher = null;

	private AgentNodeDataBase _otherNodesBase = null;
	/**
	 * holds the current logic time. 
	 * These are iterational steps marking up-to-dateness of Actions 
	 * and helps to decide if an <code>Action</code> has to be refreshed
	 */
	private long _currentLogicTime = 0;

	/**
	 * Buffers for additions to and removals from this directory.
	 * These Buffers are used to propagate changes to other nodes
	 * after each changePropagateInterval.
	 */
	private FactSet _additionBuffer = null;
	private FactSet _removalBuffer = null;
	private Object _bufferlock = new Object();

	/**
	 * flag: true if entries from other AgentNodes should be cached locally or false
	 * if they should be ignored
	 * 
	 * By default it's set to true
	 */
	private boolean _cacheIsActive = true;

	/**
	 * flag: true if changes should be propagated the moment they are made or false
	 * if they should be buffered and propagated after a configurable interval
	 * 
	 *  By default it's set to false 
	 *  for reference on the mentioned interval see changePropagationInterval
	 */
	private boolean _instantPropagation = true;

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
				if (agentNode.getState() == LifecycleStates.STARTED ){
					synchronized(_bufferlock){
						_removalBuffer.remove(agentDescription);		
						_additionBuffer.add(agentDescription);
						
						if (_instantPropagation){
							sendMessageOfChange(_additionBuffer, null, false, false);
							_additionBuffer.clear();
						}
					}
				}
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
				_removalBuffer.add(agentDescription);

				// As an Agent is removed from the directory also remove all his published Actions;
				ActionData agentAction = new ActionData();
				agentAction.setProviderDescription(agentDescription);
				agentAction.setIsLocal(true);

				Set<ActionData> actionsRemoved = space.removeAll(agentAction);

				Set<IFact> factsToRemove = new HashSet<IFact>();
				factsToRemove.addAll(actionsRemoved);
				synchronized(_bufferlock){
					_additionBuffer.remove(factsToRemove);
					_removalBuffer.add(factsToRemove);
					
					if (_instantPropagation){
						sendMessageOfChange(null, _removalBuffer, false, false);
						_removalBuffer.clear();
					}
				}
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
      synchronized (space) {
        ActionData actionData = new ActionData(_currentLogicTime);
        actionData.setActionDescription(action);
        actionData.setProviderDescription(action.getProviderDescription());
			  actionData.setIsLocal(true);

				space.write(actionData);
				synchronized(_bufferlock){
					_additionBuffer.add(actionData);
					
					if (_instantPropagation){
						sendMessageOfChange(_additionBuffer, null, false, false);
						_additionBuffer.clear();
					}
				}
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
				synchronized(_bufferlock){
					_additionBuffer.remove(actionData);
					_removalBuffer.add(actionData);
					
					if (_instantPropagation){
						sendMessageOfChange(null, _removalBuffer, false, false);
						_removalBuffer.clear();
					}
				}
			}
		}
	}

	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to initialize this AgentNodeBean
	 */
	public void doInit(){
		log.debug("##init## DirectoryAgentNodeBean on agentNode " + agentNode.getName() + " is initializing.");
		_otherNodesBase = new AgentNodeDataBase();

		_additionBuffer = new FactSet();
		_removalBuffer = new FactSet();
		_changePropagator = new MessengerOfChange();

		_messageTransport.setDefaultDelegate(this);
		try {
			_messageTransport.doInit();
			_messageTransportIsActive = true;
		} catch (Exception e) {
			_messageTransportIsActive = false;
			e.printStackTrace();
		}

		_refresher = new SpaceRefresher();
		_agentPinger = new AgentRefresher();
		_agentNodeWatcher = new AgentNodeWatcher();

		//formerly in doStart this has to happen much earlier now
		_myAddress = CommunicationAddressFactory.createMessageBoxAddress(agentNode.getUUID() + SEARCHREQUESTSUFFIX);
		if (_otherNodes == null){
			_otherNodes = CommunicationAddressFactory.createGroupAddress(AGENTNODESGROUP);
		}
		try {
			_messageTransport.listen(_myAddress, null);
			_messageTransport.listen(_otherNodes, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		log.debug("##init## DirectoryAgentNodeBean on agentNode " + agentNode.getName() + " has been initialized.");
	}


	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to get this AgentNodeBean going
	 */
	public void doStart(){

		log.debug("##start## DirectoryAgentNodeBean on agentNode " + agentNode.getName() + " is starting.");

		FactSet myData = new FactSet(getLocalActions());
		myData.add(getLocalAgents());

		// let the world now what we have to offer
		sendMessageOfChange(myData, null, true, false);

		_timer = new Timer();
		_timer.schedule(_refresher, _firstRefresh, _refreshingInterval);
		_timer.schedule(_agentPinger, _agentPingInterval, _agentPingInterval);
		_timer.schedule(_changePropagator, _changePropagateInterval, _changePropagateInterval);
		_timer.schedule(_agentNodeWatcher, 1000, 1000);
		
		List<IAgentNodeBean> nodeBeans = agentNode.getAgentNodeBeans();
		for (IAgentNodeBean bean : nodeBeans){
			if (bean instanceof ActiveMQBroker){
				_broker = (ActiveMQBroker) bean;
			}
		}
		
		log.debug("##start## DirectoryAgentNodeBean on agentNode " + agentNode.getName() + " has been started.");
	}


	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to stop this AgentNodeBean and hold all activity
	 */
	public void doStop(){
		log.debug("##stop## DirectoryAgentNodeBean on agentNode " + agentNode.getName() + " is stopping.");
		_timerStop = true;
		_timer.cancel();

		// before stopping our work let the others know that we are out of service for a while
		FactSet myData = new FactSet(getLocalActions());
		myData.add(getLocalAgents());

		// let the world now what we had to offer so the other Nodes can remove it
		sendMessageOfChange(null, myData, false, true);

		try {
			_messageTransport.stopListen(_myAddress, null);
			_messageTransport.stopListen(_otherNodes, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		_messageTransportIsActive = false;
		log.debug("##stop## DirectoryAgentNodeBean on agentNode " + agentNode.getName() + " has stopped.");
	}

	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to cleanup this AgentNodeBean and give free used memory
	 */
	public void doCleanup(){
		log.debug("##cleanup## DirectoryAgentNodeBean on agentNode " + agentNode.getName() + " is cleaning up.");
		try {
			_messageTransport.doCleanup();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			destroyer.destroy();
		} catch (DestroyFailedException e) {
			e.printStackTrace();
		}
		space = null;
		destroyer = null;
		_otherNodesBase = null;
		_timer.purge();
		log.debug("##cleanup## DirectoryAgentNodeBean on agentNode " + agentNode.getName() + " has cleaned up.");
	}

	private void sendMessage(JiacMessage message, ICommunicationAddress address){
		message.setSender(_myAddress);
		message.setHeader("UUID", this.agentNode.getUUID());

		if (_messageTransportIsActive){
			try {
				_messageTransport.send(message, address);
			} catch (CommunicationException e) {
				if (log.isErrorEnabled()){
					log.error("Sending of " + message.getProtocol() + " message failed!");
				}
			}
		}
	}

	private void sendMessage(JiacMessage message, ICommunicationAddress address, ICommunicationAddress replyTo){
		message.setSender(replyTo);
		message.setHeader("UUID", this.agentNode.getUUID());

		if (_messageTransportIsActive){
			try{
				_messageTransport.send(message, address);
			}catch (CommunicationException e) {
				if (log.isErrorEnabled()){
					log.error("Sending of " + message.getProtocol() + " message failed!");
				}
			}
		}
	}

	private void sendMessageOfChange(FactSet additions, FactSet removals, boolean helloWorld, boolean byeWorld){

		MessageOfChange moc = new MessageOfChange(additions, removals);

		JiacMessage message = new JiacMessage(moc);
		message.setSender(_myAddress);
		message.setProtocol(CHANGE_PROPAGATION_PROTOCOL_ID);

		if (helloWorld){
			message.setHeader("HelloWorld", "true");
		}

		if (byeWorld) {
			message.setHeader("ByeWorld", "true");
		}

		message.setHeader("UUID", this.agentNode.getUUID());

		if (_messageTransportIsActive){
			try{
				_messageTransport.send(message, _otherNodes);
			}catch (CommunicationException e) {
				if (log.isErrorEnabled()){
					log.error("Sending of " + message.getProtocol() + " message failed!");
				}
			}
		}
	}

	private void sendMessageOfChange(FactSet additions, FactSet removals, boolean helloWorld, ICommunicationAddress otherAddress){

		MessageOfChange moc = new MessageOfChange(additions, removals);

		JiacMessage message = new JiacMessage(moc);
		message.setSender(_myAddress);
		message.setProtocol(CHANGE_PROPAGATION_PROTOCOL_ID);

		if (helloWorld){
			message.setHeader("HelloWorld", "true");
		}

		message.setHeader("UUID", this.agentNode.getUUID());

		if (_messageTransportIsActive){
			try{
				_messageTransport.send(message, otherAddress);
			}catch (CommunicationException e) {
				if (log.isErrorEnabled()){
					log.error("Sending of " + message.getProtocol() + " message failed!");
				}
			}
		}
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
	 * determines if the DirectoryAgentNodeBean should change the networkTTL property of the networkconnector of the broker
	 * dynamicly based upon the number of other AgentNodes it knows. The networkTTL property determines how many hops a
	 * message may make over other brokers to get to all consumers within the network and so should be either changed dynamicly
	 * or set to a number equal or higher than all AgentNodes/Brokers connected.
	 * <br /> <b>Default = true</b>
	 */
	public void setDynamicNetworkTTL(boolean dynamicNetworkTLL){
		_dynamicNetworkTTL = dynamicNetworkTLL;
	}

	/**
	 * Sets the refreshing interval. After this interval the space will be checked for old actions,
	 * for each agent holding an possible obsolete action a message will be sent to him, requesting
	 * him to refresh his Actions within the Directory. 
	 * When the next interval begins all actions that havn't got a refreshment are erased.
	 * 
	 * Default: 2000 milliseconds
	 *
	 * @param interval time in milliseconds
	 */
	public void setRefreshingInterval(long interval){
		_refreshingInterval = interval;
	}

	/**
	 * gets the Interval in milliseconds after which actionentries will be refreshed
	 * and their presence on the providing agents will be checked
	 * 
	 * Default: 2000 milliseconds
	 * 
	 * @return interval in milliseconds
	 */
	public long getRefreshingInterval(){
		return _refreshingInterval;
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
	 * @return first time of refresh
	 */
	public long getFirstRefresh(){
		return _firstRefresh;
	}

	/**
	 * Sets the interval after which the directory will check all local agents stored within it
	 * if they are still alive.
	 * 
	 * Default: 12000 milliseconds
	 * 
	 * @param agentPingInterval time in milliseconds
	 */
	public void setAgentPingInterval(long agentPingInterval){
		_agentPingInterval = agentPingInterval;
	}


	/**
	 * Gets the interval after which the directory will check all local agents stored within it
	 * if they are still alive.
	 * 
	 * Default: 12000 milliseconds
	 *  
	 * @return time in milliseconds
	 */
	public long getAgentPingInterval(){
		return _agentPingInterval;
	}

	/**
	 * Sets the interval after which changes are propagated to the other nodes  (if instantPropagation == false)
	 *  This Interval is used for "Alivedetection" of other AgentNodes too. If there will be no message from another
	 *  AgentNode within two times this interval the AgentNode will be removed from this Directory with all entries
	 *  of Agents or Actions from it.
	 *  
	 * Default: 3000 milliseconds
	 * 
	 * @param cpInterval	interval in milliseconds
	 */
	public void setChangePropagateInterval(long cpInterval){
		_changePropagateInterval = cpInterval;
	}

	/**
	 * gets the interval after which changes are propagated to the other nodes (if instantPropagation == false)
	 *  This Interval is used for "Alivedetection" of other AgentNodes too. If there will be no message from another
	 *  AgentNode within two times this interval the AgentNode will be removed from this Directory with all entries
	 *  of Agents or Actions from it.
	 * 
	 * Default: 3000 milliseconds
	 * 
	 */
	public long getChangePropagateInterval(){
		return _changePropagateInterval;
	}



	/**
	 * sets the communicationAddress on which all <code>AgentNode</code>s group together and exchange searchRequests and
	 * necessary overhead
	 * 
	 * @param groupName <code>GroupAddress</code> on which all <code>AgentNode</code>s register
	 */
	public void setNodeGroup(String groupName) {
		_groupName = groupName;
		_otherNodes = CommunicationAddressFactory.createGroupAddress(_groupName);
	}
	
	/**
	 * get the name of the group (->Groupaddress) on which all <code>AgentNode</code>s group 
	 * together and exchange searchRequests and necessary overhead
	 * 
	 * @return name for <code>GroupAddress</code> on which all <code>AgentNode</code>s register
	 */	
	public String getNodeGroup(){
		return _groupName;
	}
	
	/**
	 * 
	 * @return the actual GroupAddress this AgentNode communicates with other Nodes of it's group
	 */
	public ICommunicationAddress getNodeGroupAddress() {
		return _otherNodes;
	}
	
	/**
	 * 
	 * @return the actual GroupAddress this AgentNode communicates with other Nodes of it's group
	 */
	public String getNodeGroupAddressName() {
		return _otherNodes.toString();
	}
	

	/**
	 * sets if incoming entries from other AgentNodes will be stored of ignored
	 * 
	 * @param isActive if true, incoming entries will be cached within local directory
	 * Note: Default = true
	 * 
	 * <b>IMPORTANT</b>: if set to false during <b>runtime</b> all nonlocal entries will be removed from the directory!
	 */
	public void setCacheIsActive(boolean isActive){
		_cacheIsActive = isActive;


		if (getState() == LifecycleStates.STARTED){	
			if (isActive == false){
				log.info("Cache within DirectoryAgentNodeBean on AgentNode " + agentNode + " DISABLED -> erasing all nonlocal entries!");

				synchronized(space){
					FactSet myData = new FactSet(getLocalActions());
					myData.add(getLocalAgents());

					space.removeAll(new ActionData());
					space.removeAll(new AgentDescription());

					for (IFact fact : myData.getFacts()){
						space.write(fact);
					}

					//we don't need to keep track of alive AgentNodes out there anymore
					_otherNodesBase.clear();
				}
			} else {
				log.info("Cache within DirectoryAgentNodeBean on AgentNode " + agentNode + " ENABLED -> getting entries from other Nodes");

				FactSet myData = new FactSet(getLocalActions());
				myData.add(getLocalAgents());

				sendMessageOfChange(myData, null, true, false);

			}
		}
	}

	/**
	 * Checks, if incoming entries from other agent nodes are stored or ignored.
	 * 
	 * Default: true
	 * 
	 * @return <code>true</code>, if incoming entries are cached within local directory
	 */
	public boolean getCacheIsActive() {
		return _cacheIsActive;
	}
	
	
	/**
	 * sets if changes should be propagated instantly or a collection of changes should be send every <code>changePropagationInterval</code> ms
	 * 
	 * @param instantPropagation is false by default -> changes will be collected and send as a bundle
	 * 
	 * Note: can be changed during runtime without causing problems
	 */
	public void setInstantPropagation(boolean instantPropagation){
		_instantPropagation = instantPropagation;
	}
	
	/**
	 * 
	 * @return <code>true</code>, if changes should be propagated instantly to the other <code>AgentNode</code>s.
	 * 
	 * Default: false -> local changes will be buffered and send every <code>changePropagationInterval</code> ms
	 */
	public boolean getInstantPropagation(){
		return _instantPropagation;
	}

	/**
	 * gets all Actions that are provided by agents on this AgentNode
	 * 
	 * @return a set of ActionData from all Actions that are provided by local agents
	 */
	private synchronized Set<IFact> getLocalActions(){

		ActionData actDat = new ActionData();
		actDat.setIsLocal(true);
		Set<ActionData> localActionData = space.readAll(actDat);
		// As java generics aren't very cunning we have to make this not very cunning conversion
		Set<IFact> actionDataFacts = new HashSet<IFact>();
		actionDataFacts.addAll(localActionData);

		return actionDataFacts;
	}

	/**
	 * gets all Agents that are connected to this AgentNode
	 * 
	 * @return a set of IAgentDescriptions that are describing all Agents on the AgentNode this Bean is attached to 
	 */
	private synchronized Set<IFact> getLocalAgents(){
		AgentDescription agentDesc = new AgentDescription(null, null, null, null, this.agentNode.getUUID());

		Set<IFact> agentFacts = new HashSet<IFact>();
		agentFacts.addAll(space.readAll(agentDesc));

		return agentFacts;
	}

	/**
	 * {@inheritDoc}
	 */
	public CompositeData getSpace() {
		// read all facts from directory memory
		if(space==null) {
			throw new RuntimeException("Directory memory has not yet been initialized!");
		}
		Set<IFact> facts = space.readAllOfType(IFact.class);
		if (facts.isEmpty()) {
			return null;
		}

		// create map with current memory state
		Map<String,List<String>> map = new Hashtable<String,List<String>>();
		for (IFact fact : facts) {
			String classname = fact.getClass().getName();
			List<String> values = map.get(classname);
			if (values == null) {
				values = new ArrayList<String>();
				map.put(classname, values);
			}
			values.add(fact.toString());
		}

		// create composite data
		CompositeData data = null;
		int size = map.size();
		String[] itemNames = new String[size];
		OpenType<?>[] itemTypes = new OpenType[size];
		Object[] itemValues = new Object[size];
		Object[] classes = map.keySet().toArray();
		try {
			for (int i=0; i<size; i++) {
				String classname = (String) classes[i];
				itemNames[i] = classname;
				itemTypes[i] = new ArrayType(1, SimpleType.STRING);
				List<String> values = map.get(classname);
				String[] value = new String[values.size()];
				Iterator<String> it = values.iterator();	    		
				int j = 0;
				while (it.hasNext()) {
					value[j] = it.next();
					j++;
				}
				itemValues[i] = value;
			}
			CompositeType compositeType = new CompositeType(map.getClass().getName(), "facts stored in the directory memory", itemNames, itemNames, itemTypes);
			data = new CompositeDataSupport(compositeType, itemNames, itemValues);
		}
		catch (OpenDataException e) {
			e.printStackTrace();
		}

		return data;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isMessageTransportActive() {
		return _messageTransportIsActive;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessageTransportIdentifier() {
		if (_messageTransport == null) {
			return null;
		}
		return _messageTransport.getTransportIdentifier();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getOtherNodes() {
		if (_otherNodesBase == null) {
			return null;
		}
		return _otherNodesBase.getUUIDs();
	}


	/**
	 * gets the log of this Bean
	 */
	public Log getLog(String extension){
		//TODO Creating a method within the AgentNode to get a log for AgentNodeBeans and use it here
		return LogFactory.getLog(getClass().getName() + "." + extension);
	}

	/**
	 * receives asynchronous exceptions from the message transports and prints them into the console
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
		if (!message.getSender().toString().equalsIgnoreCase(_myAddress.toString())){

			log.debug("got message " + message);
			if (message.getProtocol().equalsIgnoreCase(SEARCH_REQUEST_PROTOCOL_ID)){

				if (message.getPayload() instanceof SearchRequest){
					SearchRequest request = (SearchRequest) message.getPayload();

					/*
					 * This header will only be set once by the AccessBean so there will be
					 * only one AgentNodeBean that get's a true at this point and so will be
					 * the only AgentNodeBean that sends a request through the AgentNodeGroup
					 * 
					 * isGlobal means if it is necessary to send messages to the other Nodes
					 * isGlobal will be true if headerflag is set and cache is deactivated
					 * If the cache is active there is no need for a true global search!
					 */

					boolean isGlobal = false;
					boolean header = false;
					if (message.getHeader("isGlobal") != null){
						header = message.getHeader("isGlobal").equalsIgnoreCase("true");
						isGlobal = header && !_cacheIsActive;
					}

					log.debug("Message is holding SearchRequest");
					if (request.getSearchTemplate() != null){

						IFact template = request.getSearchTemplate();

						log.debug("SearchRequest holds template " + template);
						Set<IFact> result;

						if (template instanceof IActionDescription){
							ActionData actDat = new ActionData();
							actDat.setActionDescription((IActionDescription) template);

							if (!header){
								// searchRequest is strictly local!
								actDat.setIsLocal(true);
							}

							Set<ActionData> actDatSet;
							synchronized(space){
								actDatSet = space.readAll(actDat);
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

						log.debug("AgentNode: sending Message " + resultMessage);
						log.debug("sending it to " + message.getSender());

						sendMessage(resultMessage, message.getSender());

						if (isGlobal){
							//GLOBAL SEARCH CALL!!!
							log.debug("SearchRequest was GLOBAL request. Sending searchmessage to otherNodes");
							JiacMessage globalMessage;
							globalMessage = new JiacMessage(request);
							globalMessage.setProtocol(SEARCH_REQUEST_PROTOCOL_ID);
							globalMessage.setHeader("SpareMe", _myAddress.toString());

							// Send a SearchRequest to the other Nodes
							sendMessage(globalMessage, _otherNodes, message.getSender());

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
				actionData.setIsLocal(true);

				log.debug("removing possible obsolete version from directory");
				synchronized (space) {
					space.remove(actionData);
					actionData.setCreationTime(_currentLogicTime + 1);

					log.debug("writing new action to tuplespace");
					space.write(actionData);
					synchronized(_bufferlock){
						actionData.setCreationTime(null);
						_removalBuffer.remove(actionData);
						_additionBuffer.add(actionData);
						
						if (_instantPropagation){
							sendMessageOfChange(_additionBuffer, null, false, false);
							_additionBuffer.clear();
						}
					}
				}

			} else if (message.getProtocol().equalsIgnoreCase(REMOVE_ACTION_PROTOCOL_ID)){
				log.debug("Message is holding Action for removal");
				IActionDescription action = (IActionDescription) message.getPayload();
				ActionData actionData = new ActionData();
				actionData.setActionDescription(action);

				log.debug("removing action " + action + " from directory");
				synchronized(space){
					ActionData removeData = space.remove(actionData);
					synchronized(_bufferlock){
						removeData.setCreationTime(null);
						_additionBuffer.remove(removeData);
						_removalBuffer.add(removeData);
						
						if (_instantPropagation){
							sendMessageOfChange(null, _removalBuffer, false, false);
							_removalBuffer.clear();
						}
					}
				}

			} else if (message.getProtocol().equalsIgnoreCase(REFRESH_PROTOCOL_ID)){
				if (message.getPayload() instanceof IActionDescription){
					IActionDescription actDesc = (IActionDescription) message.getPayload();
					
					ActionData messageData = new ActionData();
					messageData.setActionDescription(actDesc);

					synchronized(space){					
					  ActionData refreshData = new ActionData();
					  refreshData.setActionDescription(actDesc);
					  refreshData.setCreationTime(_currentLogicTime + 1);
					

						//let's simply update the entry
						space.update(messageData, refreshData);
					}
				} else if (message.getPayload() instanceof FactSet){
					FactSet FS = (FactSet) message.getPayload();

					synchronized(space){
						for (IFact fact : FS.getFacts()){
							if (fact instanceof IActionDescription){
								IActionDescription actDesc = (IActionDescription) fact;
								
								ActionData oldDat = new ActionData();
								oldDat.setActionDescription(actDesc);
								
								ActionData actDat = new ActionData();
								actDat.setActionDescription(actDesc);
								actDat.setCreationTime(_currentLogicTime + 1);
								
								space.update(oldDat, actDat);

//								if ((actDat = space.remove(actDat)) != null){
//									actDat.setCreationTime(_currentLogicTime + 1);
//									space.write(actDat);
//								}
							}
						}
					}
				}

			} else if (message.getProtocol().equalsIgnoreCase(CHANGE_PROPAGATION_PROTOCOL_ID)){

				// this Message can only come from another AgentNode so mark it as alive.
				// But first let's see if it has sent a bye message and we already know it or not

				if (message.getHeader("UUID") != null){

					// bye message => remove agent node from database
					if (message.getHeader("ByeWorld") != null){
						log.info("AgentNode with UUID " + message.getHeader("UUID") + " is shutting down.");
						_otherNodesBase.remove(message.getHeader("UUID"));
					}

					// ping message => update the timeout of known agent node or add new agent node to database
					else {
						AgentNodeData otherNode = new AgentNodeData();
						otherNode.setUUID(message.getHeader("UUID"));
						otherNode.setTimeoutTime(System.currentTimeMillis() + (2 * _changePropagateInterval));
						AgentNodeData storedData = _otherNodesBase.put(otherNode);

						if(storedData == null){
							// AgentNode was formerly unknown, so it was added to our list
							log.debug("Added agent node " + otherNode.getUUID() + " to database with timeout " + otherNode.getTimeoutTime().longValue());

							if (message.getHeader("HelloWorld") == null){
								/*
								 * This seems to be the first contact, but the other one doesn't know that.
								 * So there might have happen some partially or bad timed dis- and reconnection.
								 * We have to send an HelloWorld Message to get it's offers back into our space
								 * completely and to make sure there is no timeout of our entries over there interfering
								 * we sent all what we have to offer with it.
								 */
								FactSet myData = new FactSet(getLocalActions());
								myData.add(getLocalAgents());

								// let the world now what we have to offer						
								sendMessageOfChange(myData, null, true, message.getSender());
								log.debug("Sent own data to other AgentNode in order to get current data of AgentNode " + message.getHeader("UUID"));
							}

						} else {
							// we have already known this AgentNode, so it was updated with a new timeout
							log.debug("Updated agent node " + otherNode.getUUID() + " in database with timeout " + otherNode.getTimeoutTime().longValue());

						}
					}

					// Message holds Changes for the global Cache send from another Node
					if (message.getPayload() != null){
						if (message.getPayload() instanceof MessageOfChange){

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
											actDat.setIsLocal(false);
											space.remove(actDat);
										}
									}
								}

								// now let's add the newest additions
								if (moc.getAdditions() != null){
									FactSet additions = moc.getAdditions();
									for (IFact fact : additions.getFacts()){
									  synchronized(space){
  									  if (fact instanceof IAgentDescription){
  											space.write(fact);
  										} else if (fact instanceof ActionData){
  
  										  ActionData actDat = (ActionData) fact;
  											actDat.setIsLocal(false);
  											actDat.setCreationTime(_currentLogicTime + 1);
  											space.write(actDat);
  										}
									  }
									}
								}
							}
						} 
						if (message.getHeader("HelloWorld") != null){
							FactSet myData = new FactSet(getLocalActions());
							myData.add(getLocalAgents());

							// let the world now what we have to offer
							sendMessageOfChange(myData, null, false, message.getSender());

						}
					}
				} else {
					log.error("Received a MessageOfChange without UUID from sending AgentNode. Message will not be processed");
				}


			} else {
				log.warn("Message has unknown protocol " + message.getProtocol());
			}
		} else {
			// own Message do nothing
		}
	} 

	/**
	 * method from ILifeCycleListener that receives lifecycleevents from the local agents
	 * 
	 * @param evt
	 */
	public void onEvent(LifecycleEvent evt){
		if (evt.getState() == LifecycleStates.INITIALIZED){
			if (evt.getSource() instanceof Agent){
				Agent newAgent = (Agent) evt.getSource();
				this.addAgentDescription(newAgent.getAgentDescription());
			}
		} else if (evt.getState() == LifecycleStates.CLEANING_UP){
			if (evt.getSource() instanceof Agent){
				Agent newAgent = (Agent) evt.getSource();
				this.removeAgentDescription(newAgent.getAgentDescription());
			}
		} else if (!((evt.getState() == LifecycleStates.UNDEFINED) || (evt.getState() == LifecycleStates.INITIALIZING) || (evt.getState() == LifecycleStates.CLEANED_UP))){
			Agent newAgent = (Agent) evt.getSource();
			AgentDescription newAgentDesc = newAgent.getAgentDescription();
			
			newAgentDesc.setState(null);
			
			synchronized(space){
				AgentDescription foundAgent = space.remove(newAgent.getAgentDescription());
				foundAgent.setState(evt.getState().toString());
				space.write(foundAgent);
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
	private class AgentRefresher extends TimerTask{

		/**
		 * Checks all local <code>Agent</code>s stored within the Directory to be still present
		 */
		public void run(){
			if (_timerStop)
				this.cancel();

			Set<AgentDescription> agentsFromDirectory;
			Set<AgentDescription> agentsFromNode;

			synchronized(space){

				// get all local agents from the directory
				AgentDescription localAgentDescription = new AgentDescription();
				localAgentDescription.setAgentNodeUUID(agentNode.getUUID());
				agentsFromDirectory = space.readAll(localAgentDescription);

				// get all local agents from the agentnode and pickup their descriptions
				List<IAgent> foundAgents = agentNode.findAgents();
				agentsFromNode = new HashSet<AgentDescription>();

				for (IAgent agent : foundAgents){
					switch(agent.getState()){
					case INITIALIZED:
					case STARTING:
					case STARTED:
					case STOPPING:
					case STOPPED:
						agentsFromNode.add(agent.getAgentDescription());
					}

				}

				// filter out all agents that are still present on the agentnode
				Iterator<AgentDescription> afdIterator = agentsFromDirectory.iterator();
				while(afdIterator.hasNext()){

					AgentDescription agentDesc = afdIterator.next();
					if (agentsFromNode.remove(agentDesc)){
						AgentDescription agentToUpdate = new AgentDescription();
						agentToUpdate.setName(agentDesc.getName());
						space.update(agentToUpdate, agentDesc);
						afdIterator.remove();
					}
				}

				/*
				 * At this point 
				 * agentsFromNode is holding agents that somehow didn't got listed in the directory (should be empty)
				 * agentsFromDirectory is holding agents that are no longer listed on the agentnode
				 */

				if (agentsFromNode != null){
					if (!agentsFromNode.isEmpty()){
						log.warn("There are " + agentsFromNode.size() + " agents on the Node that aren't listed within the Directory!");
						synchronized(_bufferlock){

							for (AgentDescription forgottenAgent : agentsFromNode){
								log.warn("Adding agent " + forgottenAgent.getName() + " to the Directory");
								space.write(forgottenAgent);
								_additionBuffer.add(forgottenAgent);
							}

							if (_instantPropagation){
								sendMessageOfChange(_additionBuffer, null, false, false);
								_additionBuffer.clear();
							}
						}
					}
				}

				if (agentsFromDirectory != null){
					if (!agentsFromDirectory.isEmpty()){
						synchronized(_bufferlock){
							for (AgentDescription timeoutAgent : agentsFromDirectory){
								log.warn("Agent " + timeoutAgent.getName() + " doesn't seem to be present anymore on this AgentNode, thus removing it from the Directory");
								AgentDescription agentToRemove = new AgentDescription();
								agentToRemove.setName(timeoutAgent.getName());
								space.remove(agentToRemove);
								_removalBuffer.add(timeoutAgent);

							}

							if (_instantPropagation){
								sendMessageOfChange(null, _removalBuffer, false, false);
								_removalBuffer.clear();
							}
						}
					}
				}
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
			if (_timerStop)
				this.cancel();

			// before checking for new timeouts, let's check for old ones from the last run,
			// that didn't had replys in time to refresh them.

			// During maintenance there must not be any other changes to the tuplespace, so...
			synchronized(space){
				log.debug("Beginning refreshment of stored actions");

				ActionData oldAct = new ActionData();
				oldAct.setCreationTime(_currentLogicTime - 1);
				oldAct.setIsLocal(true);
//				System.err.println("Removing actions with timeout");
				//First let's remove all not refreshed actions

				Set<ActionData> removals = space.removeAll(oldAct);
				synchronized(_bufferlock){
					for (ActionData action : removals){
						_removalBuffer.add(action);
					}
					if (_instantPropagation){
						sendMessageOfChange(null, _removalBuffer, false, false);
						_removalBuffer.clear();
					}
				}

				ActionData actionTemplate = new ActionData(_currentLogicTime);
				actionTemplate.setIsLocal(true);


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

						JiacMessage refreshMessage = new JiacMessage(new Action());
						refreshMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);

						sendMessage(refreshMessage, refreshAddress);
					}
				}
	      _currentLogicTime++;
			}		

			// finally after processing all timeouts let's give the clock a little nudge

			log.debug("Finished refreshment of stored actions");
		}
	}

	private class MessengerOfChange extends TimerTask {
		@Override
		public void run() {
			if (_timerStop)
				this.cancel();

			if ((!_additionBuffer.isEmpty()) || (!_removalBuffer.isEmpty())){
				synchronized(_bufferlock){

					sendMessageOfChange(_additionBuffer, _removalBuffer, false, false);

					_additionBuffer.clear();
					_removalBuffer.clear();
				}
			} else {

				// we don't have actual changes but are still alive. So let's shout it out loud
				JiacMessage stillAliveMessage = new JiacMessage();
				stillAliveMessage.setProtocol(CHANGE_PROPAGATION_PROTOCOL_ID);

				// let the world now what we have to offer
				sendMessage(stillAliveMessage, _otherNodes);

			}
		}
	}

	/** TimerTask checks every second if AgentNodes weren't signaling something within 2 * _changePropagateInterval.
	 * if an AgentNode with such a timeout is found it will be removed. All agents and their actions associated with it too.
	 */ 
	private class AgentNodeWatcher extends TimerTask {
		
		private int registeredAgentNodes = _otherNodesBase.getSize();
		
		@Override
		public void run() {
			if (_timerStop)
				this.cancel();

			boolean moreTimeouts = true;
			
			//If the Broker shall be configured dynamicaly to get all other AgentNodes... check if broker was found
			if((_dynamicNetworkTTL) && (_broker == null)){
				List<IAgentNodeBean> nodeBeans = agentNode.getAgentNodeBeans();
				for (IAgentNodeBean bean : nodeBeans){
					if (bean instanceof ActiveMQBroker){
						_broker = (ActiveMQBroker) bean;
						log.warn("Warning obsolete, Broker found by DirectoryAgentNodeBean");
					}
				}
			}

			synchronized (space) {
				synchronized(_otherNodesBase){
					while (moreTimeouts){
						Long firstTimeout = _otherNodesBase.getFirstTimeout();
						if (firstTimeout != null){
							if (firstTimeout <= System.currentTimeMillis()){
								// We've got a timeout!

								// first remove agentNode from directory of known agentNodes
								AgentNodeData otherAgentNode = _otherNodesBase.removeFirstTimeoutNode();

								log.warn("AgentNode " + otherAgentNode.getUUID() + " with timeout " + firstTimeout.longValue() + " isn't available anymore. Removing associated Agents... ");

								String UUID = otherAgentNode.getUUID();
								// now get all agents associated with it
								Set<AgentDescription> timeoutAgents = space.removeAll(new AgentDescription(null, null, null, null, UUID));

								for (AgentDescription agent : timeoutAgents){

									// for every agent on the timeout node remove all actions related to it
									log.warn(agentNode.getName() + ": Removing agent from Directory because of AgentNodeTimeout. Agent is named: " + agent.getName());
									ActionData timeoutActionTemplate = new ActionData();
									timeoutActionTemplate.setProviderDescription(agent);
									for (ActionData actDat : space.removeAll(timeoutActionTemplate)){
										log.warn(agentNode.getName() + ": Removing action due to AgentNodeTimeout. Action removed is called: " + actDat.getActionDescription().getName());
									}
								}	
							} else {
								// no more timeouts
								moreTimeouts = false;
							}
						} else {
							// no more entries in the DataBase, so there can't be more timeouts either
							moreTimeouts = false;
						}
					}

					// if number of known agentNodes changed... change no. of possible messagehops to meet new requirements
					if (_dynamicNetworkTTL){
						if (_otherNodesBase.getSize() != registeredAgentNodes){
							registeredAgentNodes = _otherNodesBase.getSize();
							try {
								if (_broker != null)
									// set it a little bit higher than necessary to find new agentNodes that are connecting too.
									_broker.setNetworkTTL(registeredAgentNodes + 3);
								else
									log.warn("No Broker identified by DirectoryAgentNodeBean so far");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
	}


}
