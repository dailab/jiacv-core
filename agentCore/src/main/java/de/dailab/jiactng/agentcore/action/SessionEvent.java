package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * This class is thought to be super class of all session events.
 * Every session event belongs to a session and to an action
 * which it will trigger or be result of.
 * 
 * @author axle
 */
abstract class SessionEvent implements IFact {

	/** The session of the session this session event belongs to. */
	protected Session session;
	
	/** The action this session event will trigger or be result of. */
	protected Action action;
	
	/** The object that created this event. */
	protected Object source;
	
	/**
	 * Subclasses use this constructor to set the session and action
	 * of a session event. 
	 * 
	 * @param session the session this event belongs to
	 * @param action the action this event is trigger or result of
	 * @param source the originator of this event
	 */
	protected SessionEvent(Session session, Action action, Object source) {
		this.session = session;
		this.action = action;
		this.source = source;
	}

	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	/**
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(Session session) {
		this.session = session;
	}

	/**
	 * @return the session id of the session this event belongs to
	 */
	public String getSessionId() {
		return session.getId();
	}
	
	/**
	 * @param id sets the id of the session
	 */
	public void setSessionId(String id) {
		this.session.setId(id);
	}
}
