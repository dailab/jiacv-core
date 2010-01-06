package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.util.Arrays;

public class ActionResult extends SessionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7941825814785637285L;

	/** The return values of the action. */
	private Serializable[] _results;
	
	/**
	 * A field for exceptions, strings or other kinds of failureresults
	 */
	private Serializable _failure = null;
	
	/**
	 * An <code>ActionResult</code> will be used as return object for a
	 * <code>DoAction</code> request.
	 * 
	 * @param action
	 *            the requested action
	 * @param session
	 *            the session of the action request
	 * @param results
	 *            the result values if any
	 * @param source
	 *            the object that created the results
	 */
	public ActionResult(Action action, Session session, Serializable[] results,
			DoAction source) {
		super(session, action, source);
		if (results != null) {
			this._results = Arrays.copyOf(results, results.length);
		}
		if (session != null)
			session.addToSessionHistory(this);
		if ((source!=null) && (source.getMetaData() != null))
			super.setMetaData(source.getMetaData());
	}

	public ActionResult(DoAction source, Serializable[] results) {
		super(source);
		if (results != null) {
			this._results = Arrays.copyOf(results, results.length);
		}
		if (getSession() != null)
			getSession().addToSessionHistory(this);
		if ((source != null) && (source.getMetaData() != null))
			super.setMetaData(source.getMetaData());
	}

	public ActionResult(DoAction source, Serializable failure) {
		super(source);
		if (getSession() != null)
			getSession().addToSessionHistory(this);
		if ((source != null) && (source.getMetaData() != null))
			super.setMetaData(source.getMetaData());
		_failure = failure;
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
	public Serializable[] getResults() {
		if (_results != null) {
			return Arrays.copyOf(_results, _results.length);
		}
		return null;
	}

	/**
	 * @param results
	 *            the results to set
	 */
	public void setResults(Serializable[] results) {
		if (results != null) {
			this._results = Arrays.copyOf(results, results.length);
		}
		else {
			this._results = null;
		}
	}

	public Serializable getFailure(){
		return this._failure;
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
		if (_results != null) {
			builder.append(Arrays.asList(_results).toString());
		} else {
			builder.append("null");
		}
		builder.append('\n');
		return builder.toString();
	}
}
