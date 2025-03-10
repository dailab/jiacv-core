package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * This class enables the synchronous wait for result of invoked actions.
 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#invokeAndWaitForResult(IActionDescription,Serializable[])
 * @author Jan Keiser
 */
public final class ActionResultListener implements ResultReceiver {

	private ActionResult result = null;

	/**
	 * Informs the thread which is waiting for this listener,
	 * that a result of the invoked action was received. This
	 * method will be invoked by the execution cycle.
	 * @param newResult The received result.
	 */
	@Override
	public void receiveResult(ActionResult newResult) {
		result = newResult;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Gets the result of the invoked action.
	 * @return The received result or <code>null</code> if no result was received so far.
	 */
	public ActionResult getResult() {
		return result;
	}
}
