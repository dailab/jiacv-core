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
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * @deprecated this class is due to be removed!
 */
@Deprecated
public class SimpleLocalCommBean extends AbstractAgentBean implements Runnable,
		IEffector, ResultReceiver {

	private static HashMap<String, Action> actionMap = new HashMap<String, Action>();

	private Future myThread = null;

	private AgentDescription myDesc = null;

	private Boolean flag = new Boolean(false);

	@Override
	public void doInit() throws Exception {
		super.doInit();
		// synchronized (actionMap) {
		// processActionMap();
		// }
	}

	@Override
	public void doStart() throws Exception {
		super.doStart();
		myDesc = thisAgent.getAgentDescription();
		synchronized (flag) {
			this.flag = new Boolean(true);
		}
		ArrayList<Action> actions = thisAgent.getActionList();
		synchronized (actionMap) {
			for (Action a : actions) {
				actionMap.put(thisAgent.getAgentName() + ":" + a.getName(), a);
			}
		}
		myThread = thisAgent.getThreadPool().submit(this);
	}

	@Override
	public void doStop() throws Exception {
		super.doStop();
		synchronized (flag) {
			this.flag = new Boolean(false);
		}
		ArrayList<Action> actions = thisAgent.getActionList();
		synchronized (actionMap) {
			for (Action a : actions) {
				actionMap.remove(thisAgent.getAgentName() + ":" + a.getName());
			}
		}
		// myThread.cancel(true);
	}

	@Override
	public void doCleanup() throws Exception {
		super.doCleanup();
	}

	public void run() {
		while (flag.booleanValue()) {
			synchronized (actionMap) {

				Set<Action> memActions = memory.readAll(new Action(null, null,
						null, null));

				// check all memActions against actionMap:
				for (Action memAct : memActions) {
					boolean found = false;
					for (String s : actionMap.keySet()) {
						Action mapAct = actionMap.get(s);
						if (memAct.getName().equals(mapAct)) {
							found = true;
						}
					}
					if (found == false) {
						memory.remove(new Action(memAct.getName(), null, null,
								null));
					}
				}

				// check actionMap against memActions:
				for (String s : actionMap.keySet()) {
					Action mapAct = actionMap.get(s);
					if(memory.read(new Action(mapAct.getName(), null, null,
							null))==null) {
						memory.write(mapAct);
					}
				}

				// processActionMap();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void processActionMap() {
		HashMap<String, Action> newMap = new HashMap<String, Action>();
		for (String s : actionMap.keySet()) {
			Action a = actionMap.get(s);
			try {
				if (a.getProviderBean() != null
						&& null == memory.read(new Action(s, null, null, null))) {
					memory.write(a);
					newMap.put(s, a);
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
		Action orgAct = actionMap.get(toDo.getName());
		// IMemory orgMem = (IMemory) ot.getArg2();
		// DoAction newDo = new DoAction(orgAct, this, toDo.getParameters());
		// ThisAgentDescription tha = orgMem.read(new ThisAgentDescription(null,
		// null, null, null));
		// orgMem.write(newDo);
	}

	public ArrayList<? extends Action> getActions() {
		return new ArrayList();
	}

	public void receiveResult(ActionResult result) {
		// TODO Auto-generated method stub

	}

}
