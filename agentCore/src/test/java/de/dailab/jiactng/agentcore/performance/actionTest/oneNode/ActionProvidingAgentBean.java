package de.dailab.jiactng.agentcore.performance.actionTest.oneNode;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.performance.ActionTestOneNode;

/**
 * 
 * An agent class which provides an action
 * 
 * @author Hilmi Yildirim
 *
 */
public class ActionProvidingAgentBean extends AbstractMethodExposingBean{
	
	/**
	 * number of actions to call
	 */
	int actionsToCall;
	
	/**
	 * number of called actions
	 */
	int calledActions;
	
	/**
	 * times of receiving the results of actions
	 */
	long[] actionTimes;
	
	/**
	 * initialize the agent
	 * @param actions to call
	 */
	public void inititialize(int actionsToCall){
		this.actionsToCall = actionsToCall;
		calledActions = 0;
		actionTimes = new long[actionsToCall];
	}
	
	/**
	 * @return true if all actions are called
	 */
	public boolean allActionsCalled(){
		return calledActions == actionsToCall;
	}
	
	/**
	 * @param current time
	 * @return difference of the current time and the given time
	 */
	@Expose (name = ActionTestOneNode.CALCULATE_ACTION_CALLING_TIME_TWO, scope = ActionScope.GLOBAL)
	public long calculateActionCallingTimeTwo(long time){
		return System.currentTimeMillis() - time;
	}
	
	/**
	 * asynchron action
	 * @param current time
	 * @return difference of the current time and the given time
	 */
	@Expose (name = ActionTestOneNode.CALCULATE_ACTION_CALLING_TIME_TWO_ASYNC, scope = ActionScope.GLOBAL)
	public void calculateActionCallingTimeTwoAsync(long time){
		synchronized (ActionTestOneNode.lockObject) {
			actionTimes[calledActions] = System.currentTimeMillis() - time;
			calledActions++;
			if(allActionsCalled()){
				ActionTestOneNode.lockObject.notify();
			}
		}
	}
	
	/**
	 * @return times of receiving the result of calling a method
	 */
	public long[] getActionTimes(){
		return actionTimes;
	}
}
