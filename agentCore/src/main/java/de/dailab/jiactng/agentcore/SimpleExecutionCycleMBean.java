package de.dailab.jiactng.agentcore;

/**
 * JMX compliant management interface of agent execution cycles to get information
 * about them.
 * 
 * @author Jan Keiser
 */
public interface SimpleExecutionCycleMBean extends AbstractAgentBeanMBean {

	/**
	 * Gets the workload of this execution cycle for executing agent beans.
	 * @return The workload in percent.
	 */
	int getExecutionWorkload();

	/**
	 * Gets the workload of this execution cycle for performing actions.
	 * @return The workload in percent.
	 */
	int getDoActionWorkload();

	/**
	 * Gets the workload of this execution cycle for processing action results.
	 * @return The workload in percent.
	 */
	int getActionResultWorkload();

}
