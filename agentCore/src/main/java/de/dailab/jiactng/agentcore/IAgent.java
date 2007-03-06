package de.dailab.jiactng.agentcore;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;

/**
 * Interface for agents. This interfaces defines functionalities, that are
 * available for other components within and outside of the agent.
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public interface IAgent extends BeanNameAware, ILifecycleListener, Runnable,
    ILifecycle {

  /**
   * Setter for the agentnode that hosts this agent. Called by the agentnode,
   * when the agent is created.
   * 
   * @param agentNode
   *          the agentNode that hosts this agent.
   */
  public void setAgentNode(IAgentNode agentNode);

  /**
   * Setter for the agents memory-component. Used for dependency injection by
   * Spring.
   * 
   * @param memory
   */
  public void setMemory(IMemory memory);

  /**
   * Setter for the agents execution-cycle-component. Used for dependency
   * injection by Spring.
   * 
   * @param execution
   */
  public void setExecution(IExecutionCycle execution);

  /**
   * Setter for the agents adaptor-components. Used for dependency injection by
   * Spring.
   * 
   * @param adaptors
   */
  public void setAdaptors(ArrayList<IAgentBean> adaptors);

  /**
   * Setter for the name of this agent. Used by Spring during initialisation.
   * 
   * @param name
   *          the name of this agent
   * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
   */
  public void setBeanName(String name);

  /**
   * Getter for the name of this agent, as set by Spring
   * 
   * @return the name of this agent.
   */
  public String getAgentName();

  /**
   * Getter for the agentnode that hosts this agent.
   * 
   * @return a reference to the agentnode.
   */
  public IAgentNode getAgentNode();

  /**
   * Getter for the list of adaptors of this agent.
   * 
   * @return the lifelist of the adaptors.
   */
  public ArrayList<IAgentBean> getAdaptors();

  /**
   * Getter for the global threadpool responsivle for this agent. All threads
   * should be created via this threadpool, to make sure the ressources can be
   * controled. Note that the implementation this method should call the
   * approriate method of the agentnode to retrieve the threadpool to insure
   * consistency.
   * 
   * @return a new ExecutorService
   * @see de.dailab.jiactng.agentcore.IAgentNode#getThreadPool()
   * @see java.util.concurrent.ExecutorService
   */
  public abstract ExecutorService getThreadPool();

  /**
   * Getter for a log-instance that heeds the hierarchie of the agentnode and
   * the agent, i.e the name of the logger consists of the agentnode-name and
   * the name of the agent-parameter and the beanname. Note that the
   * implementation this method should call the approriate method of the
   * agentnode to retrieve the log-instance to insure consistency.
   * 
   * @param bean
   *          the bean for which the logger shall be retrieved.
   * @return a log-object that contains the agentnodes name, the agents name and
   *         the beanname.
   * @see org.apache.commons.logging.Log;
   */
  public Log getLog(IAgentBean bean);

}
