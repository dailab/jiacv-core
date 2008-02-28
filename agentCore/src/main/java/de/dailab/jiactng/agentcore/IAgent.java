package de.dailab.jiactng.agentcore;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.management.Manageable;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * Interface for agents. This interfaces defines functionalities, that are
 * available for other components within and outside of the agent.
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public interface IAgent extends BeanNameAware, ILifecycleListener, Runnable,
    ILifecycle, Manageable {

  /**
   * Setter for the agentnode that hosts this agent. Called by the agentnode,
   * when the agent is created.
   * 
   * @param agentNode
   *          the agentNode that hosts this agent.
   */
  public void setAgentNode(IAgentNode agentNode);

  /**
   * Setter for the agent's memory-component. Used for dependency injection by
   * Spring.
   * 
   * @param memory the memory-component of this agent.
   */
  public void setMemory(IMemory memory);

  /**
   * Setter for the agent's execution-cycle-component. Used for dependency
   * injection by Spring.
   * 
   * @param execution the execution-cycle-component of this agent.
   */
  public void setExecution(IExecutionCycle execution);

  /**
   * Setter for the agent's agentbeans. Used for dependency injection by
   * Spring.
   * 
   * @param agentbeans the agentbeans of this agent.
   */
  public void setAgentBeans(List<IAgentBean> agentbeans);

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
   * Getter for the owner of this agent.
   * 
   * @return the owner of this agent.
   */
  public String getOwner();

  /**
   * Setter for the owner of this agent.
   * 
   * @param owner the owner of this agent.
   */
  public void setOwner(String owner);

  /**
   * Getter for a list of agentbeans of this agent.
   * 
   * @return the unmodifiable list of the agentbeans.
   * 
   * @see Collections#unmodifiableList(List)
   */
  public List<IAgentBean> getAgentBeans();

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
   * @see org.apache.commons.logging.Log
   */
  public Log getLog(IAgentBean bean);

  /**
   * Getter for a log-instance that heeds the hierarchie of the agentnode,
   * the agent and the agentbean, i.e the name of the logger consists of the agentnode-name,
   * the name of the agent-parameter, the beanname and the extension. Note that the
   * implementation this method should call the approriate method of the
   * agentnode to retrieve the log-instance to insure consistency.
   * 
   * @param owner the bean for which the logger shall be retrieved.
   * @param extension the part of the bean for which the logger shall be retrieved.
   * @return a log-object that contains the agentnodes name, the agents name,
   *         the beanname and the extension or <code>null</code> if the agent node is unknown.
   */
  public Log getLog(IAgentBean owner, String extension);
  
  /**
   * Returns the agent description of this agent.
   * @return the agent description of this agent
   */
  public AgentDescription getAgentDescription();

  /**
   * Gets the list of actions exposed by this agent.
   * 
   * @return unmodifiable list of actions.
   * 
   * @see Collections#unmodifiableList(List)
   */
  public List<Action> getActionList();

  /**
   * Sets the list of actions to be exposed by this agent.
   * @param actionList the list of actions.
   */
  public void setActionList(List<Action> actionList);

}
