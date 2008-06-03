/**
 * 
 */
package de.dailab.jiactng.agentcore.comm.wp;

import java.io.Serializable;
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

	private Action _requestSearchAction;
	private Action _addActionAction;
	private Action _removeActionAction;
	private Action _addAutoEnlistActionTemplate;
	private Action _removeAutoEnlistActionTemplate;
	private ActionResult _lastFailure = null;

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
		_requestSearchAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#requestSearch"));
		_addActionAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#addActionToDirectory"));
		_removeActionAction = memory.read(new Action("de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean#removeActionFromDirectory"));
		_addAutoEnlistActionTemplate = memory.read(new Action(DirectoryAccessBean.ACTION_ADD_AUTOENTLISTMENT_ACTIONTEMPLATE));
		_removeAutoEnlistActionTemplate = memory.read(new Action(DirectoryAccessBean.ACTION_REMOVE_AUTOENTLISTMENT_ACTIONTEMPLATE));
	}

	/**
	 * Creates an action and starts a search for an Agent with the name of agentname
	 * 
	 * @param agentName name of the agent to search for
	 */
	public void searchForAgentDesc(String agentName, boolean isGlobal){
		log.debug("Searching for Agent " + agentName);
		AgentDescription desc = new AgentDescription(null, agentName, null, null);
		Serializable[] params = {desc, new Boolean(isGlobal)};
		DoAction action = _requestSearchAction.createDoAction(params, this);
		_lastDoAction = action;
		memory.write(action);
	}
	
	public void searchForAgentDesc(String agentName, boolean isGlobal, long timeToSearch){
		log.debug("Searching for Agent " + agentName);
		AgentDescription desc = new AgentDescription(null, agentName, null, null);
		Serializable[] params = {desc, new Boolean(isGlobal)};
		DoAction action = _requestSearchAction.createDoAction(params, this, timeToSearch);
		_lastDoAction = action;
		memory.write(action);
	}

	public void searchForActionDesc(IActionDescription actionDesc, boolean isGlobal){
		log.debug("Searching for Action " + actionDesc.toString());
		Serializable[] params = {actionDesc, new Boolean(isGlobal)};
		DoAction action = _requestSearchAction.createDoAction(params, this);
		_lastDoAction = action;
		memory.write(action);
	}
	
	public void searchForActionDesc(IActionDescription actionDesc, boolean isGlobal, long timeToSearch){
		log.debug("Searching for Action " + actionDesc.toString());
		Serializable[] params = {actionDesc, new Boolean(isGlobal)};
		DoAction action = _requestSearchAction.createDoAction(params, this, timeToSearch);
		_lastDoAction = action;
		memory.write(action);
	}

	public void addActionToDirectory(IActionDescription actionDesc){
		log.debug("Adding Action to Directory: " + actionDesc);
		Serializable[] params = {actionDesc};
		DoAction action = _addActionAction.createDoAction(params, null);
		_lastDoAction = action;
		memory.write(action);
	}

	public void removeActionFromDirectory(IActionDescription actionDesc){
		log.debug("removing Action from Directory");
		Serializable[] params = {actionDesc};
		DoAction action = _removeActionAction.createDoAction(params, null);
		_lastDoAction = action;
		memory.write(action);
	}

	public void TimeoutTest(){
		log.debug("trying timeoutsearch");
		DoAction action = _requestSearchAction.createDoAction(
				new Serializable[] {
						new AgentDescription(null, "NixAgentos", null, null), 
						new Boolean(false)}, 
				this, 1);
		memory.write(action); 
	}


	public Action getSendAction(){
		return memory.read(new Action("de.dailab.jiactng.agentcore.comm.ICommunicationBean#send",null,new Class[]{IJiacMessage.class, ICommunicationAddress.class},null));
	}
	
	public void addAutoEnlistActionTemplate(ArrayList<Action> templates){
		DoAction action = _addAutoEnlistActionTemplate.createDoAction(new Serializable[] {templates}, this);
		memory.write(action);
	}

	public void removeAutoEnlistActionTemplate(ArrayList<Action> templates){
		DoAction action = _removeAutoEnlistActionTemplate.createDoAction(new Serializable[] {templates}, this);
		memory.write(action);
	}

	/**
	 * Receives the result for the action created in searchForAgentDesc and stores it for later withdrawl
	 */
	@Override
	@SuppressWarnings("unchecked")
	synchronized public void receiveResult(ActionResult result) {
		log.debug("WhitePagesTestBean Receiving Result");
		if (result != null) log.debug("Result reads: " + result);

		if (result.getResults() != null){
			Object[] actionResults = result.getResults();

			if (actionResults[0] != null) {
				List<IFact> results = (List<IFact>) actionResults[0];
				synchronized(_results){
					for (Object obj : results){
						if (obj instanceof IFact)
							_results.add((IFact) obj);
					}
				}
			}
		} else {
			_lastFailure = result;
		}
	}

	public ActionResult getLastFailure(){
		ActionResult result = _lastFailure;
		_lastFailure = null;
		return result;
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
