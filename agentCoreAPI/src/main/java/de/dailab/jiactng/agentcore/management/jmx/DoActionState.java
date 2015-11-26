package de.dailab.jiactng.agentcore.management.jmx;

/**
 * Defines the states an action execution can have.
 * @author Jan Keiser
 */
public enum DoActionState {

	/**
	 * The action was invoked. This means that the method <code>doAction</code>
	 * of the providing agent bean will be invoked after an optional user 
	 * authorization.
	 */
	invoked,

	/**
	 * The action was successfully finished. This means that the action was
	 * finished with result success.
	 */
	success,

	/**
	 * The action was failed. This means that the providing agent bean is unknown,
	 * the user is unauthorized, the method <code>doAction</code> throws an
	 * exception, the asynchronous action was finished with failure, or the
	 * session has timed out.
	 */
	failed,
}
