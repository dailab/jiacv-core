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
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * @author Martin Loeffelholz
 *
 */
public class WhitePagesTestBean extends AbstractAgentBean implements ResultReceiver{

	private Action _requestAction;
	private Action _addActionAction;
	private Action _removeActionAction;
	private Action _useRemoteActionAction;
	
	List<IFact> _results = new ArrayList<IFact>();
	DoAction _lastDoAction = null;
	
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
		_addActionAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#addActionToDirectory"));
		_removeActionAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#removeActionFromDirectory"));
		_useRemoteActionAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#useRemoteAction"));
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
		_lastDoAction = action;
		memory.write(action);
	}

	public void searchForActionDesc(IActionDescription actionDesc){
		log.debug("Searching for Action " + actionDesc.toString());
		SearchRequest request = new SearchRequest(actionDesc);
		Object[] params = {request};
		DoAction action = _requestAction.createDoAction(params, this);
		_lastDoAction = action;
		memory.write(action);
	}
	
	public void addActionToDirectory(IActionDescription actionDesc){
		log.debug("Adding Action to Directory: " + actionDesc);
		Object[] params = {actionDesc};
		DoAction action = _addActionAction.createDoAction(params, null);
		_lastDoAction = action;
		memory.write(action);
	}
	
	public void removeActionFromDirectory(IActionDescription actionDesc){
		log.debug("removing Action from Directory");
		Object[] params = {actionDesc};
		DoAction action = _removeActionAction.createDoAction(params, null);
		_lastDoAction = action;
		memory.write(action);
	}
	
	public void useRemoteAction(IActionDescription actionDesc, Object[] params){
		log.debug("using remote Action " + actionDesc.getName());
		Object[] paramsToWorkOn = {actionDesc, params};
		DoAction action = _useRemoteActionAction.createDoAction(paramsToWorkOn, this);
		_lastDoAction = action;
		memory.write(action);
	}
	
	public Action getSendAction(){
		return memory.read(new Action("de.dailab.jiactng.agentcore.comm.ICommunicationBean#send",null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
	}
	
	
	/**
	 * Receives the result for the action created in searchForAgentDesc and stores it for later withdrawl
	 */
	@Override
	synchronized public void receiveResult(ActionResult result) {
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
		synchronized(_results){
			List<IFact> output = _results;
			_results = new ArrayList<IFact>();
			return output;
		}
	}
}
