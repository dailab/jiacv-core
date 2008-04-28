package de.dailab.jiactng.agentcore.comm.wp;


import java.util.ArrayList;
import java.util.List;

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
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.ThisAgentDescription;

public class RemoteActionProviderBean extends AbstractAgentBean implements IEffector{
	
	public final static String ACTION_TIMEOUT_TEST = "de.dailab.jiactng.agentcore.comm.wp.RemoteActionTestBean#timeoutTest";
	public final static String ACTION_GET_SOME_RESULT = "de.dailab.jiactng.agentcore.comm.wp.RemoteActionTestBean#getSomeResult";

	private Action _sendAction;
	private String _messageboxName;
	private ICommunicationAddress _directoryAddress;
	
	private final ResultDump _resultDump = new ResultDump();
	
	@Override
	public void doInit() throws Exception {
		super.doInit();
	}
	
	@Override
	public void doStart() throws Exception {
		super.doStart();
		_messageboxName = thisAgent.getAgentNode().getUUID() + DirectoryAgentNodeBean.SEARCHREQUESTSUFFIX;
		_directoryAddress = CommunicationAddressFactory.createMessageBoxAddress(_messageboxName);
		_sendAction = memory.read(new Action(ICommunicationBean.ACTION_SEND,null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
		
		Action timeout = new Action(ACTION_TIMEOUT_TEST, this, new Class[] {}, new Class[] {Object.class});
		Action getSomeResult = new Action(ACTION_GET_SOME_RESULT, this, new Class[] {Object.class}, new Class[] {Object.class});
		
		AgentDescription myAgentDescription = memory.read(new ThisAgentDescription());
		timeout.setProviderDescription(myAgentDescription);
		getSomeResult.setProviderDescription(myAgentDescription);
		
		JiacMessage timeoutMessage = new JiacMessage(timeout);
		JiacMessage getSomeResultMessage = new JiacMessage(getSomeResult);
		
		timeoutMessage.setProtocol(DirectoryAgentNodeBean.ADD_ACTION_PROTOCOL_ID);
		getSomeResultMessage.setProtocol(DirectoryAgentNodeBean.ADD_ACTION_PROTOCOL_ID);
		
		Object[] timeoutParams = {timeoutMessage, _directoryAddress};
		Object[] getSomeResultParams = {getSomeResultMessage, _directoryAddress};
		
		DoAction sendTimeout = _sendAction.createDoAction(timeoutParams, _resultDump);
		DoAction sendSomeResult = _sendAction.createDoAction(getSomeResultParams, _resultDump);
		
		memory.write(sendTimeout);
		memory.write(sendSomeResult);
	}

	@Override
	public List<? extends Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		Action timeout = new Action(ACTION_TIMEOUT_TEST, this, new Class[] {}, new Class[] {Object.class});
		Action getSomeResult = new Action(ACTION_GET_SOME_RESULT, this, new Class[] {Object.class}, new Class[] {Object.class});
		
		actions.add(timeout);
		actions.add(getSomeResult);
		
		return actions;
	}
	
	@Override
	public void doAction(DoAction doAction) throws Exception {
		Object[] params = doAction.getParams();
		String actionName= doAction.getAction().getName();
		
		if (actionName.equalsIgnoreCase(ACTION_TIMEOUT_TEST)){
			Object obj = timeoutTest();
			ActionResult result = ((Action) doAction.getAction()).createActionResult(doAction, new Object[] {obj});
			log.debug("writing result for timeoutTest");
			memory.write(result);
		} else if (actionName.equalsIgnoreCase(ACTION_GET_SOME_RESULT)){
			Object obj = getSomeResult(params[0]);
			ActionResult result = ((Action) doAction.getAction()).createActionResult(doAction, new Object[] {obj});
			log.debug("writing result for getSomeResult");
			memory.write(result);
		}
		
	}
	
	/*
	 * To be flexible just let's wait until the next action is coming in.
	 */ 
	public Object timeoutTest(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * will just return what's sent to it
	 */
	public Object getSomeResult(Object result) {
		log.debug("somebody wants a result? ... tztz");
		return result;
	}

}
