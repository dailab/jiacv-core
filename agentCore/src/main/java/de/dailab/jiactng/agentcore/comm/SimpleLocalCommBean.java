package de.dailab.jiactng.agentcore.comm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Future;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.knowledge.ObjectTuple;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.ThisAgentDescription;

public class SimpleLocalCommBean extends AbstractAgentBean implements Runnable,
		IEffector, ResultReceiver {

	private static HashMap<String, ObjectTuple> actionMap = new HashMap<String, ObjectTuple>();

	private Future myThread = null;

	private AgentDescription myDesc = null;

	private Boolean flag = new Boolean(false);

	@Override
	public void doInit() throws Exception {
		super.doInit();
		processActionMap();
	}

	@Override
	public void doStart() throws Exception {
		super.doStart();
		myDesc = thisAgent.getAgentDescription();
		synchronized (flag) {
			this.flag = new Boolean(true);
		}
		myThread = thisAgent.getThreadPool().submit(this);
	}

	@Override
	public void doStop() throws Exception {
		super.doStop();
		synchronized (flag) {
			this.flag = new Boolean(false);
		}
		// myThread.cancel(true);
	}

	@Override
	public void doCleanup() throws Exception {
		super.doCleanup();
	}

	public void run() {
		while (flag.booleanValue()) {
			synchronized (flag) {
				Set<Action> actions = memory.readAll(new Action(null, null,
						null, null));
				for (Action a : actions) {
					actionMap.put(a.getName(), new ObjectTuple(a, memory));
				}

				memory.removeAll(new Action(null, null, null, null));
				processActionMap();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void processActionMap() {
		HashMap<String, ObjectTuple> newMap = new HashMap<String, ObjectTuple>();
		for (String s : actionMap.keySet()) {
			ObjectTuple ot = actionMap.get(s);
			Action a = (Action) ot.getArg1();
			try {
				if (a.getProviderBean() != null
						&& null == memory.read(new Action(s, null,
								null, null))) {
					memory.write(a);
					newMap.put(s, ot);
				} else {
					// do nothing, as action will be removed
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				actionMap.put(s, null);
			}
		}
		actionMap = newMap;
	}

	public void doAction(DoAction doAction) {
		Action toDo = doAction.getAction();
		ObjectTuple ot = actionMap.get(toDo.getName());
		Action orgAct = (Action) ot.getArg1();
		IMemory orgMem = (IMemory) ot.getArg2();
		DoAction newDo = new DoAction(orgAct, this, toDo.getParameters());
		ThisAgentDescription tha = orgMem.read(new ThisAgentDescription(null,
				null, null, null));
		orgMem.write(newDo);
	}

	public ArrayList<? extends Action> getActions() {
		return new ArrayList();
	}

	public void receiveResult(ActionResult result) {
		// TODO Auto-generated method stub

	}

}
