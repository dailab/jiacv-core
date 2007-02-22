/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.knowledge.Tuple;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

public class Agent extends AbstractLifecycle implements IAgent {

  private IAgentNode                   agentNode = null;

  private Log                          agentLog  = null;

  private String                       agentName = null;

  private IMemory                      memory    = null;

  private ArrayList<AbstractAgentBean> adaptors  = null;

  private Boolean                      syncObj   = Boolean.TRUE;

  private boolean                      active    = false;

  private IExecutionCycle              execution = null;

  public static void main(String[] args) {
    ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext(
        args[0]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getMemory()
   */
  public IMemory getMemory() {
    return memory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#setMemory(de.dailab.jiactng.agentcore.knowledge.IMemory)
   */
  public void setMemory(IMemory memory) {
    this.memory = memory;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getAdaptors()
   */
  public ArrayList<AbstractAgentBean> getAdaptors() {
    return adaptors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#setAdaptors(java.util.ArrayList)
   */
  public void setAdaptors(ArrayList adaptors) {
    this.adaptors = adaptors;
  }

  protected void initAgent() {
    this.execution.setAgent(this);
    this.memory.out(new Tuple("thisAgent.name", this.agentName));

    for (AbstractAgentBean a : this.adaptors) {
      a.setMemory(memory);
      a.setThisAgent(this);
      if (a instanceof ILifecycle) a.addLifecycleListener(this);
      memory.out(new Tuple(createBeanPath(a.beanName) + ".name", a.beanName));
    }

    // doInit();
    // doStart();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#run()
   */
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
      execution.doStep();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#setBeanName(java.lang.String)
   */
  public void setBeanName(String arg0) {
    this.agentName = arg0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#onEvent(de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent)
   */
  public void onEvent(LifecycleEvent evt) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#doCleanup()
   */
  @Override
  public void doCleanup() throws LifecycleException {
    this.memory.cleanup();
    this.execution.cleanup();

    for (AbstractAgentBean a : this.adaptors) {
      try {
        a.cleanup();
        setBeanState(a.beanName, LifecycleStates.CLEANED_UP);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.CLEANING_UP);
      }
    }
    updateState(LifecycleStates.CLEANED_UP);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#doInit()
   */
  @Override
  public void doInit() throws LifecycleException {
    this.agentLog = agentNode.getLog(this);

    this.memory.init();
    this.memory.out(new Tuple("thisAgent.name", this.agentName));

    this.execution.setAgent(this);
    this.execution.init();

    for (AbstractAgentBean a : this.adaptors) {
      try {
        a.init();
        a.setMemory(memory);
        a.setThisAgent(this);
        if (a instanceof ILifecycle) a.addLifecycleListener(this);
        memory.out(new Tuple(createBeanPath(a.beanName) + ".name", a.beanName));
        setBeanState(a.beanName, LifecycleStates.INITIALIZED);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.INITIALIZING);
      }
    }

    updateState(LifecycleStates.INITIALIZED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#doStart()
   */
  @Override
  public void doStart() throws LifecycleException {

    this.memory.start();
    this.execution.start();

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
    updateState(LifecycleStates.STARTED);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#doStop()
   */
  @Override
  public void doStop() throws LifecycleException {

    this.memory.stop();
    this.execution.stop();

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

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {
    // initAgent();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getAgentState()
   */
  public LifecycleStates getAgentState() {
    return LifecycleStates.valueOf(memory.read(
        new Tuple("thisAgent.state", null)).getArg2());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#setBeanState(java.lang.String,
   *      de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates)
   */
  public void setBeanState(String beanName, LifecycleStates newState) {
    String beanPath = createBeanPath(beanName) + ".state";
    Tuple test = this.memory.test(new Tuple(beanPath, null));
    if (test != null) {
      this.memory.in(test);
    }
    this.memory.out(new Tuple(beanPath, newState.toString()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getBeanState(java.lang.String)
   */
  public LifecycleStates getBeanState(String beanName) {
    String beanPath = createBeanPath(beanName) + ".state";
    Tuple test = this.memory.test(new Tuple(beanPath, null));
    return LifecycleStates.valueOf(test.getArg2());
  }

  private String createBeanPath(String beanName) {
    return "thisAgent.beans." + beanName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getAgentName()
   */
  public String getAgentName() {
    return this.agentName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getThreadPool()
   */
  public ExecutorService getThreadPool() {
    return agentNode.getThreadPool();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getExecution()
   */
  public IExecutionCycle getExecution() {
    return execution;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#setExecution(de.dailab.jiactng.agentcore.IExecutionCycle)
   */
  public void setExecution(IExecutionCycle execution) {
    this.execution = execution;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getAgentNode()
   */
  public IAgentNode getAgentNode() {
    return agentNode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#getName()
   */
  public String getName() {
    return agentName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#setAgentNode(de.dailab.jiactng.agentcore.IAgentNode)
   */
  public void setAgentNode(IAgentNode agentNode) {
    this.agentNode = agentNode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#setName(java.lang.String)
   */
  public void setName(String name) {
    // TODO Auto-generated method stub

  }

  public Log getLog(AbstractAgentBean bean) {
    return agentNode.getLog(this, bean);
  }
}
