package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.message.IEndPoint;

/**
 * Holds information about other agents.
 * @author axle
 *
 */
public class OtherAgentDescription extends AgentDescription {

	private static final long serialVersionUID = -6296106391008839370L;

	/**
	 * Creates a new description of another agent.
	 * @param aid the agent id
	 * @param name the name of the other agent
	 * @param state the state of the other agent
	 * @param endpoint the address under which the other agent can be reached
	 */
	public OtherAgentDescription(String aid, String name, String state, IEndPoint endpoint) {
		super(aid, name, state, endpoint);
	}

	/**
	 * Creates the description of another agent from a given agent description.
	 * @param descr the agent description to create a description of an other agent
	 */
	public OtherAgentDescription(AgentDescription descr) {
		super(descr.getAid(), descr.getName(), descr.getState(), descr.getEndpoint());
	}
}
