package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.List;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;

/**
 * This bean tests the synchronously invocation of actions from an execution cycle.
 * @author Jan Keiser
 */
public class TestBean extends AbstractMethodExposingBean {

	/** The result of the test action. */
	public List<Action> actions = null;

	/**
	 * Invokes the test action synchronously and stores the result in the attribute <code>actions</code>.
	 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#invokeAndWaitForResult(Action,Serializable[])
	 * @see #actions
	 */
	public void invokeTestAction() {
		Action a = retrieveAction("test");
		actions = (List<Action>) invokeAndWaitForResult(a, new Serializable[0]).getResults()[0];
	}

	/**
	 * This test action retrieves synchronously test action descriptions from the directory.
	 * @return The list of found test actions registered in the directory.
	 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#retrieveActionsFromDirectory(Action,boolean,long)
	 */
	@Expose(name = "test")
	public List<Action> test() {
		try {
			return retrieveActionsFromDirectory(new Action("test"), false, 10000);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
