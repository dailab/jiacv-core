package de.dailab.jiactng.agentcore.directory;

import javax.management.openmbean.TabularData;

/**
 * Interface for controlling directory via JMX.
 * @author axle
 *
 */
public interface DirectoryAgentNodeBeanMBean {
	//getter, setter
	
	/**
	 * Get the interval the node sends an alive message in milliseconds.
	 * 
	 * @return the time between two alive messages in milliseconds
	 */
	long getAliveInterval();
	
	/**
	 * Set the interval the node sends an alive message in milliseconds.
	 * 
	 * @param interval the time between two alive messages in milliseconds
	 */
	void setAliveInterval(long interval);
	
	/**
	 * Get the interval the node sends an advertisement in milliseconds.
	 * 
	 * @return the time between two advertisements in milliseconds
	 */
	long getAdvertiseInterval();
	
	/**
	 * Set the interval the node sends an advertisement in milliseconds.
	 * 
	 * @param advertiseInterval the time between two advertisements in milliseconds
	 */
	void setAdvertiseInterval(long advertiseInterval);
	
	/**
	 * Whether or not a dump is printed to the console
	 * 
	 * @return if true, the dump is printed to the console
	 */
	boolean isDump();
	
	/**
	 * Set whether the dump should be printed to console.
	 * 
	 * @param dump if true, the dump will be printed to console
	 */
	void setDump(boolean dump);
	
	//sonstige operations auf dem directory

	/**
	 * Returns all locally offered actions of this node 
	 * 
	 * @return the actions offered locally
	 */
	TabularData getLocalActions();
	
	/**
	 * Returns all agents residing on this node.
	 * 
	 * @return the agents residing on this node
	 */
	TabularData getLocalAgents();

	/**
	 * Returns all actions offered by remote nodes.
	 * 
	 * @return the actions offered by remote nodes
	 */
	TabularData getRemoteActions();
	
	/**
	 * Returns all agents residing on remote nodes.
	 * 
	 * @return the agents residing on remote nodes
	 */
	TabularData getRemoteAgents();
	
	/**
	 * Return all (other) known agent nodes.
	 * 
	 * @return the known nodes
	 */
	TabularData getKnownNodes();
}
