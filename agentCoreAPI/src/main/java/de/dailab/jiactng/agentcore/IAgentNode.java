package de.dailab.jiactng.agentcore;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.management.jmx.JmxConnector;

/**
 * Interface for agent nodes. This interfaces defines functionalities, that are
 * available for other components within and outside of the node.
 * 
 * @author Joachim Fuchs
 * @author Thomas Konnerth
 */
public interface IAgentNode extends ILifecycle, ILifecycleListener {

  /**
   * Returns the unique identifier for this agent node. The identifier is created
   * when the agent node is first created, and stays fixed as long as the JVM is
   * alive.
   * 
   * @return a String containing the unique identifier for this agent node.
   */
  String getUUID();

  /**
   * The name of this agent node, as set by Spring
   * 
   * @return the name of this agent node.
   */
  String getName();

  /**
   * Setter for the name of this agent node.
   * 
   * @param newName
   *          the name of this agent node
   */
  void setBeanName(String newName);

  /**
   * Setter for the initial list of agents of this agent node. Used by Spring
   * during initialization.
   * 
   * @param newAgents
   *          the list of agents that should be started with the agent nodes.
   */
  void setAgents(List<IAgent> newAgents);

  /**
   * Adds an agent to the agent node during runtime.
   * 
   * @param agent
   *          the instance if IAgent that shall be started on the agent node.
   * @see de.dailab.jiactng.agentcore.IAgent
   */
  void addAgent(IAgent agent);

  /**
   * Removes an agent from the agent node during runtime. The agents state is set
   * to cleanup and the agent object is destroyed afterwards.
   * 
   * @param agent
   *          the instance if IAgent that shall be removed on the agent node.
   * @see de.dailab.jiactng.agentcore.IAgent
   */
  void removeAgent(IAgent agent);

  /**
   * Getter for a list of agents on the agent node
   * 
   * @return an unmodifiable list of IAgent-instances that are currently residing on this
   *         agent node.
   * 
   * @see java.util.Collections#unmodifiableList(List)
   */
  List<IAgent> findAgents();

  /**
   * Getter for a log-instance that heeds the hierarchy of the agent node, i.e
   * the name of the logger consists of the UUID of the agent node and the name
   * of the agent node bean.
   * 
   * @param nodeBean
   *          the node bean for which the logger shall be instantiated.
   * @return a log-object that contains the UUID of the agent node and the name of the 
   * 		 node bean.
   */
  Logger getLog(IAgentNodeBean nodeBean);

  /**
   * Getter for a log-instance that heeds the hierarchy of the agent node and the 
   * agent node bean, i.e the name of the logger consists of the UUID of 
   * the agent node, the name of the agent node bean and the extension.
   * 
   * @param nodeBean
   *          the node bean that contains the node bean part for which the 
   *          logger shall be instantiated.
   * @param extension
   *          the node bean part for which the logger shall be instantiated.
   * @return a log-object that contains the UUID of the agent node, the name 
   *          of the agent node bean and the extension.
   */
  Logger getLog(IAgentNodeBean nodeBean, String extension);

  /**
   * Getter for a log-instance that heeds the hierarchy of the agent node, i.e
   * the name of the logger consists of the UUID of the agent node and the ID
   * of the agent.
   * 
   * @param agent
   *          the agent for which the logger shall be instantiated.
   * @return a log-object that contains the UUID of the agent node and the ID of the agent.
   */
  Logger getLog(IAgent agent);

  /**
   * Getter for a log-instance that heeds the hierarchy of the agent node and
   * the agent, i.e the name of the logger consists of the UUID of 
   * the agent node, the ID of the agent and the extension.
   * 
   * @param agent
   *          the agent that contains the agent part for which the 
   *          logger shall be instantiated.
   * @param extension
   *          the agent part for which the logger shall be instantiated.
   * @return a log-object that contains the UUID of the agent node, the ID 
   *          of the agent and the extension.
   */
  Logger getLog(IAgent agent, String extension);

  /**
   * Getter for a log-instance that heeds the hierarchy of the agent node and
   * the agent, i.e the name of the logger consists of the UUID of the agent node,
   * the ID of the agent and the name of the agent bean.
   * 
   * 
   * @param agent
   *          the agent that contains the bean for which the logger shall be
   *          instantiated.
   * @param bean
   *          the bean for which the logger shall be instantiated.
   * @return a log-object that contains the UUID of the agent node, the ID of the agent
   *         and the name of the agent bean.
   */
  Logger getLog(IAgent agent, IAgentBean bean);
  
  /**
   * Getter for a log-instance that heeds the hierarchy of the agent node,
   * the agent and the agent bean, i.e the name of the logger consists of the UUID of 
   * the agent node, the ID of the agent, the name of the agent bean and the extension.
   * 
   * 
   * @param agent
   *          the agent that contains the bean part for which the logger shall be
   *          instantiated.
   * @param bean
   *          the bean that contains the bean part for which the logger shall be instantiated.
   * @param extension
   *          the bean part for which the logger shall be instantiated.
   * @return a log-object that contains the UUID of the agent node, the ID of the agent,
   *         the name of the agent bean and the extension.
   */
  Logger getLog(IAgent agent, IAgentBean bean, String extension);

  /**
   * Getter for the global thread pool for this agent node. All threads should be
   * created via this thread pool, to make sure the resources can be controlled.
   * 
   * @return a new ExecutorService
   * @see java.util.concurrent.ExecutorService
   */
  ExecutorService getThreadPool();
  
  /**
   * Setter for agentnode's beans. This is thought as generic port
   * for extending the functionality of an agent node with infrastructure
   * services. Beans of the agent node also underly the agent node life-cycle.
   * 
   * @param agentnodebeans the beans to set 
   */
  void setAgentNodeBeans(List<IAgentNodeBean> agentnodebeans);
  
  /**
   * Getter for beans of the agent node.
   * 
   * @return an unmodifiable list of agent node beans
   * 
   * @see java.util.Collections#unmodifiableList(List)
   */
  List<IAgentNodeBean> getAgentNodeBeans();

  /**
   * Sets the configuration of the JMX connector servers used for remote management.
   * 
   * @param newJmxConnectors
   *            the set of connectors.
   */
  void setJmxConnectors(Set<JmxConnector> newJmxConnectors);

  /**
   * Gets the configuration of the JMX connector servers used for remote management.
   * 
   * @return the set of connectors.
   */
  Set<JmxConnector> getJmxConnectors();

  /**
   * Gets the URLs of the JMX connector server.
   * 
   * @return the URLs of the JMX connector server.
   */
  Set<JMXServiceURL> getJmxURLs();

  /**
   * Utility method for retrieving a reference to an agent node bean of the given class. 
   * @param type the class of the agent node bean that you want to find.
   * @return a reference to the agent node bean. 
   */
  <T> T findAgentNodeBean(Class<T> type);

  /**
   * Tries to load a given class.
   * @param className the name of the class.
   * @throws ClassNotFoundException if the class was not found by the agent node's class loader.
   */
  void loadClass(String className) throws ClassNotFoundException;

  /**
   * Gets the discovery URI used by all brokers of the agent node.
   * @return the discovery URI or <code>null</code> if the broker use their own discovery URIs.
   */
  String getOverwriteDiscoveryURI();

}
