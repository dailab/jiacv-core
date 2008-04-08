package de.dailab.jiactng.agentcore.comm.wp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

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
public class DirectoryAccessBean extends AbstractAgentBean implements
IAgentBean, IEffector {

	public static final String ACTION_REQUEST_SEARCH = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#requestSearch";
	public static final String ACTION_ADD_ACTION_TO_DIRECTORY = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#addActionToDirectory";
	public static final String ACTION_REMOVE_ACTION_FROM_DIRECTORY = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#removeActionFromDirectory";

	private static final String REMOTEACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#UseRemoteAction";

	private int _timeoutMillis = 2500;
	private static final IJiacMessage WHITEPAGES_AGENTSEARCH_MESSAGETEMPLATE;
	private static final IJiacMessage WHITEPAGES_ACTIONSEARCH_MESSAGETEMPLATE;
	private static final IJiacMessage WHITEPAGES_REMOTEACTION_MESSAGETEMPLATE;

	private ICommunicationAddress directoryAddress = null;
	private SearchRequestHandler _searchRequestHandler = null;
	private ActionRequestHandler _actionRequestHandler = null;
	private RemoteActionHandler _remoteActionHandler = null;
	private RemoteActionWatcher _remoteActionWatcher = new RemoteActionWatcher();
	private final ResultDump _resultDump = new ResultDump();

	private Action _sendAction = null;
	private Map<String, DoAction> _requestID2ActionMap = new HashMap<String, DoAction>();


	static {
		JiacMessage agentSearchTemplate = new JiacMessage();
		agentSearchTemplate.setProtocol(DirectoryAgentNodeBean.AGENT_SEARCH_REQUEST_PROTOCOL_ID);
		WHITEPAGES_AGENTSEARCH_MESSAGETEMPLATE = agentSearchTemplate;

		JiacMessage actionSearchTemplate = new JiacMessage();
		actionSearchTemplate.setProtocol(DirectoryAgentNodeBean.ACTION_SEARCH_REQUEST_PROTOCOL_ID);
		WHITEPAGES_ACTIONSEARCH_MESSAGETEMPLATE = actionSearchTemplate;

		JiacMessage remoteActionTemplate = new JiacMessage();
		remoteActionTemplate.setProtocol(REMOTEACTION_PROTOCOL_ID);
		WHITEPAGES_REMOTEACTION_MESSAGETEMPLATE = remoteActionTemplate;	
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
	}

	public void doStart() throws Exception{
		super.doStart();
		log.debug("starting DirectoryAccessBean");
		memory.attach(_searchRequestHandler, WHITEPAGES_AGENTSEARCH_MESSAGETEMPLATE);
		memory.attach(_actionRequestHandler, WHITEPAGES_ACTIONSEARCH_MESSAGETEMPLATE);
		memory.attach(_remoteActionHandler, WHITEPAGES_REMOTEACTION_MESSAGETEMPLATE);
		memory.attach(_remoteActionWatcher, new DoAction());
		_sendAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.ICommunicationBean#send",null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
		this.setExecuteInterval( _timeoutMillis /2);
	}

	public void doStop() throws Exception{
		super.doStop();
		log.debug("stopping DirectoryAccessBean");
		memory.detach(_searchRequestHandler);
		memory.detach(_actionRequestHandler);
		memory.detach(_remoteActionHandler);
		this.setExecuteInterval(- (_timeoutMillis/2));
	}

	public void doCleanup() throws Exception{
		// nothing to do yet
		super.doCleanup();
	}


	/**
	 * Check if one of the pending requests is over timeout
	 */
	@Override
	public void execute() {
		super.execute();

		// TODO TimeoutManagment for RemoteActions
		log.debug("Collecting timed out SearchRequests, current check-interval is " + this.getExecuteInterval());
		Set<String> toRemove = new HashSet<String>();
		synchronized(_requestID2ActionMap){
			if (_requestID2ActionMap.keySet().size() > 0){	
				// make sure that the Map isn't changed during maintenance
				for (String key : _requestID2ActionMap.keySet()){
					if (key != null){
						DoAction action = _requestID2ActionMap.get(key);
						if (action.getParams()[0] instanceof SearchRequest){
							SearchRequest request =  (SearchRequest) action.getParams()[0];

							long creation = request.getCreationTime();
							long now = System.currentTimeMillis();

							if ((now - creation) > _timeoutMillis){
								String owner = action.getOwner();

								log.warn("SearchRequest from owner " + owner + " has timeout");
								// request is timed out, mark for removal
								toRemove.add(key);
								// not "throw" exception and let the requester know that his request has timed out
								ActionResult result = new ActionResult(action, new TimeoutException("Failure due to Timeout for action " + action));
								memory.write(result);
							}
						}
					} else {
						log.debug("No more Requests pending, timeout-checks are deactivated");
						this.setExecuteInterval(-1);
					}
				}
				// actually remove found timeouts
				for (String key : toRemove) {
					_requestID2ActionMap.remove(key);
				}

			} else {
				log.debug("No more Requests pending, timeout-checks are deactivated");
				this.setExecuteInterval(-1);
			}
		}
	}

	public List<? extends Action> getActions(){
		List<Action> actions = new ArrayList<Action>();

		Class<?>[] input1 = {IFact.class};
		Class<?>[] result = {List.class};
		Action action = new Action(ACTION_REQUEST_SEARCH, this, input1, result);
		actions.add(action);

		input1[0] = IActionDescription.class;
		result = null;
		action = new Action(ACTION_ADD_ACTION_TO_DIRECTORY, this, input1, result);
		actions.add(action);

		input1[0] = IActionDescription.class;
		result = null;
		action = new Action(ACTION_REMOVE_ACTION_FROM_DIRECTORY, this, input1, result);
		actions.add(action);

		return actions;
	}



	public void doAction(DoAction doAction){
		log.debug("Received DoAction... decoding begins");


		Object[] params = doAction.getParams();
		if (doAction.getAction().getName().equalsIgnoreCase(ACTION_REQUEST_SEARCH)){
			log.debug("doAction is a SearchRequest");
			if (params[0] instanceof IFact){
				IFact template = (IFact) params[0];
				SearchRequest request = new SearchRequest(template);
				request.setID(doAction.getSessionId());
				_requestID2ActionMap.put(request.getID(), doAction);
				if (this.getExecuteInterval() < 0){
					log.debug("activating timeoutchecking. Next Check will commence in " + _timeoutMillis/2 + " intervalls");
					this.setExecuteInterval(_timeoutMillis/2);
				}
				_searchRequestHandler.requestSearch(request);
			} 

		} else if (doAction.getAction().getName().equalsIgnoreCase(ACTION_ADD_ACTION_TO_DIRECTORY)){
			log.debug("doAction is an Action to add to the Directory");	
			_actionRequestHandler.addActionToDirectory((Action) params[0]);


		} else if (doAction.getAction().getName().equalsIgnoreCase(ACTION_REMOVE_ACTION_FROM_DIRECTORY)){
			log.debug("doAction is an Action to remove to the Directory");
			_actionRequestHandler.removeActionFromDirectory((Action) params[0]);

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


	public void setTimeoutMillis(int timeoutMillis){
		_timeoutMillis = timeoutMillis;
	}

	public long getTimeoutMillis(){
		return _timeoutMillis;
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
			message.setProtocol(DirectoryAgentNodeBean.AGENT_SEARCH_REQUEST_PROTOCOL_ID);

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


					if (message.getPayload() instanceof SearchResponse){
						log.debug("DirectoryAccessBean: Got reply to AgentSearchRequest");

						SearchResponse response = (SearchResponse) message.getPayload();
						SearchRequest request = response.getSearchRequest();

						log.debug("processing reply on AgentSearchRequest with ID " + request.getID());

						DoAction sourceAction = _requestID2ActionMap.remove(request.getID());

						if (sourceAction != null){
							// if request exists and hasn't timed out yet
							List<IFact> result = new ArrayList<IFact>();
							if (response.getResult() != null){
								result.addAll(response.getResult());
							}

							ActionResult actionResult = ((Action)sourceAction.getAction()).createActionResult(sourceAction, new Object[] {result});
							log.debug("DirectoryAccessBean is writing actionResult: " + actionResult);

							memory.write(actionResult);
						}
					}
				}
			}
		}

	}

	@SuppressWarnings("serial")
	private class ActionRequestHandler implements SpaceObserver<IFact> {

		/**
		 * just gets the SearchRequest to the directory
		 * 
		 * @param <E> extends IFact
		 * @param template of the entrys to look for
		 */

		public <E extends IActionDescription> void addActionToDirectory(E actionDesc){
			JiacMessage message = new JiacMessage(actionDesc);
			message.setProtocol(DirectoryAgentNodeBean.ADD_ACTION_PROTOCOL_ID);
			Object[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, _resultDump);

			log.debug("sending Message to register action in directory " + message);
			memory.write(send);
		}

		public <E extends IActionDescription> void removeActionFromDirectory(E actionDesc){
			JiacMessage message = new JiacMessage(actionDesc);
			message.setProtocol(DirectoryAgentNodeBean.REMOVE_ACTION_PROTOCOL_ID);
			Object[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, _resultDump);

			log.debug("sending Message to register action in directory " + message);
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


					if (message.getPayload() instanceof SearchResponse){
						log.debug("DirectoryAccessBean: Got reply to ActionSearchRequest");

						SearchResponse response = (SearchResponse) message.getPayload();
						SearchRequest request = response.getSearchRequest();

						log.debug("processing reply on ActionSearchRequest with ID " + request.getID());

						DoAction sourceAction = _requestID2ActionMap.remove(request.getID());

						if (sourceAction != null){
							// if request exists and hasn't timed out yet
							List<IFact> result = new ArrayList<IFact>();
							if (response.getResult() != null){
								result.addAll(response.getResult());
							}

							ActionResult actionResult = ((Action)sourceAction.getAction()).createActionResult(sourceAction, result.toArray());
							log.debug("DirectoryAccessBean is writing actionResult for ActionSearchRequest with ID" + request.getID());

							memory.write(actionResult);
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("serial")
	private class RemoteActionHandler implements SpaceObserver<IFact>, ResultReceiver {

		private class RemoteRequest{



			/**  The remoteAction to invoke */
			private DoAction _request = null;

			/** Time of Creation of this SearchRequest in millis */
			private long _creationTime = -1;

			/** SessionID of doAction for reassociation with actionResult */
			private String _ID;

			public RemoteRequest(DoAction doAction) {
				_request = doAction;
				_creationTime = System.currentTimeMillis();
				_ID = doAction.getSessionId();
			}

			public IFact getRequest(){
				return _request;
			}

			public long getCreationTime(){
				return _creationTime;
			}

			synchronized public long getAge(){
				return (System.currentTimeMillis()-_creationTime);
			}

			public void setID(String id){
				_ID = id;
			}

			public String getID(){
				return _ID;
			}

		}

		private Map<String, JiacMessage> sessionID2JiacMessageMap = null;

		public RemoteActionHandler() {
			sessionID2JiacMessageMap = new HashMap<String, JiacMessage>();
		}

		public void processRemoteAction (DoAction doAction){

			if (doAction.getAction() != null ) {
				if (doAction.getAction().getProviderDescription().getMessageBoxAddress() != null) {
					IAgentDescription provider = doAction.getAction().getProviderDescription();
					ICommunicationAddress providerAddress = provider.getMessageBoxAddress();

					JiacMessage message = new JiacMessage(doAction);
					message.setProtocol(REMOTEACTION_PROTOCOL_ID);

					Object[] params = {message, providerAddress};
					DoAction send = _sendAction.createDoAction(params, _resultDump);
					memory.write(send);
				}

			}

		}

		@Override
		@SuppressWarnings("unchecked")
		public void notify(SpaceEvent<? extends IFact> event) {
			if(event instanceof WriteCallEvent) {
				WriteCallEvent wceTemp = (WriteCallEvent) event;
				if (wceTemp.getObject() instanceof JiacMessage){
					log.debug("Got RemoteActionMessage...");
					JiacMessage message = (JiacMessage) (memory.remove((JiacMessage) wceTemp.getObject()));

					if (message.getPayload() instanceof DoAction){
						DoAction doAction = (DoAction) message.getPayload();

						Action action = (Action) doAction.getAction();

						DoAction remoteDoAction;
						Object[] params = doAction.getParams();
						boolean remoteActionFound = false;

						List<Action> actions = thisAgent.getActionList();
						for (Action foundAction : actions){
							if (actionsAreEqual(foundAction, doAction.getAction())){
								remoteDoAction = foundAction.createDoAction(params, this);
								remoteActionFound = true;

								sessionID2JiacMessageMap.put(doAction.getSessionId(), message);
								memory.write(remoteDoAction);
								break;
							}
						}
						if (!remoteActionFound) {
							ActionNotPresentException exception = new ActionNotPresentException(doAction.getAction());
							ActionResult result = action.createActionResult(doAction, new Object[] {exception});
							this.receiveResult(result);

						}
					} else if (message.getPayload() instanceof ActionResult){
						ActionResult result = (ActionResult) message.getPayload();
						JiacMessage archiveMessage = sessionID2JiacMessageMap.remove(result.getSessionId());
						DoAction action = (DoAction) archiveMessage.getPayload();
						result.setSource(action.getSource());
						memory.write(result);
					}
				} 
			}
		}

		private boolean actionsAreEqual(IActionDescription action1, IActionDescription action2){
			boolean equal = (action1 != null)&&(action2 != null); 
			equal = action1.getName().equals(action2.getName()) && equal;
			equal = action1.getInputTypes().equals(action2.getInputTypes()) && equal;
			equal = action1.getResultTypes().equals(action2.getResultTypes()) && equal;
			return equal;
		}

		@Override
		public void receiveResult(ActionResult result) {
			JiacMessage message = sessionID2JiacMessageMap.get(result.getSessionId());
			ICommunicationAddress address = message.getSender();
			
			JiacMessage resultMessage = new JiacMessage(result);
			resultMessage.setProtocol(REMOTEACTION_PROTOCOL_ID);
			
			Object[] params = {message, address};
			DoAction send = _sendAction.createDoAction(params, _resultDump);
			
			memory.write(send);
		}

		@Override
		public String getBeanName() {
			return null;
		}

		@Override
		public void setBeanName(String name) {
		}

	}

	@SuppressWarnings("serial")
	public class RemoteActionWatcher implements SpaceObserver<IFact>{

		@SuppressWarnings("unchecked")
		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			boolean isRemoteAction = true;
			if(event instanceof WriteCallEvent) {
				WriteCallEvent wce = (WriteCallEvent) event;
				if (wce.getObject() instanceof DoAction){
					DoAction doAction = (DoAction) wce.getObject();

					String agentID = doAction.getAction().getProviderDescription().getAid();
					isRemoteAction = !agentID.equals(thisAgent.getAgentId());
//					String message = (isRemoteAction ? "Got Remote Action!" : "Didn't got RemoteAction");

					if (isRemoteAction){
						_remoteActionHandler.processRemoteAction(doAction);
					}
				}
			}
		}

	}



	@SuppressWarnings("serial")
	public class TimeoutException extends RuntimeException{
		public TimeoutException(String s){
			super(s);
		}
	}

	@SuppressWarnings("serial")
	public class ActionNotPresentException extends RuntimeException{
		IActionDescription _actionDesc = null;

		public ActionNotPresentException(IActionDescription actionDesc) {
			super("Action isn't present anymore");
			_actionDesc = actionDesc;
		}
	}
}
