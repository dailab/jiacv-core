package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.util.Arrays;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * This class represents the result or a failure of an action.
 */
public class ActionResult extends SessionEvent<DoAction> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7941825814785637285L;

	/** The return values of the action. */
	private Serializable[] results;
	
	/**
	 * A field for exceptions, strings or other kinds of failureresults
	 */
	private Serializable failure = null;
	
	/**
	 * Creates an <code>ActionResult</code> which will be used as return object for a
	 * successful <code>DoAction</code> request.
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
	public ActionResult(IActionDescription action, Session session, Serializable[] results,
			DoAction source) {
		super(session, action, source);
		if (results != null) {
			this.results = Arrays.copyOf(results, results.length);
		}
		if (session != null) {
			session.addToSessionHistory(this);
		}
		if ((source!=null) && (source.getMetaData() != null)) {
			super.setMetaData(source.getMetaData());
		}
	}

	/**
	 * Creates an <code>ActionResult</code> object which represents a successful result of a
	 * <code>DoAction</code> request.
	 * @param source the object that created the results.
	 * @param results the result values if any.
	 */
	public ActionResult(DoAction source, Serializable[] results) {
		super(source);
		if (results != null) {
			this.results = Arrays.copyOf(results, results.length);
		}
		if (getSession() != null) {
			getSession().addToSessionHistory(this);
		}
		if ((source != null) && (source.getMetaData() != null)) {
			super.setMetaData(source.getMetaData());
		}
	}

	/**
	 * Creates an <code>ActionResult</code> object which represents a failure of a
	 * <code>DoAction</code> request.
	 * @param source the object that created the results.
	 * @param failure the failure.
	 */
	public ActionResult(DoAction source, Serializable failure) {
		super(source);
		if (getSession() != null) {
			getSession().addToSessionHistory(this);
		}
		if ((source != null) && (source.getMetaData() != null)) {
			super.setMetaData(source.getMetaData());
		}
		this.failure = failure;
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
	 * Gets the successful results.
	 * @return the results
	 */
	public final Serializable[] getResults() {
		if (results != null) {
			return Arrays.copyOf(results, results.length);
		}
		return results;
	}

	/**
	 * Sets the successful results.
	 * @param newResults
	 *            the results to set
	 */
	public final void setResults(Serializable[] newResults) {
		if (newResults != null) {
			results = Arrays.copyOf(newResults, newResults.length);
		}
		else {
			results = null;
		}
	}

	/**
	 * Gets the failure.
	 * @return the failure
	 */
	public final Serializable getFailure(){
		return failure;
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

	/**
	 * Returns a multiline text which contains the successful result of an action.
	 * @return a string representation of the action result
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ActionResult:\n results=");
		if (results != null) {
			builder.append(Arrays.asList(results).toString());
		} else {
			builder.append("null");
		}
		if (failure != null) {
			builder.append("\n failure=").append(String.valueOf(failure));
		}
		builder.append('\n');
		return builder.toString();
	}
}
