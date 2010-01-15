package de.dailab.jiactng.agentcore.management.jmx;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import javax.management.RuntimeOperationsException;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

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
public class JmxManager implements Manager {
	
	private final static String DOMAIN = "de.dailab.jiactng";
	private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
	private ArrayList<JMXConnectorServer> _connectorServer = new ArrayList<JMXConnectorServer>();
	private Timer ti = null;

	/**
	 * Constructs a JMX compliant name for the management of an agent node.
	 * @param nodeId the UUID of the agent node
	 * @return the JMX compliant name of the agent node
	 * @throws MalformedObjectNameException The parameter contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The parameter is null.
	 */
	public ObjectName getMgmtNameOfAgentNode(String nodeId) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent node bean.
	 * @param nodeId the UUID of the agent node
	 * @param beanName the name of the agent node bean
	 * @return the JMX compliant name of the agent node bean
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgentNodeBean(String nodeId, String beanName) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=agentNodeBean,agentnodebean=" + beanName);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent node resource. The
	 * agent node always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node
	 * @param resourceType the type of the agent node resource
	 * @return the JMX compliant name of the agent node resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgentNodeResource(String nodeId, String resourceType) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=" + resourceType);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent node resource. The
	 * agent node may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resourceName the name of the agent node resource
	 * @return the JMX compliant name of the agent node resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgentNodeResource(String nodeId, String resourceType, String resourceName) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=" + resourceType + ",resource=" + resourceName);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentName the name of the agent
	 * @return the JMX compliant name of the agent
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgent(String nodeId, String agentName) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=Agent,agent=" + agentName);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent resource. The
	 * agent always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentName the name of the agent
	 * @param resourceType the type of the agent resource
	 * @return the JMX compliant name of the agent resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgentResource(String nodeId, String agentName, String resourceType) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=Agent,agent=" + agentName + 
				",category3=" + resourceType);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent resource. The
	 * agent may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentName the name of the agent
	 * @param resourceType the type of the agent resource
	 * @param resourceName the name of the agent resource
	 * @return the JMX compliant name of the agent resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgentResource(String nodeId, String agentName, String resourceType, String resourceName) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=Agent,agent=" + agentName + 
				",category3=" + resourceType + ",resource=" + resourceName);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent bean.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentName the name of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @return the JMX compliant name of the agent bean
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgentBean(String nodeId, String agentName, String beanName) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=Agent,agent=" + agentName + 
				",category3=AgentBean,agentbean=" + beanName);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent bean resource. The
	 * agent bean always contains only one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentName the name of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param resourceType the type of the resource
	 * @return the JMX compliant name of the agent bean resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgentBeanResource(String nodeId, String agentName, String beanName, String resourceType) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=Agent,agent=" + agentName + 
				",category3=AgentBean,agentbean=" + beanName + 
				",category4=" + resourceType);
	}

	/**
	 * Constructs a JMX compliant name for the management of an agent bean resource. The
	 * agent bean may contain more than one resource of the specified type.
	 * @param nodeId the UUID of the agent node where the agent is residing on which contains the agent bean
	 * @param agentName the name of the agent which contains the agent bean
	 * @param beanName the name of the agent bean
	 * @param resourceType the type of the resource
	 * @param resourceName the name of the resource
	 * @return the JMX compliant name of the agent bean resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 */
	public ObjectName getMgmtNameOfAgentBeanResource(String nodeId, String agentName, String beanName, String resourceType, String resourceName) throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(DOMAIN + 
				":category1=AgentNode,agentnode=" + nodeId + 
				",category2=Agent,agent=" + agentName + 
				",category3=AgentBean,agentbean=" + beanName + 
				",category4=" + resourceType + ",resource=" + resourceName);
	}
	
	/**
	 * Registers an agent node as JMX resource.
	 * @param agentNode the agent node to be registered
	 * @throws MalformedObjectNameException The name of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent node is unknown
	 * @throws InstanceAlreadyExistsException The agent node is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node has thrown an exception. The agent node will not be registered.
	 * @throws NotCompliantMBeanException The agent node is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNode(String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNode(IAgentNode agentNode) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgentNode(agentNode.getUUID());
		mbs.registerMBean(agentNode, name);
	}

	/**
	 * Registers an agent node bean as JMX resource.
	 * @param agentNodeBean the agent node bean to be registered
	 * @param agentNode the agent node which contains the agent node bean
	 * @throws MalformedObjectNameException The name of the agent node bean or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent node bean or agent node is unknown
	 * @throws InstanceAlreadyExistsException The agent node bean is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node bean has thrown an exception. The agent node bean will not be registered.
	 * @throws NotCompliantMBeanException The agent node bean is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNodeBean(String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNodeBean(IAgentNodeBean agentNodeBean, IAgentNode agentNode) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgentNodeBean(agentNode.getUUID(), agentNodeBean.getBeanName());
		mbs.registerMBean(agentNodeBean, name);
	}

	/**
	 * Registers an agent node resource as JMX resource. The agent node contains only
	 * one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resource the agent node resource to be registered
	 * @throws MalformedObjectNameException The UUID of the agent node or the type of the resource contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 * @throws InstanceAlreadyExistsException The agent node resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node resource has thrown an exception. The agent node resource will not be registered.
	 * @throws NotCompliantMBeanException The agent node resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNodeResource(String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNodeResource(IAgentNode node, String resourceType, Object resource) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgentNodeResource(node.getUUID(), resourceType);
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
	 * @throws NullPointerException One of the parameters is null.
	 * @throws InstanceAlreadyExistsException The agent node resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent node resource has thrown an exception. The agent node resource will not be registered.
	 * @throws NotCompliantMBeanException The agent node resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentNodeResource(String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentNodeResource(IAgentNode node, String resourceType, String resourceName, Object resource) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgentNodeResource(node.getUUID(), resourceType, resourceName);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Registers an agent as JMX resource.
	 * @param agent the agent to be registered
	 * @throws MalformedObjectNameException The name of the agent or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent or agent node is unknown
	 * @throws InstanceAlreadyExistsException The agent is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent has thrown an exception. The agent will not be registered.
	 * @throws NotCompliantMBeanException The agent is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgent(String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgent(IAgent agent) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgent(agent.getAgentNode().getUUID(), agent.getAgentId());
		mbs.registerMBean(agent, name);
	}

	/**
	 * Registers an agent resource as JMX resource. The agent contains only
	 * one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @param resource the agent resource to be registered
	 * @throws MalformedObjectNameException The name of the agent or agent node or the type of resource contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent or agent node or the type of resource is unknown.
	 * @throws InstanceAlreadyExistsException The agent resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent resource has thrown an exception. The agent resource will not be registered.
	 * @throws NotCompliantMBeanException The agent resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentResource(String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentResource(IAgent agent, String resourceType, Object resource) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgentResource(agent.getAgentNode().getUUID(), agent.getAgentId(), resourceType);
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
	 * @throws NullPointerException The name of the agent or agent node or the type or name of resource is unknown.
	 * @throws InstanceAlreadyExistsException The agent resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent resource has thrown an exception. The agent resource will not be registered.
	 * @throws NotCompliantMBeanException The agent resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentResource(String, String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentResource(IAgent agent, String resourceType, String resourceName, Object resource) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgentResource(agent.getAgentNode().getUUID(), agent.getAgentId(), resourceType, resourceName);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Registers an agent bean as JMX resource.
	 * @param agentBean the agent bean to be registered
	 * @param agent the agent which contains this agent bean
	 * @throws MalformedObjectNameException The name of the agent bean, agent or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent bean, agent or agent node is unknown
	 * @throws InstanceAlreadyExistsException The agent bean is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent bean has thrown an exception. The agent bean will not be registered.
	 * @throws NotCompliantMBeanException The agent bean is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentBean(String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentBean(IAgentBean agentBean, IAgent agent) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgentBean(agent.getAgentNode().getUUID(), agent.getAgentId(), agentBean.getBeanName());
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
	 * @throws NullPointerException The name of the agent bean, agent, agent node or name or type of the resource is unknown
	 * @throws InstanceAlreadyExistsException The agent bean resource is already under the control of the MBean server.
	 * @throws MBeanRegistrationException The preRegister (MBeanRegistration  interface) method of the agent bean resource has thrown an exception. The agent bean resource will not be registered.
	 * @throws NotCompliantMBeanException The agent bean resource is not a JMX compliant MBean.
	 * @see #getMgmtNameOfAgentBeanResource(String, String, String, String, String)
	 * @see MBeanServer#registerMBean(Object, ObjectName)
	 */
	public void registerAgentBeanResource(IAgentBean agentBean, IAgent agent, String resourceType, String resourceName, Object resource) throws MalformedObjectNameException, NullPointerException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		ObjectName name = getMgmtNameOfAgentBeanResource(agent.getAgentNode().getUUID(), agent.getAgentId(), agentBean.getBeanName(), resourceType, resourceName);
		mbs.registerMBean(resource, name);
	}

	/**
	 * Unregisters an agent node as JMX resource.
	 * @param agentNode the agent node to be unregistered
	 * @throws MalformedObjectNameException The name of the agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent node is unknown
	 * @throws InstanceNotFoundException The agent node is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent node has thrown an exception.
	 * @see #getMgmtNameOfAgentNode(String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNode(IAgentNode agentNode) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgentNode(agentNode.getUUID());
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent node bean as JMX resource.
	 * @param agentNodeBean the agent node bean to be unregistered
	 * @param agentNode the agent node which contains the agent node bean
	 * @throws MalformedObjectNameException The name of the agent node bean or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent node bean or agent node is unknown
	 * @throws InstanceNotFoundException The agent node bean is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent node bean has thrown an exception.
	 * @see #getMgmtNameOfAgentNodeBean(String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNodeBean(IAgentNodeBean agentNodeBean, IAgentNode agentNode) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgentNodeBean(agentNode.getUUID(), agentNodeBean.getBeanName());
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent node resource as JMX resource. The agent node contains only
	 * one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 * @throws InstanceNotFoundException The agent node resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister (MBeanRegistration  interface) method of the agent node resource has thrown an exception.
	 * @see #getMgmtNameOfAgentNodeResource(String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNodeResource(IAgentNode node, String resourceType) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgentNodeResource(node.getUUID(), resourceType);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent node resource as JMX resource. The agent node may contain more
	 * than one resource of the specified type.
	 * @param node the agent node
	 * @param resourceType the type of the agent node resource
	 * @param resourceName the name of the agent node resource
	 * @throws MalformedObjectNameException One of the parameters contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException One of the parameters is null.
	 * @throws InstanceNotFoundException The agent node resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister (MBeanRegistration  interface) method of the agent node resource has thrown an exception.
	 * @see #getMgmtNameOfAgentNodeResource(String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentNodeResource(IAgentNode node, String resourceType, String resourceName) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgentNodeResource(node.getUUID(), resourceType, resourceName);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent as JMX resource.
	 * @param agent the agent to be unregistered
	 * @throws MalformedObjectNameException The name of the agent or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent or agent node is unknown
	 * @throws InstanceNotFoundException The agent is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent has thrown an exception.
	 * @see #getMgmtNameOfAgent(String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgent(IAgent agent) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgent(agent.getAgentNode().getUUID(), agent.getAgentId());
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent resource as JMX resource. The agent contains only
	 * one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @throws MalformedObjectNameException The name of the agent or agent node or the type of resource contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent or agent node or the type of resource is unknown.
	 * @throws InstanceNotFoundException The agent resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister (MBeanRegistration  interface) method of the agent resource has thrown an exception.
	 * @see #getMgmtNameOfAgentResource(String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentResource(IAgent agent, String resourceType) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgentResource(agent.getAgentNode().getUUID(), agent.getAgentId(), resourceType);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent resource as JMX resource. The agent may contain more
	 * than one resource of the specified type.
	 * @param agent the agent which contains the resource
	 * @param resourceType the type of the agent resource
	 * @param resourceName the name of the agent resource
	 * @throws MalformedObjectNameException The name of the agent or agent node or the type or name of resource contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent or agent node or the type or name of resource is unknown.
	 * @throws InstanceNotFoundException The agent resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister (MBeanRegistration  interface) method of the agent resource has thrown an exception.
	 * @see #getMgmtNameOfAgentResource(String, String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentResource(IAgent agent, String resourceType, String resourceName) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgentResource(agent.getAgentNode().getUUID(), agent.getAgentId(), resourceType, resourceName);
		mbs.unregisterMBean(name);
	}

	/**
	 * Unregisters an agent bean as JMX resource.
	 * @param agentBean the agent bean to be unregistered
	 * @param agent the agent which contains this agent bean
	 * @throws MalformedObjectNameException The name of the agent bean, agent or agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The name of the agent bean, agent or agent node is unknown
	 * @throws InstanceNotFoundException The agent bean is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent bean has thrown an exception.
	 * @see #getMgmtNameOfAgentBean(String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentBean(IAgentBean agentBean, IAgent agent) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgentBean(agent.getAgentNode().getUUID(), agent.getAgentId(), agentBean.getBeanName());
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
	 * @throws NullPointerException The name of the agent bean, agent, agent node or type or name of the resource is unknown
	 * @throws InstanceNotFoundException The agent bean resource is not registered in the MBean server.
	 * @throws MBeanRegistrationException The preDeregister ((MBeanRegistration interface) method of the agent bean resource has thrown an exception.
	 * @see #getMgmtNameOfAgentBeanResource(String, String, String, String, String)
	 * @see MBeanServer#unregisterMBean(ObjectName)
	 */
	public void unregisterAgentBeanResource(IAgentBean agentBean, IAgent agent, String resourceType, String resourceName) throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, MBeanRegistrationException {
		ObjectName name = getMgmtNameOfAgentBeanResource(agent.getAgentNode().getUUID(), agent.getAgentId(), agentBean.getBeanName(), resourceType, resourceName);
		mbs.unregisterMBean(name);
	}

	/**
	 * Gets the value of an attribute of an agent node.
	 * @param nodeID the UUID of the agent node
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The UUID of agent node is null.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent node. 
	 * @throws MBeanException Wraps an exception thrown by the agent node's getter. 
	 * @throws InstanceNotFoundException The agent node specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the getter of the agent node. 
	 * @throws RuntimeOperationsException Wraps a java.lang.IllegalArgumentException: The name of attribute is null.
	 */
	public Object getAttributeOfAgentNode(String nodeID, String attributeName) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, RuntimeOperationsException {
		ObjectName name = getMgmtNameOfAgentNode(nodeID);
		return mbs.getAttribute(name, attributeName);
	}

	/**
	 * Gets the value of an attribute of an agent node bean.
	 * @param nodeID the UUID of the agent node
	 * @param beanName the name of the agent node bean
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or name of agent node bean contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The UUID of agent node or name of agent node bean is null.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent node bean. 
	 * @throws MBeanException Wraps an exception thrown by the agent node bean's getter. 
	 * @throws InstanceNotFoundException The agent node bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the getter of the agent node bean. 
	 * @throws RuntimeOperationsException Wraps a java.lang.IllegalArgumentException: The name of attribute is null.
	 */
	public Object getAttributeOfAgentNodeBean(String nodeID, String beanName, String attributeName) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, RuntimeOperationsException {
		ObjectName name = getMgmtNameOfAgentNodeBean(nodeID, beanName);
		return mbs.getAttribute(name, attributeName);
	}

	/**
	 * Gets the value of an attribute of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the unique identifier of the agent
	 * @param attributeName the name of the attribute
	 * @return the value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or agent contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The UUID of agent node or agent is null.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent. 
	 * @throws MBeanException Wraps an exception thrown by the agent's getter. 
	 * @throws InstanceNotFoundException The agent specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the getter of the agent. 
	 * @throws RuntimeOperationsException Wraps a java.lang.IllegalArgumentException: The name of attribute is null.
	 */
	public Object getAttributeOfAgent(String nodeId, String agentId, String attributeName) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, RuntimeOperationsException {
		ObjectName name = getMgmtNameOfAgent(nodeId, agentId);
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
	 * @throws NullPointerException The UUID of agent node or agent or the name of agent bean is null.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent bean. 
	 * @throws MBeanException Wraps an exception thrown by the agent bean's getter. 
	 * @throws InstanceNotFoundException The agent bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the getter of the agent bean. 
	 * @throws RuntimeOperationsException Wraps a java.lang.IllegalArgumentException: The name of attribute is null.
	 */
	public Object getAttributeOfAgentBean(String nodeId, String agentId, String beanName, String attributeName) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, RuntimeOperationsException {
		ObjectName name = getMgmtNameOfAgentBean(nodeId, agentId, beanName);
		return mbs.getAttribute(name, attributeName);
	}

	/**
	 * Sets the value of an attribute of an agent node.
	 * @param nodeId the UUID of the agent node
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The UUID of agent node is null.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent node. 
	 * @throws MBeanException Wraps an exception thrown by the agent node's setter. 
	 * @throws InstanceNotFoundException The agent node specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the setter of the agent node. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public void setAttributeOfAgentNode(String nodeId, String attributeName, Object attributeValue) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, InvalidAttributeValueException {
		ObjectName name = getMgmtNameOfAgentNode(nodeId);
		mbs.setAttribute(name, new Attribute(attributeName, attributeValue));
	}

	/**
	 * Sets the value of an attribute of an agent node bean.
	 * @param nodeId the UUID of the agent node which contains the agent node bean
	 * @param beanName the name of the agent node bean
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or name of agent node bean contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The UUID of agent node or name of agent node bean is null.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent node bean. 
	 * @throws MBeanException Wraps an exception thrown by the agent node bean's setter. 
	 * @throws InstanceNotFoundException The agent node bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the setter of the agent node bean. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public void setAttributeOfAgentNodeBean(String nodeId, String beanName, String attributeName, Object attributeValue) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, InvalidAttributeValueException {
		ObjectName name = getMgmtNameOfAgentNodeBean(nodeId, beanName);
		mbs.setAttribute(name, new Attribute(attributeName, attributeValue));
	}

	/**
	 * Sets the value of an attribute of an agent.
	 * @param nodeId the UUID of the agent node where the agent is residing on
	 * @param agentId the unique identifier of the agent
	 * @param attributeName the name of the attribute
	 * @param attributeValue the new value of the attribute
	 * @throws MalformedObjectNameException The UUID of agent node or agent contains an illegal character or does not follow the rules for quoting.
	 * @throws NullPointerException The UUID of agent node or agent is null.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent. 
	 * @throws MBeanException Wraps an exception thrown by the agent's setter. 
	 * @throws InstanceNotFoundException The agent specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the setter of the agent. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public void setAttributeOfAgent(String nodeId, String agentId, String attributeName, Object attributeValue) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, InvalidAttributeValueException {
		ObjectName name = getMgmtNameOfAgent(nodeId, agentId);
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
	 * @throws NullPointerException The UUID of agent node or agent or the name of agent bean is null.
	 * @throws AttributeNotFoundException The attribute specified is not accessible in the agent bean. 
	 * @throws MBeanException Wraps an exception thrown by the agent bean's setter. 
	 * @throws InstanceNotFoundException The agent bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the setter of the agent bean. 
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public void setAttributeOfAgentBean(String nodeId, String agentId, String beanName, String attributeName, Object attributeValue) throws MalformedObjectNameException, NullPointerException, AttributeNotFoundException, MBeanException, InstanceNotFoundException, ReflectionException, InvalidAttributeValueException {
		ObjectName name = getMgmtNameOfAgentBean(nodeId, agentId, beanName);
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
	 * @throws NullPointerException The UUID of agent node is null.
	 * @throws MBeanException Wraps an exception thrown by the agent node's invoked method. 
	 * @throws InstanceNotFoundException The agent node specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the method of the agent node. 
	 */
	public Object invokeAgentNode(String nodeId, String operationName, Object[] params, String[] signature) throws MalformedObjectNameException, NullPointerException, MBeanException, InstanceNotFoundException, ReflectionException {
		ObjectName name = getMgmtNameOfAgentNode(nodeId);
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
	 * @throws NullPointerException The UUID of agent node or name of agent node bean is null.
	 * @throws MBeanException Wraps an exception thrown by the agent node bean's invoked method. 
	 * @throws InstanceNotFoundException The agent node bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the method of the agent node bean. 
	 */
	public Object invokeAgentNodeBean(String nodeId, String beanName, String operationName, Object[] params, String[] signature) throws MalformedObjectNameException, NullPointerException, MBeanException, InstanceNotFoundException, ReflectionException {
		ObjectName name = getMgmtNameOfAgentNodeBean(nodeId, beanName);
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
	 * @throws NullPointerException The UUID of agent node or agent is null.
	 * @throws MBeanException Wraps an exception thrown by the agent's invoked method. 
	 * @throws InstanceNotFoundException The agent specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the method of the agent. 
	 */
	public Object invokeAgent(String nodeId, String agentId, String operationName, Object[] params, String[] signature) throws MalformedObjectNameException, NullPointerException, MBeanException, InstanceNotFoundException, ReflectionException {
		ObjectName name = getMgmtNameOfAgent(nodeId, agentId);
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
	 * @throws NullPointerException The UUID of agent node or agent or the name of agent bean is null.
	 * @throws MBeanException Wraps an exception thrown by the agent bean's invoked method. 
	 * @throws InstanceNotFoundException The agent bean specified is not registered in the MBean server. 
	 * @throws ReflectionException Wraps a java.lang.Exception thrown when trying to invoke the method of the agent bean. 
	 */
	public Object invokeAgentBean(String nodeId, String agentId, String beanName, String operationName, Object[] params, String[] signature) throws MalformedObjectNameException, NullPointerException, MBeanException, InstanceNotFoundException, ReflectionException {
		ObjectName name = getMgmtNameOfAgentBean(nodeId, agentId, beanName);
		return mbs.invoke(name, operationName, params, signature);
	}

	/**
	 * Creates all specified connector server for remote management, registers
	 * them in the MBean server and announces them via multicast periodically.
	 * @param node the agent node
	 * @see IAgentNode#getJmxConnectors()
	 * @see JMXServiceURL#JMXServiceURL(String, String, int, String)
	 * @see JMXConnectorServerFactory#newJMXConnectorServer(JMXServiceURL, Map, MBeanServer)
	 * @see javax.management.remote.JMXConnectorServerMBean#start()
	 * @see JmxMulticastSender
	 */
	public void enableRemoteManagement(IAgentNode node) {
		Set<Map<String,Object>> jmxConnectors = node.getJmxConnectors();
		if (!jmxConnectors.isEmpty()) {
			System.setProperty("com.sun.management.jmxremote", "");
		}
		// Create and register all specified JMX connector servers
		for (Map<String,Object> conf : jmxConnectors) {
			// get parameters of connector server
			String protocol = (String) conf.get("protocol");
			if (protocol == null) {
				System.out.println("WARNING: No protocol specified for a JMX connector server");
				continue;
			}
			String portStr = (String) conf.get("port");
			int port = 0;
			if (portStr != null) {
				try {
					port = Integer.parseInt(portStr);
				} catch (Exception e) {
					System.err.println("Port " + portStr + " of the JMX connector server for protocol " + protocol + " is not an integer. Will use 0 instead.");
					System.err.println(e.getMessage());
				}
			}
			String path = (String) conf.get("path");
			JMXAuthenticator authenticator = (JMXAuthenticator) conf.get("authenticator");

			if (protocol.equals("rmi")) {
				// check use of RMI registry
				String registryPort = (String) conf.get("registryPort");
				String registryHost = (String) conf.get("registryHost");
				if ((registryPort != null) || (registryHost != null)) {
					path = "/jndi/rmi://" + ((registryHost == null) ? "localhost" : registryHost)
																									+ ((registryPort == null) ? "" : ":" + registryPort) + "/" + node.getUUID();
				}
			}

			// configure authentication
			HashMap<String,Object> env = new HashMap<String,Object>();
			if (authenticator != null) {
				env.put(JMXConnectorServer.AUTHENTICATOR, authenticator);
			}

			// construct server URL
			JMXServiceURL jurl = null;
			try {
				jurl = new JMXServiceURL(protocol, null, port, path);
			}
			catch (Exception e) {
				System.err.println("WARNING: Unable to construct URL of JMX connector server.");
				System.err.println("It is not possible to find the local host name, or the protocol " + protocol + ", port " + port + " or path " + path + " is incorrect.");
				System.err.println(e.getMessage());
				continue;
			}
			
			// create connector server
			System.out.println("Creating JMX connector server: " + jurl);
			JMXConnectorServer cs = null;
			try {
				cs = JMXConnectorServerFactory.newJMXConnectorServer(jurl, env, mbs);
			}
			catch (MalformedURLException e) {
				System.err.println("WARNING: Unable to create JMX connector server for " + jurl);
				System.err.println("Missing provider implementation for the specified protocol.");
				System.err.println(e.getMessage());
				continue;
			}
			catch (Exception e) {
				System.err.println("WARNING: Unable to create JMX connector server for " + jurl);
				System.err.println("Communication problem, or the found provider implementation for the specified protocol can not be used.");
				System.err.println(e.getMessage());
				continue;
			}

			// start connector server
			try {
				cs.start();
			}
			catch (Exception e) {
				System.err.println("WARNING: Start of JMX connector server failed for " + jurl);
				if ((path != null) && path.startsWith("/jndi/rmi://")) {
					System.err.println("Please ensure that a rmi registry is started on " + path.substring(12, path.length() - node.getUUID().length() - 1));
				}
				System.err.println(e.getMessage());
				continue;
			}
			System.out.println("JMX connector server successfully started: " + cs.getAddress());
			_connectorServer.add(cs);

			// register connector server as JMX resource
			try {
				this.registerAgentNodeResource(node, "JMXConnectorServer", "\"" + cs.getAddress() + "\"", cs);
			}
			catch (Exception e) {
				System.err.println("WARNING: Unable to register JMX connector server \""+ cs.getAddress() + "\" as JMX resource.");
				System.err.println(e.getMessage());
			}
		}

		// send addresses of all connector servers via multicast periodically
		String[] jmxURLs = new String[_connectorServer.size()];
		for (int i=0; i<_connectorServer.size(); i++) {
			jmxURLs[i] = _connectorServer.get(i).getAddress().toString();
		}
		ti = new Timer();
		JmxMulticastSender multiSend = new JmxMulticastSender(9999, "226.6.6.7", 1, jmxURLs);
		ti.schedule(multiSend, 1000, 3600);
		System.out.println("Initiated multicast sender on port 9999 with group 226.6.6.7 and interval 3600");
	}

	/**
	 * Deregisters and stops all connector servers.
	 * @param node the agent node
	 * @see javax.management.remote.JMXConnectorServerMBean#stop()
	 */
	public void disableRemoteManagement(IAgentNode node) {
		// stop multicast sender
		if (ti != null) {
			ti.cancel();
			ti = null;
		}

		// Deregister and stop all connector servers
		Iterator<JMXConnectorServer> i = this._connectorServer.iterator();
		while (i.hasNext()) {
			JMXConnectorServer cs = i.next();
			
			// deregister connector server
			// TODO deregister the connector server from the server directory

			// deregister connector server as JMX resource
			try {
				this.unregisterAgentNodeResource(node, "JMXConnectorServer", "\"" + cs.getAddress() + "\"");
			}
			catch (Exception e) {
				System.err.println("WARNING: Unable to deregister JMX connector server \""+ cs.getAddress() + "\" as JMX resource.");
				System.err.println(e.getMessage());				
			}

			// stop connector server
			System.out.println("Stop connector server " + cs.getAddress().toString());
			try {
				cs.stop();
			} catch (Exception e) {
				System.err.println("WARNING: Unable to stop JMX connector server!");
				System.err.println(e.getMessage());
			}
			i.remove();
		}
	}
}
