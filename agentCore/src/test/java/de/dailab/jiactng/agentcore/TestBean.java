package de.dailab.jiactng.agentcore;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.IMethodExposingBean.Expose;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.performance.ActionTestOneNode;

public class TestBean extends AbstractMethodExposingBean implements Serializable{

	private int sum;
	
	@Expose (name ="Test", scope = ActionScope.GLOBAL)
	public void method(int a, int b){
		sum = a + b;
	}
	
	public int getSum(){
		return sum;
	}
}
