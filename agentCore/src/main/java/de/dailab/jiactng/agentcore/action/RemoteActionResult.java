package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.comm.message.IJiacContent;

/** 
 * This class is a wrapper for <code>ActionResult</code>.
 * @author axle
 */
public class RemoteActionResult implements IJiacContent {

	/** The <code>ActionResult</code> to transport*/
	private ActionResult result;
	
	public RemoteActionResult(ActionResult result) {
		this.result = result;
	}

	/**
	 * @return the result
	 */
	public ActionResult getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(ActionResult result) {
		this.result = result;
	}
}
