package de.dailab.jiactng.agentcore.action;

import java.util.Arrays;

public class ActionResult extends SessionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7941825814785637285L;

	/** The return values of the action. */
	private Object[] results;

	/**
	 * An <code>ActionResult</code> will be used as return object for a
	 * <code>DoAction</code> request.
	 * 
	 * @param source
	 *            the object that created the results
	 * @param results
	 *            the result values if any
	 * @param success
	 *            whether or not this result is success or failure
	 */
	public ActionResult(Action action, Session session, Object[] results,
			DoAction source) {
		super(session, action, source);
		this.results = results;
		if (getSession() != null)
			getSession().addToSessionHistory(this);
		if (source.getMetaData() != null)
			super.setMetaData(source.getMetaData());
	}

	public ActionResult(DoAction source, Object[] results) {
		super(source);
		this.results = results;
		if (getSession() != null)
			getSession().addToSessionHistory(this);
		if (source != null && source.getMetaData() != null)
			super.setMetaData(source.getMetaData());
	}

	// /**
	// * @return the <code>DoAction</code> that triggered the action
	// */
	// public Session getResultOf() {
	// return session;
	// }
	//
	// /**
	// * @param resultOf the <code>DoAction</code> that triggered the action
	// */
	// public void setResultOf(Session resultOf) {
	// this.session = resultOf;
	// }

	/**
	 * @return the results
	 */
	public Object[] getResults() {
		return results;
	}

	/**
	 * @param results
	 *            the results to set
	 */
	public void setResults(Object[] results) {
		this.results = results;
	}

	// /**
	// * @return true if action succeeded, false otherwise
	// */
	// public boolean isSuccess() {
	// return success;
	// }
	//
	// /**
	// * @param success set true if the action succeeded, set false otherwise
	// */
	// public void setSuccess(boolean success) {
	// this.success = success;
	// }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ActionResult:\n results=");
		if (results != null) {
			builder.append(Arrays.asList(results).toString());
		} else {
			builder.append("null");
		}
		builder.append('\n');
		return builder.toString();
	}
}
