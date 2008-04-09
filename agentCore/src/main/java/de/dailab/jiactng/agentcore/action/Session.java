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
 * superclass and thus can have the same session-sessionId.
 * 
 * @author moekon
 */
public class Session implements IFact {

	/** SerialVersionUID for Serialization */
	private static final long serialVersionUID = 8699173523554827559L;

	/** The actual ID of the session. */
	private String sessionId;

	/** The creation time of the session object. */
	private Long creationTime;
	
	/** Optional attribute for timeout conditions, 
	 * it only is relevant when the session is written into the memory
	 * Default value is 60 seconds*/
	private Long timeToLive = new Long(60000); 

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
	private ArrayList<SessionEvent> history;

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
				source, new ArrayList<SessionEvent>());
	}

	public Session(){
		this.sessionId = null;
		this.creationTime = null;
		this.history = null;
		this.source = null;
		this.timeToLive = null;
	}
	
	/**
	 * Constructor to set all values of session by hand.
	 * 
	 * @param sessionId
	 *            the session sessionId
	 * @param creationTime
	 *            the time of creation of this session
	 * @param source
	 *            the creator object of this session
	 * @param history
	 *            the history of this session
	 */
	public Session(String id, Long creationTime, ResultReceiver source,
			ArrayList<SessionEvent> history) {
		this.sessionId = id;
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
		this.sessionId = session.getSessionId();
		this.creationTime = session.getCreationTime();
		this.source = session.getSource();
		this.history = session.getHistory();
	}

	/**
	 * Getter for the sessionId of this session object
	 * 
	 * @return a string representing the sessionId of this session.
	 */
	public String getSessionId() {
		return sessionId;
	}
	
	/**
	 * Sets an optional timeout attribute. If not set the default value is 60.000 milliseconds
	 * Used only when session is written into agents memory
	 * 
	 * @param timeout
	 */
	public void setTimeToLive(long timeout){
		timeToLive = new Long(timeout);
	}
	
	public Long getTimeToLive(){
		return timeToLive;
	}

	/**
	 * @param sessionId
	 *            the sessionId to set
	 */
	public void setSessionId(String id) {
		this.sessionId = id;
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
	public Long getCreationTime() {
		return creationTime;
	}

	/**
	 * @param creationTime
	 *            the creationTime to set
	 */
	public void setCreationTime(long creationTime) {
		this.creationTime = new Long(creationTime);
	}

	/**
	 * Getter for the history of this Session-object
	 * 
	 * @return an Arraylist containing the history.
	 */
	public ArrayList<SessionEvent> getHistory() {
		return history;
	}

	/**
	 * @param history
	 *            the history to set
	 */
	public void setHistory(ArrayList<SessionEvent> history) {
		this.history = history;
	}

	/**
	 * Will add the given object to the session history.
	 * 
	 * @param event
	 *            the object to add to history
	 */
	public void addToSessionHistory(SessionEvent event) {
		if (history == null) {
			history = new ArrayList<SessionEvent>();
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
	public SessionEvent removeFromSessionHistory(int index) {
		return history.remove(index);
	}

	/**
	 * Will remove the given object from the history if present.
	 * 
	 * @param o
	 *            the object to remove
	 * @return true, if history contained the given object
	 */
	public boolean removeFromSessionHistory(SessionEvent o) {
		return history.remove(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		// sessionId
		builder.append("Session:\n sessionId=");
		if (sessionId != null) {
			builder.append("'").append(sessionId).append("'");
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
			builder.append("'").append(source.toString()).append("'");
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
