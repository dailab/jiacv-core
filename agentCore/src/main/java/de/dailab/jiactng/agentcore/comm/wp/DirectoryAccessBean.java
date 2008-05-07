package de.dailab.jiactng.agentcore.comm.wp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * This Class is meant to work on the side of the agent that is searching
 * for entrys in the directory. E.g. other Agents. It processes actions
 * from the memory and delivers actionresults to the memory.
 * 
 * Note: For this Bean to be able to work properly there has to be a CommunicationBean
 * present on this Agent and a DirectoryAgentNodeBean on the AgentNode.
 * 
 * @author Martin Loeffelholz
 *
 */


public class DirectoryAccessBean extends AbstractAgentBean implements IEffector {

	public static final String ACTION_REQUEST_SEARCH = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#requestSearch";
	public static final String ACTION_ADD_ACTION_TO_DIRECTORY = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#addActionToDirectory";
	public static final String ACTION_REMOVE_ACTION_FROM_DIRECTORY = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#removeActionFromDirectory";
	public static final String ACTION_ADD_AUTOENTLISTMENT_ACTIONTEMPLATE = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#addAutoenlistActionTemplate";
	public static final String ACTION_REMOVE_AUTOENTLISTMENT_ACTIONTEMPLATE = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#removeAutoenlistActionTemplate";

	private static final IJiacMessage WHITEPAGES_SEARCH_MESSAGETEMPLATE;
	private static final IJiacMessage WHITEPAGES_REMOTEACTION_MESSAGETEMPLATE;
	private static final IJiacMessage WHITEPAGES_REFRESH_MESSAGETEMPLATE;

	private ICommunicationAddress directoryAddress = null;
	private SearchRequestHandler _searchRequestHandler = null;
	private ActionRequestHandler _actionRequestHandler = null;
	private RemoteActionHandler _remoteActionHandler = null;
	private RefreshAgent _refreshAgent = null;
	private final ResultDump _resultDump = new ResultDump();
	private final DirectoryAccessBean _myAccessBean = this;

	private Action _sendAction = null;
	private Map<String, DoAction> _requestID2ActionMap = new HashMap<String, DoAction>();

	private List<Action> _autoenlistActionTemplates = null;
	private Set<Action> _offeredActions = new HashSet<Action>();
	// the intervall the autoenlistener should check for new actions to enlist
	private long _autoEnlisteningInterval = 2000;
	private long _firstAutoEnlistening = 2000;
	private AutoEnlister _autoEnlister = null;

	private Timer _timer;


	static {
		JiacMessage agentSearchTemplate = new JiacMessage();
		agentSearchTemplate.setProtocol(DirectoryAgentNodeBean.SEARCH_REQUEST_PROTOCOL_ID);
		WHITEPAGES_SEARCH_MESSAGETEMPLATE = agentSearchTemplate;

		JiacMessage remoteActionTemplate = new JiacMessage();
		remoteActionTemplate.setProtocol(DirectoryAgentNodeBean.REMOTEACTION_PROTOCOL_ID);
		WHITEPAGES_REMOTEACTION_MESSAGETEMPLATE = remoteActionTemplate;

		JiacMessage refreshMessage = new JiacMessage();
		refreshMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);
		WHITEPAGES_REFRESH_MESSAGETEMPLATE = refreshMessage;
	}


	public DirectoryAccessBean() {
	}


	public void doInit() throws Exception{
		super.doInit();
		_searchRequestHandler = new SearchRequestHandler();
		_actionRequestHandler = new ActionRequestHandler();
		_remoteActionHandler = new RemoteActionHandler();
		String messageboxName = thisAgent.getAgentNode().getUUID() + DirectoryAgentNodeBean.SEARCHREQUESTSUFFIX;
		directoryAddress = CommunicationAddressFactory.createMessageBoxAddress(messageboxName);
		_autoenlistActionTemplates = new ArrayList<Action>();
		_autoEnlister = new AutoEnlister();
		_refreshAgent = new RefreshAgent();
	}

	public void doStart() throws Exception{
		super.doStart();
		log.debug("starting DirectoryAccessBean");
		memory.attach(_searchRequestHandler, WHITEPAGES_SEARCH_MESSAGETEMPLATE);
		memory.attach(_remoteActionHandler, WHITEPAGES_REMOTEACTION_MESSAGETEMPLATE);
		memory.attach(_refreshAgent, WHITEPAGES_REFRESH_MESSAGETEMPLATE);
		_sendAction = memory.read(new Action(ICommunicationBean.ACTION_SEND,null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
		_timer = new Timer();
		_timer.schedule(_autoEnlister, _firstAutoEnlistening, _autoEnlisteningInterval);

//		for (Action action : memory.readAll(new Action())){
//		_actionRequestHandler.addActionToDirectory(action);
//		}
	}

	public void doStop() throws Exception{
		super.doStop();
		log.debug("stopping DirectoryAccessBean");
		memory.detach(_searchRequestHandler);
		memory.detach(_remoteActionHandler);
	}

	public void doCleanup() throws Exception{
		// nothing to do yet
		super.doCleanup();
	}

	public List<? extends Action> getActions(){
		List<Action> actions = new ArrayList<Action>();

		Action action = new Action(ACTION_REQUEST_SEARCH, this, new Class<?>[]{IFact.class}, new Class<?>[]{List.class});
		actions.add(action);

		action = new Action(ACTION_ADD_ACTION_TO_DIRECTORY, this, new Class<?>[]{IActionDescription.class}, null);
		actions.add(action);

		action = new Action(ACTION_REMOVE_ACTION_FROM_DIRECTORY, this, new Class<?>[]{IActionDescription.class}, null);
		actions.add(action);

		action = new Action(ACTION_ADD_AUTOENTLISTMENT_ACTIONTEMPLATE, this, new Class<?>[] {List.class}, null);
		actions.add(action);

		action = new Action(ACTION_REMOVE_AUTOENTLISTMENT_ACTIONTEMPLATE, this, new Class<?>[] {List.class}, null);
		actions.add(action);

		return actions;
	}



	@SuppressWarnings("unchecked")
	public void doAction(DoAction doAction){
		log.debug("Received DoAction... decoding begins");


		Object[] params = doAction.getParams();
		String actionName= doAction.getAction().getName();
		if (actionName.equalsIgnoreCase(ACTION_REQUEST_SEARCH)){
			log.debug("doAction is a SearchRequest");
			if (params[0] instanceof IFact){
				IFact template = (IFact) params[0];
				SearchRequest request = new SearchRequest(template);
				request.setID(doAction.getSessionId());
				_requestID2ActionMap.put(request.getID(), doAction);
				_searchRequestHandler.requestSearch(request);
			} 

		} else if (actionName.equalsIgnoreCase(ACTION_ADD_ACTION_TO_DIRECTORY)){
			log.debug("doAction is an Action to add to the Directory");	
			_actionRequestHandler.addActionToDirectory((Action) params[0]);
			cleanupSession(doAction);
		} else if (actionName.equalsIgnoreCase(ACTION_REMOVE_ACTION_FROM_DIRECTORY)){
			log.debug("doAction is an Action to remove to the Directory");
			_actionRequestHandler.removeActionFromDirectory((Action) params[0]);
			cleanupSession(doAction);
		} else if (actionName.equalsIgnoreCase(ACTION_ADD_AUTOENTLISTMENT_ACTIONTEMPLATE)){
			synchronized(_autoenlistActionTemplates){
				_autoenlistActionTemplates.addAll((List<Action>) params[0]);
			}
			cleanupSession(doAction);
		} else if (actionName.equalsIgnoreCase(ACTION_REMOVE_AUTOENTLISTMENT_ACTIONTEMPLATE)){
			synchronized (_offeredActions) {
				List<Action> templatesToRemove = (List<Action>) params[0];
				synchronized(_autoenlistActionTemplates){
					_autoenlistActionTemplates.removeAll(templatesToRemove);
				}

				synchronized(_offeredActions){
					for (Action removeTemplate: templatesToRemove){
						Set<Action> actions = memory.readAll(removeTemplate);
						for (Action action : actions){
							if (_offeredActions.remove(action))
								_actionRequestHandler.removeActionFromDirectory(action);
						}
					}
				}
			}
			cleanupSession(doAction);
		} else {
			log.debug("doAction is an Action that has to be invoked remotely");
			_remoteActionHandler.invokeActionRemote(doAction);
		}
	}

	@Override
	public void cancelAction(DoAction doAction) {
		log.debug("DoAction has timeout!");

		synchronized(_requestID2ActionMap){
			if (_requestID2ActionMap.containsKey(doAction.getSessionId())){
				DoAction sourceAction = _requestID2ActionMap.remove(doAction.getSessionId());

				if (sourceAction != null){
					String owner = sourceAction.getOwner();
					log.warn("SearchRequest from owner " + owner + " has timeout");

					ActionResult result = new ActionResult(sourceAction, new TimeoutException("Failure due to Timeout for action " + sourceAction));
					memory.write(result);

				} else {
					log.warn("tried to cancel non existing doAction: " + doAction.getAction().getName());
				}
			}else {
				_remoteActionHandler.cancelRemoteAction(doAction);
			}
		}
	}

	/**
	 * Starts a search for DirectoryEntrys that are conform to the template given
	 * @param <E> extends IFact
	 * @param template the template to search for
	 */
	public <E extends IFact> void requestSearch(E template){
		log.debug("Received SearchRequest via direct invocation. Searching for Agents with template: " + template);
		_searchRequestHandler.requestSearch(template);
	}

	public void setAutoEnlisteningInterval(long autoEnlisteningInterval){
		_autoEnlisteningInterval = autoEnlisteningInterval;
	}

	public void setFirstAutoEnlistening(long firstAutoEnlistening){
		_firstAutoEnlistening = firstAutoEnlistening;
	}


	private void cleanupSession(DoAction doAction){
		doAction.setSource(_resultDump);
		ActionResult result = ((Action) doAction.getAction()).createActionResult(doAction, new Object[] {});
		memory.write(result);
	}




	/**
	 * Inner Class for handling searchRequests 
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	@SuppressWarnings("serial")
	private class SearchRequestHandler implements SpaceObserver<IFact> {


		/**
		 * just gets the SearchRequest to the directory
		 * 
		 * @param <E> extends IFact
		 * @param template of the entrys to look for
		 */
		public <E extends IFact> void requestSearch(E template){
			JiacMessage message = new JiacMessage(template);
			message.setProtocol(DirectoryAgentNodeBean.SEARCH_REQUEST_PROTOCOL_ID);

			Object[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, _resultDump);

			log.debug("sending message with searchrequest to directory " + message);
			memory.write(send);
		}

		/**
		 * receives the answers from the directory and processes them. 
		 * Then puts an actionresult into the agents memory
		 */
		@Override
		@SuppressWarnings("unchecked")
		public void notify(SpaceEvent<? extends IFact> event) {
			if(event instanceof WriteCallEvent) {
				WriteCallEvent wceTemp = (WriteCallEvent) event;
				if (wceTemp.getObject() instanceof IJiacMessage){
					IJiacMessage message = (IJiacMessage) wceTemp.getObject();

					if (message.getPayload() instanceof SearchResponse && ((message= memory.remove(message)) != null)){
						log.debug("DirectoryAccessBean: Got reply to SearchRequest");

						SearchResponse response = (SearchResponse) message.getPayload();
						SearchRequest request = response.getSearchRequest();

						log.debug("processing reply on SearchRequest with ID " + request.getID());

						DoAction sourceDoAction = _requestID2ActionMap.remove(request.getID());

						if (sourceDoAction != null){
							// if request exists and hasn't timed out yet
							List<IFact> result = new ArrayList<IFact>();
							if (response.getResult() != null){
								// if there is an at least empty result

								Set<IFact> facts = response.getResult();
								if (!facts.isEmpty()){
									// and it is in fact not empty
									if (facts.iterator().next() instanceof Action){
										for (IFact fact : facts){
											Action act = (Action) fact;
											act.setProviderBean(_myAccessBean);
											result.add(act);
										}
									} else {
										result.addAll(response.getResult());
									}
								}
							} 

							ActionResult actionResult = ((Action)sourceDoAction.getAction()).createActionResult(sourceDoAction, new Object[] {result});
							log.debug("DirectoryAccessBean is writing actionResult: " + actionResult);

							memory.write(actionResult);
						} 
					}
				}
			}
		}

	}


	@SuppressWarnings("serial")
	private class ActionRequestHandler {

		/**
		 * just gets the SearchRequest to the directory
		 * 
		 * @param <E> extends IFact
		 * @param template of the entrys to look for
		 */

		public void addActionToDirectory(IActionDescription actionDesc){
			JiacMessage message = new JiacMessage(actionDesc);
			message.setProtocol(DirectoryAgentNodeBean.ADD_ACTION_PROTOCOL_ID);
			Object[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, _resultDump);

			log.debug("sending Message to register action in directory " + message);
			memory.write(send);
		}

		public void removeActionFromDirectory(IActionDescription actionDesc){
			JiacMessage message = new JiacMessage(actionDesc);
			message.setProtocol(DirectoryAgentNodeBean.REMOVE_ACTION_PROTOCOL_ID);
			Object[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, _resultDump);

			log.debug("sending Message to register action in directory " + message);
			memory.write(send);
		}
	}

	@SuppressWarnings("serial")
	private class RemoteActionHandler implements SpaceObserver<IFact>, ResultReceiver {

		/* stores RemoteActions, that were received through messages from other agents 
		 * and are currently processed by other beans of this Agent
		 */ 
		private HashMap<String, SessionData> openSessionsFromClients = null;
		/* stores RemoteActions, that were received through the agents memory and holding the connection to the actual
		 * creator of that doActions so Results can handed over to them.
		 */ 
		private HashMap<String, DoAction> openSessionsToProviders = null;


		public RemoteActionHandler() {
			openSessionsFromClients = new HashMap<String, SessionData>();
			openSessionsToProviders = new HashMap<String, DoAction>();
		}

		public void cancelRemoteAction(DoAction remoteAction){
			log.debug("Canceling remoteAction");
			synchronized(openSessionsToProviders){
				DoAction sourceAction = openSessionsToProviders.remove(remoteAction.getSessionId());

				if (sourceAction != null){
					String owner = sourceAction.getSource().toString();
					log.warn("RemoteAction " + remoteAction.getAction().getName() + " from owner " + owner + " has timeout");

					ActionResult result = new ActionResult(sourceAction, new TimeoutException("Failure due to Timeout for action " + sourceAction));
					memory.write(result);

				} else {
					log.warn("tried to cancel non existing remote doAction: " + remoteAction.getAction().getName());
				} 
			}
		}

		/*
		 * Gets a DoAction that has to be invoked remotely
		 * starts a search for the real provider in the directory
		 * actual invocation will be done within the notifymethod
		 * 
		 * @param doAction
		 */
		public void invokeActionRemote (DoAction doAction){
			if (doAction.getAction() != null ) {
				log.debug("invoking remoteAction " + doAction.getAction());

				JiacMessage message = new JiacMessage(doAction);
				message.setProtocol(DirectoryAgentNodeBean.REMOTEACTION_PROTOCOL_ID);


				Object[] params = {message, directoryAddress};
				DoAction send = _sendAction.createDoAction(params, _resultDump);
				synchronized(openSessionsToProviders){
					openSessionsToProviders.put(doAction.getSessionId(), doAction);
				}
				log.debug("searching for provider of remoteAction");
				memory.write(send);

			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void notify(SpaceEvent<? extends IFact> event) {
			if(event instanceof WriteCallEvent) {
				WriteCallEvent wceTemp = (WriteCallEvent) event;
				if (wceTemp.getObject() instanceof JiacMessage){
					log.debug("Got RemoteActionMessage...");
					JiacMessage message = (JiacMessage) wceTemp.getObject();

					if((message.getPayload() instanceof ICommunicationAddress) && ((message = memory.remove(message)) != null)){
						log.debug("Got Provideragent for remote Action to contact for remote invokation");
						ICommunicationAddress providerAddress = (ICommunicationAddress) message.getPayload();
						String sessionID = message.getHeader("SESSION_ID");

						DoAction remoteAction = openSessionsToProviders.get(sessionID);
						if (remoteAction != null){
							log.debug("No timeout happend, preparing message for providerAgent");
							//If no timeout happend and the remote request still exists

							JiacMessage remoteActionInvokationMessage = new JiacMessage(remoteAction);
							remoteActionInvokationMessage.setProtocol(DirectoryAgentNodeBean.REMOTEACTION_PROTOCOL_ID);

							// now let's send the action to the agent that can provide the action...
							Object[] params = {remoteActionInvokationMessage, providerAddress};
							DoAction send = _sendAction.createDoAction(params, _resultDump);
							log.debug("sending message to providerAgent");
							memory.write(send);
						} 

					} else if (message.getPayload() instanceof NoSuchActionException){
						// Action isn't present (anymore) in the Directory
						String sessionID = message.getHeader("SESSION_ID");
						
						DoAction remoteAction = null;
						synchronized(openSessionsToProviders){
							 remoteAction = openSessionsToProviders.remove(sessionID);
						}
						
						if (remoteAction != null){
							log.debug("RemoteAction wasn't found within the Directory");

							ActionResult result = new ActionResult(remoteAction, message.getPayload()); 
							memory.write(result);
						}
					}


					if (message.getPayload() instanceof DoAction && ((message = memory.remove(message)) != null)){
						// so we got something for our agent to do here
						log.debug("got Action for this Agent to invoke ... searching for providerbean");
						DoAction doAction = (DoAction) message.getPayload();

						// now let's look if our agent still supports this action
						DoAction remoteDoAction;
						Object[] params = doAction.getParams();
						boolean remoteActionFound = false;

						List<Action> actions = thisAgent.getActionList();
						for (Action foundAction : actions){
							if (actionsAreEqual(foundAction, doAction.getAction())) {
								// so after finding it let's create the actual DoAction for our agent here
								remoteDoAction = foundAction.createDoAction(params, this);

								remoteActionFound = true;
								synchronized (openSessionsFromClients) {
									openSessionsFromClients.put(remoteDoAction.getSessionId(), new SessionData(doAction, message.getSender()));	
								}
								memory.write(remoteDoAction);
								break;
							}
						}
						if (!remoteActionFound) {
							log.debug("Requested Action doesn't exist on this Agent -- FAILURE!");
							ActionNotPresentException exception = new ActionNotPresentException(doAction.getAction());

							Action missingAction = (Action) doAction.getAction();
							ActionResult result = missingAction.createActionResult(doAction, new Object[] {exception});

							this.receiveResult(result);

						} else {
							log.debug("RemoteAction written to Agents Memory");
						}


					} else if (message.getPayload() instanceof ActionResult && ((message = memory.remove(message)) != null)){
						log.debug("got resultmessage from remoteAction, result reads: " + (ActionResult) message.getPayload());
						ActionResult result = (ActionResult) message.getPayload();
						DoAction doAction = null;
						synchronized(openSessionsToProviders){
							openSessionsToProviders.remove(result.getSessionId());
						}

						if(doAction != null) {
							result.setSource(doAction);
							memory.write(result);
						}
					}
				} 
			}
		}

		private boolean actionsAreEqual(IActionDescription action1, IActionDescription action2){
			boolean equal = (action1 != null)&&(action2 != null); 
			equal &= action1.getName().equals(action2.getName());
			equal &= action1.getInputTypes().equals(action2.getInputTypes());
			equal &= action1.getResultTypes().equals(action2.getResultTypes());
			return equal;
		}

		@Override
		public void receiveResult(ActionResult result) {
			log.debug("Got Result for Remote Action!");

			SessionData sessionData = null;
			synchronized (openSessionsFromClients) {
				sessionData = openSessionsFromClients.remove(result.getSessionId());	
			}
			if (sessionData != null){
				ICommunicationAddress recipient = sessionData.clientAddress;

				Action providerAction = (Action) sessionData.clientSource.getAction();
				ActionResult remoteResult = providerAction.createActionResult(sessionData.clientSource, result.getResults());

				if(recipient != null) {
					log.debug("Indeed waiting for a result for that remoteaction!");
					JiacMessage resultMessage = new JiacMessage(remoteResult);
					resultMessage.setProtocol(DirectoryAgentNodeBean.REMOTEACTION_PROTOCOL_ID);

					Object[] params = {resultMessage, recipient};
					DoAction send = _sendAction.createDoAction(params, _resultDump);

					log.debug("sending result to client");
					memory.write(send);
				} else {
					log.debug("No recipient for ActionResult");
				}
			} else {
				log.debug("Remote Action isn't valid anymore");
			}
			
		}

		@Override
		public String getBeanName() {
			return _myAccessBean.getBeanName();
		}

		@Override
		public void setBeanName(String name) {
			_myAccessBean.setBeanName(name);
		}

	}

	private class AutoEnlister extends TimerTask {

		public void run() {

			synchronized (_offeredActions) {
				for (Action actionTemplate : _autoenlistActionTemplates){
					Set<Action> actions = memory.readAll(actionTemplate);
					for (Action action : actions){
						if(!_offeredActions.contains(action)){
							_offeredActions.add(action);
							_actionRequestHandler.addActionToDirectory(action);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("serial")
	private class RefreshAgent implements SpaceObserver<IFact>{
		@Override
		@SuppressWarnings("unchecked")
		public void notify(SpaceEvent<? extends IFact> event) {
			if(event instanceof WriteCallEvent) {
				WriteCallEvent wceTemp = (WriteCallEvent) event;
				if (wceTemp.getObject() instanceof IJiacMessage){
					IJiacMessage message = (IJiacMessage) wceTemp.getObject();
					if (message.getProtocol().equalsIgnoreCase(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID)){

						Set<IFact> facts = new HashSet<IFact>();
						for (Action action : memory.readAll(new Action())){
							facts.add(action);
						}

						JiacMessage refreshMessage = new JiacMessage(new FactSet(facts));
						refreshMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);
						DoAction send = _sendAction.createDoAction(new Object[] {refreshMessage, message.getSender()}, _resultDump);
						memory.write(send);

					} else if (message.getProtocol().equalsIgnoreCase(DirectoryAgentNodeBean.AGENTPING_PROTOCOL_ID)){
						JiacMessage pingMessage = new JiacMessage(thisAgent.getAgentDescription());
						DoAction send = _sendAction.createDoAction(new Object[] {pingMessage, message.getSender()}, _resultDump);
						memory.write(send);
					}
				} 
			}
		}
	}


	private class SessionData {
		public DoAction clientSource;
		public ICommunicationAddress clientAddress;

		public SessionData(DoAction source, ICommunicationAddress address){
			clientSource = source;
			clientAddress = address;
		}
	}




}
