package de.dailab.jiactng.agentcore.directory;

import java.util.HashSet;
import java.util.Set;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class Advertisement implements IFact {
	private static final long serialVersionUID = 4672359214129779792L;

	private Set<IActionDescription> actions = new HashSet<IActionDescription>();

	public Advertisement(Set<IActionDescription> actions) {
		this.actions.addAll(actions);
	}
	
	public Set<IActionDescription> getActions() {
		return actions;
	}
}
