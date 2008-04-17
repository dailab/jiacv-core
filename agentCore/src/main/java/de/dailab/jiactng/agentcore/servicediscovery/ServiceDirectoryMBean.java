package de.dailab.jiactng.agentcore.servicediscovery;

import javax.management.openmbean.CompositeData;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycleMBean;

/**
 * JMX-compliant management interface for service directory.
 * @author Jan Keiser
 */
public interface ServiceDirectoryMBean extends AbstractLifecycleMBean {

	/**
	 * Gets the number of registered services.
	 * @return number of registered services
	 */
	public int getServiceNumber();

	/**
	 * Gets the time in milliseconds between publishment of services.
	 * @return time in milliseconds between publishment of services
	 */
	public int getPublishTimer();

	/**
	 * Sets the time in milliseconds between publishment of services.
	 * @param publishTimer time in milliseconds between publishment of services
	 */
	public void setPublishTimer(int publishTimer);

	/**
	 * Information about the facts stored in the service directory memory.
	 * @return information about facts stored in service directory memory
	 */
	public CompositeData getMemory();

}
