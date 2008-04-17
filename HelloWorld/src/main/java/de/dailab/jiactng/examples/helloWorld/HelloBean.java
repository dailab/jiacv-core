/*
 * Created on 20.02.2007
 */
package de.dailab.jiactng.examples.helloWorld;

import java.util.ArrayList;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.IEffector;

/**
 * The HelloWorldBean. Simply prints Hello World and quits the agent afterwards.
 * 
 * @author Thomas Konnerth
 */
public class HelloBean extends AbstractAgentBean implements IEffector {

	public void doStart() throws Exception {
		super.doStart();
		Action a = memory.read(new Action("helloWorld", null, new Class[0], null));
		memory.write(a.createDoAction(new Class[0], null));
	}

	/**
	 * Exection of the HelloWorld Example. Pretty simple.
	 * 
	 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
	 */
	public void execute() {
//		// print Hello world
//		System.out.println("Hello World from " + getBeanName());
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void doAction(DoAction arg0) {
		if ("helloWorld".equals(arg0.getAction().getName())) {
			log.info("Hello World (" + arg0.getSessionId() + ") from "
					+ getBeanName());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			log.error("Unknown Action: " + arg0);
		}
	}

	public ArrayList<Action> getActions() {
		ArrayList<Action> myActions = new ArrayList<Action>();
		myActions
				.add(new Action("helloWorld", this, new Class[0], new Class[0]));
		return myActions;
	}
}
