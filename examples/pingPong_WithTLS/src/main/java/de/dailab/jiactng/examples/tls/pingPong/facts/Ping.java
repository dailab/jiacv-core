package de.dailab.jiactng.examples.tls.pingPong.facts;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Ping implements IFact {

	private static final long serialVersionUID = -2742466530841554393L;
	private String message = null;

	public Ping() {
	}
	
	public Ping(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
