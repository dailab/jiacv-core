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
	String getLifecycleState();
	
//	/**
//	 * Getter for attribute "Strict" of the managed resource.
//	 * @return the lifecycle mode of this resource
//	 */
//	boolean isStrict();
	
	/**
	 * Initializes the managed resource.
     * @throws Exception if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#init()
	 */
	void init() throws Exception;

	/**
	 * Starts the managed resource.
     * @throws Exception if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
	 *
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#start()
	 */
	void start() throws Exception;

	/**
	 * Stops the managed resource.
     * @throws Exception if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#stop()
	 */
	void stop() throws Exception;

	/**
	 * Cleanes up the managed resource.
     * @throws Exception if the object is not in one of the expected
     * previous states (depending on mode) or an error occurs during change of 
     * the state.
     * 
     * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle#cleanup()
	 */
	void cleanup() throws Exception;

	/**
	 * Getter for attribute "Logger".
	 * @return Information about class and levels of the current logger.
	 */
	CompositeData getLogger();

	/**
	 * Gets the log level of the agent node's log4j logger.
	 * @return one of OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE and ALL.
	 */
	String getLogLevel();

	/**
	 * Sets the log level of the agent node's log4j logger. The log level will
	 * be set to DEBUG if the parameter has an illegal value.
	 * @param level one of OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE and ALL.
	 */
	void setLogLevel(String level);

	/**
	 * Checks if the logger has an own log level or inherits the log level 
	 * from the parent logger.
	 * @return <code>null</code> if the logger is not initialized, 
	 * 		<code>true</code> if the logger is not a root logger and does not 
	 * 		have an own log level, <code>false</code> otherwise
	 */
	Boolean getLogLevelInheritance();

	/**
	 * Deactivates log level inheritance by setting a log level of the logger, 
	 * or activates log level inheritance by removing the log level of the
	 * logger. Nothing will happen if the logger is a root logger or the 
	 * parameter is null.
	 * @param inheritance <code>true</code> to activate inheritance from parent
	 * 		logger and <code>false</code> to deactivate inheritance
	 */
	void setLogLevelInheritance(Boolean inheritance);
}
