package de.dailab.jiactng.agentcore;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;

public class InvokeWithBacktrackingAgentBean extends AbstractAgentBean{

	public boolean callMethod(){
		
		Action action = new Action("invokeWithBacktrackingMethod");
		ActionResult actionResult = invokeWithBacktracking(action, new Serializable[]{});
		if(actionResult == null || actionResult.getFailure() != null){
			return false;
		}else{
			return true;
		}
	}
}
