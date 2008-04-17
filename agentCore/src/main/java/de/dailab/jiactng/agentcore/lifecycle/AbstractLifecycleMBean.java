package de.dailab.jiactng.agentcore.lifecycle;

import javax.management.openmbean.CompositeData;

/**
 * Common management interface of all lifecycle-aware resources.
 * @author Jan Keiser
 */
public interface AbstractLifecycleMBean {

	/**
	 * Getter for attribute "LifecycleState" of the managed resource.
	 * @return the lifecycle state of this resource
	 */
	public String getLifecycleState();
	
	/**
	 * Getter for attribute "Strict" of the managed resource.
	 * @return the lifecycle mode of this resource
	 */
	public boolean isStrict();
	
	/**
	 * Initializes the managed resource.
     * @throws Exception if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#init()
	 */
	public void init() throws Exception;

	/**
	 * Starts the managed resource.
     * @throws Exception if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
	 *
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#start()
	 */
	public void start() throws Exception;

	/**
	 * Stops the managed resource.
     * @throws Exception if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#stop()
	 */
	public void stop() throws Exception;

	/**
	 * Cleanes up the managed resource.
     * @throws Exception if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#cleanup()
	 */
	public void cleanup() throws Exception;

	/**
	 * Getter for attribute "Logger".
	 * @return Information about class and levels of the current logger.
	 */
	public CompositeData getLogger();

	/**
	 * Gets the log level of the agent node's log4j logger.
	 * @return one of OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE and ALL.
	 */
	public String getLogLevel();

	/**
	 * Sets the log level of the agent node's log4j logger. The log level will
	 * be set to DEBUG if the parameter has an illegal value.
	 * @param level one of OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE and ALL.
	 */
	public void setLogLevel(String level);

}
