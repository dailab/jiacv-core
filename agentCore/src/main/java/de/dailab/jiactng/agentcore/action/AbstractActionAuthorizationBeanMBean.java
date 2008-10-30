package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.AbstractAgentBeanMBean;

/**
 * Common management interface of all agent beans which authorize action invocations.
 * @author Jan Keiser
 */
public interface AbstractActionAuthorizationBeanMBean extends AbstractAgentBeanMBean {

	/**
	 * Get name of authorization action.
	 * @return the name of the action to be used for authorization.
	 */
	public String getAuthorizationActionName();

	/**
	 * Set name of authorization action.
	 * @param authorizationActionName the name of the action to be used for authorization.
	 */
	public void setAuthorizationActionName(String authorizationActionName);

}
