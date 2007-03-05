package de.dailab.jiactng.agentcore;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
 * Simple platform impl
 * 
 * @author Joachim Fuchs
 */
public class SimpleAgentNode extends AbstractLifecycle implements IAgentNode,
    InitializingBean, SimpleAgentNodeMBean {

  private ExecutorService   threadPool = null;

  protected Log             log        = null;

  /**
   * this one's fake
   */
  protected String          uuid       = null;

  /**
   * 
   */
  protected String          name       = null;

  /**
   * 
   */
  private ArrayList<IAgent> agents     = null;

  public SimpleAgentNode() {
    uuid = new String("p:"
        + Long.toHexString(System.currentTimeMillis() + this.hashCode()));
  }

  public void setAgents(ArrayList<IAgent> agents) {
    this.agents = agents;
  }

  public void addAgent(IAgent agent) {
    this.agents.add(agent);
  }

  public void removeAgent(IAgent agent) {
    this.agents.remove(agent);
  }

  public ArrayList<IAgent> findAgents() {
    return this.agents;
  }

  public Log getLog(IAgent agent) {
    return LogFactory.getLog(getName() + ":" + agent.getAgentName());
  }

  public Log getLog(IAgent agent, AbstractAgentBean bean) {
    return LogFactory.getLog(getName() + ":" + agent.getAgentName() + ":"
        + bean.beanName);
  }

  public String getUUID() {
    return this.uuid;
  }

  public String getName() {
    return this.name;
  }
  
  public String getHost() throws UnknownHostException {
	return InetAddress.getLocalHost().toString();
  }

  public String getOwner() {
	return System.getProperty("user.name");
  }

  public void setBeanName(String name) {
    this.name = name;
  }

  public void afterPropertiesSet() throws Exception {
    init();
    start();
  }

  public void doInit() {
	//register agent node as JMX resource
	MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	try {
		ObjectName name = new ObjectName("de.dailab.jiactng.agentcore:type=SimpleAgentNode,name="+this.name);  
		mbs.registerMBean(this, name);
		System.out.println("Agent " + this.name + " registered as JMX resource.");
	}
	catch (Exception e) {e.printStackTrace();}
			  
    log = LogFactory.getLog(getName());
    threadPool = Executors.newCachedThreadPool();

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

  public void doStart() {
    for (IAgent a : this.agents) {
      try {
        a.start();
        // TODO: futures should be stored
        Future f = threadPool.submit(a);
      } catch (Exception ex) {
        // TODO
        ex.printStackTrace();
      }
    }

    log.warn("AgentNode " + getName() + " started with " + this.agents.size()
        + " agents");
  }

  public void doStop() {
    for (IAgent a : this.agents) {
      try {
        a.stop();
      } catch (LifecycleException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public void doCleanup() {
    for (IAgent a : this.agents) {
      try {
        a.cleanup();
        threadPool.shutdown();
      } catch (LifecycleException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    log.warn("AgentNode " + getName() + " has been closed.");
  }

  public String toString() {
    return this.getName();
  }

  public ExecutorService getThreadPool() {
    return threadPool;
  }

}
