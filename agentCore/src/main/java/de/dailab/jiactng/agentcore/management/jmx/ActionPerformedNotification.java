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

	/** The description of the action result. */
	private Object[] _actionResult;

	/** The state of action execution. */
	private DoActionState _state;

	/** The id of the user that originally triggered the session of this action execution. */
	private String _originalUser;

	/** The id of the provider that offers the top-level service that was invoked for the session of this action execution. */
	private String _originalProvider;

	/** The top-level service that was invoked for the session of this action execution. */
	private String _originalService;

	/**
	 * Constructs a notification about a performed action by an agent.
	 * @param source The notification producer, that is the execution cycle of an agent which has performed the action.
	 * @param sequenceNumber The notification sequence number within the source object.
	 * @param timeStamp The date at which the notification is being sent.
	 * @param msg A String containing the message of the notification.
	 * @param action The performed action.
	 * @param state The state of the action execution.
	 * @param result The result or failure of the action or <code>null</code> if the execution is not yet finished.
	 */
	public ActionPerformedNotification(Object source, long sequenceNumber,
			long timeStamp, String msg, DoAction action, DoActionState state, Object[] result) {
		super(ACTION_PERFORMED, source, sequenceNumber, timeStamp, msg);
		_actionName = action.getAction().getName();
		if(((Action)action.getAction()).getProviderBean() == null) {
		  System.err.println("\n-- "+((Action)action.getAction()).getName()+" / "+((Action)action.getAction()).getProviderBean());
		  _agentbeanName = "null";
		} else {
		  _agentbeanName = ((Action)action.getAction()).getProviderBean().getBeanName();
		}
		_sessionId = action.getSessionId();
		_owner = action.getOwner();
		_actionType = action.getAction().getClass().getSimpleName();
		_action = action.toString();
		_state = state;
		if (action.getSession() != null) {
			_originalUser = action.getSession().getOriginalUser();
			_originalProvider = action.getSession().getOriginalProvider();
			_originalService = action.getSession().getOriginalService();
		}

		// extract parameters
		Object[] params = action.getParams();
		int paramSize = params.length;
		_actionParameters = new Object[paramSize];
		for (int i=0; i<paramSize; i++) {
			try {
				_actionParameters[i] = ((JmxDescriptionSupport)params[i]).getDescription();
			} catch (Exception e) {
				_actionParameters[i] = params[i].toString();
			}
		}

		// extract result
		if (result != null) {
			int resultSize = result.length;
			_actionResult = new Object[resultSize];
			for (int i=0; i<resultSize; i++) {
				try {
					_actionResult[i] = ((JmxDescriptionSupport)result[i]).getDescription();
				} catch (Exception e) {
					_actionResult[i] = result[i].toString();
				}
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
	 * Gets a description of the action result or failure.
	 * @return The array of action result descriptions or <code>null</code> if the action is not yet finished. 
	 * The representation of each result is either a string or based on JMX open types.
	 * @see Object#toString()
	 * @see JmxDescriptionSupport#getDescription()
	 */
	public Object[] getActionResult() {
		return _actionResult;
	}

	/**
	 * Gets the state of the action execution.
	 * @return The state of action execution.
	 */
	public DoActionState getState() {
		return _state;
	}

	/**
	 * Gets the id of the user that originally triggered the session of this action execution.
	 * @return The user id or <code>null</code>.
	 */
	public String getOriginalUser() {
		return _originalUser;
	}

	/**
	 * Gets the id of the provider that offers the top-level service that was invoked for the 
	 * session of this action execution.
	 * @return The provider id or <code>null</code>.
	 */
	public String getOriginalProvider() {
		return _originalProvider;
	}

	/**
	 * Gets the top-level service that was invoked for the session of this action execution.
	 * @return The service id or <code>null</code>.
	 */
	public String getOriginalService() {
		return _originalService;
	}

	@Override
	public boolean equals(Object obj) {
		// check type
		if ((obj == null) || !(obj instanceof ActionPerformedNotification)) {
			return false;
		}
		ActionPerformedNotification n = (ActionPerformedNotification) obj;

		// check agent
		if (((source == null) && (n.source != null)) ||
				((source != null) && !source.equals(n.source))) {
			return false;
		}

		// check actionName
		if (((_actionName == null) && (n._actionName != null)) ||
				((_actionName != null) && !_actionName.equals(n._actionName))) {
			return false;
		}

		// check agentbeanName
		if (((_agentbeanName == null) && (n._agentbeanName != null)) ||
				((_agentbeanName != null) && !_agentbeanName.equals(n._agentbeanName))) {
			return false;
		}

		// check sessionId
		if (((_sessionId == null) && (n._sessionId != null)) ||
				((_sessionId != null) && !_sessionId.equals(n._sessionId))) {
			return false;
		}

		// check owner
		if (((_owner == null) && (n._owner != null)) ||
				((_owner != null) && !_owner.equals(n._owner))) {
			return false;
		}

		// check actionType
		if (((_actionType == null) && (n._actionType != null)) ||
				((_actionType != null) && !_actionType.equals(n._actionType))) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int prim = 31;
		int hash = prim + (source == null ? 0 : source.hashCode());
		hash = prim * hash + (_actionName == null ? 0 : _actionName.hashCode());
		hash = prim * hash + (_agentbeanName == null ? 0 : _agentbeanName.hashCode());
		hash = prim * hash + (_sessionId == null ? 0 : _sessionId.hashCode());
		hash = prim * hash + (_owner == null ? 0 : _owner.hashCode());
		hash = prim * hash + (_actionType == null ? 0 : _actionType.hashCode());
		return hash;
	}

}
