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
import org.springframework.beans.factory.InitializingBean;
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
    ApplicationContextAware, BeanNameAware, LifecycleListener, Runnable,
    InitializingBean {

  private String                agentName = null;

  private IMemory               memory    = null;

  private ArrayList<AAgentBean> adaptors  = null;

  private Thread                myThread  = null;

  private Boolean               syncObj   = Boolean.TRUE;

  private boolean               active    = false;

  public static void main(String[] args) {
    ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext(
        args[0]);
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
  }

  private void initAgent() {
    this.memory.out(new Tuple("thisAgent.name", this.agentName));
    for (AAgentBean a : this.adaptors) {
      a.setMemory(memory);
      a.setThisAgent(this);
      if (a instanceof Lifecycle) a.addLifecycleListener(this);
      memory.out(new Tuple(createBeanPath(a.beanName) + ".name", a.beanName));
    }
    doInit();
    doStart();
  }

  public void run() {
    while (active) {
      try {
        synchronized (syncObj) {
          syncObj.wait(1000);
        }
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      for (AAgentBean a : this.adaptors) {
        a.execute();
      }

    }
  }

  public void setApplicationContext(ApplicationContext arg0)
      throws BeansException {
  }

  public void setBeanName(String arg0) {
    this.agentName = arg0;
  }

  public void onEvent(LifecycleEvent evt) {
    // TODO Auto-generated method stub

  }

  @Override
  public void doCleanup() {
    for (AAgentBean a : this.adaptors) {
      a.doCleanup();
      setBeanState(a.beanName, LifecycleStates.CLEANED_UP);
    }
    updateState(LifecycleStates.CLEANED_UP);
  }

  @Override
  public void doInit() {
    for (AAgentBean a : this.adaptors) {
      a.doInit();
      setBeanState(a.beanName, LifecycleStates.INITIALIZED);
    }
    updateState(LifecycleStates.INITIALIZED);
  }

  @Override
  public void doStart() {
    for (AAgentBean a : this.adaptors) {
      a.doStart();
      setBeanState(a.beanName, LifecycleStates.STARTED);
    }
    myThread = new Thread(this);
    myThread.start();
    active = true;
    updateState(LifecycleStates.STARTED);
  }

  @Override
  public void doStop() {
    active = false;
    for (AAgentBean a : this.adaptors) {
      a.doStop();
      setBeanState(a.beanName, LifecycleStates.STOPPED);
    }
    updateState(LifecycleStates.STOPPED);
  }

  private void updateState(Lifecycle.LifecycleStates newState) {
    if (memory.test(new Tuple("thisAgent.state", null)) != null) {
      memory.in(new Tuple("thisAgent.state", null));
    }
    memory.out(new Tuple("thisAgent.state", newState.toString()));
  }

  public void afterPropertiesSet() throws Exception {
    initAgent();
  }

  public LifecycleStates getAgentState() {
    return LifecycleStates.valueOf(memory.read(
        new Tuple("thisAgent.state", null)).getArg2());
  }

  public void setBeanState(String beanName, LifecycleStates newState) {
    String beanPath = createBeanPath(beanName) + ".state";
    Tuple test = this.memory.test(new Tuple(beanPath, null));
    if (test != null) {
      this.memory.in(test);
    }
    this.memory.out(new Tuple(beanPath, newState.toString()));
  }

  public LifecycleStates getBeanState(String beanName) {
    String beanPath = createBeanPath(beanName) + ".state";
    Tuple test = this.memory.test(new Tuple(beanPath, null));
    return LifecycleStates.valueOf(test.getArg2());
  }

  private String createBeanPath(String beanName) {
    return "thisAgent.beans." + beanName;
  }

  public String getAgentName() {
    return this.agentName;
  }

}
