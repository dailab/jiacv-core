package de.dailab.jiactng.examples.pingpong.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Pong implements IFact {

	private static final long serialVersionUID = 1864781216476588494L;
	public final Ping ping;

	public Pong() {
		this(null);
	}

	public Pong(final Ping ping) {
		super();
		this.ping = ping;
	}

	@Override
	public String toString() {
		return "Pong[ping=" + String.valueOf(this.ping) + "]";
	}
}
