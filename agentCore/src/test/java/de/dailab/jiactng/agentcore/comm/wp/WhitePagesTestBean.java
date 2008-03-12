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
	List<IFact> _results = new ArrayList<IFact>();
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
	
	/**
	 * Creates an action and starts a search for an Agent with the name of agentname
	 * 
	 * @param agentName name of the agent to search for
	 */
	public void searchForAgentDesc(String agentName){
		log.debug("Searching for Agent " + agentName);
		AgentDescription desc = new AgentDescription(null, agentName, null, null);
		SearchRequest request = new SearchRequest(desc);
		Object[] params = {request};
		DoAction action = _requestAction.createDoAction(params, this);
		memory.write(action);
	}

	/**
	 * Receives the result for the action created in searchForAgentDesc and stores it for later withdrawl
	 */
	@Override
	public void receiveResult(ActionResult result) {
		log.debug("WhitePagesTestBean Receiving Result");
		if (result != null) log.debug("Result reads: " + result);
		
		Object[] actionResults = result.getResults();
		if (actionResults != null) {
			synchronized(_results){
				_results = new ArrayList<IFact>();
				for (Object obj : actionResults){
					if (obj instanceof IFact)
						_results.add((IFact) obj);
				}
			}
		}
	}
	
	/**
	 * returns the last result(s) of a searchrequest and deletes the resultstorage
	 * 
	 * @return
	 */
	public List<IFact> getLastResult(){
		List<IFact> output = _results;
		_results = new ArrayList<IFact>();
		return output;
	}
}
