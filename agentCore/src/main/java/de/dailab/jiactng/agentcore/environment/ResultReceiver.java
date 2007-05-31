/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.environment;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.ActionResult;

/**
 * This interfaces describes the way how components can register actionresults within
 * an agent. These actions include local actions for the agent as well as
 * services.
 * 
 * @author Thomas Konnerth
 * @see de.dailab.jiactng.agentcore.action.Action
 */
public interface ResultReceiver extends IAgentBean {

	public void receiveResult(ActionResult result);

}
