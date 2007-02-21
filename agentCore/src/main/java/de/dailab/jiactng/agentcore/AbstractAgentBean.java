/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;

/**
 * Abstract superclass of all agentbeans. This includes core-components as well
 * as adaptors. The class handles basic references (such as to memory and the
 * agent) and defines the methods that are necessary for (lifecycle-)management.
 * 
 * @author Thomas Konnerth
 */
public abstract class AbstractAgentBean extends AbstractLifecycle implements
    BeanNameAware {

  /**
   * Reference to the agent that holds this bean.
   */
  protected Agent   thisAgent = null;

  /**
   * Reference to the memory of the agent that holds this bean.
   */
  protected IMemory memory    = null;

  /**
   * The name this bean. Note that this is the unqualified name which is
   * assigned by Spring.
   */
  protected String  beanName  = null;

  /**
   * Setter for the agent-reference to the agent that holds this bean.
   * 
   * @param agent
   *          the agent-class that controls this bean.
   */
  public final void setThisAgent(Agent agent) {
    this.thisAgent = agent;
  }

  /**
   * Setter for the memory of the agent that holds this bean.
   * 
   * @param mem
   *          the IMemory instance of the agent.
   */
  public final void setMemory(IMemory mem) {
    this.memory = mem;
  }

  /**
   * Setter for the beanName. This method is called by Spring during
   * initialisation.
   * 
   * @param name
   *          the unqualified name of the bean.
   * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
   */
  public final void setBeanName(String name) {
    this.beanName = name;
  }

  /**
   * Getter for the beanName. This method returns the qualified name of the
   * bean. The qualfied name consists of the agentname and the beanname.
   * 
   * @return the qualified name of the bean.
   */
  public final String getBeanName() {
    return new StringBuffer(thisAgent.getAgentName()).append(".").append(
        beanName).toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doInit()
   */
  public void doInit() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStart()
   */
  public void doStart() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStop()
   */
  public void doStop() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doCleanup()
   */
  public void doCleanup() {
  }

  /**
   * The stub for the execute method, that should be implemented by all beans.
   * Note: this stub is likely to change, when the Sensor/Effector structure is
   * implemented.
   */
  public abstract void execute();

}
