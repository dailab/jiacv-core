package de.dailab.jiactng.agentcore.management.jmx;

import java.lang.management.ManagementFactory;
import java.util.Timer;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.IAgentNodeBean;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * This manager uses JMX to register and deregister agent nodes, agent node
 * beans, agent node resources, agents, agent resources, agent beans and agent
 * bean resources with a hierarchical namespace. Local objects and remote
 * applications are able to get or set attributes of registered resources 
 * (MBeans) or to invoke methods of these MBeans.
 * @author Jan Keiser
 * @see MBeanServer
 */
public final class JmxManager implements Manager {

	/** The delay for sending multicast messages is 1,000 milliseconds. */
	public static final long MULTICAST_DELAY = 0;

	/** The period for sending multicast messages is 3,600 milliseconds. */
	public static final long MULTICAST_PERIOD = 3600;
	
	/** The port for sending multicast messages is 9999. */
	public static final int MULTICAST_PORT = 9999;
	
	/** The address for sending multicast messages is "226.6.6.7". */
	public static final String MULTICAST_ADDRESS = "226.6.6.7";

	/** The domain of JMX object names is "de.dailab.jiactng".*/
	public static final String DOMAIN = "de.dailab.jiactng";

	// category names

	/** The name of the agent node category as part of the JMX object names. This is also visualized in the tree of beans within jConsole. */
	public static final String CATEGORY_AGENT_NODE = "AgentNode";

	/** The name of the agent node bean category as part of the JMX object names. This is also visualized in the tree of beans within jConsole. */
	public static final String CATEGORY_AGENT_NODE_BEAN = "agentNodeBean";

	/** The name of the agent category as part of the JMX object names. This is also visualized in the tree of beans within jConsole. */
	public static final String CATEGORY_AGENT = "Agent";

	/** The name of the agent bean category as part of the JMX object names. This is also visualized in the tree of beans within jConsole. */
	public static final String CATEGORY_AGENT_BEAN = "AgentBean";

	/** The name of the JMX connector server category as part of the JMX object names. This is also visualized in the tree of beans within jConsole. */
	public static final String CATEGORY_JMX_CONNECTOR_SERVER = "JMXConnectorServer";

	// private fields

	private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	private Timer ti = null;
	private JmxConnectorManager connectorManager = null;

	/**
	 * Constructs a JMX compliant name for the management of an agent node.
	 * @param nodeId the UUID of the agent node
	 * @return the JMX compliant name of the agent node
	 * @throws MalformedObjectNameException The parameter contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentNode(String nodeId) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN)
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent node bean.
	 * @param nodeId the UUID of the agent node
	 * @param beanName the name of the agent node bean
	 * @return the JMX compliant name of the agent node bean
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentNodeBean(String nodeId, String beanName) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(CATEGORY_AGENT_NODE_BEAN).append(",agentnodebean=").append(beanName)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent node bean resource. The
	 * agent node bean may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node
	 * @param beanName the name of the agent node bean
	 * @param resourceType the type of the agent node bean resource
	 * @param resourceName the name of the agent node bean resource
	 * @return the JMX compliant name of the agent node bean resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentNodeBeanResource(String nodeId, String beanName, String resourceType, String resourceName) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(CATEGORY_AGENT_NODE_BEAN).append(",agentnodebean=").append(beanName)
			.append(",category3=").append(resourceType).append(",resource=").append(resourceName)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent node resource. The
	 * agent node always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node
	 * @param resourceType the type of the agent node resource
	 * @return the JMX compliant name of the agent node resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentNodeResource(String nodeId, String resourceType) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(resourceType)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent node resource. The
	 * agent node may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resourceName the name of the agent node resource
	 * @return the JMX compliant name of the agent node resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentNodeResource(String nodeId, String resourceType, String resourceName) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(resourceType).append(",resource=").append(resourceName)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the ID of the agent
	 * @return the JMX compliant name of the agent
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgent(String nodeId, String agentId) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(CATEGORY_AGENT).append(",agent=").append(agentId)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent resource. The
	 * agent always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the ID of the agent
	 * @param resourceType the type of the agent resource
	 * @return the JMX compliant name of the agent resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentResource(String nodeId, String agentId, String resourceType) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(CATEGORY_AGENT).append(",agent=").append(agentId)
			.append(",category3=").append(resourceType)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent resource. The
	 * agent may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the ID of the agent
	 * @param resourceType the type of the agent resource
	 * @param resourceName the name of the agent resource
	 * @return the JMX compliant name of the agent resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentResource(String nodeId, String agentId, String resourceType, String resourceName) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(CATEGORY_AGENT).append(",agent=").append(agentId)
			.append(",category3=").append(resourceType).append(",resource=").append(resourceName)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent bean.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentId the ID of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @return the JMX compliant name of the agent bean
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentBean(String nodeId, String agentId, String beanName) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(CATEGORY_AGENT).append(",agent=").append(agentId)
			.append(",category3=").append(CATEGORY_AGENT_BEAN).append(",agentbean=").append(beanName)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent bean resource. The
	 * agent bean always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentId the ID of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param resourceType the type of the resource
	 * @return the JMX compliant name of the agent bean resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentBeanResource(String nodeId, String agentId, String beanName, String resourceType) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(CATEGORY_AGENT).append(",agent=").append(agentId)
			.append(",category3=").append(CATEGORY_AGENT_BEAN).append(",agentbean=").append(beanName)
			.append(",category4=").append(resourceType)
			.toString());
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent bean resource. The
	 * agent bean may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentId the ID of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param resourceType the type of the resource
	 * @param resourceName the name of the resource
	 * @return the JMX compliant name of the agent bean resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 */
	public ObjectName getMgmtNameOfAgentBeanResource(String nodeId, String agentId, String beanName, String resourceType, String resourceName) throws MalformedObjectNameException {
		return new ObjectName(new StringBuffer(DOMAIN) 
			.append(":category1=").append(CATEGORY_AGENT_NODE).append(",agentnode=").append(nodeId)
			.append(",category2=").append(CATEGORY_AGENT).append(",agent=").append(agentId)
			.append(",category3=").append(CATEGORY_AGENT_BEAN).append(",agentbean=").append(beanName)
			.append(",category4=").append(resourceType).append(",resource=").append(resourceName)
			.toString());
	}
	
	/**
	 * Registers an agent node as JMX resource.
	 * @param agentNode the agent node to be registered
	 * @throws MalformedObjectNameException The name of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent node is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node has thrown an exception. The agent node will not be registered.
	 * @throws NotCompliantMBeanException The agent node is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNode(String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNode(IAgentNode agentNode) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentNode(agentNode.getUUID());
		mbs.registerMBean(agentNode, name);
	}

	/**
	 * Registers an agent node bean as JMX resource.
	 * @param agentNodeBean the agent node bean to be registered
	 * @param agentNode the agent node which contains the agent node bean
	 * @throws MalformedObjectNameException The name of the agent node bean or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent node bean is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node bean has thrown an exception. The agent node bean will not be registered.
	 * @throws NotCompliantMBeanException The agent node bean is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNodeBean(String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNodeBean(IAgentNodeBean agentNodeBean, IAgentNode agentNode) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentNodeBean(agentNode.getUUID(), agentNodeBean.getBeanName());
		mbs.registerMBean(agentNodeBean, name);
	}

	/**
	 * Registers an agent node bean resource as JMX resource. The agent node bean may contain more
	 * than one resource of the specified type.
	 * @param agentNodeBean the agent node bean
	 * @param agentNode the agent node which contains the agent node bean
	 * @param resourceType the type of the agent node bean resource
	 * @param resourceName the name of the agent node bean resource
	 * @param resource the agent node bean resource to be registered
	 * @throws MalformedObjectNameException The name of the agent node bean or agent node or the type or name of the resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent node bean resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node bean resource has thrown an exception. The agent node bean resource will not be registered.
	 * @throws NotCompliantMBeanException The agent node bean resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNodeBeanResource(String, String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNodeBeanResource(IAgentNodeBean agentNodeBean, IAgentNode agentNode, String resourceType, String resourceName, Object resource) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentNodeBeanResource(agentNode.getUUID(), agentNodeBean.getBeanName(), resourceType, resourceName);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Registers an agent node resource as JMX resource. The agent node contains only
	 * one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resource the agent node resource to be registered
	 * @throws MalformedObjectNameException The UUID of the agent node or the type of the resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent node resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node resource has thrown an exception. The agent node resource will not be registered.
	 * @throws NotCompliantMBeanException The agent node resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNodeResource(String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNodeResource(IAgentNode node, String resourceType, Object resource) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentNodeResource(node.getUUID(), resourceType);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Registers an agent node resource as JMX resource. The agent node may contain more
	 * than one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resourceName the name of the agent node resource
	 * @param resource the agent node resource to be registered
	 * @throws MalformedObjectNameException The UUID of the agent node or the type or name of the resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent node resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node resource has thrown an exception. The agent node resource will not be registered.
	 * @throws NotCompliantMBeanException The agent node resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNodeResource(String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNodeResource(IAgentNode node, String resourceType, String resourceName, Object resource) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentNodeResource(node.getUUID(), resourceType, resourceName);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Registers an agent as JMX resource.
	 * @param agent the agent to be registered
	 * @throws MalformedObjectNameException The name of the agent or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent has thrown an exception. The agent will not be registered.
	 * @throws NotCompliantMBeanException The agent is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgent(String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgent(IAgent agent) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgent(agent.getAgentNode().getUUID(), agent.getAgentId());
		mbs.registerMBean(agent, name);
	}

	/**
	 * Registers an agent resource as JMX resource. The agent contains only
	 * one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @param resource the agent resource to be registered
	 * @throws MalformedObjectNameException The name of the agent or agent node or the type of resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent resource has thrown an exception. The agent resource will not be registered.
	 * @throws NotCompliantMBeanException The agent resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentResource(String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentResource(IAgent agent, String resourceType, Object resource) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentResource(agent.getAgentNode().getUUID(), agent.getAgentId(), resourceType);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Registers an agent resource as JMX resource. The agent may contain more
	 * than one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @param resourceName the name of the agent resource
	 * @param resource the agent resource to be registered
	 * @throws MalformedObjectNameException The name of the agent or agent node or the type or name of resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent resource has thrown an exception. The agent resource will not be registered.
	 * @throws NotCompliantMBeanException The agent resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentResource(String, String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentResource(IAgent agent, String resourceType, String resourceName, Object resource) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentResource(agent.getAgentNode().getUUID(), agent.getAgentId(), resourceType, resourceName);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Registers an agent bean as JMX resource.
	 * @param agentBean the agent bean to be registered
	 * @param agent the agent which contains this agent bean
	 * @throws MalformedObjectNameException The name of the agent bean, agent or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent bean is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent bean has thrown an exception. The agent bean will not be registered.
	 * @throws NotCompliantMBeanException The agent bean is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentBean(String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentBean(IAgentBean agentBean, IAgent agent) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentBean(agent.getAgentNode().getUUID(), agent.getAgentId(), agentBean.getBeanName());
		mbs.registerMBean(agentBean, name);
	}

	/**
	 * Registers an agent bean resource as JMX resource. The agent bean may contain more
	 * than one resource of the specified type.
	 * @param agentBean the agent bean which contains the resource
	 * @param agent the agent which contains this agent bean
	 * @param resourceType the type of the agent bean resource
	 * @param resourceName the name of the agent bean resource
	 * @param resource the agent bean resource to be registered
	 * @throws MalformedObjectNameException The name of the agent bean, agent, agent node or name or type of the resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceAlreadyExistsException The agent bean resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent bean resource has thrown an exception. The agent bean resource will not be registered.
	 * @throws NotCompliantMBeanException The agent bean resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentBeanResource(String, String, String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentBeanResource(IAgentBean agentBean, IAgent agent, String resourceType, String resourceName, Object resource) throws MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		final ObjectName name = getMgmtNameOfAgentBeanResource(agent.getAgentNode().getUUID(), agent.getAgentId(), agentBean.getBeanName(), resourceType, resourceName);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Unregisters an agent node as JMX resource.
	 * @param agentNode the agent node to be unregistered
	 * @throws MalformedObjectNameException The name of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent node is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent node has thrown an exception.
	 * @see #getMgmtNameOfAgentNode(String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNode(IAgentNode agentNode) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentNode(agentNode.getUUID());
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent node bean as JMX resource.
	 * @param agentNodeBean the agent node bean to be unregistered
	 * @param agentNode the agent node which contains the agent node bean
	 * @throws MalformedObjectNameException The name of the agent node bean or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent node bean is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent node bean has thrown an exception.
	 * @see #getMgmtNameOfAgentNodeBean(String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNodeBean(IAgentNodeBean agentNodeBean, IAgentNode agentNode) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentNodeBean(agentNode.getUUID(), agentNodeBean.getBeanName());
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent node bean resource as JMX resource. The agent node bean may contain more
	 * than one resource of the specified type.
	 * @param agentNodeBean the agent node bean
	 * @param agentNode the agent node which contains the agent node bean
	 * @param resourceType the type of the agent node bean resource
	 * @param resourceName the name of the agent node bean resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent node bean resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent node bean resource has thrown an exception.
	 * @see #getMgmtNameOfAgentNodeBean(String, String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNodeBeanResource(IAgentNodeBean agentNodeBean, IAgentNode agentNode, String resourceType, String resourceName) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentNodeBeanResource(agentNode.getUUID(), agentNodeBean.getBeanName(), resourceType, resourceName);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent node resource as JMX resource. The agent node contains only
	 * one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent node resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister (MBeanRegistration  interface) method of the agent node resource has thrown an exception.
	 * @see #getMgmtNameOfAgentNodeResource(String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNodeResource(IAgentNode node, String resourceType) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentNodeResource(node.getUUID(), resourceType);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent node resource as JMX resource. The agent node may contain more
	 * than one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resourceName the name of the agent node resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent node resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister (MBeanRegistration  interface) method of the agent node resource has thrown an exception.
	 * @see #getMgmtNameOfAgentNodeResource(String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNodeResource(IAgentNode node, String resourceType, String resourceName) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentNodeResource(node.getUUID(), resourceType, resourceName);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent as JMX resource.
	 * @param agent the agent to be unregistered
	 * @throws MalformedObjectNameException The name of the agent or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent has thrown an exception.
	 * @see #getMgmtNameOfAgent(String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgent(IAgent agent) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgent(agent.getAgentNode().getUUID(), agent.getAgentId());
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent resource as JMX resource. The agent contains only
	 * one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @throws MalformedObjectNameException The name of the agent or agent node or the type of resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister (MBeanRegistration  interface) method of the agent resource has thrown an exception.
	 * @see #getMgmtNameOfAgentResource(String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentResource(IAgent agent, String resourceType) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentResource(agent.getAgentNode().getUUID(), agent.getAgentId(), resourceType);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent resource as JMX resource. The agent may contain more
	 * than one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @param resourceName the name of the agent resource
	 * @throws MalformedObjectNameException The name of the agent or agent node or the type or name of resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister (MBeanRegistration  interface) method of the agent resource has thrown an exception.
	 * @see #getMgmtNameOfAgentResource(String, String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentResource(IAgent agent, String resourceType, String resourceName) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentResource(agent.getAgentNode().getUUID(), agent.getAgentId(), resourceType, resourceName);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent bean as JMX resource.
	 * @param agentBean the agent bean to be unregistered
	 * @param agent the agent which contains this agent bean
	 * @throws MalformedObjectNameException The name of the agent bean, agent or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent bean is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent bean has thrown an exception.
	 * @see #getMgmtNameOfAgentBean(String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentBean(IAgentBean agentBean, IAgent agent) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentBean(agent.getAgentNode().getUUID(), agent.getAgentId(), agentBean.getBeanName());
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent bean resource as JMX resource. The agent bean may contain more
	 * than one resource of the specified type.
	 * @param agentBean the agent bean which contains the resource
	 * @param agent the agent which contains the agent bean
	 * @param resourceType the type of the agent bean resource
	 * @param resourceName the name of the agent bean resource
	 * @throws MalformedObjectNameException The name of the agent bean, agent, agent node or type or name of the resource contains an illegal character or does not follow the rules for quoting.
	 * @throws InstanceNotFoundException The agent bean resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent bean resource has thrown an exception.
	 * @see #getMgmtNameOfAgentBeanResource(String, String, String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentBeanResource(IAgentBean agentBean, IAgent agent, String resourceType, String resourceName) throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
		final ObjectName name = getMgmtNameOfAgentBeanResource(agent.getAgentNode().getUUID(), agent.getAgentId(), agentBean.getBeanName(), resourceType, resourceName);
		mbs.unregisterMBean(name);
	}

	/**
	 * Gets the value of an attribute of an agent node.
	 * @param nodeID the UUID of the agent node
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent node. 
	 * @throws MBeanException Wraps an exception thrown by the agent node's getter. 
	 * @throws InstanceNotFoundException The agent node specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the getter of the agent node. 
	 */
	public Object getAttributeOfAgentNode(String nodeID, String attributeName) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException {
		final ObjectName name = getMgmtNameOfAgentNode(nodeID);
		return mbs.getAttribute(name, attributeName);
	}

	/**
	 * Gets the value of an attribute of an agent node bean.
	 * @param nodeID the UUID of the agent node
	 * @param beanName the name of the agent node bean
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or name of agent node bean contains an illegal character or does not follow the rules for quoting.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent node bean. 
	 * @throws MBeanException Wraps an exception thrown by the agent node bean's getter. 
	 * @throws InstanceNotFoundException The agent node bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the getter of the agent node bean. 
	 */
	public Object getAttributeOfAgentNodeBean(String nodeID, String beanName, String attributeName) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException {
		final ObjectName name = getMgmtNameOfAgentNodeBean(nodeID, beanName);
		return mbs.getAttribute(name, attributeName);
	}

	/**
	 * Gets the value of an attribute of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the unique identifier of the agent
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or agent contains an illegal character or does not follow the rules for quoting.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent. 
	 * @throws MBeanException Wraps an exception thrown by the agent's getter. 
	 * @throws InstanceNotFoundException The agent specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the getter of the agent. 
	 */
	public Object getAttributeOfAgent(String nodeId, String agentId, String attributeName) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException {
		final ObjectName name = getMgmtNameOfAgent(nodeId, agentId);
		return mbs.getAttribute(name, attributeName);
	}

	/**
	 * Gets the value of an attribute of an agent bean.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentId the unique identifier of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or agent or the name of agent bean contains an illegal character or does not follow the rules for quoting.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent bean. 
	 * @throws MBeanException Wraps an exception thrown by the agent bean's getter. 
	 * @throws InstanceNotFoundException The agent bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the getter of the agent bean. 
	 */
	public Object getAttributeOfAgentBean(String nodeId, String agentId, String beanName, String attributeName) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException {
		final ObjectName name = getMgmtNameOfAgentBean(nodeId, agentId, beanName);
		return mbs.getAttribute(name, attributeName);
	}

	/**
	 * Sets the value of an attribute of an agent node.
	 * @param nodeId the UUID of the agent node
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent node. 
	 * @throws MBeanException Wraps an exception thrown by the agent node's setter. 
	 * @throws InstanceNotFoundException The agent node specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the setter of the agent node. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public void setAttributeOfAgentNode(String nodeId, String attributeName, Object attributeValue) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, InvalidAttributeValueException {
		final ObjectName name = getMgmtNameOfAgentNode(nodeId);
		mbs.setAttribute(name, new Attribute(attributeName, attributeValue));
	}

	/**
	 * Sets the value of an attribute of an agent node bean.
	 * @param nodeId the UUID of the agent node which contains the agent node bean
	 * @param beanName the name of the agent node bean
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or name of agent node bean contains an illegal character or does not follow the rules for quoting.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent node bean. 
	 * @throws MBeanException Wraps an exception thrown by the agent node bean's setter. 
	 * @throws InstanceNotFoundException The agent node bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the setter of the agent node bean. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public void setAttributeOfAgentNodeBean(String nodeId, String beanName, String attributeName, Object attributeValue) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, InvalidAttributeValueException {
		final ObjectName name = getMgmtNameOfAgentNodeBean(nodeId, beanName);
		mbs.setAttribute(name, new Attribute(attributeName, attributeValue));
	}

	/**
	 * Sets the value of an attribute of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the unique identifier of the agent
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or agent contains an illegal character or does not follow the rules for quoting.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent. 
	 * @throws MBeanException Wraps an exception thrown by the agent's setter. 
	 * @throws InstanceNotFoundException The agent specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the setter of the agent. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public void setAttributeOfAgent(String nodeId, String agentId, String attributeName, Object attributeValue) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, InvalidAttributeValueException {
		final ObjectName name = getMgmtNameOfAgent(nodeId, agentId);
		mbs.setAttribute(name, new Attribute(attributeName, attributeValue));
	}

	/**
	 * Sets the value of an attribute of an agent bean.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentId the unique identifier of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or agent or the name of agent bean contains an illegal character or does not follow the rules for quoting.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent bean. 
	 * @throws MBeanException Wraps an exception thrown by the agent bean's setter. 
	 * @throws InstanceNotFoundException The agent bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the setter of the agent bean. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public void setAttributeOfAgentBean(String nodeId, String agentId, String beanName, String attributeName, Object attributeValue) throws MalformedObjectNameException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, InvalidAttributeValueException {
		final ObjectName name = getMgmtNameOfAgentBean(nodeId, agentId, beanName);
		mbs.setAttribute(name, new Attribute(attributeName, attributeValue));
	}

	/**
	 * Invokes an operation on an agent node.
	 * @param nodeId The UUID of the agent node.
	 * @param operationName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the operation is invoked.
	 * @param signature An array containing the signature of the operation.
	 * @return The object returned by the operation.
	 * @throws MalformedObjectNameException The UUID of agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws MBeanException Wraps an exception thrown by the agent node's invoked method. 
	 * @throws InstanceNotFoundException The agent node specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the method of the agent node. 
	 */
	public Object invokeAgentNode(String nodeId, String operationName, Object[] params, String[] signature) throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
		final ObjectName name = getMgmtNameOfAgentNode(nodeId);
		return mbs.invoke(name, operationName, params, signature);
	}

	/**
	 * Invokes an operation on an agent node bean.
	 * @param nodeId The UUID of the agent node which contains the agent node bean.
	 * @param beanName The name of the agent node bean.
	 * @param operationName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the operation is invoked.
	 * @param signature An array containing the signature of the operation.
	 * @return The object returned by the operation.
	 * @throws MalformedObjectNameException The UUID of agent node or name of agent node bean contains an illegal character or does not follow the rules for quoting.
	 * @throws MBeanException Wraps an exception thrown by the agent node bean's invoked method. 
	 * @throws InstanceNotFoundException The agent node bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the method of the agent node bean. 
	 */
	public Object invokeAgentNodeBean(String nodeId, String beanName, String operationName, Object[] params, String[] signature) throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
		final ObjectName name = getMgmtNameOfAgentNodeBean(nodeId, beanName);
		return mbs.invoke(name, operationName, params, signature);
	}

	/**
	 * Invokes an operation on an agent.
	 * @param nodeId The UUID of the agent node where the agent is residing on.
	 * @param agentId The unique identifier of the agent.
	 * @param operationName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the operation is invoked.
	 * @param signature An array containing the signature of the operation.
	 * @return The object returned by the operation.
	 * @throws MalformedObjectNameException The UUID of agent node or agent contains an illegal character or does not follow the rules for quoting.
	 * @throws MBeanException Wraps an exception thrown by the agent's invoked method. 
	 * @throws InstanceNotFoundException The agent specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the method of the agent. 
	 */
	public Object invokeAgent(String nodeId, String agentId, String operationName, Object[] params, String[] signature) throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
		final ObjectName name = getMgmtNameOfAgent(nodeId, agentId);
		return mbs.invoke(name, operationName, params, signature);
	}

	/**
	 * Invokes an operation on an agent bean.
	 * @param nodeId The UUID of the agent node where the agent is residing on which contains the agent bean.
	 * @param agentId The unique identifier of the agent which contains the agent bean.
	 * @param beanName The name of the agent bean.
	 * @param operationName The name of the operation to be invoked.
	 * @param params An array containing the parameters to be set when the operation is invoked.
	 * @param signature An array containing the signature of the operation.
	 * @return The object returned by the operation.
	 * @throws MalformedObjectNameException The UUID of agent node or agent or the name of agent bean contains an illegal character or does not follow the rules for quoting.
	 * @throws MBeanException Wraps an exception thrown by the agent bean's invoked method. 
	 * @throws InstanceNotFoundException The agent bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the method of the agent bean. 
	 */
	public Object invokeAgentBean(String nodeId, String agentId, String beanName, String operationName, Object[] params, String[] signature) throws MalformedObjectNameException, MBeanException, InstanceNotFoundException, ReflectionException {
		final ObjectName name = getMgmtNameOfAgentBean(nodeId, agentId, beanName);
		return mbs.invoke(name, operationName, params, signature);
	}

	/**
	 * Creates all specified connector server for remote management, registers
	 * them in the MBean server and announces them via multicast periodically.
	 * @param node the agent node
	 * @return The URLs of all created connector server.
	 * @see IAgentNode#getJmxConnectors()
	 * @see JmxConnectorManager
	 * @see Timer#schedule(java.util.TimerTask, long, long)
	 */
	public void enableRemoteManagement(IAgentNode node) {
		if (node.getJmxConnectors().isEmpty()) {
			return;
		}

		System.setProperty("com.sun.management.jmxremote", "");
		try {
			// create connector manager
			connectorManager = new JmxConnectorManager(node, MULTICAST_PORT, MULTICAST_ADDRESS, 1);
		}
		catch (Exception e) {
			return;
		}

		// schedule timer task
		ti = new Timer();
		ti.schedule(connectorManager, MULTICAST_DELAY, MULTICAST_PERIOD);
		System.out.println("Initiated multicast sender on port " + MULTICAST_PORT + " with group " + MULTICAST_ADDRESS + " and interval " + MULTICAST_PERIOD);
	}

	/**
	 * Deregisters and stops all connector servers.
	 * @param node the agent node
	 * @see javax.management.remote.JMXConnectorServerMBean#stop()
	 */
	public void disableRemoteManagement(IAgentNode node) {
		// stop connector manager
		if (ti != null) {
			ti.cancel();
			ti = null;
		}

		// remove all connector servers
		if (connectorManager != null) {
			connectorManager.removeAll();
			connectorManager = null;
		}
	}
}
