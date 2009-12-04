package de.dailab.jiactng.agentcore;

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
	 * @param name the new name of this agent bean
	 */
	public void setBeanName(String name);

}
