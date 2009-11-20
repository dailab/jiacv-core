// $Id$

package de.dailab.ccact.tools.agentunit;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;


public class TestMethodExposingBean extends AbstractMethodExposingBean {
	
	@Expose(name="testAction")
	public boolean testAction(){
		return true;
	}
	
}