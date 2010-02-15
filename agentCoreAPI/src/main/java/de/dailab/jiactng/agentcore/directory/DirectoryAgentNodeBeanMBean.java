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
	 * @param interval the time between two advertisements in milliseconds
	 */
	void setAdvertiseInterval(long interval);
	
	/**
	 * Whether or not a dump is printed to the console
	 * 
	 * @return if true, the dump is printed to the console
	 */
	boolean isDump();
	
	/**
	 * Set whether the dump should be printed to console.
	 * 
	 * @param newDump if true, the dump will be printed to console
	 */
	void setDump(boolean newDump);
	
	//sonstige operations auf dem directory

	/**
	 * Returns all locally offered actions of this node 
	 * The names of the composite items (columns) are the item names of action descriptions.
	 * @return the actions offered locally
	 * @see de.dailab.jiactng.agentcore.ontology.IActionDescription
	 */
	TabularData getLocalActions();
	
	/**
	 * Returns all agents residing on this node.
	 * The names of the composite items (columns) are "Agent ID" and "description".
	 * @return the agents residing on this node
	 * @see de.dailab.jiactng.agentcore.ontology.IAgentDescription
	 */
	TabularData getLocalAgents();

	/**
	 * Returns all actions offered by remote nodes.
	 * The names of the composite items (columns) are "agent node UUID" and "remote actions".
	 * @return the actions offered by remote nodes
	 * @see de.dailab.jiactng.agentcore.ontology.IActionDescription
	 */
	TabularData getRemoteActions();
	
	/**
	 * Returns all agents residing on remote nodes.
	 * The names of the composite items (columns) are "Agent ID" and "description".
	 * @return the agents residing on remote nodes
	 * @see de.dailab.jiactng.agentcore.ontology.IAgentDescription
	 */
	TabularData getRemoteAgents();
	
	/**
	 * Return all (other) known agent nodes.
	 * The names of the composite items (columns) are "UUID" and "description".
	 * @return the known nodes
	 * @see de.dailab.jiactng.agentcore.ontology.IAgentNodeDescription
	 */
	TabularData getKnownNodes();
}
