package de.dailab.jiactng.agentcore.knowledge;

import java.io.Serializable;

/**
 * Wrapper class for wrapping any Serializable content into an IFact.
 *
 * The idea behind IFact was that one should not send meaningless objects
 * like strings, or numbers, in messages, but only meaningful "IFacts", i.e.
 * objects of some specific ontology. But if this ontology is not a collection
 * of IFact POJOs, but instead e.g. an Ecore model, this is a problem. Such
 * objects can be wrapped in this FactWrapper (if they are Serializable).
 *
 * @author kuester
 */
public class FactWrapper<T extends Serializable> implements IFact {

	private static final long serialVersionUID = -6439494102527522140L;

	public final T payload;

	public FactWrapper(T payload) {
		this.payload = payload;
	}

	@Override
	public String toString() {
		return String.format("FactWrapper(%s)", this.payload);
	}

}
