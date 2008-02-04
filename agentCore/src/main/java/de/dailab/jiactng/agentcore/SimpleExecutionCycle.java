/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.management.jmx.ActionPerformedNotification;

/**
 * A simple ExecutionCycle implementation. This class executes active agentbeans
 * (those agentbeans where the <code>executionInterval</code> is set to a
 * value greater than 0) and takes care of action requests (<code>DoAction</code>)
 * and their results (<code>ActionResult</code>).
 * 
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see IExecutionCycle
 * @see DoAction
 * @see ActionResult
 */
public class SimpleExecutionCycle extends AbstractAgentBean implements
		IExecutionCycle {

	/**
	 * Run-method for the execution cycle. The method iterates over the list of
	 * agentbeans and calls the execute method of each <i>active</i> agentbean.
	 * 
	 * This method also takes care of new <code>DoAction</code>s and 
	 * <code>ActionResult</code>s.
	 * 
	 * The <code>SimpleExecutionCycle</code> only executes agentbeans and
	 * handles DoActions and ActionResults when it has reached 
	 * <code>LifecycleStates.STARTED</code>.
	 * 
	 * @see de.dailab.jiactng.agentcore.IExecutionCycle#run()
	 * @see LifecycleStates
	 */
	public void run() {
		//check if lifecycle has been started --> execute if STARTED
		if (getState() == LifecycleStates.STARTED) {
			// execute the ripest bean
			IAgentBean minBean = null;
			long minExecutionTime = Long.MAX_VALUE;
			long now = System.currentTimeMillis();
			for (IAgentBean bean : thisAgent.getAgentBeans()) {
				// check bean's state, if not started --> reject
				if (bean.getState() != LifecycleStates.STARTED) {
					continue;
				}
	
				// check if bean has cyclic behavior, if not --> reject
				if (bean.getExecuteInterval() <= 0) {
					continue;
				}
	
				// execution time not reached yet --> reject
				if (bean.getNextExecutionTime() > now) {
					continue;
				}
	
				// execution time is not minimum --> reject
				if (bean.getNextExecutionTime() > minExecutionTime) {
					continue;
				}
	
				minBean = bean;
				minExecutionTime = bean.getNextExecutionTime();
			}
	
			// if there is a minBean then execute
			if (minBean != null) {
				try {
					minBean.execute();
				} catch (Exception ex) {
					log.error("Error when executing bean \'"
							+ minBean.getBeanName() + "\'", ex);
				}
	
				// reschedule bean
				minBean.setNextExecutionTime(now + minBean.getExecuteInterval());
			} 
//			else {
//				log.debug("No active beans to execute");
//			}
	
			// process one doAction
			// TODO: check if read can be used
			DoAction act = memory.remove(new DoAction(null, null, null, null));
	
			if (act != null) {
				synchronized (this) {
					performDoAction(act);
				}
			}
	
			// process one actionResult
			// TODO: check if read can be used
			ActionResult actionResult = memory.remove(new ActionResult(null, null));
			if (actionResult != null) {
				synchronized (this) {
					processResult(actionResult);
				}
			}
		} 

		//reject execution if SimpleExecutionCycle hasn't been started
		else {
			log.error("Trying to run SimpleExecutionCycle in state "
					+ getState());
		}
	}

	private void performDoAction(DoAction act) {
		if (act.getAction().getProviderBean() != null) {
			memory.write(act.getSession());
			long start = System.nanoTime();
			act.getAction().getProviderBean().doAction(act);
			long end = System.nanoTime();
			actionPerformed(act, end-start);
		} else {
			log.error("--- found action without bean: "
					+ act.getAction().getName());
		}
	}

	private void processResult(ActionResult actionResult) {
		// entweder den ResultReceiver informieren oder das Ergebnis in
		// den Memory schreiben
		if (((DoAction) actionResult.getSource()) == null
				|| ((DoAction) actionResult.getSource()).getSource() == null) {
			memory.write(actionResult);
		} else {
			((ResultReceiver) ((DoAction) actionResult.getSource()).getSource())
					.receiveResult(actionResult);
		}
		// ArrayList history = actionResult.getSession().getHistory();
		// for (int i = history.size() - 1; i >= 0; i--) {
		// if (history.get(i) instanceof DoAction) {
		// ((ResultReceiver) ((DoAction) history.get(i))
		// .getSource()).receiveResult(actionResult);
		// break;
		// }
		// }
	}

    /**
     * Uses JMX to send notifications that an action was performed
     * by the managed execution cycle of an agent.
     *
     * @param action the performed action.
     * @param duration the duration of the execution.
     */
    public void actionPerformed(DoAction action, long duration) {
        Notification n =
                new ActionPerformedNotification(this,
                sequenceNumber++,
                System.currentTimeMillis(),
                "Action performed",
                action, duration);
        
        sendNotification(n);
    }

    /**
     * Gets information about all notifications this execution cycle instance may send.
     * This contains also information about the <code>ActionPerformedNotification</code> 
     * to notify about performed actions.
     * @return list of notification information.
     */
    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
    	MBeanNotificationInfo[] parent = super.getNotificationInfo();
    	int size = parent.length;
    	MBeanNotificationInfo[] result = new MBeanNotificationInfo[size + 1];
    	for (int i=0; i<size; i++) {
    		result[i] = parent[i];
    	}
    	
        String[] types = new String[] {
        		ActionPerformedNotification.ACTION_PERFORMED
        };
        String name = ActionPerformedNotification.class.getName();
        String description = "An action was performed";
        MBeanNotificationInfo info =
                new MBeanNotificationInfo(types, name, description);
        result[size] = info;
        return result;
    }
    
	/**
	 * Registers the execution cycle for management
	 * 
	 * @param manager
	 *            the manager for this executionCycle
	 */
	public void enableManagement(Manager manager) {
		// do nothing if management already enabled
		if (isManagementEnabled()) {
			return;
		}

		// register execution cycle for management
		try {
			manager.registerAgentResource(thisAgent, "ExecutionCycle", this);
		} catch (Exception e) {
			System.err
					.println("WARNING: Unable to register execution cycle of agent "
							+ thisAgent.getAgentName()
							+ " of agent node "
							+ thisAgent.getAgentNode().getName()
							+ " as JMX resource.");
			System.err.println(e.getMessage());
		}

		_manager = manager;
	}

	/**
	 * Deregisters the execution cycle from management
	 * 
	 * @param manager
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}

		// deregister execution cycle from management
		try {
			_manager.unregisterAgentResource(thisAgent, "ExecutionCycle");
		} catch (Exception e) {
			System.err
					.println("WARNING: Unable to deregister execution cycle of agent "
							+ thisAgent.getAgentName()
							+ " of agent node "
							+ thisAgent.getAgentNode().getName()
							+ " as JMX resource.");
			System.err.println(e.getMessage());
		}

		_manager = null;
	}
}
