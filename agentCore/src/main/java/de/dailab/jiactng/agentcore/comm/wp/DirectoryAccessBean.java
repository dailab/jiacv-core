package de.dailab.jiactng.agentcore.comm.wp;

import java.io.Serializable;
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
import de.dailab.jiactng.agentcore.comm.wp.exceptions.ActionNotPresentException;
import de.dailab.jiactng.agentcore.comm.wp.exceptions.DirectoryAccessException;
import de.dailab.jiactng.agentcore.comm.wp.exceptions.NoSuchActionException;
import de.dailab.jiactng.agentcore.comm.wp.exceptions.TimeoutException;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.FactSet;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.ResultDump;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.SearchRequest;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.SearchResponse;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
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

	/*
	 * NOTE: For more Infos about parameters of Actions see Comments within getActions
	 */

	/**
	 * Action to request a search for agents or actions. Theoretically it allows
	 * to even search for every IFact that is stored within a directory
	 * <br />
	 * <b>InputParameter</b> <br /> 
	 * 	IFact 	- template for what you are searching for<br />
	 * 	Boolean - global? - false if you are wanting local entries only<br />
	 * 	Long	- time(out) in milliseconds for the search to run at max.<br />
	 * 			Default is 60.000 ms although a search witch an active cache should take less than 1000 ms<br />
	 * <br />
	 * <b>Result Types:</b> <br />
	 * 	List 	- List of entries matching the template. List will be empty if no matching entry was found<br />
	 */
	public static final String ACTION_REQUEST_SEARCH = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#requestSearch";

	/**
	 * Action to add an action to the directory
	 * <br />
	 * <b>InputParameter:</b><br />
	 * 	IActionDescription - the Action you want to store<br />
	 * <br />
	 * <b>Result Types:</b><br />
	 * 	none<br />
	 */
	public static final String ACTION_ADD_ACTION_TO_DIRECTORY = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#addActionToDirectory";

	/**
	 * Action to remove an action from the directory
	 * <br />
	 * <b>InputParameter:</b><br />
	 * 	IActionDescription - the Action you want to remove<br />
	 * <br />
	 * <b>Result Types:</b><br />
	 * 	none<br />
	 */
	public static final String ACTION_REMOVE_ACTION_FROM_DIRECTORY = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#removeActionFromDirectory";

	/**
	 * Action to add ActionTemplates. All Actions that are provided within the Agent that match these templates will
	 * be offered through the directory on the local AgentNode. The AccessBean checks for changes on Actions matching
	 * with the templates stored within it regulary.
	 * <br />
	 * <b>Input Parameter:</b><br />
	 * 	List of IActionDescriptions. All entries will be added to the List<br />
	 * <br />
	 * <b>Result Types:</b><br />
	 * 	none<br />
	 */
	public static final String ACTION_ADD_AUTOENTLISTMENT_ACTIONTEMPLATE = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#addAutoenlistActionTemplate";

	/**
	 * Action to remove ActionTemplates. All Actions that are matching these templates will be removed from
	 * from the local directory. 
	 * <br />
	 * <b>Input Parameter:</b><br />
	 * 	List of IActionDescriptions. All entries will be removed from the List<br />
	 * <br />
	 * <b>Result Types:</b><br />
	 * 	none<br />
	 */
	public static final String ACTION_REMOVE_AUTOENTLISTMENT_ACTIONTEMPLATE = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#removeAutoenlistActionTemplate";

	/**
	 * Protocol for coordinating remoteaction-invocations 
	 */
	public final static String REMOTEACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#UseRemoteAction";

	/**
	 * the address of the directory at the local agentnode
	 */
	private ICommunicationAddress directoryAddress = null;

	/**
	 * handles searchrequests
	 */
	private SearchRequestHandler _searchRequestHandler = null;

	/**
	 * Handles adding and removal of actions to the directory 
	 */
	private ActionRequestHandler _actionRequestHandler = null;

	/**
	 * handles remote action invocation, processing and resultdelivery
	 */
	private RemoteActionHandler _remoteActionHandler = null;

	/**
	 * handles messages needed to refresh agents and actions in the directory 
	 */
	private RefreshAgent _refreshAgent = null;

	/**
	 * sometimes you realy don't want to get a result
	 * besides that a ResultDump keeps the memory clean
	 */
	private final ResultDump _resultDump = new ResultDump();

	/**
	 * myself. Needed by some modules
	 */
	private final DirectoryAccessBean _myAccessBean = this;

	/**
	 * Action needed to send messages through the communicationBean
	 */
	private Action _sendAction = null;

	/**
	 * stores searchRequests while waiting on reply(s)
	 * This has to be a global Map as it is needed by different modules especially in case of timeouts.
	 */
	private Map<String, DoAction> _requestID2ActionMap = new HashMap<String, DoAction>();

	/**
	 * if a SearchRequest is global this Map stores all responses got for it until a timeout
	 * happens and ends the search. Then all responses will be delivered as actionresult to
	 * the source of the original searchrequest.
	 */
	private Map<String, ArrayList<IFact>> _requestID2ResponseMap = new HashMap<String, ArrayList<IFact>>();

	/**
	 * List of templates. All actions provided by this agent that are matching on one of these templates
	 * will be offered within the directory
	 */
	private List<IActionDescription> _autoenlistActionTemplates = null;

	/**
	 * This set stores all actions that are allready stored within the directory.
	 * Those actions that are forwarded by the autoenlister as well as any action
	 * that is offered "by hand" through an action
	 */
	private Set<IActionDescription> _offeredActions = new HashSet<IActionDescription>();

	/**
	 * the intervall the autoenlistener should check for new actions to enlist
	 * Availability of offered actions will also be checked at these times.
	 * If an Action isn't present anymore within the agent it will be removed.  
	 */
	private long _autoEnlisteningInterval = 2000;

	/**
	 * the time of the first check as mentioned above.
	 */
	private long _firstAutoEnlistening = 2000;

	/**
	 * The autoenlistener checkes for changes in the availability of already
	 * presented actions within the agent and will remove actions offered that
	 * are present nomore and offer new actions that are matching with one of
	 * the autoenlistmenttemplates to the directory to store them.
	 */
	private AutoEnlister _autoEnlister = null;

	/**
	 * Timerobject to schedule refreshing and autoenlisting of actions
	 */
	private Timer _timer;


	/**
	 * Messagetemplates to channel messages to the handlingmodules
	 */
	private static final IJiacMessage WHITEPAGES_SEARCH_MESSAGETEMPLATE;
	private static final IJiacMessage WHITEPAGES_REMOTEACTION_MESSAGETEMPLATE;
	private static final IJiacMessage WHITEPAGES_REFRESH_MESSAGETEMPLATE;

	// Initializing messagetemplates for getting the incomming messages to the modules they are needed to go
	static {
		JiacMessage agentSearchTemplate = new JiacMessage();
		agentSearchTemplate.setProtocol(DirectoryAgentNodeBean.SEARCH_REQUEST_PROTOCOL_ID);
		WHITEPAGES_SEARCH_MESSAGETEMPLATE = agentSearchTemplate;

		JiacMessage remoteActionTemplate = new JiacMessage();
		remoteActionTemplate.setProtocol(REMOTEACTION_PROTOCOL_ID);
		WHITEPAGES_REMOTEACTION_MESSAGETEMPLATE = remoteActionTemplate;

		JiacMessage refreshMessage = new JiacMessage();
		refreshMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);
		WHITEPAGES_REFRESH_MESSAGETEMPLATE = refreshMessage;
	}


	public DirectoryAccessBean() {
	}


	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to initialize this AccessBean and make it listen
	 */
	public void doInit() throws Exception{
		super.doInit();
		_searchRequestHandler = new SearchRequestHandler();
		_actionRequestHandler = new ActionRequestHandler();
		_remoteActionHandler = new RemoteActionHandler();
		String messageboxName = thisAgent.getAgentNode().getUUID() + DirectoryAgentNodeBean.SEARCHREQUESTSUFFIX;
		directoryAddress = CommunicationAddressFactory.createMessageBoxAddress(messageboxName);
		_autoenlistActionTemplates = new ArrayList<IActionDescription>();
		_autoEnlister = new AutoEnlister();
		_refreshAgent = new RefreshAgent();
		
	}

	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to get this AccessBean started up and running
	 */
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

	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to stop this AccessBean, so it...
	 * <ul> 
	 * 	<li> won't receive any more messages</li>
	 * 	<li> stops to check for actions to update</li>
	 * 	<li> will abort all ongoing searchRequests and write results to the memory</li>
	 * 	<li> will remove all offered actions from the directory </li>
	 * 	<li> all remote actions still waiting for a result will be canceled, ActionResults(Failures) will be written into the memory for them</li>
	 * 	<li> erase all Data</li>
	 * </ul> 
	 * 
	 */
	public void doStop() throws Exception{
		super.doStop();
		log.debug("stopping DirectoryAccessBean");
		_timer.cancel();
		memory.detach(_searchRequestHandler);
		memory.detach(_remoteActionHandler);
//		memory.detach(_refreshAgent);

		synchronized (_requestID2ActionMap){
			for (String key : _requestID2ActionMap.keySet()){
				DoAction searchAction = _requestID2ActionMap.remove(key);
				ActionResult result = cancelAction(searchAction);
				memory.write(result);
			}
		}
		synchronized(_autoenlistActionTemplates){
			_autoEnlister.removeActionTemplates(_autoenlistActionTemplates);
			_autoenlistActionTemplates.clear();
		}

		_actionRequestHandler.removeAllActionsFromDirectory();
		_remoteActionHandler.cancelAllRemoteActions();

	}

	/**
	 * Method of the LifeCycle Interface
	 * This method will be called to cleanup this AccessBean after stoping it
	 * All data will be erased.
	 */
	public void doCleanup() throws Exception{
		// nothing to do yet
		super.doCleanup();
		_requestID2ActionMap.clear();
		_requestID2ResponseMap.clear();
		_offeredActions.clear();
		_actionRequestHandler = null;
		_autoEnlister = null;
		_searchRequestHandler = null;
		_refreshAgent = null;
		_remoteActionHandler = null;
	}

	/**
	 * returns all Actions provided by this Bean
	 */
	public List<? extends Action> getActions(){
		List<Action> actions = new ArrayList<Action>();

		/**
		 * Action to search for IFacts within the directory(s) on the AgentNode(s).
		 * Input Parameters:
		 * <ul>
		 * 	<li>IFact 	e.g. implementation of IActionDescription or IAgentDescription </li>
		 * 	<li>Boolean	decides if this search <b>isGlobal</b>, if false search will be only within directory of local AgentNode </li>
		 * 	<li>Long	OPTIONAL gives search a timeout. Makes it possible to decide how long to wait for responses in global search </li>
		 * </ul>
		 * 
		 * Result Types:
		 * 	List of IFacts depending on the IFact-implementation the search was started with
		 * 	
		 */
		Action action = new Action(ACTION_REQUEST_SEARCH, this, new Class<?>[]{IFact.class, Boolean.class, Long.class}, new Class<?>[]{List.class});
		actions.add(action);

		/**
		 * Action to add another Action to the local Directory
		 * Input Parameter:
		 * 	IActionDescription or implementation to store
		 * 
		 * ResultTypes: 
		 * 	none
		 */
		action = new Action(ACTION_ADD_ACTION_TO_DIRECTORY, this, new Class<?>[]{IActionDescription.class}, null);
		actions.add(action);

		/**
		 * Action to remove an Action from the local Directory
		 * Input Parameter:
		 * 	IActionDescription or implementation to remove from the local Directory
		 */
		action = new Action(ACTION_REMOVE_ACTION_FROM_DIRECTORY, this, new Class<?>[]{IActionDescription.class}, null);
		actions.add(action);

		/**
		 * Action to add ActionTemplates. All Actions that are provided within the Agent that match these templates will
		 * be offered through the directory on the local AgentNode. The AccessBean checks for changes on Actions matching
		 * with the templates stored within it regulary.
		 * 
		 * Input Parameter:
		 * 	List of IActionDescriptions. All entries will be added to the List
		 * 
		 * Result Types:
		 * 	none
		 */
		action = new Action(ACTION_ADD_AUTOENTLISTMENT_ACTIONTEMPLATE, this, new Class<?>[] {List.class}, null);
		actions.add(action);

		/**
		 * Action to remove ActionTemplates. All Actions that are matching these templates will be removed from
		 * from the local directory. 
		 * 
		 * Input Parameter:
		 * 	List of IActionDescriptions. All entries will be removed from the List
		 * 
		 * Result Types:
		 * 	none
		 */
		action = new Action(ACTION_REMOVE_AUTOENTLISTMENT_ACTIONTEMPLATE, this, new Class<?>[] {List.class}, null);
		actions.add(action);

		return actions;
	}



	/**
	 * This method get's all doActions for actions this bean provides and forwards them to the places where they are processed
	 */
	@SuppressWarnings("unchecked")
	public void doAction(DoAction doAction){
		log.debug("Received DoAction... decoding begins");


		Object[] params = doAction.getParams();
		String actionName= doAction.getAction().getName();

		/*
		 * If a Search is started it will be handled here 
		 */
		if (actionName.equalsIgnoreCase(ACTION_REQUEST_SEARCH)){
			log.debug("doAction is a SearchRequest");

			Object[] actionParams = doAction.getParams();
			// Check if parameter have minimum length as the last parameter (timeToSearch) is optional
			if (actionParams.length >= 2){
				if ((actionParams[0] instanceof IFact) && (actionParams[1] instanceof Boolean)){
					IFact template = (IFact) actionParams[0];
					Boolean isGlobal = (Boolean) actionParams[1];
					if (actionParams.length > 2){
						/*
						 * if time to search is set, it has to be set within the Session, 
						 * so a timeout will happen at the right moment, ending the search
						 */

						Long timeToSearch = (Long) actionParams[2];
						doAction.getSession().setTimeToLive(timeToSearch);

					}
					SearchRequest request = new SearchRequest(template);
					request.setID(doAction.getSessionId());
					/*
					 *  put running search into the Map to make it possible to find the 
					 *  original doAction for ActionResult creation purposes later when
					 *  the reply is coming in.
					 */
					_requestID2ActionMap.put(request.getID(), doAction);
					if (isGlobal){
						/*
						 *  if the search is global, responses from different AgentNodes have to be
						 *  collected here.
						 */
						_requestID2ResponseMap.put(request.getID(), new ArrayList<IFact>());
					}
					// now let's get the SearchRequest out and to the AgentNode
					JiacMessage message = new JiacMessage(request);
					message.setProtocol(DirectoryAgentNodeBean.SEARCH_REQUEST_PROTOCOL_ID);
					message.setHeader("isGlobal", isGlobal.toString());

					Serializable[] newParams = {message, directoryAddress};
					DoAction send = _sendAction.createDoAction(newParams, _resultDump);

					log.debug("sending message with searchrequest to directory " + message);
					memory.write(send);
				} 
			} else {
				log.error("Request for search was called with false no. of arguments. Arguments are as follows:");
				log.error("First argument: IFact so search for; second Argument: boolean is this search global or just on that agentnode?");
				log.error("Third Argument: (OPTIONAL) TimeToSearch. Default: 60 seconds");
				log.error("given Arguments were: " + actionParams);
			}




			/*
			 * if an Action is added to the Directory it will be handled here
			 */
		} else if (actionName.equalsIgnoreCase(ACTION_ADD_ACTION_TO_DIRECTORY)){
			log.debug("doAction is an Action to add to the Directory");	
			_actionRequestHandler.addActionToDirectory((Action) params[0]);

			/*
			 * if an Action has to be removed from the Directory it will be handled here
			 */
		} else if (actionName.equalsIgnoreCase(ACTION_REMOVE_ACTION_FROM_DIRECTORY)){
			log.debug("doAction is an Action to remove to the Directory");
			_actionRequestHandler.removeActionFromDirectory((Action) params[0]);

			/*
			 * if an Actiontemplate should be added to the autoenlistment list it will be handled here 
			 */
		} else if (actionName.equalsIgnoreCase(ACTION_ADD_AUTOENTLISTMENT_ACTIONTEMPLATE)){
			List<IActionDescription> templatesToAdd = (List<IActionDescription>) params[0];
			_autoEnlister.addActionTemplates(templatesToAdd);

			/*
			 * if an Actiontemplate should be removed from the autoenlistment list it will be handled here
			 */
		} else if (actionName.equalsIgnoreCase(ACTION_REMOVE_AUTOENTLISTMENT_ACTIONTEMPLATE)){
			List<IActionDescription> templatesToRemove = (List<IActionDescription>) params[0];
			_autoEnlister.removeActionTemplates(templatesToRemove);

			/*
			 * All other Actions this AccessBean is set as providerbean are remote Actions these will
			 * be handled here
			 */
		} else {
			log.debug("doAction is an Action that has to be invoked remotely");
			_remoteActionHandler.invokeRemoteAction(doAction);
		}
	}

	/**
	 * if an Action has a timeout or should be aborted this Method will do the actual work outside of the <code>SimpleExecutioncycle</code>
	 */
	@Override
	public ActionResult cancelAction(DoAction doAction) {
		log.debug("DoAction has timeout!");

		synchronized(_requestID2ActionMap){
			if (_requestID2ActionMap.containsKey(doAction.getSessionId())){
				//if still waiting for this action to finish get it out of the map
				DoAction sourceAction = _requestID2ActionMap.remove(doAction.getSessionId());

				if (sourceAction != null){
					ActionResult result = null;
					// now let's check if answers for this request were stored
					ArrayList<IFact> results = _requestID2ResponseMap.remove(doAction.getSessionId());
					if (results != null){
						log.debug("DoAction was global SearchRequest with replys ... writing result.");
						// DoAction was global SearchRequest
						result = new ActionResult(sourceAction, new Serializable[] {results});
					} else {
						String owner = sourceAction.getSource().toString();
						log.warn("SearchRequest from " + owner + " has timeout");
						// DoAction was local SearchRequest or global without answers
						result = new ActionResult(sourceAction, new TimeoutException("Failure due to Timeout for action " + sourceAction));
					}

					return result;

				} else {
					log.warn("tried to cancel non existing doAction: " + doAction.getAction().getName());
					return null;
				}
			}else {
				return _remoteActionHandler.cancelRemoteAction(doAction);
			}
		}
	}

//	/**
//	* Starts a search for DirectoryEntrys that are conform to the template given
//	* @param <E> extends IFact
//	* @param template the template to search for
//	*/
//	public <E extends IFact> void requestSearch(E template, Boolean isGlobal){
//	System.err.println("SEARCHREQUEST VIA DIRECT INVOCATION ! ! !");
//	log.debug("Received SearchRequest via direct invocation. Searching for Agents with template: " + template);
//	_searchRequestHandler.requestSearch(template, isGlobal);
//	}

	/**
	 * sets the intervall in milliseconds after which the autoenlistener will check for changes in the present actions of the agent
	 * Default is 2000 ms
	 */
	public void setAutoEnlisteningInterval(long autoEnlisteningInterval){
		_autoEnlisteningInterval = autoEnlisteningInterval;
	}

	/**
	 * sets the time in milliseconds after the start of this bean which shall look for changes in the present actions of the agent for the first time
	 * Default is 2000 ms 
	 */
	public void setFirstAutoEnlistening(long firstAutoEnlistening){
		_firstAutoEnlistening = firstAutoEnlistening;
	}

	/**
	 * checks if offered actions are still present at the agent
	 */
	private synchronized void checkActionPresence(){
		for (IActionDescription action : _offeredActions){
			// if action isn't present anymore...
			if (memory.read(action) == null){
				// remove it and discard it from the directory
				_offeredActions.remove(action);
				_actionRequestHandler.removeActionFromDirectory(action);
			}
		}
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

						if ((_requestID2ResponseMap.containsKey(request.getID())) && (response.getResult() != null)){
							// if SearchRequest was global just add results to the others already stored
							List<IFact> results = _requestID2ResponseMap.get(request.getID());

							//but don't forget to add our AccessBean as providerbean for remoteActions
							for (IFact fact : response.getResult()){
								if (fact instanceof Action){
									Action act = (Action) fact;
									act.setProviderBean(_myAccessBean);
									results.add(act);
								} else {
									results.add(fact);
								}
							}
							return;

						} else {
							// The SearchRequest was local so it's time to let the source know the results
							DoAction sourceDoAction = _requestID2ActionMap.remove(request.getID());

							if (sourceDoAction != null){
								// if request exists and hasn't timed out yet
								ArrayList<IFact> result = new ArrayList<IFact>();
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

								ActionResult actionResult = ((Action)sourceDoAction.getAction()).createActionResult(sourceDoAction, new Serializable[] {result});
								log.debug("DirectoryAccessBean is writing actionResult: " + actionResult);

								memory.write(actionResult);

							}
						} 
					}
				}
			}
		}

	}


	/**
	 * Inner Class that handles adding & removal of Actions to the Directory 
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	@SuppressWarnings("serial")
	private class ActionRequestHandler {


		/**
		 * adds an <code>Action</code> to the Directory on the <code>AgentNode</code>
		 * 
		 * @param actionDesc	implements IActionDescription
		 */
		public void addActionToDirectory(IActionDescription actionDesc){
			JiacMessage message = new JiacMessage(actionDesc);
			message.setProtocol(DirectoryAgentNodeBean.ADD_ACTION_PROTOCOL_ID);
			Serializable[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, _resultDump);

			log.debug("sending Message to register action in directory " + message);
			memory.write(send);
			synchronized(_offeredActions){
				_offeredActions.add(actionDesc);
			}
		}

		/**
		 * removes an <code>Action</code> to the Directory on the <code>AgentNode</code>
		 * 
		 * @param actionDesc	implements IActionDescription
		 */
		public void removeActionFromDirectory(IActionDescription actionDesc){
			JiacMessage message = new JiacMessage(actionDesc);
			message.setProtocol(DirectoryAgentNodeBean.REMOVE_ACTION_PROTOCOL_ID);
			Serializable[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, _resultDump);

			log.debug("sending Message to remove action from directory " + message);
			memory.write(send);
			synchronized(_offeredActions){
				_offeredActions.remove(actionDesc);
			}
		}

		/**
		 * removes <b>all</b> <code>Action</code>s from the Directory that were offered through this <code>DirectoryAccessBean</code>
		 */
		public void removeAllActionsFromDirectory(){
			synchronized(_offeredActions){
				for (IActionDescription actionDesc : _offeredActions){
					JiacMessage message = new JiacMessage(actionDesc);
					message.setProtocol(DirectoryAgentNodeBean.REMOVE_ACTION_PROTOCOL_ID);
					Serializable[] params = {message, directoryAddress};
					DoAction send = _sendAction.createDoAction(params, _resultDump);

					log.debug("sending Message to remove action from directory " + message);
					memory.write(send);
				}
				_offeredActions.clear();
			}
		}
	}


	/**
	 * Inner Class that handles invocation and result-delivery of remoteactions 
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	@SuppressWarnings("serial")
	private class RemoteActionHandler implements SpaceObserver<IFact>, ResultReceiver {

		/** 
		 * stores RemoteActions, that were received through messages from other agents 
		 * and are currently processed by other beans of this Agent
		 */ 
		private HashMap<String, SessionData> openSessionsFromClients = null;
		/** 
		 * stores RemoteActions, that were received through the agents memory and holding the connection to the actual
		 * creator of that doActions so Results can handed over to them.
		 */ 
		private HashMap<String, DoAction> openSessionsToProviders = null;

		/**
		 * standard constructor method
		 */
		public RemoteActionHandler() {
			openSessionsFromClients = new HashMap<String, SessionData>();
			openSessionsToProviders = new HashMap<String, DoAction>();
		}

		/**
		 * Cancels a RemoteAction in case of a timeout
		 * 
		 * @param remoteAction
		 * @return
		 */
		public ActionResult cancelRemoteAction(DoAction remoteAction){
			log.debug("Canceling remoteAction");
			synchronized(openSessionsToProviders){
				DoAction sourceAction = openSessionsToProviders.remove(remoteAction.getSessionId());
				
				if (sourceAction != null){
					if (sourceAction.getSource() != null){
						String owner = sourceAction.getSource().toString();
						log.warn("RemoteAction " + remoteAction.getAction().getName() + " from owner " + owner + " has timeout");

						ActionResult result = new ActionResult(sourceAction, new TimeoutException("Failure due to Timeout for action " + sourceAction));
						return result;
					} else {
						log.warn("RemoteAction " + remoteAction.getAction().getName() + " without source has timeout");
						return null;
					}

				} else {
					log.warn("tried to cancel non existing remote doAction: " + remoteAction.getAction().getName());
					return null;
				} 
			}
		}

		/**
		 * Cancels all still open remoteActions
		 */
		public void cancelAllRemoteActions(){
			synchronized(openSessionsToProviders){
				for (String key : openSessionsToProviders.keySet()){
					DoAction actionToCancel = openSessionsToProviders.get(key);

					ActionResult result = new ActionResult(actionToCancel, new TimeoutException("Failure due to ordered stop of AccessBean"));

					memory.write(result);
				}
			}
		}



		/**
		 * Gets a DoAction that has to be invoked remotely
		 * starts a search for the real provider in the directory
		 * actual invocation will be done within the notify-method
		 * 
		 * @param doAction
		 */
		public void invokeRemoteAction (DoAction doAction){
			if (doAction.getAction() != null ) {
				log.debug("invoking remoteAction " + doAction.getAction());

				JiacMessage message = new JiacMessage(doAction);
				message.setProtocol(REMOTEACTION_PROTOCOL_ID);

				ICommunicationAddress address = null;
				if (doAction.getAction().getProviderDescription() != null){
					if (doAction.getAction().getProviderDescription().getMessageBoxAddress() != null){
						// so there is indeed a messageboxaddress.. wonderful, so let's use it.
						address = doAction.getAction().getProviderDescription().getMessageBoxAddress();
					} else {
						log.error("Action is not valid! No MessageBoxAddress attached! Action: " + doAction.getAction());
						if (doAction.getAction().getResultTypeNames() != null){
							if (!doAction.getAction().getResultTypeNames().isEmpty()){
								ActionResult result = new ActionResult(doAction, new DirectoryAccessException("No MessageBoxAddress attached"));
								memory.write(result);
								return;
							}
						} else {
							log.error("There aren't ResultTypes either!");
						}
					}
				} else {
					log.error("Action is not valid! No ProviderDescription attached! Action: " + doAction.getAction());
					if (doAction.getAction().getResultTypeNames() != null){
						if (!doAction.getAction().getResultTypeNames().isEmpty()){
							ActionResult result = new ActionResult(doAction, new DirectoryAccessException("No ProviderDescription attached"));
							memory.write(result);
							return;
						}
					} else {
						log.error("There aren't ResultTypes either!");
					}
				}

				Serializable[] params = {message, address};
				DoAction send = _sendAction.createDoAction(params, _resultDump);
				synchronized(openSessionsToProviders){
					openSessionsToProviders.put(doAction.getSessionId(), doAction);
				}
				log.debug("sending DoAction to provider of remoteAction");
				memory.write(send);

			}
		}


		/**
		 * Receives messages with remoteActioncalls to invoke locally, results of such actions etc.
		 * and handles them
		 */
		@Override
		@SuppressWarnings("unchecked")
		public void notify(SpaceEvent<? extends IFact> event) {
			if(event instanceof WriteCallEvent) {
				WriteCallEvent wceTemp = (WriteCallEvent) event;
				if (wceTemp.getObject() instanceof JiacMessage){
					log.debug("Got RemoteActionMessage...");
					JiacMessage message = (JiacMessage) wceTemp.getObject();

					if (message.getPayload() instanceof NoSuchActionException){
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
						Serializable[] params = doAction.getParams();

						Action foundAction = (Action) memory.read(doAction.getAction());

						if (foundAction == null) {
							log.debug("Requested Action doesn't exist on this Agent -- FAILURE!");
							ActionNotPresentException exception = new ActionNotPresentException(doAction.getAction());

							Action missingAction = (Action) doAction.getAction();
							ActionResult result = missingAction.createActionResult(doAction, new Serializable[] {exception});

							this.receiveResult(result);

						} else {
							// so after finding it let's create the actual DoAction for our agent here
							remoteDoAction = foundAction.createDoAction(params, this);
							synchronized (openSessionsFromClients) {
								openSessionsFromClients.put(remoteDoAction.getSessionId(), new SessionData(doAction, message.getSender()));	
							}
							memory.write(remoteDoAction);

							log.debug("RemoteAction written to Agents Memory");
						}

					} else if (message.getPayload() instanceof ActionResult && ((message = memory.remove(message)) != null)){
						log.debug("got resultmessage from remoteAction, result reads: " + (ActionResult) message.getPayload());
						ActionResult result = (ActionResult) message.getPayload();
						DoAction doAction = null;
						synchronized(openSessionsToProviders){
							doAction = openSessionsToProviders.remove(result.getSessionId());
						}

						if(doAction != null) {
							log.debug("Found doAction for that result. Now delivering Actionresult to " + doAction.getSource());
							result.setSource(doAction);
							memory.write(result);
						} else {
							log.debug("Discarding ActionResult for no more existing DoAction!");
						}
					}
				} 
			}
		}


		/**
		 * Implementation of ResultReceiver. Gets a result and handles it.
		 */
		@Override
		public void receiveResult(ActionResult result) {
			log.debug("Got Result for Remote Action! Actionname reads: " + result.getAction().getName());

			SessionData sessionData = null;
			synchronized (openSessionsFromClients) {
				sessionData = openSessionsFromClients.remove(result.getSessionId());	
			}
			if (sessionData != null){
				ICommunicationAddress recipient = sessionData.clientAddress;

				Action providerAction = (Action) sessionData.clientSource.getAction();
				ActionResult remoteResult;
				if (result.getResults() != null){
					remoteResult = providerAction.createActionResult(sessionData.clientSource, result.getResults());
				} else {
					remoteResult = new ActionResult(sessionData.clientSource, result.getFailure());
				}

				if(recipient != null) {
					log.debug("Indeed waiting for a result for that remoteaction!");
					JiacMessage resultMessage = new JiacMessage(remoteResult);
					resultMessage.setProtocol(REMOTEACTION_PROTOCOL_ID);

					Serializable[] params = {resultMessage, recipient};
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

//		/**
//		* getter for local beanname
//		*/
//		@Override
//		public String getBeanName() {
//		return _myAccessBean.getBeanName();
//		}

//		/**
//		* setter for local beanname
//		*/
//		@Override
//		public void setBeanName(String name) {
//		_myAccessBean.setBeanName(name);
//		}

	}

	/**
	 * 
	 * Inner Class that handles ActionTemplates and automatically once at the beginning of each interval...
	 * <ul>
	 * <li> adds actions that are matching to the templates to the Directory </li>
	 * <li> removes actions that are no longer present on the agent (and were formerly added) from the directory </li>
	 * <li> if an ActionTemplate is removed, removes all actions that are matching on that template and were formerly offered from the directory </li>
	 * </ul>
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
	//TODO ThreadPool?? Ask Marcel about it .. later
	private class AutoEnlister extends TimerTask {

		public void run() {

			synchronized (_offeredActions) {
				// check if offered actions are still present at the agent
				checkActionPresence();

				// now check for something new
				for (IActionDescription actionTemplate : _autoenlistActionTemplates){
					Set<IActionDescription> actions = memory.readAll(actionTemplate);
					for (IActionDescription action : actions){
						if(!_offeredActions.contains(action)){
							_offeredActions.add(action);
							_actionRequestHandler.addActionToDirectory(action);
						}
					}
				}
			}
		}

		/**
		 * adds a template for autoenlistment 
		 * @param actionTemplatesToAdd
		 */
		public void addActionTemplates(List<IActionDescription> actionTemplatesToAdd) {
			synchronized(_autoenlistActionTemplates){
				_autoenlistActionTemplates.addAll(actionTemplatesToAdd);
			}
		}

		/**
		 * removes a template for autoenlistment and all <code>Action</code>s that are matching on that template
		 * that are formerly added to the directory from it.
		 * 
		 * @param templatesToRemove
		 */
		public void removeActionTemplates(List<IActionDescription> templatesToRemove){
			synchronized (_offeredActions) {
				synchronized(_autoenlistActionTemplates){
					_autoenlistActionTemplates.removeAll(templatesToRemove);
				}

				synchronized(_offeredActions){
					for (IActionDescription removeTemplate: templatesToRemove){
						Set<IActionDescription> actions = memory.readAll(removeTemplate);
						for (IActionDescription action : actions){
							if (_offeredActions.remove(action))
								_actionRequestHandler.removeActionFromDirectory(action);
						}
					}
				}
			}
		}
	}

	/**
	 * InnerClass that replys to refreshmessages from the AgentNode either on messages regarding actions or on such ones regarding this agent
	 * 
	 * @author Martin Loeffelholz
	 *
	 */
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
						if (message.getPayload() instanceof Action){
							// check if offered actions are still present at the agent
							checkActionPresence();

							Set<IFact> facts = new HashSet<IFact>();
							facts.addAll(_offeredActions);

							JiacMessage refreshMessage = new JiacMessage(new FactSet(facts));
							refreshMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);
							DoAction send = _sendAction.createDoAction(new Serializable[] {refreshMessage, message.getSender()}, _resultDump);
							memory.write(send);
						} else if (message.getPayload() instanceof AgentDescription){
							JiacMessage pingMessage = new JiacMessage(thisAgent.getAgentDescription());
							pingMessage.setProtocol(DirectoryAgentNodeBean.REFRESH_PROTOCOL_ID);
							DoAction send = _sendAction.createDoAction(new Serializable[] {pingMessage, message.getSender()}, _resultDump);
							memory.write(send);
						}
					} 
				}
			}
		}
	}

		/**
		 * wrapper class to connect a doAction with the address of an Agent
		 * 
		 * @author Martin Loeffelholz
		 *
		 */
		private class SessionData {
			public DoAction clientSource;
			public ICommunicationAddress clientAddress;

			public SessionData(DoAction source, ICommunicationAddress address){
				clientSource = source;
				clientAddress = address;
			}
		}


	
}