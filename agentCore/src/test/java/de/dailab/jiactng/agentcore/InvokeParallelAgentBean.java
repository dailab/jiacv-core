package de.dailab.jiactng.agentcore;

import java.io.Serializable;
import java.util.List;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;

public class InvokeParallelAgentBean extends AbstractAgentBean{

	/**
	 *	Waits for at least {@code minActions} to be available.
	 */
	public boolean callMethod(int minActions){

		long t = System.currentTimeMillis();
		while (thisAgent.searchAllActions(new Action("invokeWithBacktrackingMethod")).size() < minActions) {
			if (t + 15000 < System.currentTimeMillis() ) {
				throw new RuntimeException("Required actions not found within time.");
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Action action = new Action("invokeWithBacktrackingMethod");
		List<ActionResult> actionResults = invokeParallel(action, new Serializable[]{}, 3000L);

		if (actionResults.size() < minActions) {
			return false;
		}
		for (ActionResult actionResult : actionResults) {
			if(actionResult == null || actionResult.getFailure() != null){
				return false;
			}
		}

		return true;
	}
}
