package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * This class enables the synchronous wait for result of invoked actions.
 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#invokeAndWaitForResult(Action,Serializable[])
 * @author Jan Keiser
 */
public class ActionResultListener implements ResultReceiver {

	private ActionResult _result = null;

	/**
	 * Informs the thread which is waiting for this listener,
	 * that a result of the invoked action was received. This
	 * method will be invoked by the execution cycle.
	 * @param result The received result.
	 */
	@Override
	public void receiveResult(ActionResult result) {
		_result = result;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Gets the result of the invoked action.
	 * @return The received result or <code>null</code> if no result was received so far.
	 */
	public ActionResult getResult() {
		return _result;
	}
}
