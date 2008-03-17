package de.dailab.jiactng.agentcore.comm.wp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentNodeBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.knowledge.IFact;
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
public class DirectoryAgentNodeBean extends AbstractAgentNodeBean implements
		IAgentNodeBean {
	
	public final static String SEARCHREQUESTSUFFIX = "DirectoryAgentNodeBean";
	public final static String PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp#DirectoryAgentNodeBean";
	private long _refreshingIntervall = 4000;
	private long _firstRefresh = 5000;
	// if this bean should get the actions from all present agents itself on startphase
	// if false agents have to expose their actions themselves in order for their actions to be found in the directory
	private boolean _pullModeActive = false;
	
	private SpaceDestroyer<IFact> destroyer = null;
	private EventedTupleSpace<IFact> space = null;
	private MessageTransport messageTransport = null;
	private ICommunicationAddress myAddress = null; 

	private SearchRequestHandler _searchRequestHandler = null;
	
	private Timer _timer;
	private SpaceRefresher _refresher; 
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
		System.err.println("AgentDesc added: " + agentDescription);
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
	public <T extends Action> void addAction(T action){
		if (action != null){
			ActionData actionData = new ActionData(_currentLogicTime);
			actionData.setAction(action);
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
	public <T extends Action> void removeAction(T action){
		if (action != null) {
			ActionData actionData = new ActionData();
			actionData.setAction(action);
			space.remove(actionData);
		}
	}
	
	
	public void doInit(){
		_searchRequestHandler = new SearchRequestHandler();
		messageTransport.setDefaultDelegate(_searchRequestHandler);
		try {
			messageTransport.doInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void doStart(){
		myAddress = CommunicationAddressFactory.createMessageBoxAddress(agentNode.getName() + SEARCHREQUESTSUFFIX);
		try {
			messageTransport.listen(myAddress, null);
		} catch (CommunicationException e) {
			e.printStackTrace();
		}
		
		if (_pullModeActive){
			List<IAgent> agents = this.agentNode.findAgents();

			for (IAgent agent : agents){
				List<Action> actions = agent.getActionList();
				for (Action action : actions){
					ActionData actionData = new ActionData(_currentLogicTime);
					actionData.setAction(action);
					space.write(actionData);
				}
			}
		}
		
		_timer = new Timer();
		_refresher = new SpaceRefresher();
		_timer.schedule(_refresher, _firstRefresh, _refreshingIntervall);
		
	}
	
	public void doStop(){
		try {
			messageTransport.stopListen(myAddress, null);
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
		messageTransport = mt;
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
	private class SearchRequestHandler implements IMessageTransportDelegate {
		
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
			if (message.getPayload() instanceof SearchRequest){
				SearchRequest request = (SearchRequest) message.getPayload();

				log.debug("Message is holding SearchRequest");
				if (request.getSearchTemplate() instanceof IAgentDescription){
					log.debug("SearchRequest hold SearchTemplate of type IAgentDescription");
					IAgentDescription template = (IAgentDescription) request.getSearchTemplate();

					log.debug("SearchRequest holds template " + template);
					Set<IAgentDescription> descriptions = space.readAll(template);
					Set<IFact> result = new HashSet<IFact>();
					result.addAll(descriptions);
					System.err.println("Space reads as follows: " + space.readAll());
					log.debug("Result to send reads " + result);
					
					SearchResponse response = new SearchResponse(request, result);

					JiacMessage resultMessage = new JiacMessage(response);
					resultMessage.setProtocol(DirectoryAgentNodeBean.PROTOCOL_ID);
					try {
						log.debug("AgentNode: sending Message " + resultMessage);
						log.debug("sending it to " + message.getSender());
						messageTransport.send(resultMessage, message.getSender());
					} catch (CommunicationException e) {
						e.printStackTrace();
					}
				}
			} else if (message.getPayload() instanceof Action){
				//TODO ActionDescriptionhandling.... vielleicht auch Actions unten auf IAgentDescription umstellen.
			}
		}
	}
	
	private class SpaceRefresher extends TimerTask{
		
		/**
		 * Method to keep the actions stored within the space up to date.
		 * within a regular intervall this method is called. It checks which actiondata
		 * tends to be obsolete and get's updated informations from each agent that actions
		 * might be 
		 */
		@Override
		public void run() {
			ActionData actionTemplate = new ActionData(_currentLogicTime);
			
			// Check the Space for timeouts by using the current and now obsolet logical time
			Set<ActionData> timeouts = space.removeAll(actionTemplate);
			
			// as long as timeouts are existing...
			while (!timeouts.isEmpty()){
				//get the first of them and the action stored within it
				ActionData actionData = timeouts.iterator().next();
				Action timeoutAction = actionData.getAction();
				
				// now let's see if the agent still provides any actions
				List<Action> actions = new ArrayList<Action>();
				actions = (List<Action>) timeoutAction.getProviderBean().getActions();
				
				/*
				 * It would be quite inefficient to not check the other actions of this agent also
				 * to possibly be outdated since all actions of an agent are usually stored at the
				 * same time
				 */
				for (Action action: actions){
					
					ActionData refreshAction = new ActionData(_currentLogicTime);
					refreshAction.setAction(action);
					// remove this action from the timeouts if it is within them so it hasn't to be looked up twice
					timeouts.remove(refreshAction);
					
					/*  now nevertheless if the action is allready within the space or not, the directory is updated
					 *  with what the agent has to offer which kind of is the purpose of this directory
					 */
					refreshAction.setCreationTime(_currentLogicTime + 1);
					space.write(refreshAction);
				}
			}

			// finally after processing all timeouts let's give the clock a little nudge
			_currentLogicTime++;
		}
		
		
	}
	
	private class ActionData implements IFact{
		private Action _action;
		private Long _creationTime = new Long(0);
		
		public ActionData(long creationtime){
			_creationTime = new Long(creationtime);
		}
		
		/**
		 * This Constructor is only meant to be used for Templatecreation
		 */
		public ActionData(){
			_creationTime = null;
		}
		
		public void setCreationTime(long creationTime){
			_creationTime = new Long(creationTime);
		}
		
		public long getCreationTime(){
			return _creationTime.longValue();
		}
		
		public void setAction(Action action){
			_action = action;
		}
		
		public Action getAction(){
			return _action;
		}
		
	}
	
}
