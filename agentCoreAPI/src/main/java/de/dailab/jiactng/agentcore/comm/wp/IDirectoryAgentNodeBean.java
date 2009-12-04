package de.dailab.jiactng.agentcore.comm.wp;

import de.dailab.jiactng.agentcore.IAgentNodeBean;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * Interface of agent node beans which implement a directory service.
 */

public interface IDirectoryAgentNodeBean extends IAgentNodeBean {

	/** suffix for address-creation purposes. Will be added to the UUID of AgentNode to create Beanaddress */
	public final static String SEARCHREQUESTSUFFIX = "DirectoryAgentNodeBean";

	/** Protocol for search Requests */
	public final static String SEARCH_REQUEST_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#SearchRequest";

	/** Protocol for adding of actions to the Directory */
	public final static String ADD_ACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#AddAction";

	/** Protocol for removing of actions to the Directory */
	public final static String REMOVE_ACTION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#RemoveAction";

	/** Protocol for refreshing of Actions */
	public final static String REFRESH_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#ActionRefresh";

	/** Protocol for propagating changes on an AgentNode-Directory and to communicate all what is stored within it when a new AgentNode shows up */
	public final static String CHANGE_PROPAGATION_PROTOCOL_ID = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#ChangePropagation";

	/** Address of AgentNodeGroup. Is used to communicate between AgentNodes for purposes like global searches. */
	public final static String AGENTNODESGROUP = "de.dailab.jiactng.agentcore.comm.wp.DirectoryAgentNodeBean#GroupAddress";
	
	/**
	 * This method is meant to give the AgentNode the option to directly
	 * add an AgentDescription object when an agent is added to the node.
	 */
	public void addAgentDescription(IAgentDescription agentDescription);

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * remove an AgentDescription object from the directory when an agent
	 * is removed from the node.
	 */
	public void removeAgentDescription(IAgentDescription agentDescription);

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * add an Action object when an agent is added to the node.
	 * 
	 * @param <T>
	 * @param action
	 */
	public <T extends IActionDescription> void addAction(T action);

	/**
	 * This method is meant to give the AgentNode the option to directly
	 * remove an Action object from the directory when an agent
	 * is removed from the node.
	 * 
	 * @param <T>
	 * @param action
	 */
	public <T extends IActionDescription> void removeAction(T action);

}
