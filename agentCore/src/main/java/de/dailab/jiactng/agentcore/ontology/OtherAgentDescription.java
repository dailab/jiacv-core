package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.IEndPoint;

public class OtherAgentDescription extends AgentDescription {

	private static final long serialVersionUID = -6296106391008839370L;

	public OtherAgentDescription(String aid, String name, String state, IEndPoint endpoint) {
		super(aid, name, state, endpoint);
	}

	public OtherAgentDescription(AgentDescription descr) {
		super(descr.getAid(), descr.getName(), descr.getState(), descr.getEndpoint());
	}
}
