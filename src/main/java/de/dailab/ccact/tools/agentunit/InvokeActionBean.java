package de.dailab.ccact.tools.agentunit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;


/**
 * This is a helping agent bean. It is designed for usage in JUnit-4 tests. Async JIAC 
 * action invokes will be synchronized into Java method invoke. 
 * @author Michael Burkhardt
 */
public class InvokeActionBean extends AbstractAgentBean implements ResultReceiver {

	private TreeMap<String, List<Serializable>> invokes = new TreeMap<String, List<Serializable>>();

	/**
	 * SLEEP_TIME = 200 ms
	 */
	public static final Integer SLEEP_TIME = 200;

	
	@Override
	public void doInit() throws Exception {
		super.doInit();
	}


	
	public void receiveResult(ActionResult result) {

		// ((Log4JLogger) log).getLogger().setLevel(Level.DEBUG);

		if (log != null) {
			log.info(result.getAction().getName());
		}
		Serializable error = null;
		if ((error = result.getFailure()) != null) {
			if (log != null) {
				log.error(error);
			}
		}

		String actionName = result.getAction().getName();
		Serializable[] results = result.getResults();

		log.debug("got result for action '" + actionName + "': "
				+ Arrays.asList(results));

		if (invokes.containsKey(actionName)) {
			log.debug("ping");

			invokes.put(actionName, ( results != null ? Arrays.asList(results)
					: new ArrayList<Serializable>() ) );
			log.debug("pong");
		}

	}
	
	
	public List<IActionDescription> getFoundActions() {
		
		Action tpl = new Action();
		tpl.setScope(ActionScope.NODE);
		
		return thisAgent.searchAllActions(tpl);
		
	}



	public synchronized Serializable[] invokeAction(final String serviceName, Serializable[] input) throws Exception {

		log.debug("Invoking action " + serviceName + " with input " + input);
		
		Action tpl = new Action(serviceName);
		tpl.setScope(ActionScope.NODE);

		// fetch action from directory
		IActionDescription tpldesc = thisAgent.searchAction(tpl);
		Action action = (Action)tpldesc;
		// Action action = memory.read(tpl); // old version, fetches action from local memory
		
		if (action == null) {
			log.error("action '" + serviceName + "' not found; try again later.");
			throw new Exception("action '" + serviceName + "' not found; try again later.");
		}

		
		if (input != null) {
			invoke(action, input, this);
		} else {
			invoke(action, new Serializable[0], this);
		}

		log.debug("invokes action " + action.getName() + "; " + invokes.keySet());

		invokes.put(serviceName, null);

		log.debug("invokes map: " + invokes);

		while (invokes.get(serviceName) == null) {
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				log.error("exception while waiting", e);
			}
		}

		List<Serializable> ret = null;
		ret = invokes.remove(serviceName);
		log.debug("return value: " + ret);

		return (Serializable[]) ret.toArray();
	}

}
