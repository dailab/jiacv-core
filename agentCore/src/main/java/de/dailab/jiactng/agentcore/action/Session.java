package de.dailab.jiactng.agentcore.action;

import java.util.ArrayList;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * This class is used as a superclass for everything that is connected to the execution of an action. The idea is, that
 * all phases of action execution (an consequently all objects associated with those phases) have a commen superclass
 * and thus can have the same session-id.
 * 
 * @author moekon
 */
public class Session implements IFact {

	/** The actual ID of the session */
	private String id;

	/**
	 * The history of the session. This list contains all sources of the session (i.E. of actions, results, etc.) in order
	 * of appearance.
	 */
	private ArrayList<Object> history;

	/**
	 * Constructor for a new session. Can be calles with either any kind of object as source (usually the class, that
	 * initiated the session), or with a class that is a subclass of Session, thus creating the next step in the Session
	 * with the same id.
	 * 
	 * @param source the source that created the new session.
	 */
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

	/**
	 * Getter for the history of this Session-object
	 * 
	 * @return an Arraylist containing the history.
	 */
	public ArrayList<Object> getHistory() {
		return history;
	}

	/**
	 * Getter for the id of this session object
	 * 
	 * @return a string representing the id of this session.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Utility-method for creation of a new id.
	 * 
	 * @return a string representing a new id.
	 */
	private String generateID() {
		// return IdFactory.createAgentId(this);
		// TODO: replace with usefull code
		return new Double(Math.random()).toString();
	}

}
