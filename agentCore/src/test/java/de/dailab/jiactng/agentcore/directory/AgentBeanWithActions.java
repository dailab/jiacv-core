package de.dailab.jiactng.agentcore.directory;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;

public class AgentBeanWithActions extends AbstractMethodExposingBean {

	@Expose(scope=ActionScope.GLOBAL)
	public String echo(String s) {
		return s;
	}

	@Expose(scope=ActionScope.GLOBAL)
	public String mirror(String s) {
		StringBuffer tmp = new StringBuffer();
		for (int i = s.length() - 1; i >= 0; i--) {
			tmp.append(s.charAt(i));
		}
		return tmp.toString();
	}
}
