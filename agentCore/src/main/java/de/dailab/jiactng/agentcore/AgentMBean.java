package de.dailab.jiactng.agentcore;

/**
 * @author Jan Keiser
 *
 */
public interface AgentMBean {

	/**
	 * Getter for attribute "Name" of the managed agent.
	 */
	public String getName();

	/**
	 * Getter for attribute "AgentNodeUUID" of the managed agent.
	 */
	public String getAgentNodeUUID();

	//public Service[] getServices();
	//public String getDeploymentDescriptor();

	//public URL[] getAddresses();

	/**
	 * Getter for attribute "LifecycleState" of the managed agent.
	 */
	public String getLifecycleState();
	
	/**
	 * Setter for attribute "LifecycleState" of the managed agent.
	 */
	public void setLifecycleState(String state);

	//public long getInitTime();
	
	//public long getStartTime();

	//public String getOwner();
}
