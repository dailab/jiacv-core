package de.dailab.jiactng.agentcore;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

/**
 * Simple agent platform
 * 
 * @author Joachim Fuchs
 */
public interface IAgentNode extends ILifecycle, BeanNameAware {

  public String getUUID();

  public String getName();

  public void setBeanName(String name);

  public void setAgents(ArrayList<IAgent> agents);

  public void addAgent(IAgent agent);

  public void removeAgent(IAgent agent);

  public ArrayList<IAgent> findAgents();

  public Log getLog(IAgent agent);

  public Log getLog(IAgent agent, AbstractAgentBean bean);

  public ExecutorService getThreadPool();

}
