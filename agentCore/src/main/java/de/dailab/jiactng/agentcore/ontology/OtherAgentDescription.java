package de.dailab.jiactng.agentcore.ontology;


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
	 * @param endpoint the address under which the other agent can be reached
	 */
	public OtherAgentDescription(String aid, String name, String state) {
		super(aid, name, state);
	}

	/**
	 * Creates the description of another agent from a given agent description.
	 * @param descr the agent description to create a description of an other agent
	 */
	public OtherAgentDescription(AgentDescription descr) {
		super(descr.getAid(), descr.getName(), descr.getState(), descr.getMessageBoxAddress());
	}
	
	public OtherAgentDescription(IAgentDescription descr) {
	    super(descr.getAid(), descr.getName(), null, descr.getMessageBoxAddress());
	}
}
