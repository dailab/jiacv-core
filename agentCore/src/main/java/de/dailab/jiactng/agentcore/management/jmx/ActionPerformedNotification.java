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

	/** The description of the performed action. */
	private String _action;

	/** The duration of action execution in nanoseconds. */
	private long _duration;

	/**
	 * Constructs a notification about a performed action by an agent.
	 * @param source The notification producer, that is the execution cycle of an agent which has performed the action.
	 * @param sequenceNumber The notification sequence number within the source object.
	 * @param timeStamp The date at which the notification is being sent.
	 * @param msg A String containing the message of the notification.
	 * @param action The performed action.
	 * @param duration The duration of action execution in nanoseconds.
	 */
	public ActionPerformedNotification(Object source, long sequenceNumber,
			long timeStamp, String msg, DoAction action, long duration) {
		super(ACTION_PERFORMED, source, sequenceNumber, timeStamp, msg);
		_actionName = action.getAction().getName();
		_agentbeanName = ((Action)action.getAction()).getProviderBean().getBeanName();
		_action = action.toString();
		_duration = duration;
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
	 * Gets the description of the performed action.
	 * @return The description of the performed action.
	 * @see Object#toString()
	 */
	public String getAction() {
		return _action;
	}

	/**
	 * Gets the duration of the action execution.
	 * @return The duration of action execution in nanoseconds.
	 */
	public long getDuration() {
		return _duration;
	}

}
