package de.dailab.ccact.tools.agentunit;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;


/**
 * This is a helping agent bean. It is designed for usage in JUnit-4 tests. Async JIAC 
 * action invokes will be synchronized into Java method invoke. 
 * 
 * @author Michael Burkhardt
 */
public final class InvokeActionBean extends AbstractAgentBean {

	

	/**
	 * SLEEP_TIME = 200 ms
	 */
	public static final Integer SLEEP_TIME = 200;


	public Collection<IActionDescription> getLocalActions() {
		
		Collection<IActionDescription> ret = new HashSet<IActionDescription>();
		
		for(IActionDescription action : this.thisAgent.searchAllActions(new Action())) {
			
			if ( ( action.getScope().equals(ActionScope.NODE) || action.getScope().equals(ActionScope.GLOBAL) ) 
				&& action.getProviderDescription().getAgentNodeUUID().equals(thisAgent.getAgentNode().getUUID())) {
				ret.add(action);
			}
			
		}
		
		return ret;
	}
	
	/**
	 * @deprecated does <b>NOT</b> only return node local actions; use {@link InvokeActionBean#getLocalActions()} 
	 * @return returns local registered actions
	 */
	@Deprecated
	public List<IActionDescription> getFoundActions() {
		
		Action tpl = new Action();
		tpl.setScope(ActionScope.NODE);
		
		return thisAgent.searchAllActions(tpl);
		
	}

	
	public void invokeAndForget(final String servicename, Serializable[] params) {
		
		Action tpl = new Action(servicename);
		tpl.setScope(ActionScope.NODE);
		
		IActionDescription tpldesc = thisAgent.searchAction(tpl);
		Action action = (Action) tpldesc;
		
		if (action == null) {
			log.error("action not found");
			return;
		}

		if (params != null) {
			this.invoke(action, params);
		}
		else {
			this.invoke(action, new Serializable[0]);
		}
		
	}


	/**
	 * Invokes the action requested from the service directory, if available.
	 * 
	 * @param serviceName
	 * @param input
	 * @return null it the action could not be found, the results of the operation otherwise.
	 * @throws Exception
	 */
	public Serializable[] invokeAction(final String serviceName, Serializable[] input) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Invoking action " + serviceName + " with input " + input);
		}
		
		Action tpl = new Action(serviceName);
		tpl.setScope(ActionScope.NODE);

		// fetch action from directory
		IActionDescription tpldesc = thisAgent.searchAction(tpl);
		Action action = (Action)tpldesc;
		// Action action = memory.read(tpl); // old version, fetches action from local memory
		
		if (action == null) {
			log.warn("action '" + serviceName + "' not found; try again later.");
			// throw new Exception("action '" + serviceName + "' not found; try again later.");
			return new Serializable[]{};
		}

		ActionResult result = null;
		
		if ( input != null ) {
			result =  this.invokeAndWaitForResult(action, input);
		}
		else {
			result = this.invokeAndWaitForResult(action, new Serializable[]{});
		}

		
		if (result.getFailure() != null) {
			throw new Exception(result.getFailure().toString());
		}
		
		Serializable[] ret = result.getResults();

		return  ret;
	}
	
	

}
