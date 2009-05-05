package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * This abstract class enables the remote management of JIAC TNG resources.
 * @author Jan Keiser
 */
public abstract class JmxAbstractManagementClient {

	private MBeanServerConnection mbsc = null;
	private ObjectName resource = null;

	/**
	 * Constructor.
	 * @param mbsc The JMX connection used for the resource management.
	 * @param resource The JMX name of the managed resource.
	 */
	protected JmxAbstractManagementClient(MBeanServerConnection mbsc, ObjectName resource) {
		this.mbsc = mbsc;
		this.resource = resource;
	}

	/**
	 * Changes the state of the managed resource.
	 * @param action The operation name for the change of the resource's lifecycle state.
	 * @throws IOException A communication problem occurred when invoking the lifecycle method of the remote resource.
	 * @throws InstanceNotFoundException The resource does not exist in the JVM.
	 * @throws LifecycleException if an error occurs during change of the resource's lifecycle.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 */
	protected void changeState(String action) throws IOException, InstanceNotFoundException, LifecycleException {
		try {
			mbsc.invoke(resource, action, new Object[]{}, new String[]{});
		}
		catch (InstanceNotFoundException e) {
			throw new RuntimeException(e);
		}
		catch (MBeanException e) {
			if ((e.getCause() != null) && (e.getCause() instanceof LifecycleException)) {
				throw ((LifecycleException) e.getCause());
			}
			else {
				throw new RuntimeException(e);
			}
		}
		catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the value of an attribute of the managed resource.
	 * @param attributeName The name of the attribute.
	 * @return The value of the resource's attribute.
	 * @throws IOException A communication problem occurred when reading the attribute of the remote resource.
	 * @throws InstanceNotFoundException The resource does not exist in the JVM.
	 * @see MBeanServerConnection#getAttribute(ObjectName, String)
	 */
	protected Object getAttribute(String attributeName) throws IOException, InstanceNotFoundException {
      	try {
      		return mbsc.getAttribute(resource, attributeName);
      	}
		catch (MBeanException e) {
			if ((e.getCause() != null) && (e.getCause() instanceof RuntimeException)) {
				throw ((RuntimeException) e.getCause());
			}
			else {
				throw new RuntimeException(e);
			}
		}
		catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
		catch (AttributeNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Sets the value of an attribute of the managed resource.
	 * @param attributeName The name of the attribute.
	 * @param attributeValue The new value of the resource's attribute.
	 * @throws IOException A communication problem occurred when changing the attribute of the remote resource.
	 * @throws InstanceNotFoundException The resource does not exist in the JVM.
	 * @throws InvalidAttributeValueException The value specified for the attribute is not valid.
	 * @see MBeanServerConnection#setAttribute(ObjectName, Attribute)
	 */
	protected void setAttribute(String attributeName, Object attributeValue) throws IOException, InstanceNotFoundException, InvalidAttributeValueException {
      	try {
      		mbsc.setAttribute(resource, new Attribute(attributeName, attributeValue));
      	}
		catch (MBeanException e) {
			if ((e.getCause() != null) && (e.getCause() instanceof RuntimeException)) {
				throw ((RuntimeException) e.getCause());
			}
			else {
				throw new RuntimeException(e);
			}
		}
		catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
		catch (AttributeNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Invokes an operation on the managed resource.
	 * @param operationName The name of the operation.
	 * @param params The parameter values for the method invocation.
	 * @param signature The name of the parameter types of the operation.
	 * @return The result of the invoked operation.
	 * @throws IOException A communication problem occurred when invoking the operation of the remote resource.
	 * @throws InstanceNotFoundException The resource does not exist in the JVM.
	 * @see MBeanServerConnection#invoke(ObjectName, String, Object[], String[])
	 */
	protected Object invokeOperation(String operationName, Object[] params, String[] signature) throws IOException, InstanceNotFoundException {
      	try {
      		return mbsc.invoke(resource, operationName, params, signature);
      	}
		catch (MBeanException e) {
			if ((e.getCause() != null) && (e.getCause() instanceof RuntimeException)) {
				throw ((RuntimeException) e.getCause());
			}
			else {
				throw new RuntimeException(e);
			}
		}
		catch (ReflectionException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Checks if the managed resource is an instance of a class.
	 * @param className name of the class
	 * @return true if the managed resource is an instance of this class.
	 * @throws IOException A communication problem occurred when invoking the operation of the remote resource.
	 * @throws InstanceNotFoundException The resource does not exist in the JVM.
	 */
	protected boolean isInstanceOf(String className) throws IOException, InstanceNotFoundException {
		return mbsc.isInstanceOf(resource, className);
	}
	
	/**
	 * Adds a listener to the managed resource.
	 * @param listener The listener object which will handle the notifications emitted by the managed resource.
	 * @param filter The filter object. If filter is null, no filtering will be performed before handling notifications.
	 * @throws IOException A communication problem occurred when adding the listener to the remote resource.
	 * @throws InstanceNotFoundException The resource does not exist in the JVM.
	 * @see MBeanServerConnection#addNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	protected void addNotificationListener(NotificationListener listener, NotificationFilter filter) throws IOException, InstanceNotFoundException {
		mbsc.addNotificationListener(resource, listener, filter, null);
	}

	/**
	 * Removes a listener from the managed resource.
	 * @param listener The listener object which will no longer handle notifications from the managed resource.
	 * @param filter The used filter object.
	 * @throws IOException A communication problem occurred when removing the listener from the remote resource.
	 * @throws InstanceNotFoundException The resource does not exist in the JVM.
	 * @throws ListenerNotFoundException The listener is not registered in the managed resource, or it is not registered with the given filter.
	 * @see MBeanServerConnection#removeNotificationListener(ObjectName, NotificationListener, NotificationFilter, Object)
	 */
	protected void removeNotificationListener(NotificationListener listener, NotificationFilter filter) throws IOException, InstanceNotFoundException, ListenerNotFoundException {
		mbsc.removeNotificationListener(resource, listener, filter, null);
	}
}
