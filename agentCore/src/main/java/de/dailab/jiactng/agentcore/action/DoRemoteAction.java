package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.comm.message.IJiacContent;

/**
 * This is a wrapper for a <code>DoAction</code>.
 * 
 * @author axle
 */
public class DoRemoteAction implements IJiacContent {
	private DoAction action;
	
	public DoRemoteAction (DoAction action) {
		this.action = action;
	}

	/**
	 * @return the action
	 */
	public DoAction getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(DoAction action) {
		this.action = action;
	}
}
