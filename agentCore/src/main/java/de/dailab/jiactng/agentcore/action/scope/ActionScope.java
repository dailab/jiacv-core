package de.dailab.jiactng.agentcore.action.scope;

public enum ActionScope {
	AGENT {
		public boolean contains(ActionScope scope) {
			return scope == AGENT;
		}
	},
	NODE {
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE;
		}
		
	},
	GLOBAL{
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE 
				|| scope == GLOBAL;
		}
		
	},
	WEBSERVICE  {
		public boolean contains(ActionScope scope) {
			return scope == AGENT 
				|| scope == NODE 
				|| scope == GLOBAL 
				|| scope == WEBSERVICE;
		}
	};
	
	public abstract boolean contains(ActionScope scope);
}
