/**
 * 
 */
package de.dailab.jiactng.agentcore.comm.wp;

import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * @author Martin Loeffelholz
 *
 */
public class WhitePagesTestBean extends AbstractAgentBean implements ResultReceiver{

	Action _requestAction;
	List<AgentDescription> _results = null;
	/**
	 * 
	 */
	public WhitePagesTestBean() {
		Class<?>[] input = {IFact.class};
		Class<?>[] result = {List.class};
		_requestAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#requestSearch",null,input ,result));
	}
	
	public void searchForAgentDesc(String agentName){
		AgentDescription desc = new AgentDescription(null, agentName, null);
		Object[] params = {desc};
		DoAction action = _requestAction.createDoAction(params, this);
		memory.write(action);
	}

	@Override
	public void receiveResult(ActionResult result) {
		Object[] actionResults = result.getResults();
		if (actionResults[0] instanceof IFact[]){
			IFact[] facts = (IFact[]) actionResults[0];
			_results = new ArrayList<AgentDescription>();
			for (IFact fact : facts){
				if (fact instanceof AgentDescription){
					_results.add((AgentDescription) fact);
				}
			}
		} else {
			// result == null or no result
			_results = null;
		}
	}
	
	public List<AgentDescription> getLastResult(){
		List<AgentDescription> output = _results;
		_results = null;
		return output;
	}
}
