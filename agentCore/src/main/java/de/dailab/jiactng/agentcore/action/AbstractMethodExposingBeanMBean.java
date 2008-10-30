package de.dailab.jiactng.agentcore.action;

import javax.management.openmbean.TabularData;

/**
 * Common management interface of all agent beans which provide actions.
 * @author Jan Keiser
 */
public interface AbstractMethodExposingBeanMBean extends AbstractActionAuthorizationBeanMBean {

	/**
	 * Creates management information about the provided actions.
	 * @return list of action descriptions
	 */
	public TabularData getActionList();
}
