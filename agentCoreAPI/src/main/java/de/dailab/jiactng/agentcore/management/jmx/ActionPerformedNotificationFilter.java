package de.dailab.jiactng.agentcore.management.jmx;

import java.util.Vector;

import javax.management.Notification;
import javax.management.NotificationFilter;

/**
 * This class implements the <code>NotificationFilter</code> interface for the 
 * <code>ActionPerformedNotification</code>. The filtering is performed on the 
 * name of the performed action, the name of the executing agent bean and the
 * state of the action execution.
 *
 * It manages a list of enabled action names and agent bean names. Methods allow 
 * users to enable/disable as many action names and agent bean names as required
 * as well as all invoked, started, successful and failed actions. 
 *  
 * @author Jan Keiser
 */
public class ActionPerformedNotificationFilter implements NotificationFilter {

	private static final long serialVersionUID = 1L;

	/** Indicates if the action list contains the enabled or disabled actions. */
	private boolean _actionsEnabled = false;

	/** The names of enabled or disabled actions. */
	private Vector<String> _actions = new Vector<String>();

	/** Indicates if the agent bean list contains the enabled or disabled agent beans. */
	private boolean _agentbeansEnabled = false;

	/** The names of enabled or disabled agent beans. */
	private Vector<String> _agentbeans = new Vector<String>();

	/** Indicates if notifications will be sent for invoked actions. */
	private boolean _invokedEnabled = true;

	/** Indicates if notifications will be sent for started actions. */
	private boolean _startedEnabled = true;

	/** Indicates if notifications will be sent for successful actions. */
	private boolean _successEnabled = true;

	/** Indicates if notifications will be sent for failed actions. */
	private boolean _failedEnabled = true;

	/**
	 * Invoked before sending the specified notification to the listener.
	 * This filter compares the action name and agent bean name of the specified 
	 * action performed notification with each enabled action name and agent bean 
	 * name. If the action name equals one of the enabled action names, the 
	 * agent bean name equals one of the enabled agent bean names and the state
	 * type of the action execution is enabled, then the notification 
	 * must be sent to the listener and this method returns <code>true</code>.
	 * @param notification The action performed notification to be sent.
	 * @return <code>true</code> if the notification has to be sent to the listener, 
	 * <code>false</code> otherwise.
	 */
	@Override
	public boolean isNotificationEnabled(Notification notification) {
		// check notification type
		if (!notification.getType().equals(ActionPerformedNotification.ACTION_PERFORMED)) {
			return false;
		}

		// check state of action
		DoActionState state = ((ActionPerformedNotification) notification).getState();
		if (state.equals(DoActionState.invoked) && !_invokedEnabled) {
			return false;
		}
		if (state.equals(DoActionState.started) && !_startedEnabled) {
			return false;
		}
		if (state.equals(DoActionState.success) && !_successEnabled) {
			return false;
		}
		if (state.equals(DoActionState.failed) && !_failedEnabled) {
			return false;
		}

		// get action name and agent bean name
		String actionName;
		String agentbeanName;
		try {
			ActionPerformedNotification n = (ActionPerformedNotification) notification;
			actionName = n.getActionName();
			agentbeanName = n.getAgentbeanName();
		}
		catch (Exception e) {
			return false;
		}
		if ((actionName == null) || (agentbeanName == null)) {
			return false;
		}

		// check action name
		if (_actionsEnabled) {
			if (!_actions.contains(actionName)) {
				return false;
			}
		}
		else {
			if (_actions.contains(actionName)) {
				return false;
			}
		}
		
		// check agent bean name
		if (_agentbeansEnabled) {
			if (!_agentbeans.contains(agentbeanName)) {
				return false;
			}
		}
		else {
			if (_agentbeans.contains(agentbeanName)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Enables all the action performed notifications the action name of which 
	 * equals the specified name to be sent to the listener. If the specified 
	 * name is already in the list of enabled action names, this method has no effect.
	 * @param name The action name.
	 * @throws IllegalArgumentException The action name parameter is null.
	 */
	public void enableAction(String name) throws IllegalArgumentException {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		if (_actionsEnabled) {
			// add to enabled actions
			if (!_actions.contains(name)) {
				_actions.add(name);
			}
		}
		else {
			// remove from disabled actions
			_actions.remove(name);
		}
	}

	/**
	 * Disables all the action performed notifications the action name of which 
	 * equals the specified action name to be sent to the listener. If the 
	 * specified name is not in the list of enabled action names, this method 
	 * has no effect.
	 * @param name The action name.
	 */
	public void disableAction(String name) {
		if (name == null) {
			return;
		}
		if (_actionsEnabled) {
			// remove enabled actions
			_actions.remove(name);
		}
		else {
			// add to disabled actions
			if (!_actions.contains(name)) {
				_actions.add(name);
			}
		}
	}

	/**
	 * Disables all the action names.
	 */
	public void disableAllActions() {
		_actionsEnabled = true;
		_actions.removeAllElements();
	}

	/**
	 * Enables all the action performed notifications the agent bean name of which 
	 * equals the specified name to be sent to the listener. If the specified 
	 * name is already in the list of enabled agent bean names, this method has no effect.
	 * @param name The agent bean name.
	 * @throws IllegalArgumentException The agent bean name parameter is null.
	 */
	public void enableAgentbean(String name) throws IllegalArgumentException {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		if (_agentbeansEnabled) {
			// add to enabled agent beans
			if (!_agentbeans.contains(name)) {
				_agentbeans.add(name);
			}
		}
		else {
			// remove from disabled agent beans
			_agentbeans.remove(name);
		}
	}

	/**
	 * Disables all the action performed notifications the agent bean name of which 
	 * equals the specified agent bean name to be sent to the listener. If the 
	 * specified name is not in the list of enabled agent bean names, this method 
	 * has no effect.
	 * @param name The agent bean name.
	 */
	public void disableAgentbean(String name) {
		if (name == null) {
			return;
		}
		if (_agentbeansEnabled) {
			// remove enabled agent beans
			_agentbeans.remove(name);
		}
		else {
			// add to disabled agent beans
			if (!_agentbeans.contains(name)) {
				_agentbeans.add(name);
			}
		}
	}

	/**
	 * Disables all the agent bean names.
	 */
	public void disableAllAgentbeans() {
		_agentbeansEnabled = true;
		_agentbeans.removeAllElements();
	}

	/**
	 * Disables all invoked action executions.
	 */
	public void disableInvokedActions() {
		_invokedEnabled = false;
	}

	/**
	 * Disables all started action executions.
	 */
	public void disableStartedActions() {
		_startedEnabled = false;
	}

	/**
	 * Disables all successful action executions.
	 */
	public void disableSuccessfulActions() {
		_successEnabled = false;
	}

	/**
	 * Disables all failed action executions.
	 */
	public void disableFailedActions() {
		_failedEnabled = false;
	}
}
