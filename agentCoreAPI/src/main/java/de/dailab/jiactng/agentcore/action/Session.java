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
  private static final long        serialVersionUID = 8699173523554827559L;

  /** The default value for timeout of sessions is 60,000 milliseconds. */
  public static final long DEFAULT_TIMETOLIVE = 60000;

  /** The actual ID of the session. */
  private String                   sessionId;

  /** The creation time of the session object. */
  private Long                     creationTime;

  /**
   * Optional attribute for timeout conditions, it only is relevant when the
   * session is written into the memory. It is initialized with the default value.
   * @see Session#DEFAULT_TIMETOLIVE
   */
  private Long                     timeToLive       = Long.valueOf(DEFAULT_TIMETOLIVE);

  /**
   * The token of the user that originally triggered this session.
   */
  private String                   userToken     = null;

  /**
   * The id of the user that originally triggered this session.
   */
  private String                   originalUser = null;

  /**
   * The id of the provider that offers the top-level service that was invoked
   * for this session. This field should always hold this top-level provider and
   * not be changed by sub-service invocations.
   */
  private String                   originalProvider = null;
  
  /**
   * The top level service that was invoked for this session. This field should
   * always hold this top-level service and not be changed by sub-service
   * invocations.
   */
  private String                   originalService  = null;

  /** Stores the reference to the creator of this session. */
  /*
   * FIXME: this source is transient now... examine whether this provides issues ->
   * remember that session objects are serialised in the Remote* classes!
   */
  private transient ResultReceiver source;

  /**
   * The history of the session. This list contains all sources of the session
   * (i.E. of actions, results, etc.) in order of appearance. TODO should be of
   * type ArrayList<SessionEvent>
   */
  private ArrayList<SessionEvent>  history;

  private transient Integer currentCallDepth = null;
  
  /**
   * Constructor for a new session. Can be called with any kind of object as
   * source, usually the class, that initiated the session.
   * 
   * @param source
   *          the source that created the new session
   */
  public Session(ResultReceiver source) {
    this(IdFactory.createSessionId((source != null) ? source.hashCode() : Session.class.hashCode()), Long.valueOf(System
        .currentTimeMillis()), source, new ArrayList<SessionEvent>());
  }

  /**
   * Constructor for a new session. Can be called with any kind of object as
   * source, usually the class, that initiated the session.
   * 
   * @param source
   *          the source that created the new session
   * @param timeToLive
   *          time to live in milliseconds
   */
  public Session(ResultReceiver source, long timeToLive) {
    this(IdFactory.createSessionId((source != null) ? source.hashCode() : Session.class.hashCode()), Long.valueOf(System
        .currentTimeMillis()), source, new ArrayList<SessionEvent>());
    this.timeToLive = Long.valueOf(timeToLive);
  }

  /**
   * Constructs an empty session.
   */
  public Session() {
    this.sessionId = null;
    this.creationTime = null;
    this.history = null;
    this.source = null;
    this.timeToLive = null;
  }

  /**
   * Constructor to set all values of session by hand.
   * 
   * @param id
   *          the session sessionId
   * @param creationTime
   *          the time of creation of this session
   * @param source
   *          the creator object of this session
   * @param history
   *          the history of this session
   * @param timeToLive
   *          time until timeout if 0 it will be set to null
   * 
   */
  public Session(String id, Long creationTime, ResultReceiver source, ArrayList<SessionEvent> history, Long timeToLive) {
    this.sessionId = id;
    this.creationTime = creationTime;
    this.source = source;
    this.history = history;
    if (timeToLive != null && timeToLive.longValue() > 0) {
      this.timeToLive = timeToLive;
    } else {
      this.timeToLive = null;
    }
  }

  /**
   * Constructor to set all values of session by hand.
   * 
   * @param id
   *          the session sessionId
   * @param creationTime
   *          the time of creation of this session
   * @param source
   *          the creator object of this session
   * @param history
   *          the history of this session
   * 
   * Note: TimeToLive will keep at default of 60 seconds
   * 
   */
  public Session(String id, Long creationTime, ResultReceiver source, ArrayList<SessionEvent> history) {
    this.sessionId = id;
    this.creationTime = creationTime;
    this.source = source;
    this.history = history;
  }

  /**
   * Copying constructor. This session will be initiated with the values of the
   * parameter <code>session</code>.
   * 
   * @param session
   *          the session to copy the values from
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
  public final String getSessionId() {
    return sessionId;
  }

  /**
   * Sets an optional timeout attribute. If not set the default value is 60.000
   * milliseconds. Used only when session is written into agents memory.
   * 
   * @param timeout the timeout value
   */
  public final void setTimeToLive(Long timeout) {
    timeToLive = timeout;
  }

  /**
   * Gets the timeout attribute. The default value is 60.000 milliseconds.
   * @return the timeout value
   */
  public final Long getTimeToLive() {
    return timeToLive;
  }

  /**
   * Sets the unique Id of this session.
   * @param id
   *          the sessionId to set
   */
  public final void setSessionId(String id) {
    this.sessionId = id;
  }

  /**
   * Gets the receiver of the session result.
   * @return the source
   */
  public final ResultReceiver getSource() {
    return source;
  }

  /**
   * Sets the receiver of the session result.
   * @param newSource
   *          the source to set
   */
  public final void setSource(ResultReceiver newSource) {
    source = newSource;
  }

  /**
   * Gets the time when the session was created.
   * @return the creationTime
   */
  public final Long getCreationTime() {
    return creationTime;
  }

  /**
   * Checks if the session has timed out.
   * @return <code>true</code> if the timeout was reached.
   */
  public final boolean isTimeout() {
    return (System.currentTimeMillis() > (creationTime.longValue() + timeToLive.longValue()));
  }

  /**
   * Sets the time when the session was created.
   * @param newCreationTime
   *          the creationTime to set
   */
  public final void setCreationTime(long newCreationTime) {
    creationTime = Long.valueOf(newCreationTime);
  }

  /**
   * Getter for the history of this Session-object
   * 
   * @return a list containing the history.
   */
  public final ArrayList<SessionEvent> getHistory() {
    final ArrayList<SessionEvent> copy = new ArrayList<SessionEvent>();
    copy.addAll(history);
    return copy;
  }

  // /**
  // * @param history
  // * the history to set
  // */
  // public void setHistory(ArrayList<SessionEvent> history) {
  // this.history = history;
  // }

  /**
   * Will add the given object to the session history.
   * 
   * @param event
   *          the object to add to history
   */
  public final void addToSessionHistory(SessionEvent event) {
    if (history == null) {
      history = new ArrayList<SessionEvent>();
    }
    history.add(event);
  }

  /**
   * Will remove the object indexed by the given number from the history.
   * 
   * @param index
   *          the index of the object to remove
   * @return the removed object
   */
  public final SessionEvent removeFromSessionHistory(int index) {
    return history.remove(index);
  }

  /**
   * Will remove the given object from the history if present.
   * 
   * @param o
   *          the object to remove
   * @return true, if history contained the given object
   */
  public final boolean removeFromSessionHistory(SessionEvent o) {
    return history.remove(o);
  }

  /**
   * Returns a multiline text which contains the ID, the creation time, 
   * the source, and the history of the session.
   * @return a string representation of the session
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    // sessionId
    builder.append("Session:\n sessionId=");
    if (sessionId != null) {
      builder.append("'").append(sessionId).append("'");
    } else {
      builder.append("null");
    }

    // time
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(creationTime != null ? creationTime.longValue() : 0L);
    builder.append("\n created='").append(calendar.getTime().toString()).append("'");

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

  /**
   * Returns the hash code of the session's ID.
   * @return the hash code of the session ID
   */
  @Override
  public int hashCode() {
    return sessionId.hashCode();
  }

  /**
   * Checks the equality of two sessions. The sessions are equal
   * if their session IDs are equal or null.
   * @param obj the other session
   * @return the result of the equality check
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Session)) {
      return false;
    }
    final Session other = (Session) obj;
    if (this.sessionId == null || other.sessionId == null) {
      return true;
    } else {
      return this.sessionId.equals(other.sessionId);
    }
  }

  /**
   * Getter for the token of the user that originally invoked this session.
   * 
   * @return The token of the original user.
   */
  public final String getUserToken() {
    return userToken;
  }

  /**
   * Setter for the token of the user that originally invoked this session.
   * 
   * @param newUserToken
   *          The token of the original user.
   */
  public final void setUserToken(String newUserToken) {
    userToken = newUserToken;
  }

  /**
   * Getter for the provider of the service that was originally invoked by this
   * session.
   * 
   * @return The user-id of the provider.
   */
  public final String getOriginalProvider() {
    return originalProvider;
  }

  /**
   * Setter for the provider of the service that was originally invoked by this
   * session.
   * 
   * @param newOriginalProvider
   *          The user-id of the provider.
   */
  public final void setOriginalProvider(String newOriginalProvider) {
    originalProvider = newOriginalProvider;
  }

  /**
   * Setter for the service that was originally invoked by this session.
   * 
   * @return The name of the original service.
   */
  public final String getOriginalService() {
    return originalService;
  }

  /**
   * Setter for the service that was originally invoked by this session.
   * 
   * @param newOriginalService
   *          The name of the original service.
   */
  public final void setOriginalService(String newOriginalService) {
    originalService = newOriginalService;
  }

  /**
   * Gets the user which creates the root session of the parent hierarchy.
   * @return the original user of this session.
   */
  public final String getOriginalUser() {
    return originalUser;
  }

  /**
   * Sets the user which creates the root session of the parent hierarchy.
   * @param newOriginalUser the original user of this session.
   */
  public final void setOriginalUser(String newOriginalUser) {
    originalUser = newOriginalUser;
  }

  /**
   * Gets the depth of the parent hierarchy.
   * @return the session call depth.
   */
  public final Integer getCurrentCallDepth() {
    return currentCallDepth;
  }

  /**
   * Sets the depth of the parent hierarchy.
   * @param newCurrentCallDepth the session call depth.
   */
  public final void setCurrentCallDepth(int newCurrentCallDepth) {
    currentCallDepth = Integer.valueOf(newCurrentCallDepth);
  }
}
