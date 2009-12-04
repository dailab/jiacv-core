package de.dailab.jiactng.agentcore.execution;

/**
 * JMX compliant management interface of non-blocking execution cycles to get information
 * about them.
 * 
 * @author Jan Keiser
 */
public interface NonBlockingExecutionCycleMBean extends AbstractExecutionCycleMBean {

	/**
	 * Gets the number of currently running handler threads.
	 * @return The number of running handler threads.
	 */
	int getRunningHandlers();
	
}
