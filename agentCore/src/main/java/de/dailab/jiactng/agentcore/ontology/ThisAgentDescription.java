package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.IEndPoint;

public class ThisAgentDescription extends AgentDescription {

	private static final long serialVersionUID = 3319772309911503648L;

	public ThisAgentDescription(String aid, String name, String state, IEndPoint endpoint) {
		super(aid, name, state, endpoint);
	}
}
