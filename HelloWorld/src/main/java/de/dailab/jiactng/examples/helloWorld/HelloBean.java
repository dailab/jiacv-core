/*
 * Created on 20.02.2007
 */
package de.dailab.jiactng.examples.helloWorld;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;

/**
 * The HelloWorldBean. Simply prints Hello World.
 * 
 * @author Thomas Konnerth
 */
public class HelloBean extends AbstractMethodExposingBean {

	public void doStart() throws Exception {
		super.doStart();
		Action a = memory.read(new Action("helloWorld"));
		memory.write(a.createDoAction(new Class[0], null));
	}

	@Expose(name = "helloWorld")
	public void helloWorld() {
		log.info("Hello World");
	}
}
