package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.IMethodExposingBean.Expose;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;

public class NonFaultyActionAgentBean extends AbstractMethodExposingBean{

	@Expose (name = "invokeWithBacktrackingMethod", scope = ActionScope.GLOBAL)
	public Object NonFaulteMethod(){
		return new Long(0);
	}
}
