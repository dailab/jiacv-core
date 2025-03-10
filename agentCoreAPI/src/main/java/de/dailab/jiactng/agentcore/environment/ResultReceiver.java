/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.environment;

import de.dailab.jiactng.agentcore.action.ActionResult;

/**
 * This interfaces describes the way how components can register actionresults within
 * an agent. These actions include local actions for the agent as well as
 * services.
 * 
 * @see ActionResult
 * @see de.dailab.jiactng.agentcore.ontology.IActionDescription
 * @see de.dailab.jiactng.agentcore.action.DoAction
 *
 * @author Thomas Konnerth
 */
public interface ResultReceiver { 

	
	/**
	 * This method is called when a result for an action is delivered .
	 * 
	 * @param result the result of an action
	 */
	void receiveResult(ActionResult result);

}
