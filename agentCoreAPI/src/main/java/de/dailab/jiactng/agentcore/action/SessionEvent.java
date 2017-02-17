package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * This class is thought to be super class of all session events. Every session
 * event belongs to a session and to an action which it will trigger or be
 * result of.
 * 
 * @author axle
 * 
 * @param<T>	type of the source of the event
 */
public class SessionEvent<T> implements IFact {

	/** SerialVersionUID for Serialization */
	private static final long serialVersionUID = 2754758742968423185L;

	/** The session of the session this session event belongs to. */
	private Session session;

	/** Redundant for the sake of SimpleSpace. */
	private String sessionId;

	/** The action this session event will trigger or be result of. */
	private IActionDescription action;

	/** The object that created this event. */
	private transient T source;

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
	public SessionEvent(Session session, IActionDescription action, T source) {
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
	 * 
	 * XXX shouldn't this be moved to ActionResult and this class be made abstract?
	 * 
	 * @param source the action request.
	 */
	public SessionEvent(T source) {
		this(null, null, source);
		if (source instanceof DoAction) {
			this.session = ((DoAction) source).getSession();
			if (this.session != null) {
				this.sessionId = this.session.getSessionId();
			}
			this.action = ((DoAction) source).getAction();
		}
	}

	/**
	 * Gets the action description for this session event.
	 * @return the action
	 */
	public final IActionDescription getAction() {
		return action;
	}

	/**
	 * Sets the action description for this session event.
	 * @param newAction
	 *            the action to set
	 */
	public final void setAction(IActionDescription newAction) {
		action = newAction;
	}

	/**
	 * Gets the object which creates this session event.
	 * @return the source
	 */
	public final T getSource() {
		return source;
	}

	/**
	 * Sets the object which creates this session event.
	 * @param newSource
	 *            the source to set
	 */
	public final void setSource(T newSource) {
		source = newSource;
	}

	/**
	 * Gets the session of this session event.
	 * @return the session
	 */
	public final Session getSession() {
		return session;
	}

//	/**
//	 * @param session
//	 *            the session to set
//	 */
//	final public void setSession(Session session) {
//		this.session = session;
//		if (session != null)
//			this.sessionId = session.getSessionId();
//	}

	/**
	 * Gets the session Id of this session event.
	 * @return the session id of the session this event belongs to
	 */
	public final String getSessionId() {
		return sessionId;
	}

	/**
	 * Sets the session Id of this session event.
	 * @param id
	 *            sets the id of the session
	 */
	public final void setSessionId(String id) {
		this.sessionId = id;
	}

	/**
	 * Gets meta information about this session event.
	 * @return the meta data
	 */
	public final Object getMetaData() {
		return metaData;
	}

	/**
	 * Sets meta information about this session event.
	 * @param newMetaData the meta data
	 */
	public final void setMetaData(Object newMetaData) {
		metaData = newMetaData;
	}

}
