package de.dailab.jiactng.examples.pingpong.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * The second simples fact in agent's world - a pong. This is used as example
 * for creating own agent knowledge facts with references.
 *
 * @author mib
 *
 */
public class Pong implements IFact {

	private static final long serialVersionUID = 1864781216476588494L;
	/**
	 * The reference to a Ping.
	 */
	public final Ping ping;

	/**
	 * An empty constrctor that calls the parameterized constructor below.
	 */
	public Pong() {
		this(null);
	}

	/**
	 * A identifiable Pong fact with a reference to Ping.
	 *
	 * @param ping
	 *          the referenced Ping
	 */
	public Pong(final Ping ping) {
		super();
		this.ping = ping;
	}

	/**
	 * A helping method to have a human readable String out of every instance of
	 * this class.
	 *
	 * @return a human readable string of Pong.
	 */
	@Override
	public String toString() {
		return "Pong[ping=" + String.valueOf(this.ping) + "]";
	}
}
