/*
 * Created on 27.02.2007
 */
package de.dailab.jiactng.agentcore;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manageable;

/**
 * Interface for all agentbeans. This Interfaces declares the generally
 * accessible methods for all agents beans and insures inheritance from the
 * ILifecycle and Manageable interfaces.
 * 
 * @author moekon
 */

public interface IAgentBean extends ILifecycle, BeanNameAware, Manageable {

	/**
	 * Setter for the agent-reference to the agent that holds this bean.
	 * 
	 * @param agent
	 *            the agent-class that controls this bean.
	 */
	public void setThisAgent(IAgent agent);

	/**
	 * Setter for the memory of the agent that holds this bean.
	 * 
	 * @param mem
	 *            the IMemory instance of the agent.
	 */
	public void setMemory(IMemory mem);

	/**
	 * Setter for the beanName. This method is called by Spring during
	 * initialisation.
	 * 
	 * @param name
	 *            the unqualified name of the bean.
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name);

	/**
	 * Getter for the name of the agentbean
	 * 
	 * @return a string representing the name of the agentbean.
	 */
	public String getBeanName();

	/**
	 * Getter for Interval by which the execute()-method of the bean is called.
	 * If non-positive, the execute-method is never called.
	 * 
	 * @return the time interval between two calls of the execute()-method.
	 */
	public int getExecuteInterval();

	/**
	 * Setter for Interval by which the execute()-method of the bean is called.
	 * If non-positive, the execute-method is never called.
	 * 
	 * @param executeInterval
	 *            the time interval between two calls of the execute()-method.
	 *            If negative, the method is never called.
	 */
	public void setExecuteInterval(int executeInterval);

	/**
	 * The stub for the execute method, that should be implemented by all beans.
	 * Note: this stub is likely to change, when the Sensor/Effector structure
	 * is implemented.
	 */
	public void execute();

	/**
	 * This method will be used by the execution cycle. If <code>getExecuteInterval</code>
	 * is greater than 0 this method returns the next scheduled time. The return value is
	 * undefined else. 
	 * 
	 * Do not use it except you are implementing the scheduler or execution cycle.
	 * 
	 * @return the next time this bean must be executed
	 */
	long getNextExecutionTime();
	
	/**
	 * This method will be used by the scheduler. If <code>getExecuteInterval</code>
	 * is greater than 0 the execution cycle will store the next scheduled time here. 
	 * 
	 * Do not use it except you are implementing the scheduler or execution cycle.
	 * 
	 * @param nextExecutionTime the next time to execute this IAgentBean
	 */ 
	void setNextExecutionTime(long nextExecutionTime);
	
	/**
	 * Recovery method for handling LifecycleExceptions. If a statechange for an
	 * agentbean has failed, this method is called. It may fix the problem.
	 * Afterwards the statechange is called one more time. If it fails again,
	 * the bean is supposed to be defective.
	 * 
	 * @param e
	 *            the exception that occured during the first statechange.
	 * @param state
	 *            the state to which the bean should change.
	 */
	public void handleLifecycleException(LifecycleException e,
			LifecycleStates state);

}