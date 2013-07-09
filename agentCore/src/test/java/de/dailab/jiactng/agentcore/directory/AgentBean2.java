package de.dailab.jiactng.agentcore.directory;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class AgentBean2 extends AbstractAgentBean{

	long time = -1;
	@Override
	public void doStart() throws Exception {
		super.doStart();
		time = System.currentTimeMillis();
	}
	
	@Override
	public void execute() {
			Action action = new Action("TestAction3928742937");
			IActionDescription calculateSendTimeD = thisAgent.searchAction(action);
			
			if(calculateSendTimeD == null){
				System.err.println("2: Action not found");
			}else{
				System.err.println("2: Action found");
			}
	}
}
