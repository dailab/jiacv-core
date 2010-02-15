package de.dailab.jiactng.agentcore.directory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Advertisement implements IFact {
	private static final long serialVersionUID = 4672359214129779792L;

	private Set<IActionDescription> actions = new HashSet<IActionDescription>();
	
	private Hashtable<String, IAgentDescription> agents = new Hashtable<String, IAgentDescription>();

	public Advertisement(Hashtable<String, IAgentDescription> agents) {
		this(agents, null);
	}
	
	public Advertisement(Set<IActionDescription> actions) {
		this(null, actions);
	}

	public Advertisement(Hashtable<String, IAgentDescription> agents, Set<IActionDescription> actions) {
		if (agents != null) {
			this.agents.putAll(agents);
		}
		if (actions != null) {
			this.actions.addAll(actions);
		}
	}
	
	public Set<IActionDescription> getActions() {
		return actions;
	}
	
	public void setActions(Set<IActionDescription> newActions) {
		actions.addAll(newActions);
	}

	public Hashtable<String, IAgentDescription> getAgents() {
		return agents;
	}

	public void setAgents(Hashtable<String, IAgentDescription> newAgents) {
		agents.putAll(newAgents);
	}
}
