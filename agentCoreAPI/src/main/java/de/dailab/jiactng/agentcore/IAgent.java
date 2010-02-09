package de.dailab.jiactng.agentcore;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.directory.IDirectory;
import de.dailab.jiactng.agentcore.execution.IExecutionCycle;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * Interface for agents. This interfaces defines functionalities, that are
 * available for other components within and outside of the agent.
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public interface IAgent extends ILifecycleListener, Runnable,
    ILifecycle, IDirectory {

  /**
   * Setter for the agent node that hosts this agent. Called by the agent node,
   * when the agent is created.
   * 
   * @param newAgentNode
   *          the agent node that hosts this agent.
   */
  void setAgentNode(IAgentNode newAgentNode);

  /**
   * Setter for the agent's memory-component. Used for dependency injection by
   * Spring.
   * 
   * @param newMemory the memory-component of this agent.
   */
  void setMemory(IMemory newMemory);
  
  
  /**
   * Setter for the agent's execution-cycle-component. Used for dependency
   * injection by Spring.
   * 
   * @param newExecution the execution-cycle-component of this agent.
   */
  void setExecution(IExecutionCycle newExecution);
    

  /**
   * Setter for the agent's agentbeans. Used for dependency injection by
   * Spring.
   * 
   * @param agentbeans the agentbeans of this agent.
   */
  void setAgentBeans(List<IAgentBean> agentbeans);

  /** TODO rename to setAgentName
   * Setter for the name of this agent. Used by Spring during initialisation.
   * 
   * @param name
   *          the name of this agent
   * @deprecated use #setAgentName(String) instead
   */
  void setBeanName(String name);

  /**
   * Setter for the name of this agent. Used by Spring during initialisation.
   * 
   * @param name the name of this agent.
   */
  void setAgentName(String name);
  
  /**
   * Getter for the name of this agent, as set by Spring.
   * 
   * @return the name of this agent.
   */
  String getAgentName();

  /**
   * Getter for the agent identifier.
   * 
   * @return the agent identifier of this agent.
   */
  String getAgentId();

  /**
   * Getter for the agentnode that hosts this agent.
   * 
   * @return a reference to the agentnode.
   */
  IAgentNode getAgentNode();

  /**
   * Getter for the owner of this agent.
   * 
   * @return the owner of this agent.
   */
  String getOwner();

  /**
   * Setter for the owner of this agent.
   * 
   * @param newOwner the owner of this agent.
   */
  void setOwner(String newOwner);

  /**
   * Getter for a list of agentbeans of this agent.
   * 
   * @return the unmodifiable list of the agentbeans.
   * 
   * @see java.util.Collections#unmodifiableList(List)
   */
  List<IAgentBean> getAgentBeans();

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
  ExecutorService getThreadPool();

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
  Log getLog(IAgentBean bean);

  /**
   * Getter for a log-instance that heeds the hierarchie of the agentnode,
   * the agent and the agentbean, i.e the name of the logger consists of the agentnode-name,
   * the name of the agent-parameter, the beanname and the extension. Note that the
   * implementation this method should call the approriate method of the
   * agentnode to retrieve the log-instance to insure consistency.
   * 
   * @param bean the bean for which the logger shall be retrieved.
   * @param extension the part of the bean for which the logger shall be retrieved.
   * @return a log-object that contains the agentnodes name, the agents name,
   *         the beanname and the extension or <code>null</code> if the agent node is unknown.
   */
  Log getLog(IAgentBean bean, String extension);
  
  /**
   * Returns the agent description of this agent.
   * @return the agent description of this agent
   */
  IAgentDescription getAgentDescription();

  /**
   * Gets the list of actions exposed by this agent.
   * 
   * @return unmodifiable list of actions.
   * 
   * @see java.util.Collections#unmodifiableList(List)
   */
  List<Action> getActionList();

  /**
   * Sets the list of actions to be exposed by this agent.
   * @param newActionList the list of actions.
   */
  void setActionList(List<Action> newActionList);

  /**
   * Gets the timeout after which the execution of a bean will be stopped.
   * 
   * @return the timeout in milliseconds
   */
  long getBeanExecutionTimeout();
  
  /**
   * Gets the Spring configuration xml snippet for this agent.
   * 
   * @return bytearray of the xml spring source
   */
  byte[] getSpringConfigXml();
  
  /**
   * Stores the Spring configuration xml snippet. Note: this function only stores a
   * xml code snippet, it will NOT configure the agent.
   * @param springConfig Spring Configuration xml source
   */
  void setSpringConfigXml(byte[] springConfig);

}
