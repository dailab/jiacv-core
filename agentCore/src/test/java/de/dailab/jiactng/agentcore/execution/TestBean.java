package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.List;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

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
	 * @see de.dailab.jiactng.agentcore.IAgent#searchAllActions(IActionDescription)
	 */
	@Expose(name = "test")
	public List<IActionDescription> test() {
		try {
			return thisAgent.searchAllActions(new Action("test"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
