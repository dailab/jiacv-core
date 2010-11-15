package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class AgentGroupDescription implements IFact {
	private static final long serialVersionUID = 7813451264270405139L;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
