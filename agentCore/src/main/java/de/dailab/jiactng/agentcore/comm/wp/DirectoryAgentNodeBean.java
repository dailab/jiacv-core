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
import de.dailab.jiactng.agentcore.action.DoAction;
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
	public final static String REMOTEACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#UseRemoteAction";
	public final static String REFRESH_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#Refresh";
	public final static String AGENTPING_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#AgentPing";
	
	private long _refreshingIntervall = 4000;
	private long _firstRefresh = 5000;
	
	private long _agentPingIntervall = 12000;

	private SpaceDestroyer<IFact> destroyer = null;
	private EventedTupleSpace<IFact> space = null;
	private MessageTransport _messageTransport = null;
	private ICommunicationAddress _myAddress = null; 
	private Set<IActionDescription> _ongoingRefreshs;
	private Set<IAgentDescription> _ongoingAgentPings;

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
			space.write(agentDescription);
		}
	}

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * remove an AgentDescription object from the directory when an agent
	 * is removed from the node.
	 */
	public void removeAgentDescription(IAgentDescription agentDescription){
		if (agentDescription != null) {
			space.remove(agentDescription);
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
			space.write(actionData);
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
			space.remove(actionData);
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
		_ongoingRefreshs = new HashSet<IActionDescription>();
		_ongoingAgentPings = new HashSet<IAgentDescription>();

	}

	public void doStart(){
		_myAddress = CommunicationAddressFactory.createMessageBoxAddress(agentNode.getUUID() + SEARCHREQUESTSUFFIX);
		try {
			_messageTransport.listen(_myAddress, null);
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
				SearchRequest request = (SearchRequest) message.getPayload();

				log.debug("Message is holding SearchRequest");
				if (request.getSearchTemplate() instanceof IAgentDescription){
					log.debug("SearchRequest hold SearchTemplate of type IAgentDescription");
					IAgentDescription template = (IAgentDescription) request.getSearchTemplate();

					log.debug("SearchRequest holds template " + template);
					Set<IAgentDescription> descriptions;
					synchronized(space){
						descriptions = space.readAll(template);
					}
					Set<IFact> result = new HashSet<IFact>();
					result.addAll(descriptions);
					log.debug("Result to send reads " + result);

					SearchResponse response = new SearchResponse(request, result);

					JiacMessage resultMessage = new JiacMessage(response);
					resultMessage.setProtocol(SEARCH_REQUEST_PROTOCOL_ID);
					try {
						log.debug("AgentNode: sending Message " + resultMessage);
						log.debug("sending it to " + message.getSender());
						_messageTransport.send(resultMessage, message.getSender());
					} catch (CommunicationException e) {
						e.printStackTrace();
					}

				} else if (request.getSearchTemplate() instanceof IActionDescription){
					log.debug("SearchRequest hold SearchTemplate of type Action");
					IActionDescription template = (IActionDescription) request.getSearchTemplate();

					log.debug("SearchRequest holds template " + template);

					ActionData templateData = new ActionData();
					templateData.setActionDescription(template);

					Set<ActionData> foundData;
					synchronized (space) {
						foundData = space.readAll(templateData);
					}
					Set<IActionDescription> resultDescriptions = new HashSet<IActionDescription>();

					for (ActionData data: foundData){
						resultDescriptions.add(data.getActionDescription());
					}

					Set<IFact> result = new HashSet<IFact>();
					result.addAll(resultDescriptions);
					log.debug("Result to send reads " + result);

					SearchResponse response = new SearchResponse(request, result);

					JiacMessage resultMessage = new JiacMessage(response);
					resultMessage.setProtocol(SEARCH_REQUEST_PROTOCOL_ID);
					try {
						log.debug("AgentNode: sending Message " + resultMessage);
						log.debug("sending it to " + message.getSender());
						_messageTransport.send(resultMessage, message.getSender());
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
				} else {
					log.warn("SearchRequest hold SearchTemplate of unknown type "+request.getSearchTemplate().getClass().getName());
				}


			} else if (message.getProtocol().equalsIgnoreCase(ADD_ACTION_PROTOCOL_ID)){
				log.debug("Message is holding Action for storage");
				IActionDescription action = (IActionDescription) message.getPayload();
				ActionData actionData = new ActionData();
				actionData.setActionDescription(action);

				log.debug("removing possible obsolete version from directory");
				space.remove(actionData);

				actionData.setCreationTime(_currentLogicTime + 1);

				log.debug("writing new action to tuplespace");
				space.write(actionData);

			} else if (message.getProtocol().equalsIgnoreCase(REMOVE_ACTION_PROTOCOL_ID)){
				log.debug("Message is holding Action for removal");
				IActionDescription action = (IActionDescription) message.getPayload();
				ActionData actionData = new ActionData();
				actionData.setActionDescription(action);

				log.debug("removing action " + action + " from directory");
				synchronized(space){
					space.remove(actionData);
				}

			} else if (message.getProtocol().equalsIgnoreCase(REMOTEACTION_PROTOCOL_ID)){
				log.debug("Message is request for provideraddress of remote Action");

				DoAction remoteAction = (DoAction) message.getPayload();
				log.debug("Searching for provider of (remote)Action " + remoteAction.getAction().getName());

				ActionData templateData = new ActionData();
				templateData.setActionDescription(remoteAction.getAction());

				ActionData foundData = space.read(templateData);

				JiacMessage resultMessage;
				
				
				if (foundData != null){
					IActionDescription resultDescription = foundData.getActionDescription();
					ICommunicationAddress providerAddress = resultDescription.getProviderDescription().getMessageBoxAddress();
					resultMessage = new JiacMessage(providerAddress);
				} else {
					// TODO resultMessage doesn't seem to be serializeable. Ask Marcel about that problem!
					NoSuchActionException exp = new NoSuchActionException("Action " ); //+ remoteAction.getAction() + " isn't present within Directory");
					resultMessage = new JiacMessage(exp);
				}

				resultMessage.setProtocol(REMOTEACTION_PROTOCOL_ID);
				resultMessage.setHeader("SESSION_ID", remoteAction.getSessionId());
				try {
					_messageTransport.send(resultMessage, message.getSender());
				} catch (CommunicationException e) {
					e.printStackTrace();
					System.err.println("THIS ERROR!!!");
				}


			} else if (message.getProtocol().equalsIgnoreCase(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID)){
				if (message.getPayload() instanceof IActionDescription){
					IActionDescription actDesc = (IActionDescription) message.getPayload();
					_ongoingRefreshs.remove(actDesc);
					ActionData actDat = new ActionData();
					actDat.setActionDescription(actDesc);
					//let's remove the old one
					space.remove(actDat);
					
					// put the new version into it
					actDat.setCreationTime(_currentLogicTime +1);
					space.write(actDat);
				}
			} else if (message.getProtocol().equalsIgnoreCase(DirectoryAgentNodeBean.AGENTPING_PROTOCOL_ID)){
				IAgentDescription agentDesc = (IAgentDescription) message.getPayload();
				_ongoingAgentPings.remove(agentDesc);
			} else {
				log.warn("Message has unknown protocol " + message.getProtocol());
			}
		}
	}
	
	@SuppressWarnings("serial")
	private class AgentPinger extends TimerTask{
		
		public void run(){
			// All Agents that haven't ping back are most likely to be non existent anymore
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
			for(IActionDescription actionDesc : _ongoingRefreshs){
				ActionData actDat = new ActionData();
				actDat.setActionDescription(actionDesc);
				space.remove(actDat);
			}
			
			_ongoingRefreshs.clear();
			
			
			ActionData actionTemplate = new ActionData(_currentLogicTime);

			// During maintenance there must not be any other changes to the tuplespace, so...

				log.debug("Beginning refreshment of stored actions");
				// Check the Space for timeouts by using the current and now obsolete logical time
				Set<ActionData> timeouts = space.readAll(actionTemplate);
				

				// as long as timeouts are existing...
				for (ActionData actionData : timeouts){
					//get the first of them and the action stored within it
					IActionDescription timeoutActionDesc = actionData.getActionDescription();

					IAgentDescription agentDesc = timeoutActionDesc.getProviderDescription();
					ICommunicationAddress refreshAddress = agentDesc.getMessageBoxAddress();
					
					_ongoingRefreshs.add(actionData._action);
					
					JiacMessage refreshMessage = new JiacMessage(actionData._action);
					refreshMessage.setSender(_myAddress);
					refreshMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);
					try {
						_messageTransport.send(refreshMessage, refreshAddress);
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
				}
					
					
//					String timeoutAgentID = agentDesc.getAid();
//
//					IAgent timeoutAgent = null;
//
//					List<IAgent> agents = agentNode.findAgents();
//					
//					for (IAgent agent : agents){
//						if (agent.getAgentId().equals(timeoutAgentID)){
//							timeoutAgent = agent;
//							break;
//						}
//					}
//					
//					if (timeoutAgent == null){
//						log.warn("Agent with ID " + timeoutAgentID + " hasn't removed it's actions from Directory and isn't present anymore");
//						space.remove(actionData);
//						continue;
//					} else {
//						actions = timeoutAgent.getActionList();
//						if (actions != null){
//							/*
//							 * It would be quite inefficient to not check the other actions of this agent also
//							 * to possibly be outdated since all actions of an agent are usually stored at the
//							 * same time
//							 */
//							for (IActionDescription action: actions){
//
//								ActionData refreshAction = new ActionData();
//								refreshAction.setActionDescription((IActionDescription) action);
//								// remove this action from the timeouts if it is within them so it hasn't to be looked up twice
//								
//								if ( (space.remove(refreshAction)) != null){
//									/*  if the action was within the timeouts and though shall be known to the outside world
//									 *  let's refresh it too since we're already refreshing
//									 */
//									timeouts.remove(refreshAction);
//									refreshAction.setCreationTime(_currentLogicTime + 1);
//									space.write(refreshAction);
//								}
//							}
//						} else {
//							// Action isn't present anymore on agent
//							space.remove(actionData);
//						}
//					}
//				}
			
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
	
	@SuppressWarnings("serial")
	public class NoSuchActionException implements IFact{
		private String exception;
		
		public NoSuchActionException(String s){
			exception = s;
		}
		
		public String getException(){
			return exception;
		}
		
		public String toString (){
			return exception;
		}
	}

}
