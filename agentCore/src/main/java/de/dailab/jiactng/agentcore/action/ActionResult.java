package de.dailab.jiactng.agentcore.action;

public class ActionResult extends SessionEvent {

	/** The action trigger this obejct is result of. */
	private DoAction resultOf;

	/** The return values of the action. */
	private Object[] results;

	/** Whether this result means success or not. */
	private boolean success;

	/**
	 * An <code>ActionResult</code> will be used as return object for a <code>DoAction</code> request.
	 * 
	 * @param thisAction the action this object is result of
	 * @param resultOf the trigger for the action
	 * @param success whether or not this result is success or failure
	 * @param results the result values if any
	 * @param source the object that created the results
	 */
	public ActionResult(Action thisAction, DoAction resultOf, boolean success, Object[] results, Object source) {
		super(resultOf.getSession(), thisAction, source);
		this.resultOf = resultOf;
		this.success = success;
		this.results = results;
		if (session != null) session.addToSessionHistory(this);
	}

	/**
	 * @return the <code>DoAction</code> that triggered the action
	 */
	public DoAction getResultOf() {
		return resultOf;
	}

	/**
	 * @param resultOf the <code>DoAction</code> that triggered the action 
	 */
	public void setResultOf(DoAction resultOf) {
		this.resultOf = resultOf;
	}

	/**
	 * @return the results
	 */
	public Object[] getResults() {
		return results;
	}

	/**
	 * @param results the results to set
	 */
	public void setResults(Object[] results) {
		this.results = results;
	}

	/**
	 * @return true if action succeeded, false otherwise
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * @param success set true if the action succeeded, set false otherwise
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	
}
