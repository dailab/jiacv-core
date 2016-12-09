package de.dailab.jiactng.agentcore.directory;

import java.util.Map;
import java.util.Set;

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
	 * @deprecated
	 */
	long getAliveInterval();
	
	/**
	 * Set the interval the node sends an alive message in milliseconds.
	 * 
	 * @param interval the time between two alive messages in milliseconds
	 * @deprecated
	 */
	void setAliveInterval(long interval);
	
	/**
	 * Get the interval the node sends an advertisement in milliseconds.
	 * 
	 * @return the time between two advertisements in milliseconds
	 * @deprecated
	 */
	long getAdvertiseInterval();
	
	/**
	 * Set the interval the node sends an advertisement in milliseconds.
	 * 
	 * @param interval the time between two advertisements in milliseconds
	 * @deprecated
	 */
	void setAdvertiseInterval(long interval);
	
	/**
	 * Get the intervals of the node for sending alive messages to each group.
	 * 
	 * @return the time between two alive messages in milliseconds
	 */
	Map<String,Long> getAliveIntervals();

	/**
	 * Get the intervals of the node for sending advertisements to each group.
	 * 
	 * @return the time between two advertisements in milliseconds
	 */
	Map<String,Long> getAdvertiseIntervals();

	/**
	 * Get the allowed delay for sending or receiving alive messages without
	 * getting a warning.
	 * @return the allowed delay in milliseconds
	 */
	long getAllowedAliveDelay();

	/**
	 * Set the allowed delay for sending or receiving alive messages without
	 * getting a warning.
	 * @param allowedAliveDelay the allowed delay in milliseconds
	 */
	void setAllowedAliveDelay(long allowedAliveDelay);

	/**
	 * Get the maximum delay for receiving alive messages without removing the
	 * remote node from the directory.
	 * @return the maximum delay in milliseconds
	 */
	long getMaxAliveDelay();

	/**
	 * Set the maximum delay for receiving alive messages without removing the
	 * remote node from the directory.
	 * @param maxAliveDelay the maximum delay in milliseconds
	 */
	void setMaxAliveDelay(long maxAliveDelay);

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
	 * Returns all (other) known agent nodes.
	 * The names of the composite items (columns) are "UUID" and "description".
	 * @return the known nodes
	 * @see de.dailab.jiactng.agentcore.ontology.IAgentNodeDescription
	 */
	TabularData getKnownNodes();

	/**
	 * Returns all agent nodes, which neither sent a bye nor an alive message.
	 * @return the UUID of the nodes
	 */
	Set<String> getMissingNodes();
}
