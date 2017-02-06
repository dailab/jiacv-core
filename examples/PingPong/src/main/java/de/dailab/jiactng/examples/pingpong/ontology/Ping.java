package de.dailab.jiactng.examples.pingpong.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * One of the simples fact in agent's world - a ping. This is used as example
 * for creating own agent knowledge facts.
 *
 * @author mib
 *
 */
public class Ping implements IFact {

	/*
	 * All facts are serializable and need this version UID. This should be
	 * up-to-date to help the Java VM to identify classes within of data streams.
	 */
	private static final long serialVersionUID = 7801792272870848314L;

	/*
	 * All field variable must be serializable. There should never be a reference
	 * to a stream or process. Otherwise use the field modifier 'transient'.
	 */
	/**
	 * A identifier of this fact. If a field never changes except within of the
	 * constructor, the field could be 'public' and must be 'final'. Don't use
	 * native types, instead use the corresponding classes from
	 * <code>java.lang</code> package.
	 */
	public final Integer ping;

	/**
	 * An empty constrctor that calls the parameterized constructor below.
	 */
	public Ping() {
		this(null);
	}

	/**
	 * A identifiable Ping fact with an Integer parameter.
	 *
	 * @param ping
	 *          originally the creation count of the pings
	 */
	public Ping(final Integer ping) {
		this.ping = ping;
	}

	/**
	 * A helping method to have a human readable String out of every instance of
	 * this class.
	 *
	 * @return a human readable string of Ping.
	 */
	@Override
	public String toString() {
		return "Ping[" + String.valueOf(this.ping) + "]";
	}

}
