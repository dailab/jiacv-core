/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.environment;

import java.util.ArrayList;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
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
	 * @return An ArrayList containing all actions from this component.
	 */
	public ArrayList<Action> getActions();

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
	public void doAction(DoAction doAction);

}
