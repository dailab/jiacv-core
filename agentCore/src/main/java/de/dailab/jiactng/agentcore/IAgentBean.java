/*
 * Created on 27.02.2007
 */
package de.dailab.jiactng.agentcore;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
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

}