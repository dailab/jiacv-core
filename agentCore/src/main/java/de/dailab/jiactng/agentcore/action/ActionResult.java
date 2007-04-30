package de.dailab.jiactng.agentcore.action;

public class ActionResult extends Session {

	private Action thisAction;

	private DoAction resultOf;

	private Object[] results;

	private boolean success;
	
	public ActionResult(Action thisAction, DoAction resultOf, boolean success, Object[] results,
			Object source) {
		super(source);
		this.resultOf = resultOf;
		this.thisAction = thisAction;
		this.results = results;
	}

	public Object[] getResults() {
		return results;
	}

	public Action getThisAction() {
		return thisAction;
	}

	public DoAction getResultOf() {
		return resultOf;
	}

	public boolean isSuccess() {
		return success;
	}

}
