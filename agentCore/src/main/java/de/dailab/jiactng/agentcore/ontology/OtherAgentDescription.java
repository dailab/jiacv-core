package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;


/**
 * Holds information about other agents.
 * @author axle
 *
 *
 * TODO: remove inheritance from {@link AgentDescription}.
 */
public class OtherAgentDescription extends AgentDescription implements IAgentDescription {
    private static final long serialVersionUID = -7568071559906302487L;

    /**
	 * Creates a new description of another agent.
	 * @param aid the agent id
	 * @param name the name of the other agent
	 * @param state the state of the other agent
	 */
	public OtherAgentDescription(String aid, String name, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID) {
		super(aid, name, state, messageBoxAddress, agentNodeUUID);
	}

	/**
	 * Creates the description of another agent from a given agent description.
	 * @param descr the agent description to create a description of an other agent
	 */
	public OtherAgentDescription(AgentDescription descr) {
		super(descr.getAid(), descr.getName(), descr.getState(), descr.getMessageBoxAddress(), descr.getAgentNodeUUID());
	}
	
	public OtherAgentDescription(IAgentDescription descr) {
	    super(descr.getAid(), descr.getName(), null, descr.getMessageBoxAddress(), descr.getAgentNodeUUID());
	}
}
