package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;


/**
 * Holds information about this agent.
 */
public class ThisAgentDescription extends AgentDescription {
    private static final long serialVersionUID = -4734957489524953244L;

    /**
     * Constructor for an empty agent description.
     */
    public ThisAgentDescription() {
        super();
    }
    
    /**
	 * Constructor for a new agent description.
	 * 
	 * @param aid the agents uid.
	 * @param name the name of the agent.
	 * @param owner the owner of the agent.
	 * @param state the agents current state.
	 * @param messageBoxAddress the communication address of the agent.
	 * @param agentNodeUUID the UUID of the agents node.
	 */
	public ThisAgentDescription(String aid, String name, String owner, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID) {
		super(aid, name, owner, state, messageBoxAddress, agentNodeUUID);
	}
}
