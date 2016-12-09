package de.dailab.jiactng.agentcore.directory;

public class AgentStopped extends Amendment {

	private static final long serialVersionUID = 427868377038725946L;

	private String agentId;
	
	public AgentStopped(String agentId) {
		this.agentId = agentId;
	}

	public String getAgentId() {
		return agentId;
	}
}
