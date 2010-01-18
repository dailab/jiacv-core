package de.dailab.jiactng.agentcore.action.scope;

/**
 * The action scope specifies the visibility range of an action.
 */
public enum ActionScope {
	/** The action will not be registered in the action directory
	 * and thus it is only visible within the providing agent.*/
	AGENT {
		  /**
		   * {@inheritDoc}
		   */
		public boolean contains(ActionScope scope) {
			return scope == AGENT
			|| scope == null;
		}
	},
	/** The action will be registered in the action directory of
	 * the local agent node and is not exchanged with other agent
	 * nodes. Thus the action is only visible for agents on the
	 * same agent node.*/
	NODE {
		  /**
		   * {@inheritDoc}
		   */
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE
				|| scope == null;
		}
		
	},
	/**
	 * The action will be registered in the action directory of
	 * the local agent node which will also exchange it with other
	 * agent nodes. Thus the action is visible for all agents on the
	 * known agent nodes.
	 */
	GLOBAL{
		  /**
		   * {@inheritDoc}
		   */
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE 
				|| scope == GLOBAL
				|| scope == null;
		}
		
	},
	/**
	 * The action will be registered in the action directory of
	 * the local agent node which will also exchange it with other
	 * agent nodes. Additionally the action will be also exposed as
	 * web service. Thus the action is visible for all agents on the
	 * known agent nodes and for other web service based software. 
	 */
	WEBSERVICE  {
		  /**
		   * {@inheritDoc}
		   */
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE 
				|| scope == GLOBAL 
				|| scope == WEBSERVICE
				|| scope == null;
		}
	};

	/**
	 * Checks if this scope extends a given action scope.
	 * @param scope the other action scope
	 * @return <code>true</code> if the given scope is part of this action scope.
	 */
	public abstract boolean contains(ActionScope scope);
}
