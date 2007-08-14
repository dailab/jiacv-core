/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.management.Manageable;

public interface IAgentBean extends ILifecycle, BeanNameAware, Manageable {

  /**
   * Setter for the agent-reference to the agent that holds this bean.
   * 
   * @param agent
   *          the agent-class that controls this bean.
   */
  public void setThisAgent(IAgent agent);

  /**
   * Setter for the memory of the agent that holds this bean.
   * 
   * @param mem
   *          the IMemory instance of the agent.
   */
  public void setMemory(IMemory mem);

  /**
   * Setter for the beanName. This method is called by Spring during
   * initialisation.
   * 
   * @param name
   *          the unqualified name of the bean.
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
   * The stub for the execute method, that should be implemented by all beans.
   * Note: this stub is likely to change, when the Sensor/Effector structure is
   * implemented.
   */
  public void execute();

}