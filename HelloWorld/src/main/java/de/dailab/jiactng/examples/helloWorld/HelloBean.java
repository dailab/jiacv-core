/*
 * Created on 20.02.2007
 */
package de.dailab.jiactng.examples.helloWorld;

import java.util.List;

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
/*
		try {
			List<Action> actions = retrieveActionsFromDirectory(new Action("helloWorld"), false, 10000);
			log.info("Got actions:");
			for (Action a : actions) {
				System.out.println(a);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

}
