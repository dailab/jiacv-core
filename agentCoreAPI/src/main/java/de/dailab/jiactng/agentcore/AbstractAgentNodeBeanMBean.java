package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;

/**
 * Common management interface of all agent beans.
 * @author Jan Keiser
 */
public interface AbstractAgentNodeBeanMBean extends AbstractLifecycleMBean {

	/**
	 * Getter for attribute "BeanName" of the managed agent bean.
	 * @return name of this agent bean
	 */
	String getBeanName();
	
	/**
	 * Setter for attribute "BeanName" of the managed agent bean.
	 * @param name the new name of this agent bean
	 */
	void setBeanName(String name);

}
