/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.util.ArrayList;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.knowledge.Tuple;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.Lifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleListener;

public class Agent extends AbstractLifecycle implements
    ApplicationContextAware, BeanNameAware, LifecycleListener {

  private String                agentName  = null;

  private IMemory               memory     = null;

  private String                statePath  = null;

  private ArrayList<AAgentBean> adaptors   = null;

  private ApplicationContext    appContext = null;

  public static void main(String[] args) {
    ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext(
        args[0]);

    // Object memBean = appContext.getBean("memory");
  }

  public IMemory getMemory() {
    return memory;
  }

  public void setMemory(IMemory memory) {
    this.memory = memory;
  }

  public ArrayList getAdaptors() {
    return adaptors;
  }

  public void setAdaptors(ArrayList adaptors) {
    this.adaptors = adaptors;
    if (appContext != null) initAgent();
  }

  private void initAgent() {
    memory.out(new Tuple("thisAgent.name", agentName));
    for (AAgentBean a : this.adaptors) {
      if (a instanceof Lifecycle) a.addLifecycleListener(this);
      memory.out(new Tuple(this.agentName + ".beans", a.beanName));
    }
    doInit();
    memory.out(new Tuple("thisAgent.state", LifecycleStates.INITIALIZED
        .toString()));
    doStart();
    memory
        .out(new Tuple("thisAgent.state", LifecycleStates.STARTED.toString()));
  }

  public void setApplicationContext(ApplicationContext arg0)
      throws BeansException {
    appContext = arg0;
    if (adaptors != null) initAgent();
  }

  public void setBeanName(String arg0) {
    this.agentName = arg0;
    this.statePath = agentName + ".states.";
  }

  public void onEvent(LifecycleEvent evt) {
    // TODO Auto-generated method stub

  }

  @Override
  public void doCleanup() {
    for (AAgentBean a : this.adaptors) {
      a.doCleanup();
      memory.out(new Tuple(this.statePath + a.beanName,
          LifecycleStates.CLEANED_UP.toString()));
    }
  }

  @Override
  public void doInit() {
    for (AAgentBean a : this.adaptors) {
      a.doInit();
      memory.out(new Tuple(this.statePath + a.beanName,
          LifecycleStates.INITIALIZED.toString()));
    }
  }

  @Override
  public void doStart() {
    for (AAgentBean a : this.adaptors) {
      a.doStart();
      memory.out(new Tuple(this.statePath + a.beanName, LifecycleStates.STARTED
          .toString()));
    }
  }

  @Override
  public void doStop() {
    for (AAgentBean a : this.adaptors) {
      a.doStop();
      memory.out(new Tuple(this.statePath + a.beanName, LifecycleStates.STOPPED
          .toString()));
    }
  }

}
