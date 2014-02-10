package de.dailab.jiactng.agentcore;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;

public class TestBean extends AbstractMethodExposingBean implements Serializable{

	/** Serial UID */
    private static final long serialVersionUID = -1605652567777259449L;
    
    private int sum;
	
	@Expose (name ="Test", scope = ActionScope.GLOBAL)
	public void method(int a, int b){
		sum = a + b;
	}
	
	public int getSum(){
		return sum;
	}
}
