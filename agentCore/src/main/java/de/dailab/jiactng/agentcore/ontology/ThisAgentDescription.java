package de.dailab.jiactng.agentcore.ontology;



public class ThisAgentDescription extends AgentDescription {

	/**
	 * {@inheritDoc}
	 */
	private static final long serialVersionUID = 3319772309911503648L;

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
