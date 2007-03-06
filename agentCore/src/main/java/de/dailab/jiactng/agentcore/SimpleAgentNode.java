package de.dailab.jiactng.agentcore;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * Simple platform implementation
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public class SimpleAgentNode extends AbstractLifecycle implements IAgentNode,
    InitializingBean, SimpleAgentNodeMBean {

  /**
   * The threadPool object
   */
  private ExecutorService         threadPool   = null;

  /**
   * Log-instance for the agentnode
   */
  protected Log                   log          = null;

  /**
   * this one's fake
   */
  protected String                uuid         = null;

  /**
   * The name of the agentnode.
   */
  protected String                name         = null;

  /**
   * The list of agents.
   */
  private ArrayList<IAgent>       agents       = null;

  /**
   * Storage for the agentFutures. Used to stop/cancel agentthreads.
   */
  private HashMap<String, Future> agentFutures = null;

  /**
   * Constructur. Creates the uuid for the agentnode.
   */
  public SimpleAgentNode() {
    uuid = new String("p:"
        + Long.toHexString(System.currentTimeMillis() + this.hashCode()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#setAgents(java.util.ArrayList)
   */
  public void setAgents(ArrayList<IAgent> agents) {
    this.agents = agents;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#addAgent(de.dailab.jiactng.agentcore.IAgent)
   */
  public void addAgent(IAgent agent) {
    // TODO: statechanges?
    this.agents.add(agent);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#removeAgent(de.dailab.jiactng.agentcore.IAgent)
   */
  public void removeAgent(IAgent agent) {
    // TODO: statechanges?
    this.agents.remove(agent);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#findAgents()
   */
  public ArrayList<IAgent> findAgents() {
    // TODO: Security must decide whether the lifelist should be returned or
    // not.
    return this.agents;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#getLog(de.dailab.jiactng.agentcore.IAgent)
   */
  public Log getLog(IAgent agent) {
    return LogFactory.getLog(getName() + ":" + agent.getAgentName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#getLog(de.dailab.jiactng.agentcore.IAgent,
   *      de.dailab.jiactng.agentcore.AbstractAgentBean)
   */
  public Log getLog(IAgent agent, IAgentBean bean) {
    return LogFactory.getLog(getName() + ":" + agent.getAgentName() + ":"
        + bean.getBeanName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#getUUID()
   */
  public String getUUID() {
    return this.uuid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#getName()
   */
  public String getName() {
    return this.name;
  }

  public String getHost() throws UnknownHostException {
    return InetAddress.getLocalHost().toString();
  }

  public String getOwner() {
    return System.getProperty("user.name");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#setBeanName(java.lang.String)
   */
  public void setBeanName(String name) {
    this.name = name;
  }

  /**
   * Initialisation-method. This method is called by Spring after startup
   * (through the InitializingBean-Interface) and is used to start the agentnode
   * after all beans haven been instantiated by Spring. Currently only calls the
   * init() and start()-methods from ILifefycle for this.
   * 
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  public void afterPropertiesSet() throws Exception {
    init();
    start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doInit()
   */
  public void doInit() {
    // register agent node as JMX resource
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName name = new ObjectName(
          "de.dailab.jiactng.agentcore:type=SimpleAgentNode,name=" + this.name);
      mbs.registerMBean(this, name);
      System.out.println("Agent " + this.name + " registered as JMX resource.");
    } catch (Exception e) {
      e.printStackTrace();
    }

    log = LogFactory.getLog(getName());
    threadPool = Executors.newCachedThreadPool();
    agentFutures = new HashMap<String, Future>();

    // call init and set references for all agents
    for (IAgent a : this.agents) {
      a.setAgentNode(this);
      a.addLifecycleListener(this.lifecycle.createLifecycleListener());
      log.warn("Initializing agent: " + a.getAgentName());
      try {
        a.init();
      } catch (LifecycleException e) {
        // TODO:
        e.printStackTrace();
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStart()
   */
  public void doStart() {
    // call start() and instantiate Threads for all agents
    for (IAgent a : this.agents) {
      try {
        a.start();
        Future f = threadPool.submit(a);
        agentFutures.put(a.getAgentName(), f);
      } catch (Exception ex) {
        // TODO
        ex.printStackTrace();
      }
    }

    log.warn("AgentNode " + getName() + " started with " + this.agents.size()
        + " agents");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStop()
   */
  public void doStop() {
    // call stop() for all agents
    for (IAgent a : this.agents) {
      try {
        a.stop();
        Future f = agentFutures.get(a.getAgentName());
        if (f == null) {
          throw new LifecycleException("Agentfuture not found");
        } else {
          // if soft-cancel fails, do a force-cancel.
          if (!f.cancel(false) && !f.isDone()) {
            log.warn("Agent " + a.getAgentName()
                + " did not respond then stopping. Thread is forcecanceled.");
            f.cancel(true);
          }
        }

      } catch (LifecycleException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doCleanup()
   */
  public void doCleanup() {
    // unregister agent node as JMX resource
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName name = new ObjectName(
          "de.dailab.jiactng.agentcore:type=SimpleAgentNode,name=" + this.name);
      if (mbs.isRegistered(name)) {
        mbs.unregisterMBean(name);
      }
      System.out.println("Agent " + this.name + " registered as JMX resource.");
    } catch (Exception e) {
      e.printStackTrace();
    }

    // call cleanup for all agents
    for (IAgent a : this.agents) {
      try {
        a.cleanup();
      } catch (LifecycleException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    threadPool.shutdown();
    log.warn("AgentNode " + getName() + " has been closed.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return this.getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.IAgentNode#getThreadPool()
   */
  public ExecutorService getThreadPool() {
    return threadPool;
  }

}
