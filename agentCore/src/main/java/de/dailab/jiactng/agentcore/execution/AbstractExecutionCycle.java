package de.dailab.jiactng.agentcore.execution;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.AbstractActionAuthorizationBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
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
  private static final String[] ATTRIBUTES             = { "ExecutionWorkload", "DoActionWorkload",
      "ActionResultWorkload"                          };
  protected static final int    EXECUTION              = 0;
  protected static final int    DO_ACTION              = 1;
  protected static final int    ACTION_RESULT          = 2;
  private int                   queueSize              = 100;
  private LinkedBlockingQueue[] queues                 = { new LinkedBlockingQueue<Boolean>(queueSize),
      new LinkedBlockingQueue<Boolean>(queueSize), new LinkedBlockingQueue<Boolean>(queueSize) };

  private List<String>          autoExecutionServices  = null;

  private boolean               continousAutoExecution = false;

  /** Class for handling remote agent actions. */
  private RemoteExecutor        remoteExecutor;

  /** If true, RemoteExecutor will be used, if false something different. */
  private boolean               useRemoteExecutor      = true;

  /**
   * During start of the execution cycle an optional remote executor will be created.
   * 
   * @throws Exception
   *           if the execution cycle can not be started.
   * @see AbstractAgentBean#doStart()
   * @see #setUseRemoteExecutor(boolean)
   * @see RemoteExecutor#RemoteExecutor(de.dailab.jiactng.agentcore.knowledge.IMemory, org.apache.commons.logging.Log)
   */
  @Override
  public void doStart() throws Exception {
    super.doStart();
    
    boolean commBeanFound = false;
    List<IAgentBean> abList = thisAgent.getAgentBeans();
    for(IAgentBean iab : abList) {
      if(iab instanceof ICommunicationBean) {
        commBeanFound = true;
        break;
      }
    }
    
    if(!commBeanFound) {
      log.warn("Could not find CommunicationBean in this agent - RemoteExecutors are disabled!");
      useRemoteExecutor = false;
    }
    
    if (useRemoteExecutor) {
      remoteExecutor = new RemoteExecutor(memory, thisAgent.getLog(this, "RemoteExecutor"));
    }
  }

  /**
   * During stop of the execution cycle an existing remote executor will be destroyed.
   * 
   * @throws Exception
   *           if the execution cycle can not be stopped.
   * @see AbstractAgentBean#doStop()
   * @see #setUseRemoteExecutor(boolean)
   * @see RemoteExecutor#cleanup()
   */
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
    if (act.getSession().isTimeout()) {
      log.warn("Session for DoAction is timed out, returning failure.");
      memory.write(new ActionResult(act, new TimeoutException(TIMEOUT_MESSAGE)));
      // actionPerformed(act, DoActionState.failed, null);
      return;
    }

    // fishing out delegations
    if (useRemoteExecutor) {
      if (act.getAction().getProviderDescription() != null
          && !act.getAction().getProviderDescription().getAid().equals(thisAgent.getAgentId())) {
        remoteExecutor.executeRemote(act);
        return;
      }
    }

    actionPerformed(act, DoActionState.invoked, null);

    final IEffector providerBean = ((Action) act.getAction()).getProviderBean();
    if (providerBean != null) {
      try {
        if ((act.getAction().getResultTypes() != null) && (act.getAction().getResultTypes().size() > 0)) {
          final Session session = act.getSession();
          if (session.getCurrentCallDepth() == null) {
            session.setCurrentCallDepth(1);
          } else {
            session.setCurrentCallDepth(session.getCurrentCallDepth().intValue() + 1);
          }
          memory.write(act.getSession());
        }

        // test for authorization if applicable
        final Session session = act.getSession();
        if ((session.getUserToken() == null) && (session.getOriginalProvider() != null)
            && (session.getOriginalUser() != null) && session.getOriginalUser().equals(session.getOriginalProvider())) {
          // no user token, and user is equal to provider - invoke is allowed
          providerBean.doAction(act);

        } else if (providerBean instanceof AbstractActionAuthorizationBean) {
          // use authorizationAction
          ((AbstractActionAuthorizationBean) providerBean).authorize(act);

        } else {
          // no authorization required
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
    final DoAction doAct = (DoAction) actionResult.getSource();
    actionPerformed(doAct, (actionResult.getFailure() == null) ? DoActionState.success : DoActionState.failed,
        (actionResult.getFailure() == null) ? actionResult.getResults() : new Object[] { actionResult.getFailure() });

    final Session session = doAct.getSession();
    if (session.getCurrentCallDepth() == null) {
      session.setCurrentCallDepth(1);
    }
    session.setCurrentCallDepth(session.getCurrentCallDepth().intValue() - 1);
    if (memory.read(session) == null) {
      if ((doAct.getAction().getResultTypeNames() != null) && doAct.getAction().getResultTypeNames().size() > 0) {
        if (doAct.getSession().isTimeout()) {
          log.info("ActionResult for Action " + actionResult.getAction().getName() + " written after session timeout");
        } else {
          log.warn("ActionResult for Action " + actionResult.getAction().getName()
              + " written with non existing Session.");
        }
      }
    } else if (session.getCurrentCallDepth().intValue() <= 0) {
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
      if (actionResult.getSession().isTimeout() && !(actionResult.getFailure() instanceof TimeoutException)) {
        log.debug("Skipping result due to session timeout");
      
      } else {
        ((ResultReceiver) doAct.getSource()).receiveResult(actionResult);
        log.debug("ResultReceiver informed about result of action " + doAct.getAction().getName());
      }
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
    final LinkedBlockingQueue<Boolean> queue = (LinkedBlockingQueue<Boolean>) queues[type];
    if (queue.remainingCapacity() == 0) {
      queue.poll();
    }
    queue.offer(Boolean.valueOf(active));

    // update workload
    int actives = 0;
    for (boolean elem : queue) {
      if (elem) {
        actives++;
      }
    }
    final int oldWorkload = workload[type];
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
    final Notification n = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
        "Workload changed ", attribute, "int", Integer.valueOf(oldWorkload), Integer.valueOf(newWorkload));
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
    final Notification n = new ActionPerformedNotification(this, sequenceNumber++, System.currentTimeMillis(),
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
    final MBeanNotificationInfo[] parent = super.getNotificationInfo();
    final int size = parent.length;
    final MBeanNotificationInfo[] result = new MBeanNotificationInfo[size + 1];
    for (int i = 0; i < size; i++) {
      result[i] = parent[i];
    }

    final String[] types = new String[] { ActionPerformedNotification.ACTION_PERFORMED };
    final String name = ActionPerformedNotification.class.getName();
    final String description = "An action was performed";
    final MBeanNotificationInfo info = new MBeanNotificationInfo(types, name, description);
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

  /**
   * This exception will be thrown, if the timeout for action execution is reached.
   * 
   * @author Jan Keiser
   */
  @SuppressWarnings("serial")
  public static class TimeoutException extends RuntimeException {

    /**
     * Creates a timeout exception with a given description.
     * 
     * @param s
     *          the description
     */
    public TimeoutException(String s) {
      super(s);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void setAutoExecutionServices(List<String> actionIds) {
    autoExecutionServices = actionIds;
  }

  /**
   * {@inheritDoc}
   */
  public final List<String> getAutoExecutionServices() {
    return autoExecutionServices;

  }

  /**
   * {@inheritDoc}
   */
  public final void setAutoExecutionType(boolean continous) {
    continousAutoExecution = continous;
  }

  /**
   * {@inheritDoc}
   */
  public final boolean getAutoExecutionType() {
    return continousAutoExecution;
  }

  /**
   * Check if a remote executor is used.
   * 
   * @return remote executor is used or not
   */
  public final boolean isUseRemoteExecutor() {
    return useRemoteExecutor;
  }

  /**
   * Set that a remote executor will be used or not.
   * 
   * @param newUseRemoteExecutor
   *          <code>true</code> if a remote executor will be used
   */
  public final void setUseRemoteExecutor(boolean newUseRemoteExecutor) {
    useRemoteExecutor = newUseRemoteExecutor;
  }

}
