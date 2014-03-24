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
	String getBeanName();
	
	/**
	 * Setter for attribute "BeanName" of the managed agent bean.
	 * @param name the new name of this agent bean
	 */
	void setBeanName(String name);

	/**
	 * Getter for Interval by which the execute()-method of the bean is called.
	 * If non-positive, the execute-method is never called.
	 * 
	 * @return the time interval between two calls of the execute()-method
	 *            in milliseconds.
	 */
	int getExecutionInterval();

	/**
	 * Setter for Interval by which the execute()-method of the bean is called.
	 * If non-positive, the execute-method is never called.
	 * 
	 * @param newExecuteInterval
	 *            the time interval between two calls of the execute()-method
	 *            in milliseconds. If negative, the method is never called.
	 */
	void setExecutionInterval(int newExecutionInterval);

}
