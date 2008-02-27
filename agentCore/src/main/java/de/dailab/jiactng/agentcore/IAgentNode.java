package de.dailab.jiactng.agentcore;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.management.Manageable;

/**
 * Interface for agentnodes. This interfaces defines functionalities, that are
 * available for other components within and outside of the node.
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public interface IAgentNode extends ILifecycle, BeanNameAware, ILifecycleListener, Manageable {

  /**
   * Returns the unique identifier for this agentnode. The identifier is created
   * when the agentnode is first created, and stays fixed as long as the jvm is
   * alive.
   * 
   * @return a String containing the unique identifier for this agentnode.
   */
  public String getUUID();

  /**
   * The name of this agentnode, as set by Spring
   * 
   * @return the name of this agentnode.
   */
  public String getName();

  /**
   * Setter for the name of this agentnode. Used by Spring during
   * initialisation.
   * 
   * @param name
   *          the name of this agentnode
   * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
   */
  public void setBeanName(String name);

  /**
   * Setter for the initial list of agents of this agentnode. Used by Spring
   * during initialisation.
   * 
   * @param agents
   *          the list of agents that should be started with the agendnodes.
   */
  public void setAgents(List<IAgent> agents);

  /**
   * Adds an agent to the agentnode during runtime.
   * 
   * @param agent
   *          the instance if IAgent that shall be started on the agentnode.
   * @see de.dailab.jiactng.agentcore.IAgent
   */
  public void addAgent(IAgent agent);

  /**
   * Removes an agent from the agentnode during runtime. The agents state is set
   * to cleanup and the agentobject is destroyed afterwards.
   * 
   * @param agent
   *          the instance if IAgent that shall be removed on the agentnode.
   * @see de.dailab.jiactng.agentcore.IAgent
   */
  public void removeAgent(IAgent agent);

  /**
   * Getter for a list of agents on the agentnode
   * 
   * @return an unmodifiable list of IAgent-instances that are currently residing on this
   *         agentnode.
   * 
   * @see Collections#unmodifiableList(List)
   */
  public List<IAgent> findAgents();

  /**
   * Getter for a log-instance that heeds the hierarchie of the agentnode, i.e
   * the name of the logger consists of the agentnode-name and the name of the
   * agent-parameter.
   * 
   * @param agent
   *          the agent for which the logger shall be instantiated.
   * @return a log-object that contains the agentnodes name and the agents name.
   * @see org.apache.commons.logging.Log;
   */
  public Log getLog(IAgent agent);

  /**
   * Getter for a log-instance that heeds the hierarchie of the agentnode and
   * the agent, i.e the name of the logger consists of the agentnode-name and
   * the name of the agent-parameter and the beanname.
   * 
   * 
   * @param agent
   *          the agent that contains the bean for which the logger shall be
   *          instantiated.
   * @param bean
   *          the bean for which the logger shall be instantiated.
   * @return a log-object that contains the agentnodes name, the agents name and
   *         the beanname.
   * @see org.apache.commons.logging.Log;
   */
  public Log getLog(IAgent agent, IAgentBean bean);
  
  /**
   * Getter for a log-instance that heeds the hierarchie of the agentnode,
   * the agent and the agent bean, i.e the name of the logger consists of the agentnode-name,
   * the name of the agent-parameter, the beanname and the extension.
   * 
   * 
   * @param agent
   *          the agent that contains the bean for which the logger shall be
   *          instantiated.
   * @param bean
   *          the bean for which the logger shall be instantiated.
   * @param extension
   *          the bean part for which the logger shall be instantiated.
   * @return a log-object that contains the agentnodes name, the agents name,
   *         the beanname and the extension.
   * @see org.apache.commons.logging.Log;
   */
  public Log getLog(IAgent agent, IAgentBean bean, String extension);

  /**
   * Getter for the global threadpool for this agentnode. All threads should be
   * created via this threadpool, to make sure the ressources can be controled.
   * 
   * @return a new ExecutorService
   * @see java.util.concurrent.ExecutorService
   */
  public ExecutorService getThreadPool();
  
  /**
   * Setter for agentnode's beans. This is thought as generic port
   * for extending the functionality of an agentnode with infrastructure
   * services. Beans of the agentnode also underly the agentnode lifecycle.
   * 
   * @param agentnodebeans the beans to set 
   */
  public void setAgentNodeBeans(List<IAgentNodeBean> agentnodebeans);
  
  /**
   * Getter for beans of the agentnode.
   * 
   * @return an unmodifiable list of agentnode beans
   * 
   * @see Collections#unmodifiableList(List)
   */
  public List<IAgentNodeBean> getAgentNodeBeans();
}
