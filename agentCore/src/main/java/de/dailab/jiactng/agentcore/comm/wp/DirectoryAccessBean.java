package de.dailab.jiactng.agentcore.comm.wp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import org.apache.commons.logging.Log;
import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class DirectoryAccessBean extends AbstractAgentBean implements
IAgentBean, IEffector {

	private MessageTransport messageTransport = null;
	private ICommunicationAddress directoryAddress = null;
//	private ICommunicationAddress myAddress = null; 
	private SearchRequestHandler _searchRequestHandler = null;
	
	Action _sendAction = null;
	private Map<IFact, DoAction> _request2ActionMap = new HashMap<IFact, DoAction>();

	public DirectoryAccessBean() {
		setBeanName("DirectoryAccessBean");
		String name = thisAgent.getAgentName() + getBeanName();

//      Die Addresse des Agenten steht ab der Initialisierungsphase im Memory!
//      siehe CommunicationBean
//		myAddress = CommunicationAddressFactory.createMessageBoxAddress(name);
		String boxName = thisAgent.getAgentNode().getName() + DirectoryAgentNodeBean.SEARCHREQUESTSUFFIX;
		
		// TODO: wir muessen einen Weg finden, die Addresse des Verzeichnisses zu dieser
		//       Bean zu kommunizieren. MessageBoxAddressen sollten allerdings nicht auf
		//       Agentenebene erzeugt werden!
		directoryAddress = CommunicationAddressFactory.createMessageBoxAddress(boxName);
	}

	public DirectoryAccessBean(boolean strict) {
		super(strict);
		// TODO Auto-generated constructor stub
	}

	public <E extends IFact> void requestSearch(E template){
		_searchRequestHandler.requestSearch(template);
	}

	public void doInit(){
		_searchRequestHandler = new SearchRequestHandler();
	}

	public void doStart(){
			memory.attach(_searchRequestHandler);
			_sendAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.ICommunicationBean#send",null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
	}

	public void doStop(){
		memory.detach(_searchRequestHandler);
		// nothing to do yet
	}

	public void doCleanup(){
		// nothing to do yet
	}

	public void setMessageTransport(MessageTransport mt){
		messageTransport = mt;
	}

	
	public List<? extends Action> getActions(){
		List<Action> actions = new ArrayList<Action>();
		
		Class<?>[] input = {IFact.class};
		Class<?>[] output = {IFact.class};
		Action action = new Action("requestSearch", this, input, output);
		actions.add(action);
		
		return actions;
	}

	/**
	 * Executes a selected action. This method should be implemented by the
	 * component and be able to deal with each of the registered Actions. Note
	 * that this action is called automatically by the agents kernel, whenever
	 * an action registered by this component should be executed.
	 * 
	 * @see de.dailab.jiactng.agentcore.action.DoAction
	 * @param doAction
	 *            the action-invocation that describes the action to be executed
	 *            as well as its parameters.
	 */
	public void doAction(DoAction doAction){
		// TODO Timeoutmanagment for Actions
		
		Object[] params = doAction.getParams();
		if (params[0] instanceof SearchRequest){
			SearchRequest request = (SearchRequest) params[0];
			IFact template = request.getSearchTemplate();
			_request2ActionMap.put(template, doAction);
			_searchRequestHandler.requestSearch(request);
		}
		
		
	}
	
	private class SearchRequestHandler implements SpaceObserver<IFact> {

		public <E extends IFact> void requestSearch(E template){
//			IJiacMessage message = new JiacMessage(template, myAddress);
//          Absender wird von der CommunicationBean selber gesetzt
		    IJiacMessage message = new JiacMessage(template);
		    
		    Object[] params = {};
			DoAction send = _sendAction.createDoAction(params, null);
		    
		    try {
				
				messageTransport.send(message, directoryAddress);
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void notify(SpaceEvent<? extends IFact> event) {
			if (event instanceof IJiacMessage){
				IJiacMessage message = (IJiacMessage) event;
				if (message.getPayload() instanceof SearchRequest){
					SearchRequest request = (SearchRequest) message.getPayload();
					
					IFact template = request.getSearchTemplate();
					DoAction sourceAction = _request2ActionMap.remove(template);
					Object[] results = request.getResult().toArray();
					ActionResult result = sourceAction.getAction().createActionResult(sourceAction, results);

					memory.write(result);
				}
			}
			
		}
	}
	
}
