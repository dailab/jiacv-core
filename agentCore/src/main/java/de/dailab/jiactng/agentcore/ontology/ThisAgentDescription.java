package de.dailab.jiactng.agentcore.ontology;



public class ThisAgentDescription extends AgentDescription {
    private static final long serialVersionUID = -4734957489524953244L;

    /**
	 * Constructor for a new agent description.
	 * 
	 * @param aid the agents uid.
	 * @param name the name of the agent.
	 * @param state the agents current state.
	 * @param endpoint the address of the agent.
	 */
	public ThisAgentDescription(String aid, String name, String state) {
		super(aid, name, state);
	}
}
