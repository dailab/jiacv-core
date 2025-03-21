package de.dailab.jiactng.agentcore.management.jmx;

import java.util.Arrays;

import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * This class represents a JMX-compliant notification about a performed
 * action by an agent.
 * @author Jan Keiser
 */
public class ActionPerformedNotification extends Notification {

	private static final long serialVersionUID = 1L;

	/** Notification type which indicates that an action was performed. */
	public static final String ACTION_PERFORMED = "jiactng.action.perform";

	/** The name of the performed action. */
	private String actionName;

	/** The name of the agent bean which has performed the action. */
	private String agentbeanName;

	/** The session id of the action execution. */
	private String sessionId;

	/** The name of the owner which has invoked the action. */
	private String owner;

	/** The type of the performed action. */
	private String actionType;

	/** The description of the action parameters. */
	private Object[] actionParameters;

	/** The description of the action result. */
	private Object[] actionResult;

	/** The state of action execution. */
	private DoActionState state;

	/** The id of the user that originally triggered the session of this action execution. */
	private String originalUser;

	/** The id of the provider that offers the top-level service that was invoked for the session of this action execution. */
	private String originalProvider;

	/** The top-level service that was invoked for the session of this action execution. */
	private String originalService;

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
		actionName = action.getAction().getName();
		if(((IActionDescription)action.getAction()).getProviderBean() == null) {
		  agentbeanName = "null";
		} else {
		  agentbeanName = ((IActionDescription)action.getAction()).getProviderBean().getBeanName();
		}
		sessionId = action.getSessionId();
		owner = action.getOwner();
		actionType = action.getAction().getClass().getSimpleName();
		this.state = state;
		if (action.getSession() != null) {
			originalUser = action.getSession().getOriginalUser();
			originalProvider = action.getSession().getOriginalProvider();
			originalService = action.getSession().getOriginalService();
		}

		// get parameters
		final Object[] params = action.getParams();
		final int paramSize = params.length;
		actionParameters = new Object[paramSize];

		// get result
		int resultSize = 0;
		if (result != null) {
			resultSize = result.length;
			actionResult = new Object[resultSize];
		}

		// create data based on JMX open types
		final int userDataSize = paramSize + resultSize + 2;
		String[] itemNames = new String[userDataSize];
		itemNames[0] = "name";
		itemNames[1] = "session";
		Object[] itemValues = new Object[userDataSize];
		itemValues[0] = actionName;
		itemValues[1] = sessionId;
		OpenType<?>[] itemTypes = new OpenType<?>[userDataSize];
		itemTypes[0] = SimpleType.STRING;
		itemTypes[1] = SimpleType.STRING;

		// extract parameters
		for (int i=0; i<paramSize; i++) {
			Object value;
			try {
				final JmxDescriptionSupport param = (JmxDescriptionSupport)params[i];
				value = param.getDescription();
				itemTypes[i+2] = param.getDescriptionType();
			} 
			catch (Exception e) {
				value = String.valueOf(params[i]);
				itemTypes[i+2] = SimpleType.STRING;
			}
			actionParameters[i] = value;
			itemNames[i+2] = "param" + (i+1);
			itemValues[i+2] = value;
		}

		// extract result
		for (int i=0; i<resultSize; i++) {
			Object value;
			try {
				final JmxDescriptionSupport res = (JmxDescriptionSupport)result[i];
				value = res.getDescription();
				itemTypes[i+paramSize+2] = res.getDescriptionType();
			} 
			catch (Exception e) {
				value = String.valueOf(result[i]);
				itemTypes[i+paramSize+2] = SimpleType.STRING;
			}
			actionResult[i] = value;
			itemNames[i+paramSize+2] = "result" + (i+1);
			itemValues[i+paramSize+2] = value;
		}

		// set user data
		try {
			final CompositeType type = new CompositeType((DoAction.class).getName(), 
					"performed action and result", itemNames, itemNames, itemTypes);
			final CompositeData data = new CompositeDataSupport(type, itemNames, itemValues);
			setUserData(data);
		}
		catch (OpenDataException e) {
			System.err.println("Unable to create open data for ActionPerformedNotification: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Gets the name of the performed action.
	 * @return The name of the performed action.
	 */
	public final String getActionName() {
		return actionName;
	}

	/**
	 * Gets the name of the agent bean which has performed the action.
	 * @return The name of the agent bean.
	 */
	public final String getAgentbeanName() {
		return agentbeanName;
	}

	/**
	 * Gets the session id of the action execution.
	 * @return The session id of the action execution.
	 */
	public final String getSessionId() {
		return sessionId;
	}

	/**
	 * Gets the owner of the action-invocation.
	 * @return The name of the owner.
	 */
	public final String getOwner() {
		return owner;
	}

	/**
	 * Gets the type of the performed action.
	 * @return The simple class name of the action object.
	 * @see Class#getSimpleName()
	 */
	public final String getActionType() {
		return actionType;
	}

	/**
	 * Gets the description of the action parameters.
	 * @return The array of action parameter descriptions. The representation of each parameter is 
	 * either a string or based on JMX open types.
	 * @see Object#toString()
	 * @see JmxDescriptionSupport#getDescription()
	 */
	public final Object[] getActionParameters() {
		return Arrays.copyOf(actionParameters, actionParameters.length);
	}

	/**
	 * Gets a description of the action result or failure.
	 * @return The array of action result descriptions or <code>null</code> if the action is not yet finished. 
	 * The representation of each result is either a string or based on JMX open types.
	 * @see Object#toString()
	 * @see JmxDescriptionSupport#getDescription()
	 */
	public final Object[] getActionResult() {
		if (actionResult != null) {
			return Arrays.copyOf(actionResult, actionResult.length);
		}
		return actionResult;
	}

	/**
	 * Gets the state of the action execution.
	 * @return The state of action execution.
	 */
	public final DoActionState getState() {
		return state;
	}

	/**
	 * Gets the id of the user that originally triggered the session of this action execution.
	 * @return The user id or <code>null</code>.
	 */
	public final String getOriginalUser() {
		return originalUser;
	}

	/**
	 * Gets the id of the provider that offers the top-level service that was invoked for the 
	 * session of this action execution.
	 * @return The provider id or <code>null</code>.
	 */
	public final String getOriginalProvider() {
		return originalProvider;
	}

	/**
	 * Gets the top-level service that was invoked for the session of this action execution.
	 * @return The service id or <code>null</code>.
	 */
	public final String getOriginalService() {
		return originalService;
	}

	  /**
	   * Checks the equality of two ActionPerformedNotifications. 
	   * The notifications are equal if their source, action name, 
	   * agent bean name, session ID, owner, and action type are equal.
	   * @param obj the other notification
	   * @return the result of the equality check
	   */
	@Override
	public boolean equals(Object obj) {
		// check type
		if ((obj == null) || !(obj instanceof ActionPerformedNotification)) {
			return false;
		}
		final ActionPerformedNotification n = (ActionPerformedNotification) obj;

		// check agent
		if (((source == null) && (n.source != null)) ||
				((source != null) && !source.equals(n.source))) {
			return false;
		}

		// check actionName
		if (((actionName == null) && (n.actionName != null)) ||
				((actionName != null) && !actionName.equals(n.actionName))) {
			return false;
		}

		// check agentbeanName
		if (((agentbeanName == null) && (n.agentbeanName != null)) ||
				((agentbeanName != null) && !agentbeanName.equals(n.agentbeanName))) {
			return false;
		}

		// check sessionId
		if (((sessionId == null) && (n.sessionId != null)) ||
				((sessionId != null) && !sessionId.equals(n.sessionId))) {
			return false;
		}

		// check owner
		if (((owner == null) && (n.owner != null)) ||
				((owner != null) && !owner.equals(n.owner))) {
			return false;
		}

		// check actionType
		if (((actionType == null) && (n.actionType != null)) ||
				((actionType != null) && !actionType.equals(n.actionType))) {
			return false;
		}

		return true;
	}

	  /**
	   * Calculates the hash code of the notification with consideration of
	   * source, action name, agent bean name, session ID, owner, and action type.
	   * @return the hash code of the notification
	   */
	@Override
	public int hashCode() {
		final int prim = 31;
		int hash = prim + (source == null ? 0 : source.hashCode());
		hash = prim * hash + (actionName == null ? 0 : actionName.hashCode());
		hash = prim * hash + (agentbeanName == null ? 0 : agentbeanName.hashCode());
		hash = prim * hash + (sessionId == null ? 0 : sessionId.hashCode());
		hash = prim * hash + (owner == null ? 0 : owner.hashCode());
		hash = prim * hash + (actionType == null ? 0 : actionType.hashCode());
		return hash;
	}

    /**
     * Returns a String representation of this notification.
     *
     * @return A String representation of this notification.
     */
    @Override
    public String toString() {
        return super.toString()+"[actionName="+actionName+"][actionType="+actionType+
        		"][sessionId="+sessionId+"][owner="+owner+"][state="+state+
        		"][agentbeanName="+agentbeanName+"][originalUser="+originalUser+
        		"][originalProvider="+originalProvider+"][originalService="+originalService+"]";
    }

}
