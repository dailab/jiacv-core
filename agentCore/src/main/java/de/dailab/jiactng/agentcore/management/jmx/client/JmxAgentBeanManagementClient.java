package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;

import de.dailab.jiactng.agentcore.management.jmx.JmxManager;

/**
 * This JMX client enables the remote management of JIAC TNG agent beans.
 * @author Jan Keiser
 */
public class JmxAgentBeanManagementClient extends JmxAbstractManagementClient {

	/**
	 * Creates a client for the management of an agent bean.
	 * @param mbsc The JMX connection used for the agent management.
	 * @param agentNodeName The name of the agent node where the agent of the agent bean resides on.
	 * @param agentID The global unique identifier of the agent which contains the agent bean.
	 * @param agentBeanName The name of the agent bean.
	 * @throws MalformedObjectNameException The agent node name, agent identifier or agent bean name contains an illegal character or does not follow the rules for quoting.
	 * @see ObjectName#ObjectName(String)
	 */
	protected JmxAgentBeanManagementClient(MBeanServerConnection mbsc, String agentNodeName, String agentID, String agentBeanName) throws MalformedObjectNameException {
		super(mbsc, new JmxManager().getMgmtNameOfAgentBean(agentNodeName, agentID, agentBeanName));
	}

	/**
	 * Gets detailed information about all actions provided by the managed <code>AbstractMethodExposingBean</code>.
	 * @return Information about the provided actions.
	 * @throws InstanceNotFoundException The agent bean does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent bean.
	 * @throws AttributeNotFoundException if the specified agent bean is not an <code>AbstractMethodExposingBean</code>.
	 * @throws SecurityException if the agent bean's attribute cannot be read for security reasons.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 * @see de.dailab.jiactng.agentcore.action.AbstractMethodExposingBeanMBean#getActionList()
	 */
	public TabularData getActionList() throws IOException, InstanceNotFoundException, AttributeNotFoundException {
		try {
			return (TabularData) getAttribute("ActionList");
		}
		catch (RuntimeException e) {
			if ((e.getCause() != null) && (e.getCause() instanceof AttributeNotFoundException)) {
				throw ((AttributeNotFoundException) e.getCause());
			}
			else {
				throw e;
			}
		}
	}
	
	/**
	 * Checks if the managed bean is an instance of <code>AbstractActionAuthorizationBean</code>.
	 * @return true if it is an instance of AbstractActionAuthorizationBean
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent bean.
	 * @throws InstanceNotFoundException The agent bean does not exist on the managed agent node.
	 */
	public boolean isActionAuthorizationBean() throws IOException, InstanceNotFoundException {
		return isInstanceOf("de.dailab.jiactng.agentcore.action.AbstractActionAuthorizationBean");
	}
	
	/**
	 * Sets the name of the action used to authorize action users by the managed <code>AbstractActionAuthorizationBean</code>.
	 * @param authorizationActionName the name of the authorization action.
	 * @throws InstanceNotFoundException The agent bean does not exist on the managed agent node. 
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent bean.
	 * @throws AttributeNotFoundException if the specified agent bean is not an <code>AbstractActionAuthorizationBean</code>.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @throws SecurityException if the agent bean's attribute cannot be changed for security reasons.
	 * @see MBeanServerConnection#setAttribute(ObjectName, javax.management.Attribute)
	 * @see de.dailab.jiactng.agentcore.action.AbstractActionAuthorizationBeanMBean#setAuthorizationActionName(String)
	 */
	public void setAuthorizationActionName(String authorizationActionName) throws IOException, InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException {
		try {
			setAttribute("AuthorizationActionName", authorizationActionName);
		}
		catch (RuntimeException e) {
			if ((e.getCause() != null) && (e.getCause() instanceof AttributeNotFoundException)) {
				throw ((AttributeNotFoundException) e.getCause());
			}
			else {
				throw e;
			}
		}
	}
	
	/**
	 * Gets the name of the action used to authorize action users by the managed <code>AbstractActionAuthorizationBean</code>.
	 * @return the name of the authorization action
	 * @throws IOException A communication problem occurred when invoking the method of the remote agent bean.
	 * @throws InstanceNotFoundException The agent bean does not exist on the managed agent node.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 */
	public String getAuthorizationActionName() throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
		return (String) getAttribute("AuthorizationActionName");
	}

}
