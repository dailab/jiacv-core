package de.dailab.jiactng.agentcore.comm.wp;

import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.ThisAgentDescription;

public class GlobalRemoteActionProviderBean extends AbstractAgentBean implements IEffector{

	public final static String ACTION_GET_GLOBAL_RESULT = "de.dailab.jiactng.agentcore.comm.wp.GlobalRemoteActionProviderBean#getGlobalResult";
	
private Action _addAction;
	
	private final ResultDump _resultDump = new ResultDump();
	
	@Override
	public void doInit() throws Exception {
		super.doInit();
	}
	
	@Override
	public void doStart() throws Exception {
		super.doStart();
		_addAction = memory.read(new Action(DirectoryAccessBean.ACTION_ADD_ACTION_TO_DIRECTORY));
		
		Action getGlobalResult = new Action(ACTION_GET_GLOBAL_RESULT, this, new Class[] {}, new Class[] {String.class});
		
		AgentDescription myAgentDescription = memory.read(new ThisAgentDescription());
		getGlobalResult.setProviderDescription(myAgentDescription);
		
		DoAction addGlobalResult = _addAction.createDoAction(new Object[] {getGlobalResult}, _resultDump);
		
		memory.write(addGlobalResult);
		
	}

	@Override
	public List<? extends Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		Action getGlobalResult = new Action(ACTION_GET_GLOBAL_RESULT, this, new Class[] {}, new Class[] {String.class});
		
		actions.add(getGlobalResult);
		
		return actions;
	}
	
	@Override
	public void doAction(DoAction doAction) throws Exception {
		String actionName= doAction.getAction().getName();
		
		if (actionName.endsWith(ACTION_GET_GLOBAL_RESULT)){
			String name = getGlobalResult();
			ActionResult result = ((Action) doAction.getAction()).createActionResult(doAction, new Object[] {name});
			log.debug("writing result for getGlobalResult");
			memory.write(result);
		}
		
	}

	public String getGlobalResult(){
		return thisAgent.getAgentName();
	}
	
}
