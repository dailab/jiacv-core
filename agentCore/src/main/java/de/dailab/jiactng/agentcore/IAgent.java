package de.dailab.jiactng.agentcore;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;

/**
 * Simple agent
 * 
 * @author Joachim Fuchs
 */
public interface IAgent extends BeanNameAware, ILifecycleListener, Runnable,
    ILifecycle {

  public void setAgentNode(IAgentNode agentNode);

  public void setMemory(IMemory memory);

  public void setExecution(IExecutionCycle execution);

  public void setAdaptors(ArrayList adaptors);

  public void setBeanName(String name);

  public String getAgentName();

  public IAgentNode getAgentNode();

  public ArrayList<AbstractAgentBean> getAdaptors();

  public abstract ExecutorService getThreadPool();

  public Log getLog(AbstractAgentBean bean);
  
}
