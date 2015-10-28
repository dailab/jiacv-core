package de.dailab.jiactng.examples.pingpong.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Ping implements IFact {

	private static final long serialVersionUID = 7801792272870848314L;
	public final Integer ping;

	public Ping() {
		this(null);
	}

	public Ping(final Integer ping) {
		this.ping = ping;
	}

	@Override
	public String toString() {
		return "Ping[" + String.valueOf(this.ping) + "]";
	}

}
