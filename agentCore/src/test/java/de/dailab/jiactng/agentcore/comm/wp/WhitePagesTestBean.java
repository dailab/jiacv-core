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
	}
	
	@Override
	public void doInit() throws Exception {
		super.doInit();
		//nothing to do yet
	}
	
	@Override
	public void doStart() throws Exception {
		super.doStart();
		_requestAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#requestSearch"));
	}
	
	public void searchForAgentDesc(String agentName){
		log.debug("Searching for Agent " + agentName);
		AgentDescription desc = new AgentDescription(null, agentName, null, null);
		SearchRequest request = new SearchRequest(desc);
		Object[] params = {request};
		DoAction action = _requestAction.createDoAction(params, this);
		memory.write(action);
	}

	@Override
	public void receiveResult(ActionResult result) {
		log.debug("Receiving Result");
		if (result != null) log.debug("Result reads: " + result);
		Object[] actionResults = result.getResults();
		if (actionResults != null){
			if (actionResults instanceof IFact[]){
				IFact[] facts = (IFact[]) actionResults;
				_results = new ArrayList<AgentDescription>();
				for (IFact fact : facts){
					if (fact instanceof AgentDescription){
						_results.add((AgentDescription) fact);
					}
				}
			}
		}
		
//		} else {
//			// result == null or no result
//			_results = null;
//		}
	}
	
	public List<AgentDescription> getLastResult(){
		List<AgentDescription> output = _results;
		_results = null;
		return output;
	}
}
