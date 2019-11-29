package de.dailab.jiactng.agentcore;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.directory.IDirectory;
import de.dailab.jiactng.agentcore.execution.IExecutionCycle;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;
import de.dailab.jiactng.agentcore.util.jar.JARClassLoader;

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
   * Setter for the agent's communication-component. Used for dependency
   * injection by Spring.
   * 
   * @param newCommunication the communication-component of this agent.
   */
  void setCommunication(ICommunicationBean newCommunication);

  /**
   * Setter for the agent's agent beans. Used for dependency injection by
   * Spring.
   * 
   * @param agentbeans the agent beans of this agent.
   */
  void setAgentBeans(List<IAgentBean> agentbeans);

  /** TODO rename to setAgentName
   * Setter for the name of this agent. Used by Spring during initialization.
   * 
   * @param name
   *          the name of this agent
   * @deprecated use #setAgentName(String) instead
   */
  void setBeanName(String name);

  /**
   * Setter for the name of this agent. Used by Spring during initialization.
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
   * Getter for the agent node that hosts this agent.
   * 
   * @return a reference to the agent node.
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
   * Getter for a list of agent beans of this agent.
   * 
   * @return the unmodifiable list of the agent beans.
   * 
   * @see java.util.Collections#unmodifiableList(List)
   */
  List<IAgentBean> getAgentBeans();

  /**
   * Getter for the global thread pool responsible for this agent. All threads
   * should be created via this thread pool, to make sure the resources can be
   * controlled. Note that the implementation this method should call the
   * appropriate method of the agent node to retrieve the thread pool to insure
   * consistency.
   * 
   * @return a new ExecutorService
   * @see de.dailab.jiactng.agentcore.IAgentNode#getThreadPool()
   * @see java.util.concurrent.ExecutorService
   */
  ExecutorService getThreadPool();

  /**
   * Getter for a log-instance that heeds the hierarchy of the agent node and
   * the agent, i.e the name of the logger consists of the agent node's name and
   * the name of the agent-parameter and the bean name. Note that the
   * implementation this method should call the appropriate method of the
   * agent node to retrieve the log-instance to insure consistency.
   * 
   * @param bean
   *          the bean for which the logger shall be retrieved.
   * @return a log-object that contains the agent node's name, the agents name and
   *         the bean name.
   */
  Logger getLog(IAgentBean bean);

  /**
   * Getter for a log-instance that heeds the hierarchy of the agent node,
   * the agent and the agent bean, i.e the name of the logger consists of the agent node's name,
   * the name of the agent-parameter, the bean name and the extension. Note that the
   * implementation this method should call the appropriate method of the
   * agent node to retrieve the log-instance to insure consistency.
   * 
   * @param bean the bean for which the logger shall be retrieved.
   * @param extension the part of the bean for which the logger shall be retrieved.
   * @return a log-object that contains the agent node's name, the agents name,
   *         the bean name and the extension or <code>null</code> if the agent node is unknown.
   */
  Logger getLog(IAgentBean bean, String extension);
  
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
  List<IActionDescription> getActionList();

  /**
   * Sets the list of actions to be exposed by this agent.
   * @param newActionList the list of actions.
   */
  void setActionList(List<IActionDescription> newActionList);

  /**
   * Gets the timeout after which the execution of a bean will be stopped.
   * 
   * @return the timeout in milliseconds
   */
  long getBeanExecutionTimeout();
  
  /**
   * Gets the Spring configuration XML snippet for this agent.
   * 
   * @return byte array of the XML spring source
   */
  byte[] getSpringConfigXml();
  
  /**
   * Stores the Spring configuration XML snippet. Note: this function only stores a
   * XML code snippet, it will NOT configure the agent.
   * @param springConfig Spring Configuration XML source
   */
  void setSpringConfigXml(byte[] springConfig);

  /**
   * Utility method for retrieving a reference to an agent bean of the given class. 
   * @param type the class of the agent bean that you want to find.
   * @return a reference to the agent bean. 
   */
  <T> T findAgentBean(Class<T> type);
 
  
  /**
   * Getter for the communication bean of this agent.
   * @return a reference to the communication bean.
   */
  ICommunicationBean getCommunication();
  
  List<IAgentRole> getRoles();
  
  void setRoles(List<IAgentRole> roles);

  /**
   * Gets the agent specific class loader.
   * @return the class loader
   */
  JARClassLoader getClassLoader();

  /**
   * Sets the agent specific class loader.
   * @param cl the class loader
   */
  void setClassLoader(JARClassLoader cl);

  /**
   * Tries to load a given class.
   * @param className the name of the class.
   * @throws ClassNotFoundException if the class was not found by the agent's class loader.
   */
  void loadClass(String className) throws ClassNotFoundException;

}
