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

public interface IEffector extends IAgentBean {

	public ArrayList<Action> getActions();

	public void doAction(DoAction doAction);

}
