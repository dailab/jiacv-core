package de.dailab.jiactng.agentcore.comm.wp;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.ThisAgentDescription;

public class RemoteActionProviderBean extends AbstractAgentBean implements IEffector{
	
	public final static String ACTION_TIMEOUT_TEST = "de.dailab.jiactng.agentcore.comm.wp.RemoteActionTestBean#timeoutTest";
	public final static String ACTION_GET_SOME_RESULT = "de.dailab.jiactng.agentcore.comm.wp.RemoteActionTestBean#getSomeResult";

	private Action _addAction;
	
	private final ResultDump _resultDump = new ResultDump();
	
	@Override
	public void doInit() throws Exception {
		super.doInit();
	}
	
	@Override
	public void doStart() throws Exception {
		super.doStart();
		_addAction = memory.read(new Action(DirectoryAccessBean.ACTION_ADD_ACTION_TO_DIRECTORY));
		
		Action timeout = new Action(ACTION_TIMEOUT_TEST, this, new Class[] {}, new Class[] {Object.class});
		Action getSomeResult = new Action(ACTION_GET_SOME_RESULT, this, new Class[] {Object.class}, new Class[] {Object.class});
		
		AgentDescription myAgentDescription = memory.read(new ThisAgentDescription());
		timeout.setProviderDescription(myAgentDescription);
		getSomeResult.setProviderDescription(myAgentDescription);
		
		DoAction addTimeout = _addAction.createDoAction(new Serializable[] {timeout}, _resultDump);
		DoAction addSomeResult = _addAction.createDoAction(new Serializable[] {getSomeResult}, _resultDump);
		
		memory.write(addTimeout);
		memory.write(addSomeResult);
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
		Serializable[] params = doAction.getParams();
		String actionName= doAction.getAction().getName();
		
		if (actionName.equalsIgnoreCase(ACTION_TIMEOUT_TEST)){
			String obj = timeoutTest();
			ActionResult result = ((Action) doAction.getAction()).createActionResult(doAction, new Serializable[] {obj});
			log.debug("writing result for timeoutTest");
			memory.write(result);
		} else if (actionName.equalsIgnoreCase(ACTION_GET_SOME_RESULT)){
			Serializable obj = getSomeResult(params[0]);
			ActionResult result = ((Action) doAction.getAction()).createActionResult(doAction, new Serializable[] {obj});
			log.debug("writing result for getSomeResult");
			memory.write(result);
		}
		
	}
	
	/*
	 * To be flexible just let's wait until the next action is coming in.
	 */ 
	public String timeoutTest(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return "This message should never come through to the caller of this action";
	}

	/*
	 * will just return what's sent to it
	 */
	public Serializable getSomeResult(Serializable result) {
		log.debug("somebody wants a result? ... tztz");
		return result;
	}
	


}
