package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.IMethodExposingBean.Expose;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.performance.ActionTestTwoNodes;

public class FaultyActionAgentBean extends AbstractMethodExposingBean{

	@Expose (name = "invokeWithBacktrackingMethod", scope = ActionScope.GLOBAL)
	public Object faulteMethod() throws Exception{
		throw new RuntimeException("called faulty method");
	}
}
