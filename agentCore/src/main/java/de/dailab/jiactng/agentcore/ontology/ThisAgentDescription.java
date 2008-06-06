package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;



public class ThisAgentDescription extends AgentDescription {
    private static final long serialVersionUID = -4734957489524953244L;

    public ThisAgentDescription() {
        super();
    }
    
    /**
	 * Constructor for a new agent description.
	 * 
	 * @param aid the agents uid.
	 * @param name the name of the agent.
	 * @param state the agents current state.
	 */
	public ThisAgentDescription(String aid, String name, String state, IMessageBoxAddress messageBoxAddress, String agentNodeUUID) {
		super(aid, name, state, messageBoxAddress, agentNodeUUID);
	}
}
