package de.dailab.jiactng.agentcore.execution;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.AbstractActionAuthorizationBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.management.jmx.ActionPerformedNotification;
import de.dailab.jiactng.agentcore.management.jmx.DoActionState;

/**
 * Super class for all implementations of JIAC TNG agent execution cycles.
 * 
 * @author Jan Keiser
 */
public abstract class AbstractExecutionCycle extends AbstractAgentBean implements IExecutionCycle,
    AbstractExecutionCycleMBean {

  private int[]                 workload               = { 0, 0, 0 };
  private final String[]        ATTRIBUTES             = { "ExecutionWorkload", "DoActionWorkload",
      "ActionResultWorkload"                          };
  protected final int           EXECUTION              = 0;
  protected final int           DO_ACTION              = 1;
  protected final int           ACTION_RESULT          = 2;
  private int                   queueSize              = 100;
  private LinkedBlockingQueue[] queues                 = { new LinkedBlockingQueue<Boolean>(queueSize),
      new LinkedBlockingQueue<Boolean>(queueSize), new LinkedBlockingQueue<Boolean>(queueSize) };

  private List<String>          autoExecutionServices  = null;

  private boolean               continousAutoExecution = false;

  /** Class for handling remote agent actions.*/
  private RemoteExecutor remoteExecutor;
  
  /** If true, RemoteExecutor will be used, if false something different.*/
  private boolean useRemoteExecutor = true;
  
  
  @Override
  public void doStart() throws Exception {
	  super.doStart();
	  if (useRemoteExecutor) {
		  remoteExecutor = new RemoteExecutor(memory);
	  }
  }

  @Override
  public void doStop() throws Exception {
	  super.doStop();
	  if (useRemoteExecutor) {
		  remoteExecutor.cleanup();
		  remoteExecutor = null;
	  }
  }

  /**
   * Performs an action request.
   * 
   * @param act
   *          The action invocation.
   * @see AbstractActionAuthorizationBean#authorize(DoAction)
   * @see IEffector#doAction(DoAction)
   * @see #actionPerformed(DoAction, DoActionState, Object[])
   */
  protected void performDoAction(DoAction act) {
	//fishing out delegations
    if (useRemoteExecutor) {
    	if (act.getAction().getProviderDescription() != null && 
    			!act.getAction().getProviderDescription().getAid().equals(thisAgent.getAgentId())) {
    		remoteExecutor.executeRemote(act);
    		return;
    	}
	}

    actionPerformed(act, DoActionState.invoked, null);
    
    IEffector providerBean = ((Action) act.getAction()).getProviderBean();
    if (providerBean != null) {
      try {
        if ((act.getAction().getResultTypes() != null) && (act.getAction().getResultTypes().size() > 0)) {
          Session session = act.getSession();
          if (session.getCurrentCallDepth() == null) {
            session.setCurrentCallDepth(1);
          } else {
            session.setCurrentCallDepth(session.getCurrentCallDepth() + 1);
          }
          memory.write(act.getSession());
        }
        Session session = act.getSession();
        if ((session.getUserToken() == null) && (session.getOriginalProvider() != null)
            && (session.getOriginalUser() != null) && session.getOriginalUser().equals(session.getOriginalProvider())) {
          providerBean.doAction(act);
        } else if (providerBean instanceof AbstractActionAuthorizationBean) {
          ((AbstractActionAuthorizationBean) providerBean).authorize(act);
        } else {
          providerBean.doAction(act);
        }
        actionPerformed(act, DoActionState.started, null);
      } catch (Throwable t) {
        memory.write(new ActionResult(act, t));
        log.error("--- action failed: " + act.getAction().getName(), t);
      }
    } else {
      actionPerformed(act, DoActionState.failed, new Object[] { "Action without provider bean" });
      log.error("--- found action without bean: " + act.getAction().getName());
    }
  }

  /**
   * Processes the result of an action request.
   * 
   * @param actionResult
   *          The result of the action invocation.
   * @see ResultReceiver#receiveResult(ActionResult)
   * @see #actionPerformed(DoAction, DoActionState, Object[])
   */
  protected void processResult(ActionResult actionResult) {
    DoAction doAct = (DoAction) actionResult.getSource();
    actionPerformed(doAct, (actionResult.getFailure() == null) ? DoActionState.success : DoActionState.failed,
        (actionResult.getFailure() == null) ? actionResult.getResults() : new Object[] { actionResult.getFailure() });

    Session session = doAct.getSession();
    if (session.getCurrentCallDepth() == null) {
      session.setCurrentCallDepth(1);
    }
    session.setCurrentCallDepth(session.getCurrentCallDepth() - 1);
    if (memory.read(session) == null) {
      if ((doAct.getAction().getResultTypeNames() != null) && doAct.getAction().getResultTypeNames().size() > 0) {
        log.warn("ActionResult for Action " + actionResult.getAction().getName()
            + " written with non existing Session.");
      }
    } else if (session.getCurrentCallDepth() <= 0) {
      memory.remove(session);
      log.debug("Session removed for action " + doAct.getAction().getName());
    }

    // remove session from memory
    // if (memory.remove(doAct.getSession()) == null){
    // log.warn("ActionResult for Action " + actionResult.getAction().getName() + " written with non existing
    // Session.");
    // } else {
    // log.debug("Session removed for action " + doAct.getAction().getName());
    // }
    // Session template= new
    // Session(doAct.getSessionId(),doAct.getSession().getCreationTime(),null,null);
    // memory.remove(template);

    // inform ResultReceiver
    if (doAct.getSource() == null) {
      // memory.write(actionResult);
      log.debug("No ResultReceiver for action " + doAct.getAction().getName());
    } else {
      ((ResultReceiver) doAct.getSource()).receiveResult(actionResult);
      log.debug("ResultReceiver informed about result of action " + doAct.getAction().getName());
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
   * {@inheritDoc}
   */
  public int getExecutionWorkload() {
    return workload[EXECUTION];
  }

  /**
   * {@inheritDoc}
   */
  public int getDoActionWorkload() {
    return workload[DO_ACTION];
  }

  /**
   * {@inheritDoc}
   */
  public int getActionResultWorkload() {
    return workload[ACTION_RESULT];
  }

  /**
   * Updates the value of a workload considering whether the execution cycle was active or not in the current step.
   * 
   * @param type
   *          The type of workload (one of EXECUTION, DO_ACTION or ACTION_RESULT).
   * @param active
   *          <code>true</code>, if the execution cycle was active in this step.
   */
  protected void updateWorkload(int type, boolean active) {
    // update queue
    LinkedBlockingQueue<Boolean> queue = (LinkedBlockingQueue<Boolean>) queues[type];
    if (queue.remainingCapacity() == 0) {
      queue.poll();
    }
    queue.offer(active);

    // update workload
    int actives = 0;
    for (boolean elem : queue) {
      if (elem) {
        actives++;
      }
    }
    int oldWorkload = workload[type];
    workload[type] = (actives * 100) / queueSize;
    if (oldWorkload != workload[type]) {
      workloadChanged(ATTRIBUTES[type], oldWorkload, workload[type]);
    }
  }

  /**
   * Uses JMX to send notifications that one of the workload attributes of the managed execution cycle has been changed.
   * 
   * @param attribute
   *          The name of the workload attribute.
   * @param oldWorkload
   *          The old value of the workload attribute.
   * @param newWorkload
   *          The new value of the workload attribute.
   */
  private void workloadChanged(String attribute, int oldWorkload, int newWorkload) {
    Notification n = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
        "Workload changed ", attribute, "int", oldWorkload, newWorkload);
    sendNotification(n);
  }

  /**
   * Uses JMX to send notifications that an action was performed by the managed execution cycle of an agent.
   * 
   * @param action
   *          The performed action.
   * @param state
   *          The state of the execution.
   * @param result
   *          The result or failure of the action execution or <code>null</code> if the execution is not yet finished.
   */
  public void actionPerformed(DoAction action, DoActionState state, Object[] result) {
    Notification n = new ActionPerformedNotification(this, sequenceNumber++, System.currentTimeMillis(),
        "Action performed", action, state, result);

    sendNotification(n);
  }

  /**
   * Gets information about all notifications this execution cycle instance may send. This contains also information
   * about the <code>ActionPerformedNotification</code> to notify about performed actions.
   * 
   * @return list of notification information.
   */
  @Override
  public MBeanNotificationInfo[] getNotificationInfo() {
    MBeanNotificationInfo[] parent = super.getNotificationInfo();
    int size = parent.length;
    MBeanNotificationInfo[] result = new MBeanNotificationInfo[size + 1];
    for (int i = 0; i < size; i++) {
      result[i] = parent[i];
    }

    String[] types = new String[] { ActionPerformedNotification.ACTION_PERFORMED };
    String name = ActionPerformedNotification.class.getName();
    String description = "An action was performed";
    MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description);
    result[size] = info;
    return result;
  }

  /**
   * Registers the execution cycle for management
   * 
   * @param manager
   *          the manager for this executionCycle
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
      System.err.println("WARNING: Unable to register execution cycle of agent " + thisAgent.getAgentName()
          + " of agent node " + thisAgent.getAgentNode().getName() + " as JMX resource.");
      System.err.println(e.getMessage());
    }

    _manager = manager;
  }

  /**
   * Deregisters the execution cycle from management
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
      System.err.println("WARNING: Unable to deregister execution cycle of agent " + thisAgent.getAgentName()
          + " of agent node " + thisAgent.getAgentNode().getName() + " as JMX resource.");
      System.err.println(e.getMessage());
    }

    _manager = null;
  }

  @SuppressWarnings("serial")
  public static class TimeoutException extends RuntimeException {
    public TimeoutException(String s) {
      super(s);
    }
  }

  public void setAutoExecutionServices(List<String> actionIds) {
    this.autoExecutionServices = actionIds;
  }

  public List<String> getAutoExecutionServices() {
    return this.autoExecutionServices;

  }

  public void setAutoExecutionType(boolean continous) {
    this.continousAutoExecution = continous;
  }

  public boolean getAutoExecutionType() {
    return this.continousAutoExecution;
  }

}
