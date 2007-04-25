package de.dailab.jiactng.agentcore.action;

import java.util.ArrayList;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Session implements IFact {

	private String id;

	private ArrayList<Object> history;

	public Session(Object source) {
		if (source instanceof Session) {
			this.id = ((Session) source).getId();
			this.history = ((Session) source).getHistory();
			this.history.add(source);
		} else {
			history = new ArrayList<Object>();
			history.add(source);
			this.id = generateID();
		}
	}

	public ArrayList<Object> getHistory() {
		return history;
	}

	public String getId() {
		return id;
	}

	private String generateID() {
		// TODO replace with usefull code
		return new Double(Math.random()).toString();
	}

}
