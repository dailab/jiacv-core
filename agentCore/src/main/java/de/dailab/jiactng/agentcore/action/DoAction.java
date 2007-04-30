package de.dailab.jiactng.agentcore.action;

/**
 * Used to submit an action for execution. An agent will check its memory
 * periodically for DoAction-objects, an it will call the approriate component
 * if such an object is encountered. Note that this class is also a subclass of
 * Session to make sure executed actions can be identified and tracked.
 * 
 * @see de.dailab.jiactng.agentcore.action.Action
 * @see de.dailab.jiactng.agentcore.action.Session
 * @see de.dailab.jiactng.agentcore.environment.IEffector
 * @author Thomas Konnerth
 * 
 */
public class DoAction extends Session {

	/** The action that shall be executed. */
	private Action thisAction;

	/** The input-parameters for the action-call */
	private Object[] params;

	/**
	 * Constructor for a new action-call. The created object should be written
	 * to the agents memory to trigger the action execution. Note that it may be
	 * usefull to store the session-id from this object after creation, as it
	 * will be your only method of retrieving the results of the action-call.
	 * 
	 * @param thisAction
	 *            the action that shall be called.
	 * @param source
	 *            the source of the action-call.
	 * @param params
	 *            the input-parameters for the call.
	 */
	public DoAction(Action thisAction, Object source, Object[] params) {
		super(source);
		this.thisAction = thisAction;
		this.params = params;
	}

	/**
	 * Getter for the input-parameters of the action-call.
	 * 
	 * @return an array containing the parameters for the action-call.
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * Getter for the action.
	 * 
	 * @return the action-object.
	 */
	public Action getThisAction() {
		return thisAction;
	}

}
