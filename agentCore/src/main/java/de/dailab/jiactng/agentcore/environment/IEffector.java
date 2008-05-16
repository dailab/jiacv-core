/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.environment;

import java.util.List;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;

/**
 * This interfaces describes the way how components can register actions within
 * an agent. These actions include local actions for the agent as well as
 * services.
 * 
 * @author Thomas Konnerth
 * @see de.dailab.jiactng.agentcore.action.Action
 */
public interface IEffector extends IAgentBean {

	/**
	 * This method is called by the doInit-method of the agent and is used to
	 * retrieve all actions that this bean is able to perform. The
	 * action-objects are written to the memory where they are available for all
	 * components.
	 * 
	 * @see de.dailab.jiactng.agentcore.action.Action
	 * @return A List containing all actions from this component.
	 */
	public List<? extends Action> getActions();

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
	 * @throws Exception An exception during the execution of the action has been occurred.
	 */
	public void doAction(DoAction doAction) throws Exception;
	
	/**
	 * Cancels a <code>DoAction</code> because it's <code>Session</code> had a timeout.
	 * Notes: At this moment the only place this method is called is within the sessiontimeoutmanagment.
	 * 
	 * Important: Is only meant to be called by <code>SimpleExecutionCycle</code> for timeoutmanagment
	 * purposes
	 * 
	 * @param doAction This is the DoAction that has to be canceled
	 * @return may be null or an ActionResult, that has finaly to be delivered to the source of the action 
	 */
	public ActionResult cancelAction(DoAction doAction);

}
