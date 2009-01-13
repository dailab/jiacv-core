package de.dailab.jiactng.agentcore.management.jmx;

/**
 * Defines the states an action execution can have.
 * @author Jan Keiser
 */
public enum DoActionState {

	/**
	 * The action was invoked. This means that the method <code>doAction</code>
	 * of the providing agent bean will be invoked.
	 */
	invoked,

	/**
	 * The action was successfully started, but not finished. This means that
	 * the method <code>doAction</code> was successfully finished or the user
	 * authorization was started, but the result of the asynchronous action 
	 * is still unknown.
	 */
	started,

	/**
	 * The action was successfully finished. This means that the asynchronous
	 * action was finished with result success.
	 */
	success,

	/**
	 * The action was failed. This means that the providing agent bean is unknown,
	 * the user is unauthorized, the method <code>doAction</code> throws an
	 * exception or the asynchronous action was finished with failure.
	 */
	failed,
}
