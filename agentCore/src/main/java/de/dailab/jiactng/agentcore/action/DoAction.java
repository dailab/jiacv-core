package de.dailab.jiactng.agentcore.action;

public class DoAction extends Session {

	private Action thisAction;

	private Object[] params;

	public DoAction(Action thisAction, Object source, Object[] params) {
		super(source);
		this.thisAction = thisAction;
		this.params = params;
	}

	public Object[] getParams() {
		return params;
	}

	public Action getThisAction() {
		return thisAction;
	}

}
