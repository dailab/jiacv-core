/*
 * Created on 20.02.2007
 */
package de.dailab.jiactng.examples.helloWorld;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;

/**
 * The HelloWorldBean. Simply invokes an action when started to print Hello World.
 * 
 * @author Thomas Konnerth
 */
public class HelloBean extends AbstractMethodExposingBean {

	/**
	 * This method will be executed during the start of the agent bean.
	 * It invokes the action <code>helloWorld</code> of the same agent.
	 */
	public void doStart() throws Exception {
		super.doStart();

		// invoke action of the same agent
		Action a = memory.read(new Action("helloWorld"));
		memory.write(a.createDoAction(new Class[0], null));
	}

	/**
	 * Implementation of the action <code>helloWorld</code>.
	 * It only creates a log message, which will be written to the console and log file. 
	 */
	@Expose(name = "helloWorld")
	public void helloWorld() {
		log.info("Hello World");
	}

}
