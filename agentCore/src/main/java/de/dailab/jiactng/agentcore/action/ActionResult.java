package de.dailab.jiactng.agentcore.action;

public class ActionResult extends Session {

	private Action thisAction;

	private Object[] results;

	public ActionResult(Action thisAction, Object source, Object[] results) {
		super(source);
		this.thisAction = thisAction;
		this.results = results;
	}

	public Object[] getResults() {
		return results;
	}

	public Action getThisAction() {
		return thisAction;
	}

}
