/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.knowledge.Tuple;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * Agentclass implementing the IAgent interface and therby realizing the basic
 * JIAC-TNG agent. The Agent currently holds a Memory-Component, an
 * ExecutionCycle component and a list of adaptors.
 * 
 * @author Thomas Konnerth
 * @see de.dailab.jiactng.agentcore.IAgent
 */
public class Agent extends AbstractLifecycle implements IAgent, InitializingBean, AgentMBean {

  /**
   * Reference to the agentnode that holds this agent.
   */
  private IAgentNode            agentNode       = null;

  /**
   * The log-instance for this agent.
   */
  private Log                   agentLog        = null;

  /**
   * The name of this agent.
   */
  private String                agentName       = null;

  /**
   * Comment for <code>memory</code>
   */
  private IMemory               memory          = null;

  /**
   * The list of adaptors of this agent.
   */
  private ArrayList<IAgentBean> adaptors        = null;

  /**
   * Synchronization object for the Thread
   */
  private Boolean               syncObj         = Boolean.TRUE;

  /**
   * activity Flag (could be replaced by statecheck
   */
  private boolean               active          = false;

  /**
   * Reference to the Object that handles the executionCycle
   */
  private IExecutionCycle       execution       = null;

  /**
   * Future for the executionCycle of this agent. Used to store and cancel the
   * executionThread.
   */
  private Future                executionFuture = null;
  
  /**
   * Main method for starting JIAC-TNG. Loads a spring-configuration file
   * denoted by the first argument and uses a ClassPathXmlApplicationContext to
   * instantiate its contents
   * 
   * @param args
   *          the first argument is interpreted as a classpathrelative name of a
   *          spring configurations file. Other arguments are ignored.
   * @see org.springframework.context.support.ClassPathXmlApplicationContext
   */
  public static void main(String[] args) {
    ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext(
        args[0]);
  }

  /**
   * Getter for the memory-component
   * 
   * @return a reference to the IMemory implementation of this agent.
   * 
   * public IMemory getMemory() { return memory; }
   */

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
  public ArrayList<IAgentBean> getAdaptors() {
    return adaptors;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgent#setAdaptors(java.util.ArrayList)
   */
  public void setAdaptors(ArrayList<IAgentBean> adaptors) {
    this.adaptors = adaptors;
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
          executionFuture = agentNode.getThreadPool().submit(execution);
          FutureTask t = ((FutureTask) executionFuture);
          try {
            t.get(500, TimeUnit.MILLISECONDS);
          } catch (TimeoutException to) {
            System.err.print("this: " + agentName);
            to.printStackTrace();
            t.cancel(true);
            this.stop();
            agentLog.error("ExecutionCycle did not return");
          }
        }
      } catch (Exception e) {
        agentLog.error("Critical error in controlcycle of agent: " + agentName
            + ". Stopping Agent.");
        e.printStackTrace();
        try {
          this.stop();
        } catch (LifecycleException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
      }
    }
  }

  /**
   * Setter for the agentname. Called by Spring via the BeanNameAware interface.
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
    synchronized (syncObj) {
      if (executionFuture != null) {
        executionFuture.cancel(true);
        executionFuture = null;
      }
    }
    this.memory.cleanup();
    this.execution.cleanup();

    // call cleanup for all adaptors
    for (IAgentBean a : this.adaptors) {
      try {
        a.cleanup();
        setBeanState(a.getBeanName(), LifecycleStates.CLEANED_UP);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.CLEANING_UP);
      }
    }

    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName name = new ObjectName(
          "de.dailab.jiactng.agentcore:type=Agent,name=" + this.agentName);
      if (mbs.isRegistered(name)) {
        mbs.unregisterMBean(name);
      }
      System.out.println("Agent " + this.agentName
          + " unregistered as JMX resource.");
    } catch (Exception e) {
      e.printStackTrace();
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
    // register agent as JMX resource
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName name = new ObjectName(
          "de.dailab.jiactng.agentcore:type=Agent,name=" + this.agentName);
      mbs.registerMBean(this, name);
      System.out.println("Agent " + this.agentName
          + " registered as JMX resource.");
    } catch (Exception e) {
      e.printStackTrace();
    }

    // initialize agent elements
    this.agentLog = agentNode.getLog(this);

    this.memory.init();
    this.memory.out(new Tuple("thisAgent.name", this.agentName));

    this.execution.setAgent(this);
    this.execution.init();

    // call init for all adaptors
    for (IAgentBean a : this.adaptors) {
      try {
        a.setMemory(memory);
        a.setThisAgent(this);
        a.init();
        if (a instanceof ILifecycle) a.addLifecycleListener(this);
        memory.out(new Tuple(createBeanPath(a.getBeanName()) + ".name", a
            .getBeanName()));
        setBeanState(a.getBeanName(), LifecycleStates.INITIALIZED);
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

    // call start for all adaptors
    for (IAgentBean a : this.adaptors) {
      try {
        a.start();
        setBeanState(a.getBeanName(), LifecycleStates.STARTED);
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

    // call stop for all adaptors
    for (IAgentBean a : this.adaptors) {
      try {
        a.stop();
        setBeanState(a.getBeanName(), LifecycleStates.STOPPED);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.STOPPING);
      }
    }
    synchronized (syncObj) {
      active = false;
      if (executionFuture != null) {
        executionFuture.cancel(false);
      }

    }

    updateState(LifecycleStates.STOPPED);
  }

  /**
   * Utility-Method for handling bean exections during lifecycle changes.
   * 
   * @param a
   *          the bean that threw the exception
   * @param e
   *          the actual exception
   * @param state
   *          the state to which the bean should have changed.
   */
  private void handleBeanException(IAgentBean a, LifecycleException e,
      LifecycleStates state) {
    e.printStackTrace();
  }

  /**
   * Utility-Method that updates the state of the agent in the Memory
   * 
   * @param newState
   *          the new state
   */
  private void updateState(ILifecycle.LifecycleStates newState) {
    if (memory.test(new Tuple("thisAgent.state", null)) != null) {
      memory.in(new Tuple("thisAgent.state", null));
    }
    memory.out(new Tuple("thisAgent.state", newState.toString()));
  }

  /**
   * Initialisation-method. This method is called by Spring after startup
   * (through the InitializingBean-Interface) and is used to start the agent
   * after all beans haven been instantiated by Spring. Currently only calls the
   * init() and start()-methods from ILifefycle for this if the agent is deployed
   * in a running agent node.
   * 
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {
	  if (applicationContext != null) {
		  ApplicationContext parent = applicationContext.getParent();
		  if (parent != null) {
			  Map agentNodes = parent.getBeansOfType(SimpleAgentNode.class);
			  if (agentNodes.size() != 1) {
				  System.out.println("ERROR: More than one agent node in parent context!");
				  return;
			  }
			  agentNode = (IAgentNode)agentNodes.values().toArray()[0];
			  agentNode.addAgent(this);
			  init();
			  start();			  
		  }
	  }
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
   * @see de.dailab.jiactng.agentcore.AgentMBean#getName()
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

  public Log getLog(IAgentBean bean) {
    return agentNode.getLog(this, bean);
  }

  /**
   * @see de.dailab.jiactng.agentcore.AgentMBean#getAgentNodeUUID()
   */
  public String getAgentNodeUUID() {
    return agentNode.getUUID();
  }

}
