package de.dailab.jiactng.agentcore.comm.wp;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class RemoteActionTestBean extends AbstractAgentBean implements ResultReceiver{

	private Action _requestAction;
	private ActionResult _lastActionResult = null;
	@SuppressWarnings("unused")
	private DoAction _lastDoAction = null;

	public void doStart() throws Exception {
		super.doStart();
		_requestAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#requestSearch"));
	}


	public void searchForActionDesc(IActionDescription actionDesc, boolean isGlobal){
		log.debug("Searching for Action " + actionDesc.toString());
		Serializable[] params = {actionDesc, new Boolean(isGlobal)};
		DoAction action = _requestAction.createDoAction(params, this);
		_lastDoAction = action;
		memory.write(action);
	}

	public void useRemoteAction(Action action, Serializable[] params, long timeToLive){
		log.debug("using remote Action " + action.getName());
		DoAction remoteAction = action.createDoAction(params, this, timeToLive);
		
		_lastDoAction = remoteAction;
		memory.write(remoteAction);
	}

	public void useRemoteAction(Action action, Serializable[] params){
		log.debug("using remote Action " + action.getName());
		DoAction remoteAction = action.createDoAction(params, this);

		_lastDoAction = remoteAction;
		memory.write(remoteAction);
	}
	
	public ActionResult getLastActionResult(){
		ActionResult result = _lastActionResult;
		_lastActionResult = null;
		return result;
	}

	/**
	 * Receives the result for the action created in <code>useRemoteAction</code> and stores it for later withdrawl
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public void receiveResult(ActionResult result) {
		log.debug("RemoteActionTestBean Receiving Result");
		if (result != null) log.debug("Result reads: " + result);

		_lastActionResult = result;

	}

}
