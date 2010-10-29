package de.dailab.jiactng.agentcore.management;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.IAgentNodeBean;

/**
 * Interface for all managers which are able to register and deregister agent
 * nodes, agent node beans, agent node resources, agents, agent resources, agent
 * beans and agent bean resources. The manager enables local objects or remote
 * applications to get or set attributes of registered resources or to
 * invoke methods of these resources.
 * @author Jan Keiser
 */
public interface Manager {

	/**
	 * Constructs a unique name for the management of an agent node.
	 * @param nodeId the UUID of the agent node
	 * @return unique name of the agent node
	 * @throws Exception If the parameter is incorrect.
	 */
	Object getMgmtNameOfAgentNode(String nodeId) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent node bean.
	 * @param nodeId the UUID of the agent node which contains the agent node bean
	 * @param beanName the name of the agent node bean
	 * @return unique name of the agent node bean
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentNodeBean(String nodeId, String beanName) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent node bean resource. The
	 * agent node bean may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node which contains the agent node bean
	 * @param beanName the name of the agent node bean
	 * @param resourceType the type of the agent node bean resource
	 * @param resourceName the name of the agent node bean resource
	 * @return unique name of the agent node bean resource
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentNodeBeanResource(String nodeId, String beanName, String resourceType, String resourceName) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent node resource. The
	 * agent node always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node
	 * @param resourceType the type of the agent node resource
	 * @return unique name of the agent node resource
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentNodeResource(String nodeId, String resourceType) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent node resource. The
	 * agent node may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resourceName the name of the agent node resource
	 * @return the unique name of the agent node resource
	 * @throws Exception One of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentNodeResource(String nodeId, String resourceType, String resourceName) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentName the name of the agent
	 * @return unique name of the agent
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgent(String nodeId, String agentName) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent resource. The
	 * agent always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentName the name of the agent
	 * @param resourceType the type of the agent resource
	 * @return unique name of the agent resource
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentResource(String nodeId, String agentName, String resourceType) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent resource. The
	 * agent may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentName the name of the agent
	 * @param resourceType the type of the agent resource
	 * @param resourceName the name of the agent resource
	 * @return unique name of the agent resource
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentResource(String nodeId, String agentName, String resourceType, String resourceName) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent bean.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentName the name of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @return unique name of the agent bean
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentBean(String nodeId, String agentName, String beanName) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent bean resource. The
	 * agent bean always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentName the name of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param resourceType the type of the resource
	 * @return unique name of the agent bean resource
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentBeanResource(String nodeId, String agentName, String beanName, String resourceType) throws Exception;

	/**
	 * Constructs a unique name for the management of an agent bean resource. The
	 * agent bean may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentName the name of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param resourceType the type of the resource
	 * @param resourceName the name of the resource
	 * @return unique name of the agent bean resource
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getMgmtNameOfAgentBeanResource(String nodeId, String agentName, String beanName, String resourceType, String resourceName) throws Exception;
	
	/**
	 * Registers an agent node for management.
	 * @param agentNode the agent node to be registered
	 * @throws Exception The name of the agent node is incorrect or the agent node is already registered.
	 */
	void registerAgentNode(IAgentNode agentNode) throws Exception;

	/**
	 * Registers an agent node bean for management.
	 * @param agentNodeBean the agent node bean to be registered
	 * @param agentNode the agent node which contains the agent node bean
	 * @throws Exception The name of the agent node bean or agent node is incorrect or the agent node bean is already registered.
	 */
	void registerAgentNodeBean(IAgentNodeBean agentNodeBean, IAgentNode agentNode) throws Exception;

	/**
	 * Registers an agent node bean resource for management. The agent node bean may contain more
	 * than one resource of the specified type.
	 * @param agentNodeBean the agent node bean
	 * @param agentNode the agent node which contains the agent node bean
	 * @param resourceType the type of the agent node bean resource
	 * @param resourceName the name of the agent node bean resource
	 * @param resource the agent node bean resource to be registered
	 * @throws Exception The name of the agent node bean or agent node is incorrect or the type or name of the resource is incorrect or the agent node bean resource is already registered.
	 */
	void registerAgentNodeBeanResource(IAgentNodeBean agentNodeBean, IAgentNode agentNode, String resourceType, String resourceName, Object resource) throws Exception;

	/**
	 * Registers an agent node resource for management. The agent node contains only
	 * one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resource the agent node resource to be registered
	 * @throws Exception The UUID of the agent node or the type of the resource is incorrect or the agent node resource is already registered.
	 */
	void registerAgentNodeResource(IAgentNode node, String resourceType, Object resource) throws Exception;

	/**
	 * Registers an agent node resource for management. The agent node may contain more
	 * than one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resourceName the name of the agent node resource
	 * @param resource the agent node resource to be registered
	 * @throws Exception The UUID of the agent node or the type or name of the resource is incorrect or the agent node resource is already registered.
	 */
	void registerAgentNodeResource(IAgentNode node, String resourceType, String resourceName, Object resource) throws Exception;

	/**
	 * Registers an agent for management.
	 * @param agent the agent to be registered
	 * @throws Exception The name of the agent or agent node is incorrect or the agent is already registered.
	 */
	void registerAgent(IAgent agent) throws Exception;

	/**
	 * Registers an agent resource for management. The agent contains only
	 * one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @param resource the agent resource to be registered
	 * @throws Exception The name of the agent or agent node or the type of resource is incorrect or the agent resource is already registered.
	 */
	void registerAgentResource(IAgent agent, String resourceType, Object resource) throws Exception;

	/**
	 * Registers an agent resource for management. The agent may contain more
	 * than one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @param resourceName the name of the agent resource
	 * @param resource the agent resource to be registered
	 * @throws Exception The name of the agent or agent node or the type or name of resource is incorrect or the agent resource is already registered.
	 */
	void registerAgentResource(IAgent agent, String resourceType, String resourceName, Object resource) throws Exception;

	/**
	 * Registers an agent bean for management.
	 * @param agentBean the agent bean to be registered
	 * @param agent the agent which contains this agent bean
	 * @throws Exception The name of the agent bean, agent or agent node is incorrect or the agent bean is already registered.
	 */
	void registerAgentBean(IAgentBean agentBean, IAgent agent) throws Exception;

	/**
	 * Registers an agent bean resource for management. The agent bean may contain more
	 * than one resource of the specified type.
	 * @param agentBean the agent bean which contains the resource
	 * @param agent the agent which contains this agent bean
	 * @param resourceType the type of the agent bean resource
	 * @param resourceName the name of the agent bean resource
	 * @param resource the agent bean resource to be registered
	 * @throws Exception The name of the agent bean, agent, agent node or type or name of the resource is incorrect or the agent bean resource is already registered.
	 */
	void registerAgentBeanResource(IAgentBean agentBean, IAgent agent, String resourceType, String resourceName, Object resource) throws Exception;

	/**
	 * Unregisters an agent node from management.
	 * @param agentNode the agent node to be unregistered
	 * @throws Exception The name of the agent node is incorrect or the agent node is not registered.
	 */
	void unregisterAgentNode(IAgentNode agentNode) throws Exception;

	/**
	 * Unregisters an agent node bean from management.
	 * @param agentNodeBean the agent node bean to be unregistered
	 * @param agentNode the agent node which contains the agent node bean
	 * @throws Exception The name of the agent node bean or agent node is incorrect or the agent node bean is not registered.
	 */
	void unregisterAgentNodeBean(IAgentNodeBean agentNodeBean, IAgentNode agentNode) throws Exception;

	/**
	 * Unregisters an agent node bean resource from management. The agent node bean may contain more
	 * than one resource of the specified type.
	 * @param agentNodeBean the agent node bean
	 * @param agentNode the agent node which contains the agent node bean
	 * @param resourceType the type of the agent node bean resource
	 * @param resourceName the name of the agent node bean resource
	 * @throws Exception One of the parameters is incorrect or the agent node bean resource is not registered.
	 */
	void unregisterAgentNodeBeanResource(IAgentNodeBean agentNodeBean, IAgentNode agentNode, String resourceType, String resourceName) throws Exception;

	/**
	 * Unregisters an agent node resource from management. The agent node contains only
	 * one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @throws Exception One of the parameters is incorrect or the agent node resource is not registered.
	 */
	void unregisterAgentNodeResource(IAgentNode node, String resourceType) throws Exception;

	/**
	 * Unregisters an agent node resource from management. The agent node may contain more
	 * than one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resourceName the name of the agent node resource
	 * @throws Exception One of the parameters is incorrect or the agent node resource is not registered.
	 */
	void unregisterAgentNodeResource(IAgentNode node, String resourceType, String resourceName) throws Exception;

	/**
	 * Unregisters an agent from management.
	 * @param agent the agent to be unregistered
	 * @throws Exception The name of the agent or agent node is incorrect or the agent is not registered.
	 */
	void unregisterAgent(IAgent agent) throws Exception;

	/**
	 * Unregisters an agent resource from management. The agent contains only
	 * one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @throws Exception The name of the agent or agent node or the type of resource is incorrect or the agent resource is not registered.
	 */
	void unregisterAgentResource(IAgent agent, String resourceType) throws Exception;

	/**
	 * Unregisters an agent resource from management. The agent may contain more
	 * than one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @param resourceName the name of the agent resource
	 * @throws Exception The name of the agent or agent node or the type or name of resource is incorrect or the agent resource is not registered.
	 */
	void unregisterAgentResource(IAgent agent, String resourceType, String resourceName) throws Exception;

	/**
	 * Unregisters an agent bean from management.
	 * @param agentBean the agent bean to be unregistered
	 * @param agent the agent which contains this agent bean
	 * @throws Exception The name of the agent bean, agent or agent node is incorrect or the agent bean is not registered.
	 */
	void unregisterAgentBean(IAgentBean agentBean, IAgent agent) throws Exception;

	/**
	 * Unregisters an agent bean resource from management. The agent bean may contain more
	 * than one resource of the specified type.
	 * @param agentBean the agent bean which contains the resource
	 * @param agent the agent which contains the agent bean
	 * @param resourceType the type of the agent bean resource
	 * @param resourceName the name of the agent bean resource
	 * @throws Exception The name of the agent bean, agent, agent node or type or name of the resource is incorrect or the agent bean resource is not registered.
	 */
	void unregisterAgentBeanResource(IAgentBean agentBean, IAgent agent, String resourceType, String resourceName) throws Exception;

	/**
	 * Gets the value of an attribute of an agent node.
	 * @param nodeId the UUID of the agent node
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws Exception If one of the parameter is incorrect.
	 */
	Object getAttributeOfAgentNode(String nodeId, String attributeName) throws Exception;

	/**
	 * Gets the value of an attribute of an agent node bean.
	 * @param nodeId the UUID of the agent node which contains the agent node bean
	 * @param beanName the name of the agent node bean
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getAttributeOfAgentNodeBean(String nodeId, String beanName, String attributeName) throws Exception;

	/**
	 * Gets the value of an attribute of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the unique identifier of the agent
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getAttributeOfAgent(String nodeId, String agentId, String attributeName) throws Exception;

	/**
	 * Gets the value of an attribute of an agent bean.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentId the unique identifier of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object getAttributeOfAgentBean(String nodeId, String agentId, String beanName, String attributeName) throws Exception;

	/**
	 * Sets the value of an attribute of an agent node.
	 * @param nodeId the UUID of the agent node
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws Exception If one of the parameter is incorrect.
	 */
	void setAttributeOfAgentNode(String nodeId, String attributeName, Object attributeValue) throws Exception;

	/**
	 * Sets the value of an attribute of an agent node bean.
	 * @param nodeId the UUID of the agent node which contains the agent node bean
	 * @param beanName the name of the agent node bean
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws Exception If one of the parameters is incorrect.
	 */
	void setAttributeOfAgentNodeBean(String nodeId, String beanName, String attributeName, Object attributeValue) throws Exception;

	/**
	 * Sets the value of an attribute of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the unique identifier of the agent
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws Exception If one of the parameters is incorrect.
	 */
	void setAttributeOfAgent(String nodeId, String agentId, String attributeName, Object attributeValue) throws Exception;

	/**
	 * Sets the value of an attribute of an agent bean.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentId the unique identifier of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws Exception If one of the parameters is incorrect.
	 */
	void setAttributeOfAgentBean(String nodeId, String agentId, String beanName, String attributeName, Object attributeValue) throws Exception;

	/**
	 * Invokes an operation on an agent node.
	 * @param nodeId The UUID of the agent node.
	 * @param operationName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the operation is invoked.
	 * @param signature An array containing the signature of the operation.
	 * @return The object returned by the operation.
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object invokeAgentNode(String nodeId, String operationName, Object[] params, String[] signature) throws Exception;

	/**
	 * Invokes an operation on an agent node bean.
	 * @param nodeId The UUID of the agent node which contains the agent node bean.
	 * @param beanName The name of the agent node bean.
	 * @param operationName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the operation is invoked.
	 * @param signature An array containing the signature of the operation.
	 * @return The object returned by the operation.
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object invokeAgentNodeBean(String nodeId, String beanName, String operationName, Object[] params, String[] signature) throws Exception;

	/**
	 * Invokes an operation on an agent.
	 * @param nodeId The UUID of the agent node where the agent is residing on.
	 * @param agentId The unique identifier of the agent.
	 * @param operationName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the operation is invoked.
	 * @param signature An array containing the signature of the operation.
	 * @return The object returned by the operation.
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object invokeAgent(String nodeId, String agentId, String operationName, Object[] params, String[] signature) throws Exception;

	/**
	 * Invokes an operation on an agent bean.
	 * @param nodeId The UUID of the agent node where the agent is residing on which contains the agent bean.
	 * @param agentId The unique identifier of the agent which contains the agent bean.
	 * @param beanName The name of the agent bean.
	 * @param operationName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the operation is invoked.
	 * @param signature An array containing the signature of the operation.
	 * @return The object returned by the operation.
	 * @throws Exception If one of the parameters is incorrect.
	 */
	Object invokeAgentBean(String nodeId, String agentId, String beanName, String operationName, Object[] params, String[] signature) throws Exception;

	/**
	 * Creates all specified connector server for remote management.
	 * @param node the agent node
	 * @return Information about the created connector server.
	 */
	Object enableRemoteManagement(IAgentNode node);

	/**
	 * Stops all connector servers.
	 * @param node the agent node
	 */
	void disableRemoteManagement(IAgentNode node);
	
}
