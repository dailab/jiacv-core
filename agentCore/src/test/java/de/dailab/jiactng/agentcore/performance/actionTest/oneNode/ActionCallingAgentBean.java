package de.dailab.jiactng.agentcore.performance.actionTest.oneNode;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.performance.ActionTestOneNode;

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
	public void callActionFromAnotherNode(){
		for(int i = 0; i < actionsToCall; i++){
			actionTimes[i] = callAction(ActionTestOneNode.CALCULATE_ACTION_CALLING_TIME_TWO);
		}
	}
	
	/**
	 * calls an action from another node
	 */
	public void callActionFromAnotherNodeAsync(){
		for(int i = 0; i < actionsToCall; i++){
			callActionAsync(ActionTestOneNode.CALCULATE_ACTION_CALLING_TIME_TWO_ASYNC);
		}
	}
	
	/**
	 * calls his own action
	 */
	public void callOwnAction(){
		for(int i = 0; i < actionsToCall; i++){
			actionTimes[i] = callAction(ActionTestOneNode.CALCULATE_ACTION_CALLING_TIME_ONE);
		}
	}
	
	/**
	 * calls his own action asynchrony
	 */
	public void callOwnActionAsync(){
		for(int i = 0; i < actionsToCall; i++){
			callActionAsync(ActionTestOneNode.CALCULATE_ACTION_CALLING_TIME_ONE_ASYNC);
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
	
	/**
	 * own action from the node
	 * @param current time
	 * @return difference of the current time and the given time
	 */
	@Expose (name = ActionTestOneNode.CALCULATE_ACTION_CALLING_TIME_ONE, scope = ActionScope.GLOBAL)
	public long calculateActionCallingTimeOne(long time){
		return System.currentTimeMillis() - time;
	}
	
	/**
	 * own action from the node asynchrony
	 * @param current time
	 * @return difference of the current time and the given time
	 */
	@Expose (name = ActionTestOneNode.CALCULATE_ACTION_CALLING_TIME_ONE_ASYNC, scope = ActionScope.GLOBAL)
	public void calculateActionCallingTimeOneAsync(long time){
		synchronized (ActionTestOneNode.lockObject) {
			actionTimes[calledActions] = System.currentTimeMillis() - time;
			calledActions++;
			
			if(allActionsCalled()){
				ActionTestOneNode.lockObject.notify();
			}
		}
	}
}
