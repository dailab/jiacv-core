package de.dailab.jiactng.agentcore.management.jmx;

import javax.management.Notification;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;

/**
 * This class represents a JMX-compliant notification about a performed
 * action by an agent.
 * @author Jan Keiser
 */
public class ActionPerformedNotification extends Notification {

	private static final long serialVersionUID = 1L;

	/** Notification type which indicates that an action was performed. */
	public static String ACTION_PERFORMED = "jiactng.action.perform";

	/** The name of the performed action. */
	private String _actionName;

	/** The name of the agent bean which has performed the action. */
	private String _agentbeanName;

	/** The session id of the action execution. */
	private String _sessionId;

	/** The name of the owner which has invoked the action. */
	private String _owner;

	/** The type of the performed action. */
	private String _actionType;

	/** The description of the performed action. */
	private String _action;

	/** The description of the action parameters. */
	private Object[] _actionParameters;

	/** The duration of action execution in nanoseconds. */
	private long _duration;

	/** The success of action execution. */
	private boolean _success;

	/**
	 * Constructs a notification about a performed action by an agent.
	 * @param source The notification producer, that is the execution cycle of an agent which has performed the action.
	 * @param sequenceNumber The notification sequence number within the source object.
	 * @param timeStamp The date at which the notification is being sent.
	 * @param msg A String containing the message of the notification.
	 * @param action The performed action.
	 * @param duration The duration of action execution in nanoseconds.
	 * @param success The success of the action execution.
	 */
	public ActionPerformedNotification(Object source, long sequenceNumber,
			long timeStamp, String msg, DoAction action, long duration, boolean success) {
		super(ACTION_PERFORMED, source, sequenceNumber, timeStamp, msg);
		_actionName = action.getAction().getName();
		_agentbeanName = ((Action)action.getAction()).getProviderBean().getBeanName();
		_sessionId = action.getSessionId();
		_owner = action.getOwner();
		_actionType = action.getAction().getClass().getSimpleName();
		_action = action.toString();
		_duration = duration;
		_success = success;
		
		// extract parameters
		Object[] params = action.getParams();
		int size = params.length;
		_actionParameters = new Object[size];
		for (int i=0; i<size; i++) {
			try {
				_actionParameters[i] = ((JmxDescriptionSupport)params[i]).getDescription();
			} catch (Exception e) {
				_actionParameters[i] = params[i].toString();
			}
		}
	}

	/**
	 * Gets the name of the performed action.
	 * @return The name of the performed action.
	 */
	public String getActionName() {
		return _actionName;
	}

	/**
	 * Gets the name of the agent bean which has performed the action.
	 * @return The name of the agent bean.
	 */
	public String getAgentbeanName() {
		return _agentbeanName;
	}

	/**
	 * Gets the session id of the action execution.
	 * @return The session id of the action execution.
	 */
	public String getSessionId() {
		return _sessionId;
	}

	/**
	 * Gets the owner of the action-invocation.
	 * @return The name of the owner.
	 */
	public String getOwner() {
		return _owner;
	}

	/**
	 * Gets the type of the performed action.
	 * @return The simple class name of the action object.
	 * @see Class#getSimpleName()
	 */
	public String getActionType() {
		return _actionType;
	}

	/**
	 * Gets the description of the performed action.
	 * @return The description of the performed action.
	 * @see Object#toString()
	 */
	public String getAction() {
		return _action;
	}

	/**
	 * Gets the description of the action parameters.
	 * @return The array of action parameter descriptions. The representation of each parameter is 
	 * either a string or based on JMX open types.
	 * @see Object#toString()
	 * @see JmxDescriptionSupport#getDescription()
	 */
	public Object[] getActionParameters() {
		return _actionParameters;
	}

	/**
	 * Gets the duration of the action execution.
	 * @return The duration of action execution in nanoseconds.
	 */
	public long getDuration() {
		return _duration;
	}

	/**
	 * Gets the success of the action execution.
	 * @return The success of action execution.
	 */
	public boolean getSuccess() {
		return _success;
	}

}
