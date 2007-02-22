/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.knowledge.Tuple;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

public class Agent extends AbstractLifecycle implements BeanNameAware,
    ILifecycleListener, Runnable, InitializingBean {

  private Log                          agentLog  = null;

  private String                       agentName = null;

  private IMemory                      memory    = null;

  private ArrayList<AbstractAgentBean> adaptors  = null;

  private Thread                       myThread  = null;

  private Boolean                      syncObj   = Boolean.TRUE;

  private boolean                      active    = false;

  private IExecutionCycle              execCycle = null;

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

  public ArrayList<AbstractAgentBean> getAdaptors() {
    return adaptors;
  }

  public void setAdaptors(ArrayList adaptors) {
    this.adaptors = adaptors;
  }

  private void initAgent() {
    this.execCycle = new SimpleExecutionCycle(this);
    this.memory.out(new Tuple("thisAgent.name", this.agentName));
    for (AbstractAgentBean a : this.adaptors) {
      a.setMemory(memory);
      a.setThisAgent(this);
      if (a instanceof ILifecycle) a.addLifecycleListener(this);
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
      execCycle.doStep();
    }
  }

  public void setBeanName(String arg0) {
    this.agentName = arg0;
  }

  public void onEvent(LifecycleEvent evt) {
    // TODO Auto-generated method stub

  }

  @Override
  public void doCleanup() {
    agentLog.warn("Cleaning Up Agent " + agentName + "...");

    try {
      execCycle.cleanup();
    } catch (LifecycleException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    for (AbstractAgentBean a : this.adaptors) {
      try {
        a.cleanup();
        setBeanState(a.beanName, LifecycleStates.CLEANED_UP);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.CLEANING_UP);
      }
    }
    myThread = null;
    updateState(LifecycleStates.CLEANED_UP);
    agentLog.warn("  done");
  }

  @Override
  public void doInit() {
    agentLog = LogFactory.getLog("Agent:" + agentName);
    agentLog.warn("Initializing Agent " + agentName + "...");

    try {
      execCycle.init();
    } catch (LifecycleException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    for (AbstractAgentBean a : this.adaptors) {
      try {
        a.init();
        setBeanState(a.beanName, LifecycleStates.INITIALIZED);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.INITIALIZING);
      }
    }
    myThread = new Thread(this);
    updateState(LifecycleStates.INITIALIZED);
    agentLog.warn("  done");
  }

  @Override
  public void doStart() {
    agentLog.warn("Starting Agent " + agentName + "...");

    try {
      execCycle.start();
    } catch (LifecycleException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    for (AbstractAgentBean a : this.adaptors) {
      try {
        a.start();
        setBeanState(a.beanName, LifecycleStates.STARTED);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.STARTING);
      }
    }
    synchronized (syncObj) {
      active = true;
    }
    myThread.start();
    updateState(LifecycleStates.STARTED);
    agentLog.warn("  done");
  }

  @Override
  public void doStop() {
    agentLog.warn("Stopping Agent " + agentName + "...");

    try {
      execCycle.stop();
    } catch (LifecycleException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    for (AbstractAgentBean a : this.adaptors) {
      try {
        a.stop();
        setBeanState(a.beanName, LifecycleStates.STOPPED);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.STOPPING);
      }
    }
    synchronized (syncObj) {
      active = false;
    }
    updateState(LifecycleStates.STOPPED);
    agentLog.warn("  done");
  }

  private void handleBeanException(AbstractAgentBean a, LifecycleException e,
      LifecycleStates initializing) {
    e.printStackTrace();
  }

  private void updateState(ILifecycle.LifecycleStates newState) {
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
