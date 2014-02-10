package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;

public class FaultyActionAgentBean extends AbstractMethodExposingBean{

	@Expose (name = "invokeWithBacktrackingMethod", scope = ActionScope.GLOBAL)
	public Object faulteMethod() throws Exception{
		throw new RuntimeException("called faulty method");
	}
}
