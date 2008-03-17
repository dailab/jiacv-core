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
import de.dailab.jiactng.agentcore.knowledge.IFact;

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

	private int _timeoutMillis = 2500;
	private static final IJiacMessage WHITEPAGESMESSAGETEMPLATE;

	private ICommunicationAddress directoryAddress = null;
	private SearchRequestHandler _searchRequestHandler = null;

	private Action _sendAction = null;
	private Map<String, DoAction> _requestID2ActionMap = new HashMap<String, DoAction>();

	static {
		JiacMessage template = new JiacMessage();
		template.setProtocol(DirectoryAgentNodeBean.PROTOCOL_ID);
		WHITEPAGESMESSAGETEMPLATE = template;
	}


	public DirectoryAccessBean() {
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

	/**
	 * Check if one of the pending requests is over timeout
	 */
	@Override
	public void execute() {
		super.execute();
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

	public void doInit() throws Exception{
		super.doInit();
		_searchRequestHandler = new SearchRequestHandler();
		String messageboxName = thisAgent.getAgentNode().getName() + DirectoryAgentNodeBean.SEARCHREQUESTSUFFIX;
		directoryAddress = CommunicationAddressFactory.createMessageBoxAddress(messageboxName);
	}

	public void doStart() throws Exception{
		super.doStart();
		log.debug("starting DirectoryAccessBean");
		memory.attach(_searchRequestHandler, WHITEPAGESMESSAGETEMPLATE);
		_sendAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.ICommunicationBean#send",null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
		this.setExecuteInterval( _timeoutMillis /2);
	}

	public void doStop() throws Exception{
		super.doStop();
		log.debug("stopping DirectoryAccessBean");
		memory.detach(_searchRequestHandler);
		this.setExecuteInterval(- (_timeoutMillis/2));
	}

	public void doCleanup() throws Exception{
		// nothing to do yet
		super.doCleanup();
	}


	public List<? extends Action> getActions(){
		List<Action> actions = new ArrayList<Action>();

		Class<?>[] input = {IFact.class};
		Class<?>[] result = {List.class};
		Action action = new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#requestSearch", this, input, result);
		actions.add(action);
		return actions;
	}

	public void doAction(DoAction doAction){
		log.debug("Received SearchRequest through Action");
		Object[] params = doAction.getParams();
		if (params[0] instanceof SearchRequest){
			log.debug("parameter is actual SearchRequest");
			SearchRequest request = (SearchRequest) params[0];
			request.setID(doAction.getSessionId());
			_requestID2ActionMap.put(request.getID(), doAction);
			if (this.getExecuteInterval() < 0){
				log.debug("activating timeoutchecking. Next Check will commence in " + _timeoutMillis/2 + " intervalls");
				this.setExecuteInterval(_timeoutMillis/2);
			}
			_searchRequestHandler.requestSearch(request);
		}


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
			message.setProtocol(DirectoryAgentNodeBean.PROTOCOL_ID);

			Object[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, null);

			log.debug("DirectoryAccessBean sends message to directory: " + message);
			memory.write(send);
		}
		
		

		/**
		 * receives the answers from the directory and processes them. 
		 * Then puts an actionresult into the agents memory
		 */
		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			if(event instanceof WriteCallEvent) {
				WriteCallEvent wceTemp = (WriteCallEvent) event;
				if (wceTemp.getObject() instanceof IJiacMessage){
					IJiacMessage message = (IJiacMessage) wceTemp.getObject();


					if (message.getPayload() instanceof SearchResponse){
						log.debug("DirectoryAccessBean: Got reply to SearchRequest");

						SearchResponse response = (SearchResponse) message.getPayload();
						SearchRequest request = response.getSearchRequest();

						log.debug("processing reply on SearchRequest with ID " + request.getID());

						DoAction sourceAction = _requestID2ActionMap.remove(request.getID());

						if (sourceAction != null){
							// if request exists and hasn't timed out yet
							List<IFact> result = new ArrayList<IFact>();
							if (response.getResult() != null){
								result.addAll(response.getResult());
							}

							ActionResult actionResult = sourceAction.getAction().createActionResult(sourceAction, result.toArray());
							log.debug("DirectoryAccessBean is writing actionResult");

							memory.write(actionResult);
						}
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
}
