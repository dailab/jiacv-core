/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

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
    IAgentBean {

  /**
   * Reference to the agent that holds this bean.
   */
  protected IAgent  thisAgent = null;

  /**
   * Reference to the memory of the agent that holds this bean.
   */
  protected IMemory memory    = null;

  /**
   * The name this bean. Note that this is the unqualified name which is
   * assigned by Spring.
   */
  protected String  beanName  = null;

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentBean#setThisAgent(de.dailab.jiactng.agentcore.IAgent)
   */
  public final void setThisAgent(IAgent agent) {
    this.thisAgent = agent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentBean#setMemory(de.dailab.jiactng.agentcore.knowledge.IMemory)
   */
  public final void setMemory(IMemory mem) {
    this.memory = mem;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentBean#setBeanName(java.lang.String)
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
    return beanName;
    // return new StringBuffer(thisAgent.getAgentName()).append(".").append(
    // beanName).toString();
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

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentBean#execute()
   */
  public abstract void execute();

}
