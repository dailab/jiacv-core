/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiangtng.agentcore;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.dailab.jiangtng.agentcore.knowledge.IMemory;
import de.dailab.jiangtng.agentcore.lifecycle.AbstractLifecycle;

public abstract class AAgentBean extends AbstractLifecycle implements
    ApplicationContextAware, BeanNameAware {

  private ApplicationContext appContext = null;

  protected IMemory          memory     = null;

  protected String           beanName   = null;

  public void setApplicationContext(ApplicationContext arg0)
      throws BeansException {
    this.appContext = arg0;
    memory = (IMemory) appContext.getBean("memory");
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

