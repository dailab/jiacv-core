// $Id$

package de.dailab.ccact.tools.agentunit;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;


public class TestMethodExposingBean extends AbstractMethodExposingBean {
	
	@Expose(name="testAction", scope=ActionScope.NODE)
	public boolean testAction(){
		return true;
	}
	
}