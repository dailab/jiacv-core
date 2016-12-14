package de.dailab.jiactng.agentcore.action;

import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * This class enables the synchronous wait for all results of invoked actions.
 * 
 * @author Jan Keiser
 */
public final class ActionResultsListener implements ResultReceiver {

	private List<ActionResult> results = new ArrayList<ActionResult>();
	private int expectedResults;

	/**
	 * Constructor for the results listener.
	 * @param expectedResults the number of the expected results
	 */
	public ActionResultsListener(int expectedResults) {
		this.expectedResults = expectedResults;
	}

	/**
	 * Informs the thread which is waiting for this listener,
	 * when the number of expected results of the invoked action was received.
	 * This method will be invoked by the execution cycle.
	 * @param newResult The received result.
	 */
	@Override
	public void receiveResult(ActionResult newResult) {
		synchronized (this) {
			results.add(newResult);
			if (results.size() == expectedResults) {
				this.notify();
			}
		}
	}

	/**
	 * Gets the results of all invoked actions.
	 * @return All received results until now.
	 */
	public List<ActionResult> getResults() {
		return results;
	}

	/**
	 * Checks if all expected results are received.
	 * @return <code>true</code> if all results are received, otherwise <code>false</code>
	 */
	public boolean isFinished() {
		return results.size() >= expectedResults;
	}

}
