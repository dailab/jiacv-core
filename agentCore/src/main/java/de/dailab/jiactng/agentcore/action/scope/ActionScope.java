package de.dailab.jiactng.agentcore.action.scope;

public enum ActionScope {
	AGENT {
		public boolean contains(ActionScope scope) {
			return scope == AGENT
			|| scope == null;
		}
	},
	NODE {
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE
				|| scope == null;
		}
		
	},
	GLOBAL{
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE 
				|| scope == GLOBAL
				|| scope == null;
		}
		
	},
	WEBSERVICE  {
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE 
				|| scope == GLOBAL 
				|| scope == WEBSERVICE
				|| scope == null;
		}
	};
	
	public abstract boolean contains(ActionScope scope);
}
