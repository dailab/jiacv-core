package de.dailab.jiactng.agentcore;

import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;

/**
 * Common management interface of all agent beans.
 * @author Jan Keiser
 */
public interface AbstractAgentBeanMBean extends AbstractLifecycleMBean {

	/**
	 * Getter for attribute "BeanName" of the managed agent bean.
	 * @return name of this agent bean
	 */
	public String getBeanName();
	
	/**
	 * Setter for attribute "BeanName" of the managed agent bean.
	 * @param the new name of this agent bean
	 */
	public void setBeanName(String name);

	/**
	 * Getter for attribute "Log" of the managed agent bean.
	 * @return information about the logger of this agent bean
	 */
	public CompositeData getLog();
}
