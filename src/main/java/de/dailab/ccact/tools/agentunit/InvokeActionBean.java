package de.dailab.ccact.tools.agentunit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;

//import de.dailab.jiactng.jadl.*;

/**
 * This is a helping agent bean. It is designed for usage in JUnit-4 tests. Async JIAC 
 * action invokes will be synchronized into Java method invoke. 
 * @author Michael Burkhardt
 */
public class InvokeActionBean extends AbstractAgentBean implements ResultReceiver {

	public final static List<String> NIL = new ArrayList<String>();

	private TreeMap<String, List<Serializable>> invokes = new TreeMap<String, List<Serializable>>();

	/**
	 * 200 ms
	 */
	public static final Integer SLEEP_TIME = 200;

	
	@Override
	public void doInit() throws Exception {
		super.doInit();

/*		for (IAgentBean bean : thisAgent.getAgentBeans()) {
			if (bean.getClass().equals(JadlInterpreterAgentBean.class)) {
				interpreter = (JadlInterpreterAgentBean) bean;
			}
		}
*/
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
			// synchronized (this) {
			invokes.put(actionName, ( results != null ? Arrays.asList(results)
					: new ArrayList<Serializable>() ) );
			log.debug("pong");
			// }
		}

	}



	public synchronized Serializable[] invokeAction(final String serviceName, Serializable[] input) throws Exception {

		Action tpl = new Action(serviceName);
		tpl.setScope(null);

		Action action = memory.read(tpl);
		
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
