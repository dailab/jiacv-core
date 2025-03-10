package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;


/**
 * Holds information about other agents.
 * @author axle
 *
 *
 * TODO: remove inheritance from {@link AgentDescription}.
 */
public class OtherAgentDescription extends AgentDescription {
    private static final long serialVersionUID = -7568071559906302487L;

    /**
	 * Creates a new description of another agent.
	 * @param aid the agent id
	 * @param name the name of the other agent
	 * @param owner the owner of the other agent
	 * @param state the state of the other agent
	 * @param messageBoxAddress the communication address of the agent.
	 * @param agentNodeUUID the UUID of the agents node.
	 */
	public OtherAgentDescription(String aid, String name, String owner, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID) {
		super(aid, name, owner, state, messageBoxAddress, agentNodeUUID);
	}

	/**
	 * Creates the description of another agent from a given agent description.
	 * @param descr the agent description to create a description of an other agent
	 */
	public OtherAgentDescription(AgentDescription descr) {
		super(descr.getAid(), descr.getName(), descr.getOwner(), descr.getState(), descr.getMessageBoxAddress(), descr.getAgentNodeUUID());
	}

	/**
	 * Creates the description of another agent from a given agent description, but ignoring the life-cycle state by setting to <code>null</code>.
	 * @param descr the agent description to create a description of an other agent
	 */
	public OtherAgentDescription(IAgentDescription descr) {
	    super(descr.getAid(), descr.getName(), descr.getOwner(), null, descr.getMessageBoxAddress(), descr.getAgentNodeUUID());
	}
}
