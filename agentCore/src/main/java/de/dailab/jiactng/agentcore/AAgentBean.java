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

public abstract class AAgentBean extends AbstractLifecycle implements
    BeanNameAware {

  protected Agent thisAgent = null;
  
  protected IMemory memory   = null;

  protected String  beanName = null;

  public void setThisAgent(Agent agent) {
    this.thisAgent=agent;
  }
  
  public void setMemory(IMemory mem) {
    this.memory = mem;
  }
  
  public abstract void execute();

  public void setBeanName(String arg0) {
    this.beanName = arg0;
  }

  public void doInit() {
  }

  public void doStart() {
  }

  public void doStop() {
  }

  public void doCleanup() {
  }

}
