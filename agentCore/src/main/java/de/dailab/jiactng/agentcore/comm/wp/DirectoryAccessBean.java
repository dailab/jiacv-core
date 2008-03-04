package de.dailab.jiactng.agentcore.comm.wp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;

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

public class DirectoryAccessBean extends AbstractAgentBean implements
IAgentBean, IEffector {

	private long _timeoutMillis = 2500;
	private int _stdExecutionIntervall = 100;
	
	private ICommunicationAddress directoryAddress = null;
	private SearchRequestHandler _searchRequestHandler = null;
	
	private Action _sendAction = null;
	private Map<IFact, DoAction> _request2ActionMap = new HashMap<IFact, DoAction>();
	
	public DirectoryAccessBean() {
	}

	public DirectoryAccessBean(boolean strict) {
		super(strict);
	}

	public <E extends IFact> void requestSearch(E template){
		_searchRequestHandler.requestSearch(template);
	}

	/**
	 * Check if one of the pending requests is over timeout
	 */
	@Override
	public void execute() {
		super.execute();
		if (_request2ActionMap.keySet().size() > 0){
			synchronized(_request2ActionMap){
				for (IFact key : _request2ActionMap.keySet()){
					SearchRequest request = (SearchRequest) key;

					long creation = request.getCreationTime();
					long now = System.currentTimeMillis();

					if ((now - creation) > _timeoutMillis){
						DoAction action = _request2ActionMap.remove(key);
						ActionResult result = new ActionResult(action, new TimeoutException("Failure due to Timeout for action " + action));
						memory.write(result);
					}
				}
			}
		} else {
			this.setExecuteInterval(-1);
		}
	}
	
	public void doInit(){
		_searchRequestHandler = new SearchRequestHandler();
		String messageboxName = thisAgent.getAgentNode().getName() + DirectoryAgentNodeBean.SEARCHREQUESTSUFFIX;
		directoryAddress = CommunicationAddressFactory.createMessageBoxAddress(messageboxName);
	}

	public void doStart(){
			memory.attach(_searchRequestHandler);
			_sendAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.ICommunicationBean#send",null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
			this.setExecuteInterval(_stdExecutionIntervall);
	}

	public void doStop(){
		memory.detach(_searchRequestHandler);
		this.setExecuteInterval(-_stdExecutionIntervall);
	}

	public void doCleanup(){
		// nothing to do yet
	}

	
	public List<? extends Action> getActions(){
		List<Action> actions = new ArrayList<Action>();
		
		Class<?>[] input = {IFact.class};
		Class<?>[] result = {List.class};
		Action action = new Action("requestSearch", this, input, result);
		actions.add(action);
		
		return actions;
	}

	public void doAction(DoAction doAction){
		
		Object[] params = doAction.getParams();
		if (params[0] instanceof SearchRequest){
			SearchRequest request = (SearchRequest) params[0];
			IFact template = request.getSearchTemplate();
			_request2ActionMap.put(template, doAction);
			if (this.getExecuteInterval() < 0){
				this.setExecuteInterval(_stdExecutionIntervall);
			}
			_searchRequestHandler.requestSearch(request);
		}
		
		
	}

	public void setTimeoutMillis(long timeoutMillis){
		_timeoutMillis = timeoutMillis;
	}
	
	public long getTimeoutMillis(){
		return _timeoutMillis;
	}
	
	private class SearchRequestHandler implements SpaceObserver<IFact> {

		public <E extends IFact> void requestSearch(E template){
		    IJiacMessage message = new JiacMessage(template);
		    
		    Object[] params = {message, directoryAddress};
			DoAction send = _sendAction.createDoAction(params, null);
		    
		    memory.write(send);
		}

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			if (event instanceof IJiacMessage){
				IJiacMessage message = (IJiacMessage) event;
				if (message.getPayload() instanceof SearchResponse){
					SearchResponse response = (SearchResponse) message.getPayload();
					SearchRequest request = response.getSearchRequest();
					
					IFact template = request.getSearchTemplate();
					DoAction sourceAction = _request2ActionMap.remove(template);
					if (sourceAction != null){
						// if request exists and hasn't timed out yet
						List<IFact> result = new ArrayList<IFact>();
						if (response.getResult() != null){
							result.addAll(response.getResult());
						}
						
						Object[] results = {result};
						ActionResult actionResult = sourceAction.getAction().createActionResult(sourceAction, results);

						memory.write(actionResult);
					}
				}
			}
		}
	}
	
	public class TimeoutException extends RuntimeException{
		public TimeoutException(String s){
			super(s);
		}
	}
}
