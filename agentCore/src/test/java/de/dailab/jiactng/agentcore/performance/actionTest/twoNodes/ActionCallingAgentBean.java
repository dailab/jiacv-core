package de.dailab.jiactng.agentcore.performance.actionTest.twoNodes;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.performance.ActionTestTwoNodes;

/**
 * 
 * An agent class which calls an action from another node or his own action
 * 
 * @author Hilmi Yildirim
 *
 */
public class ActionCallingAgentBean extends AbstractMethodExposingBean{
	
	/**
	 * number of actions to call
	 */
	private int actionsToCall;
	
	/**
	 * number of called actions
	 */
	private int calledActions;
	
	/**
	 * times of receiving the results of actions
	 */
	private long[] actionTimes;
	
	
	/**
	 * initialize the agent
	 * @param number actions to call
	 */
	public void initialize(int actionsToCall){
		this.actionsToCall = actionsToCall;
		calledActions = 0;
		actionTimes = new long[actionsToCall];
	}
	
	/**
	 * calls an action from another node
	 */
	public void callAction(){
		for(int i = 0; i < actionsToCall; i++){
			actionTimes[i] = callAction(ActionTestTwoNodes.CALCULATE_ACTION_CALLING_TIME);
		}
	}
	
	/**
	 * calls an action from another node
	 */
	public void callActionAsync(){
		for(int i = 0; i < actionsToCall; i++){
			callActionAsync(ActionTestTwoNodes.CALCULATE_ACTION_CALLING_TIME_ASYNC);
		}
	}
	
	/**
	 * calls an action with the given name
	 * @param time of getting the result
	 */
	public long callAction(String actionName){
		Action action = new Action(actionName);
		IActionDescription calculateSendTimeD = thisAgent.searchAction(action);

		if(calculateSendTimeD == null){
			throw new RuntimeException("Action not found");
		}
			
		ActionResult result = invokeAndWaitForResult(calculateSendTimeD, new Serializable[]{System.currentTimeMillis()});
		return (Long)result.getResults()[0];
	}
	
	public void callActionAsync(String actionName){
		Action action = new Action(actionName);
		IActionDescription calculateSendTimeD = thisAgent.searchAction(action);
		
		if(calculateSendTimeD == null){
			throw new RuntimeException("Action not found");
		}
			
		invoke(calculateSendTimeD, new Serializable[]{System.currentTimeMillis()});
	}

	/**
	 * @return times of receiving the result of calling a method
	 */
	public long[] getActionTimes() {
		return actionTimes;
	}
	
	/**
	 * @return true if all actions are called
	 */
	public boolean allActionsCalled(){
		return calledActions == actionsToCall;
	}
}
