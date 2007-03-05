/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.environment;

import java.util.ArrayList;

import de.dailab.jiactng.agentcore.action.IAction;

public interface IEffector {

  public IEnvironment getEnvironment();

  public ArrayList<IAction> getActions();

  public void executeAction(IAction action);

}
