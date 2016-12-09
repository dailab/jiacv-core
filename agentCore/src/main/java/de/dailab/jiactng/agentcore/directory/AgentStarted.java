package de.dailab.jiactng.agentcore.directory;

import java.util.Set;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class AgentStarted extends Amendment {

	private static final long serialVersionUID = -2320374924176734731L;

	private IAgentDescription agent;

	private Set<IActionDescription> actions;

	public AgentStarted(IAgentDescription agent, Set<IActionDescription> actions) {
		this.agent = agent;
		this.actions = actions;
	}

	public IAgentDescription getAgent() {
		return agent;
	}

	public Set<IActionDescription> getActions() {
		return actions;
	}
}
