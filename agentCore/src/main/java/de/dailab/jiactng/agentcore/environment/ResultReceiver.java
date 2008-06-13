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
 * @author Thomas Konnerth
 * @see de.dailab.jiactng.agentcore.action.Action
 */
public interface ResultReceiver { 

	
	/**
	 * This method is called when a result for an action is delivered .
	 * 
	 * @param result the result of an action
	 */
	public void receiveResult(ActionResult result);

//	/**
//	 * Setter for the beanName. This method is called by Spring during
//	 * initialization.
//	 * 
//	 * @param name the unqualified name of the bean.
//	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
//	 */
//	
//	public void setBeanName(String name);
//
//	/**
//	 * Getter for the name of the agentbean.
//	 * 
//	 * @return a string representing the name of the agentbean.
//	 */
//	public String getBeanName();
}
