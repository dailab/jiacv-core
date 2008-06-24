package de.dailab.jiactng.agentcore.comm.wp.exceptions;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;
	@SuppressWarnings("serial")
	public class ActionNotPresentException extends RuntimeException{
		IActionDescription _actionDesc = null;

		public ActionNotPresentException(IActionDescription actionDesc) {
			super("Action isn't present anymore");
			_actionDesc = actionDesc;
		}
	}