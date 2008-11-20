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
/**
 * @author moekon
 * 
 */
/**
 * @author moekon
 *
 */
/**
 * @author moekon
 * 
 */
public class Session implements IFact {

  /** SerialVersionUID for Serialization */
  private static final long        serialVersionUID = 8699173523554827559L;

  /** The actual ID of the session. */
  private String                   sessionId;

  /** The creation time of the session object. */
  private Long                     creationTime;

  /**
   * Optional attribute for timeout conditions, it only is relevant when the
   * session is written into the memory Default value is 60 seconds
   */
  private Long                     timeToLive       = Long.valueOf(60000);

  /**
   * The token of the user that originally triggered this session.
   */
  private String                   userToken     = null;

  /**
   * The id of the user that originally triggered this session.
   */
  private String                   originalUser = null;

  /**
   * The id of the provider that offeres the top-level service that was invoked
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

  /**
   * Constructor for a new session. Can be called with any kind of object as
   * source, usually the class, that initiated the session.
   * 
   * @param source
   *          the source that created the new session
   */
  public Session(ResultReceiver source) {
    this(IdFactory.createSessionId((source != null) ? source.hashCode() : Session.class.hashCode()), System
        .currentTimeMillis(), source, new ArrayList<SessionEvent>());
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
    this(IdFactory.createSessionId((source != null) ? source.hashCode() : Session.class.hashCode()), new Long(System
        .currentTimeMillis()), source, new ArrayList<SessionEvent>());
    this.timeToLive = new Long(timeToLive);
  }

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
    if (timeToLive != 0) {
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
  public String getSessionId() {
    return sessionId;
  }

  /**
   * Sets an optional timeout attribute. If not set the default value is 60.000
   * milliseconds Used only when session is written into agents memory
   * 
   * @param timeout
   */
  public void setTimeToLive(Long timeout) {
    timeToLive = timeout;
  }

  public Long getTimeToLive() {
    return timeToLive;
  }

  /**
   * @param id
   *          the sessionId to set
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
   *          the source to set
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

  public boolean isTimeout() {
    return (System.currentTimeMillis() > (creationTime.longValue() + timeToLive.longValue()));
  }

  /**
   * @param creationTime
   *          the creationTime to set
   */
  public void setCreationTime(long creationTime) {
    this.creationTime = Long.valueOf(creationTime);
  }

  /**
   * Getter for the history of this Session-object
   * 
   * @return an Arraylist containing the history.
   */
  public ArrayList<SessionEvent> getHistory() {
    ArrayList<SessionEvent> copy = new ArrayList<SessionEvent>();
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
   *          the index of the object to remove
   * @return the removed object
   */
  public SessionEvent removeFromSessionHistory(int index) {
    return history.remove(index);
  }

  /**
   * Will remove the given object from the history if present.
   * 
   * @param o
   *          the object to remove
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

  public int hashCode() {
    return this.sessionId.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Session))
      return false;
    Session other = (Session) obj;
    if (this.sessionId == null || other.sessionId == null)
      return true;
    else
      return this.sessionId.equals(other.sessionId);
  }

  /**
   * Getter for the token of the user that originally invoked this session.
   * 
   * @return The token of the original user.
   */
  public String getUserToken() {
    return this.userToken;
  }

  /**
   * Setter for the token of the user that originally invoked this session.
   * 
   * @param userToken
   *          The token of the original user.
   */
  public void setUserToken(String userToken) {
    this.userToken = userToken;
  }

  /**
   * Getter for the provider of the service that was originally invoked by this
   * session.
   * 
   * @return The user-id of the provider.
   */
  public String getOriginalProvider() {
    return this.originalProvider;
  }

  /**
   * Setter for the provider of the service that was originally invoked by this
   * session.
   * 
   * @param originalProvider
   *          The user-id of the provider.
   */
  public void setOriginalProvider(String originalProvider) {
    this.originalProvider = originalProvider;
  }

  /**
   * Setter for the service that was originally invoked by this session.
   * 
   * @return The name of the original service.
   */
  public String getOriginalService() {
    return this.originalService;
  }

  /**
   * Setter for the service that was originally invoked by this session.
   * 
   * @param originalService
   *          The name of the original service.
   */
  public void setOriginalService(String originalService) {
    this.originalService = originalService;
  }

  public String getOriginalUser() {
    return this.originalUser;
  }

  public void setOriginalUser(String originalUser) {
    this.originalUser = originalUser;
  }
}
