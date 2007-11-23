package de.dailab.jiactng.agentcore.action;

import java.util.ArrayList;
import java.util.Calendar;

import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.util.IdFactory;

/**
 * This class is used as a superclass for everything that is connected to the
 * execution of an action. The idea is, that all phases of action execution (an
 * consequently all objects associated with those phases) have a commen
 * superclass and thus can have the same session-id.
 * 
 * @author moekon
 */
public class Session implements IFact {

	/** The actual ID of the session. */
	private String id;

	/** The creation time of the session object. */
	private long creationTime;

	/** Stores the reference to the creator of this session. */
	/*
	 * FIXME: this source is transient now... examine whether this provides
	 * issues -> remember that session objects are serialised in the Remote*
	 * classes!
	 */
	private transient ResultReceiver source;

	/**
	 * The history of the session. This list contains all sources of the session
	 * (i.E. of actions, results, etc.) in order of appearance. TODO should be
	 * of type ArrayList<SessionEvent>
	 */
	private ArrayList<Object> history;

	/**
	 * Constructor for a new session. Can be called with any kind of object as
	 * source, usually the class, that initiated the session.
	 * 
	 * @param source
	 *            the source that created the new session
	 */
	public Session(ResultReceiver source) {
		this(IdFactory.createSessionId((source != null) ? source.hashCode()
				: Session.class.hashCode()), System.currentTimeMillis(),
				source, new ArrayList<Object>());
	}

	/**
	 * Constructor to set all values of session by hand.
	 * 
	 * @param id
	 *            the session id
	 * @param creationTime
	 *            the time of creation of this session
	 * @param source
	 *            the creator object of this session
	 * @param history
	 *            the history of this session
	 */
	public Session(String id, long creationTime, ResultReceiver source,
			ArrayList<Object> history) {
		this.id = id;
		this.creationTime = creationTime;
		this.source = source;
		this.history = history;
	}

	/**
	 * Copying constructor. This session will be initiated with the values of
	 * the parameter <code>session</code>.
	 * 
	 * @param session
	 *            the session to copy the values from
	 */
	public Session(Session session) {
		this.id = session.getId();
		this.creationTime = session.getCreationTime();
		this.source = session.getSource();
		this.history = session.getHistory();
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
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the source
	 */
	public ResultReceiver getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(ResultReceiver source) {
		this.source = source;
	}

	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime
	 *            the creationTime to set
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
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
	 * @param history
	 *            the history to set
	 */
	public void setHistory(ArrayList<Object> history) {
		this.history = history;
	}

	/**
	 * Will add the given object to the session history.
	 * 
	 * @param event
	 *            the object to add to history
	 */
	public void addToSessionHistory(Object event) {
		if (history == null) {
			history = new ArrayList<Object>();
		}
		history.add(event);
	}

	/**
	 * Will remove the object indexed by the given number from the history.
	 * 
	 * @param index
	 *            the index of the object to remove
	 * @return the removed object
	 */
	public Object removeFromSessionHistory(int index) {
		return history.remove(index);
	}

	/**
	 * Will remove the given object from the history if present.
	 * 
	 * @param o
	 *            the object to remove
	 * @return true, if history contained the given object
	 */
	public boolean removeFromSessionHistory(Object o) {
		return history.remove(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		// id
		builder.append("Session:\n id=");
		if (id != null) {
			builder.append("'").append(id).append("'");
		} else {
			builder.append("null");
		}

		// time
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(creationTime);
		builder.append("\n created='").append(calendar.getTime().toString())
				.append("'");

		// source
		builder.append("\n source=");
		if (source != null) {
			builder.append("'").append(source.getBeanName()).append("'");
		} else {
			builder.append("null");
		}

		// history
		builder.append("\n history=");
		if (history != null) {
			builder.append(history.toString());
		} else {
			builder.append("null");
		}

		builder.append('\n');
		return builder.toString();
	}
}
