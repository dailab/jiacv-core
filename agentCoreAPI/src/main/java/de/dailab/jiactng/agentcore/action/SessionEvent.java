package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * This class is thought to be super class of all session events. Every session
 * event belongs to a session and to an action which it will trigger or be
 * result of.
 * 
 * @author axle
 */
public class SessionEvent implements IFact {

	/** SerialVersionUID for Serialization */
	private static final long serialVersionUID = 2754758742968423185L;

	/** The session of the session this session event belongs to. */
	private Session session;

	/** Redundant for the sake of SimpleSpace. */
	private String sessionId;

	/** The action this session event will trigger or be result of. */
	private IActionDescription action;

	/** The object that created this event. */
	private transient Object source;

	private Object metaData;

	
	/**
	 * Subclasses use this constructor to set the session and action of a
	 * session event.
	 * 
	 * @param session
	 *            the session this event belongs to
	 * @param action
	 *            the action this event is trigger or result of
	 * @param source
	 *            the originator of this event
	 */
	public SessionEvent(Session session, IActionDescription action, Object source) {
		if (session != null) {
			session.addToSessionHistory(this);
			this.sessionId = session.getSessionId();
		}
		
		this.session = session;
		this.action = action;
		this.source = source;
	}

	/**
	 * Constructor to set the session and action of a session event
	 * corresponding to an action request.
	 * @param source the action request.
	 */
	public SessionEvent(DoAction source) {
		this(null, null, source);
		if (source != null) {
			this.session = source.getSession();
			if (this.session != null) {
				this.sessionId = this.session.getSessionId();
			}
			this.action = source.getAction();
		}
	}

	/**
	 * Gets the action description for this session event.
	 * @return the action
	 */
	public IActionDescription getAction() {
		return action;
	}

	/**
	 * Sets the action description for this session event.
	 * @param action
	 *            the action to set
	 */
	public void setAction(IActionDescription action) {
		this.action = action;
	}

	/**
	 * Gets the object which creates this session event.
	 * @return the source
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * Sets the object which creates this session event.
	 * @param source
	 *            the source to set
	 */
	public void setSource(Object source) {
		this.source = source;
	}

	/**
	 * Gets the session of this session event.
	 * @return the session
	 */
	public Session getSession() {
		return session;
	}

//	/**
//	 * @param session
//	 *            the session to set
//	 */
//	public void setSession(Session session) {
//		this.session = session;
//		if (session != null)
//			this.sessionId = session.getSessionId();
//	}

	/**
	 * Gets the session Id of this session event.
	 * @return the session id of the session this event belongs to
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets the session Id of this session event.
	 * @param id
	 *            sets the id of the session
	 */
	public void setSessionId(String id) {
		this.sessionId = id;
	}

	/**
	 * Gets meta information about this session event.
	 * @return the meta data
	 */
	public Object getMetaData() {
		return metaData;
	}

	/**
	 * Sets meta information about this session event.
	 * @param metaData the meta data
	 */
	public void setMetaData(Object metaData) {
		this.metaData = metaData;
	}

}
