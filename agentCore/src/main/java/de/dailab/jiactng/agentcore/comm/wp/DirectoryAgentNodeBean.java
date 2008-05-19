package de.dailab.jiactng.agentcore.comm.wp;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
public class DirectoryAgentNodeBean extends AbstractAgentNodeBean {

	public final static String SEARCHREQUESTSUFFIX = "DirectoryAgentNodeBean";
	public final static String SEARCH_REQUEST_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#SearchRequest";
	public final static String ADD_ACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#AddAction";
	public final static String REMOVE_ACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#RemoveAction";
	public final static String ACTIONREFRESH_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#ActionRefresh";
	public final static String AGENTPING_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#AgentPing";
	public final static String REMOTE_ACTION_REFRESH_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#RemoteActionRefresh";
	
	public final static String AGENTNODESGROUP = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#GroupAddress";
	/*
	 * After this intervall messages the space will be checked for old actions,
	 * for each of this actions a message will be send to the agent providing it,
	 * which will sent back a message with the actions provided to refresh them.
	 * When the next intervall begins all actions that weren't refreshed will be removed
	 */ 
	private long _refreshingIntervall = 4000;
	private long _firstRefresh = 5000;
	
	/*
	 * An interval after that a pingmessage is sent to an agent to check if it's still alive
	 * After waiting for one interval all agents that hadn't pinged back are erased from the
	 * directory.
	 */
	private long _agentPingIntervall = 12000;

	private SpaceDestroyer<IFact> destroyer = null;
	private EventedTupleSpace<IFact> space = null;
	private MessageTransport _messageTransport = null;
	private ICommunicationAddress _myAddress = null;
	private ICommunicationAddress _otherNodes = null;

	private RequestHandler _searchRequestHandler = null;

	private Timer _timer;
	private SpaceRefresher _refresher = null; 
	private AgentPinger _agentPinger = null;
	private long _currentLogicTime = 0;

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

	public void doInit(){
		_searchRequestHandler = new RequestHandler();
		_messageTransport.setDefaultDelegate(_searchRequestHandler);
		try {
			_messageTransport.doInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		_refresher = new SpaceRefresher();
		_agentPinger = new AgentPinger();
		
	}

	public void doStart(){
		_myAddress = CommunicationAddressFactory.createMessageBoxAddress(agentNode.getUUID() + SEARCHREQUESTSUFFIX);
		_otherNodes = CommunicationAddressFactory.createGroupAddress(AGENTNODESGROUP);
		try {
			_messageTransport.listen(_myAddress, null);
			_messageTransport.listen(_otherNodes, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}

		_timer = new Timer();
		_timer.schedule(_refresher, _firstRefresh, _refreshingIntervall);
		_timer.schedule(_agentPinger, _agentPingIntervall);
	}

	public void doStop(){
		try {
			_messageTransport.stopListen(_myAddress, null);
			_messageTransport.stopListen(_otherNodes, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		_timer.cancel();
	}

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

	public void setMessageTransport(MessageTransport mt){
		_messageTransport = mt;
	}

	public void setRefreshingIntervall(long intervall){
		_refreshingIntervall = intervall;
	}

	public long getRefrehsingIntervall(){
		return _refreshingIntervall;
	}

	public void setFirstRefresh(long firstRefresh){
		_firstRefresh = firstRefresh;
	}

	public long getFirstRefresh(){
		return _firstRefresh;
	}
	
	public void setAgentPingIntervall(long agentPingIntervall){
		_agentPingIntervall = agentPingIntervall;
	}
	
	public long getAgentPingIntervall(){
		return _agentPingIntervall;
	}

	/*
	 * inner Class to handle the incoming and outgoing searchRequests
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	private class RequestHandler implements IMessageTransportDelegate {

		public Log getLog(String extension){
			//TODO Creating a method within the AgentNode to get a log for AgentNodeBeans and use it here
			return LogFactory.getLog(getClass().getName() + "." + extension);
		}

		@Override
		public void onAsynchronousException(MessageTransport source, Exception e) {
			e.printStackTrace();
		}

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
					 */

					boolean isGlobal = false;
					if (message.getHeader("isGlobal") != null){
						isGlobal = message.getHeader("isGlobal").equalsIgnoreCase("true");
					}
					
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
								Set<ActionData> actDatSet;
								synchronized(space){
									actDatSet = space.readAll(actDat);
								}
								result = new HashSet<IFact>();
								for (ActionData resultData : actDatSet){
									result.add(resultData.getActionDescription());
								}
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

				log.debug("removing possible obsolete version from directory");
				synchronized (space) {
					space.remove(actionData);

					actionData.setCreationTime(_currentLogicTime + 1);

					log.debug("writing new action to tuplespace");
					space.write(actionData);
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

			} else if (message.getProtocol().equalsIgnoreCase(DirectoryAgentNodeBean.ACTIONREFRESH_PROTOCOL_ID)){
				if (message.getPayload() instanceof IActionDescription){
					IActionDescription actDesc = (IActionDescription) message.getPayload();
					ActionData refreshData = new ActionData();
					refreshData.setActionDescription(actDesc);
					synchronized(space){
						//let's remove the old one
						space.remove(refreshData);

						// put the new version into it
						refreshData.setCreationTime(_currentLogicTime + 1);
						space.write(refreshData);
					}
				} else if (message.getPayload() instanceof FactSet){
					FactSet FS = (FactSet) message.getPayload();
					
					synchronized(space){
						for (IFact fact : FS._facts){
							if (fact instanceof IActionDescription){
								IActionDescription actDesc = (IActionDescription) fact;
								ActionData actDat = new ActionData();
								actDat.setActionDescription(actDesc);
								
								if (space.remove(actDat) != null){
									actDat.setCreationTime(_currentLogicTime + 1);
									space.write(actDat);
								}
							}
						}
					}
				}
				
			} else if (message.getProtocol().equalsIgnoreCase(DirectoryAgentNodeBean.AGENTPING_PROTOCOL_ID)){
				IAgentDescription agentDesc = (IAgentDescription) message.getPayload();
				_agentPinger.removePing(agentDesc);
			} else {
				log.warn("Message has unknown protocol " + message.getProtocol());
			}
		}
	}
	
	@SuppressWarnings("serial")
	private class AgentPinger extends TimerTask{
		
		private Set<IAgentDescription> _ongoingAgentPings = new HashSet<IAgentDescription>();
		
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
		
		public void removePing(IAgentDescription agentDesc){
			synchronized(_ongoingAgentPings){
				_ongoingAgentPings.remove(agentDesc);
			}
		}
	}

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
//				System.err.println("removing outdated actions");
				ActionData oldAct = new ActionData();
				oldAct.setCreationTime(_currentLogicTime - 1);
				
				space.removeAll(oldAct);
				// First let's remove all not refreshed actions and communicate them to the other AgentNodes
//				Set<ActionData> oldSet;
//				if ((oldSet = space.removeAll(oldAct)) != null){
//					Set<IFact> oldFacts = new HashSet<IFact>();
//					oldFacts.addAll(oldSet);
//					FactSet factSet = new FactSet(oldFacts);
//					
//					JiacMessage timeoutMessage = new JiacMessage(factSet);
//					timeoutMessage.setSender(_myAddress);
//					timeoutMessage.setProtocol(REMOTE_ACTION_REFRESH_PROTOCOL_ID);
//					try {
//						_messageTransport.send(timeoutMessage, _otherNodes);
//					} catch (CommunicationException e) {
//						e.printStackTrace();
//					}
//				}
				

				ActionData actionTemplate = new ActionData(_currentLogicTime);

				log.debug("Beginning refreshment of stored actions");
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

						JiacMessage refreshMessage = new JiacMessage(actionData._action);
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
		

	@SuppressWarnings("serial")
	public static class ActionData implements IFact{
		private IActionDescription _action = null;
		private Long _creationTime = null;

		public ActionData(long creationtime){
			_creationTime = new Long(creationtime);
		}

		/**
		 * This Constructor is only meant to be used for Templatecreation
		 */
		public ActionData(){
			_creationTime = null;
		}

		public void setCreationTime(Long creationTime){
			_creationTime = creationTime;
		}

		public Long getCreationTime(){
			return _creationTime;
		}

		public void setActionDescription(IActionDescription action){
			_action = action;
		}

		public IActionDescription getActionDescription(){
			return _action;
		}
		
		public String toString(){
			String thisString = "ActionData: ";
			if (_action != null){
				thisString += "Action.name= " + _action.getName() + ";";
			}
			if (_creationTime != null){
				thisString += "CreationTime=" + _creationTime + ";";
			}
			
			return thisString;
		}

	}

}
