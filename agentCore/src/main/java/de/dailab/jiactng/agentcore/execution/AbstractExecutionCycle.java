package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;

import org.apache.log4j.Level;

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
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * Super class for all implementations of JIAC TNG agent execution cycles.
 * 
 * @author Jan Keiser
 */
public abstract class AbstractExecutionCycle extends AbstractAgentBean implements IExecutionCycle, AbstractExecutionCycleMBean {

   private int[] workload = { 0, 0, 0 };
   private static final String[] ATTRIBUTES = { "ExecutionWorkload", "DoActionWorkload", "ActionResultWorkload" };
   protected static final int EXECUTION = 0;
   protected static final int DO_ACTION = 1;
   protected static final int ACTION_RESULT = 2;
   private int queueSize = 100;
   private LinkedBlockingQueue[] queues = { new LinkedBlockingQueue<Boolean>(queueSize), new LinkedBlockingQueue<Boolean>(queueSize), new LinkedBlockingQueue<Boolean>(queueSize) };

   protected Map<String, Map<String, Serializable>> autoExecutionServices = null;
   protected Map<String, Integer> servicesExecutionTimes = null;

   /** Class for handling remote agent actions. */
   private RemoteExecutor remoteExecutor;

   /** If true, RemoteExecutor will be used, if false something different. */
   private boolean useRemoteExecutor = true;
   
   protected long time;

   /**
    * During start of the execution cycle an optional remote executor will be created.
    * 
    * @throws Exception if the execution cycle can not be started.
    * @see AbstractAgentBean#doStart()
    * @see #setUseRemoteExecutor(boolean)
    * @see RemoteExecutor#RemoteExecutor(de.dailab.jiactng.agentcore.knowledge.IMemory, org.apache.commons.logging.Log)
    */
   @Override
   public void doStart() throws Exception {
      super.doStart();
      time = System.currentTimeMillis();
      if (thisAgent.getCommunication() == null) {
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
    * @throws Exception if the execution cycle can not be stopped.
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
    * If an action has no return parameter than it will not wait for receiving the result.
    * 
    * @param act The action invocation.
    * @see AbstractActionAuthorizationBean#authorize(DoAction)
    * @see IEffector#doAction(DoAction)
    * @see #actionPerformed(DoAction, DoActionState, Object[])
    */
   protected void performDoAction(DoAction act) {
      if (act.getSession().isTimeout()) {
         log.warn("Session " + act.getSessionId() + " for DoAction " + act.getAction().getName() + " is timed out, returning failure.");
         memory.write(new ActionResult(act, new TimeoutException(TIMEOUT_MESSAGE)));
         actionPerformed(act, DoActionState.failed, new Object[] { "Session timed out" });
         return;
      }

      // adding original Agent of the Action only if not yet set
      if (act.getSession().getOriginalAgentDescription() == null) {
         act.getSession().setOriginalAgentDescription(this.thisAgent.getAgentDescription());
      }

      // fishing out delegations
      if (useRemoteExecutor) {
         if (act.getAction().getProviderDescription() != null && !act.getAction().getProviderDescription().getAid().equals(thisAgent.getAgentId())) {
            remoteExecutor.executeRemote(act);
            return;
         }
      }

      actionPerformed(act, DoActionState.invoked, null);

      final IEffector providerBean = ((Action) act.getAction()).getProviderBean();
      if (providerBean != null) {
         try {
            if ((act.getAction().getResultTypeNames() != null) && (act.getAction().getResultTypeNames().size() > 0)) {
               final Session session = act.getSession();
               if (session.getCurrentCallDepth() == null) {
                  session.setCurrentCallDepth(1);
               } else {
                  session.setCurrentCallDepth(session.getCurrentCallDepth().intValue() + 1);
               }
               if (log.isInfoEnabled()) {
                  log.info("Writing session for " + act.getAction().getName() + " to memory: " + act.getSessionId() + " (" + session.getCurrentCallDepth() + ")");
               }
               memory.write(act.getSession());
            }

            // test for authorization if applicable
            final Session session = act.getSession();
            if ((session.getUserToken() == null) && (session.getOriginalProvider() != null) && (session.getOriginalUser() != null) && session.getOriginalUser().equals(session.getOriginalProvider())) {
               // no user token, and user is equal to provider - invoke is allowed
               if (log.isInfoEnabled()) {
                  log.info("Calling doAction with authorization: " + act.getAction().getName() + " (" + act.getSessionId() + ")");
               }
               providerBean.doAction(act);

            } else if (providerBean instanceof AbstractActionAuthorizationBean) {
               // use authorizationAction
               if (log.isInfoEnabled()) {
                  log.info("Checking authorization for action: " + act.getAction().getName() + " (" + act.getSessionId() + ")");
               }
               ((AbstractActionAuthorizationBean) providerBean).authorize(act);

            } else {
               // no authorization required
               if (log.isInfoEnabled()) {
                  log.info("Calling doAction: " + act.getAction().getName() + " (" + act.getSessionId() + ")");
               }
               providerBean.doAction(act);
            }
         } catch (Throwable t) {
            memory.write(new ActionResult(act, t));
            log.error("--- action failed: " + act.getAction().getName() + " (" + act.getSessionId() + ")", t);
         }
      } else {
         actionPerformed(act, DoActionState.failed, new Object[] { "Action without provider bean" });
         log.error("--- found action without bean: " + act.getAction().getName());
      }
   }

   /**
    * Processes the result of an action request.
    * 
    * @param actionResult The result of the action invocation.
    * @see ResultReceiver#receiveResult(ActionResult)
    * @see #actionPerformed(DoAction, DoActionState, Object[])
    */
   protected void processResult(ActionResult actionResult) {
      final DoAction doAct = (DoAction) actionResult.getSource();
      actionPerformed(doAct, (actionResult.getFailure() == null) ? DoActionState.success : DoActionState.failed, (actionResult.getFailure() == null) ? actionResult.getResults() : new Object[] { actionResult.getFailure() });

      final Session session = doAct.getSession();

      if ((doAct.getAction().getResultTypeNames() != null) && (doAct.getAction().getResultTypeNames().size() > 0)) {
         if (session.getCurrentCallDepth() == null) {
            if (log.isEnabledFor(Level.WARN)) {
               log.warn("Found session with call-depth null. Setting calldepth to 1 for: " + doAct.getAction().getName() + " (" + session.getSessionId() + ")");
            }
            session.setCurrentCallDepth(1);
         }

         session.setCurrentCallDepth(session.getCurrentCallDepth().intValue() - 1);
      }
      if (memory.read(session) == null) {
         if ((doAct.getAction().getResultTypeNames() != null) && doAct.getAction().getResultTypeNames().size() > 0) {
            if (doAct.getSession().isTimeout()) {
               log.info("ActionResult for Action " + actionResult.getAction().getName() + " (" + actionResult.getSessionId() + ") written after session timeout");
            } else {
               log.warn("ActionResult for Action " + actionResult.getAction().getName() + " written with unknown Session: " + doAct.getSessionId());
            }
         }
      } else if (session.getCurrentCallDepth().intValue() <= 0) {
         if (log.isInfoEnabled()) {
            log.info("Removing session for " + actionResult.getAction().getName() + " from memory: " + session.getSessionId() + " (" + session.getCurrentCallDepth() + ")");
         }
         memory.remove(session);
      }

      // inform ResultReceiver
      if (doAct.getSource() == null) {
         // memory.write(actionResult);
         log.debug("No ResultReceiver for action " + doAct.getAction().getName() + " (" + doAct.getSessionId() + ")");
      } else {
         if (actionResult.getSession().isTimeout() && !(actionResult.getFailure() instanceof TimeoutException)) {
            log.debug("Skipping result of " + actionResult.getAction().getName() + " (" + actionResult.getSessionId() + ") due to session timeout");

         } else {
            ((ResultReceiver) doAct.getSource()).receiveResult(actionResult);
            log.debug("ResultReceiver informed about result of action " + doAct.getAction().getName() + " (" + doAct.getSessionId() + ")");
         }
      }
   }

   /**
    * Handle session timeout of DoAction. This will (try to) invoke the cancelAction method
    * of the provider bean, if any, or if that method returned null, just create the
    * standard timeout action result.
    *  
    * @param session		the session
    * @param doAction		the do-action to cancel
    */
   protected void processSessionTimeout(Session session, DoAction doAction) {
	   final Action action = (Action) doAction.getAction();
		log.info("Canceling DoAction " + doAction);

		ActionResult result = null;
		if ((action == null)) {
			log.warn("Found doAction with missing action:" + doAction);
		} else if (action.getProviderBean() == null) {
			// TODO: this happens always with transmitted doActions due to transient fields. Is there a better solution?
			if(thisAgent.getAgentDescription().getAid().equals(action.getProviderDescription().getAid())) {
				log.info("Found doAction with missing providerBean:" + doAction);
			}
		} else {
			result = action.getProviderBean().cancelAction(doAction);
		}

		// if no result was created, use TimeoutExecption as default result
		if (result == null) {
			if(doAction.getSource()!=null) {
				result = new ActionResult(doAction, new TimeoutException(TIMEOUT_MESSAGE));
			}
		}

		if ((doAction.getSource() != null) && (doAction.getSource() instanceof ResultReceiver)) {
			log.debug("sending timeout Result to source of Session " + session);
			final ResultReceiver receiver = (ResultReceiver)doAction.getSource();
	
			receiver.receiveResult(result);
		} else {
		  // TODO: this happens always with transmitted doActions due to transient fields. Is there a better solution? 
			log.info("Session without Result-Receiver Source: DoAction had to be canceled due to sessiontimeout " + doAction);
		}
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
    * @param type The type of workload (one of EXECUTION, DO_ACTION or ACTION_RESULT).
    * @param active <code>true</code>, if the execution cycle was active in this step.
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
    * @param attribute The name of the workload attribute.
    * @param oldWorkload The old value of the workload attribute.
    * @param newWorkload The new value of the workload attribute.
    */
   private void workloadChanged(String attribute, int oldWorkload, int newWorkload) {
      final Notification n = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(), "Workload changed ", attribute, "int", Integer.valueOf(oldWorkload), Integer.valueOf(newWorkload));
      sendNotification(n);
   }

   /**
    * Uses JMX to send notifications that an action was performed by the managed execution cycle of an agent.
    * 
    * @param action The performed action.
    * @param state The state of the execution.
    * @param result The result or failure of the action execution or <code>null</code> if the execution is not yet finished.
    */
   public void actionPerformed(DoAction action, DoActionState state, Object[] result) {
	  String msg;
	  switch (state) {
	  case invoked:
		  msg = "Action invoked";
		  break;
	  case success:
		  msg = "Action succeeded";
		  break;
	  case failed:
		  msg = "Action failed";
		  break;
	  default:
		  msg = "Action performed";
	  }
      final Notification n = new ActionPerformedNotification(this, sequenceNumber++, System.currentTimeMillis(), msg, action, state, result);

      sendNotification(n);
   }

   /**
    * Gets information about all notifications this execution cycle instance may send. This contains also information about the <code>ActionPerformedNotification</code> to notify
    * about performed actions.
    * 
    * @return list of notification information.
    */
   @Override
   public MBeanNotificationInfo[] getNotificationInfo() {
      final MBeanNotificationInfo[] parent = super.getNotificationInfo();
      final int size = parent.length;
      final MBeanNotificationInfo[] result = new MBeanNotificationInfo[size + 1];
//      for (int i = 0; i < size; i++) {
//         result[i] = parent[i];
//      }
      System.arraycopy(parent, 0, result, 0, size);

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
    * @param manager the manager for this executionCycle
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
         System.err.println("WARNING: Unable to register execution cycle of agent " + thisAgent.getAgentName() + " of agent node " + thisAgent.getAgentNode().getName() + " as JMX resource.");
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
         System.err.println("WARNING: Unable to deregister execution cycle of agent " + thisAgent.getAgentName() + " of agent node " + thisAgent.getAgentNode().getName() + " as JMX resource.");
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
       * @param s the description
       */
      public TimeoutException(String s) {
         super(s);
      }
   }

	/**
	 * Handles auto-invocation of actions.
	 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#invoke(IActionDescription, Serializable[])
	 */
	protected void processAutoExecutionServices(){
		if(autoExecutionServices != null){
			for(final Iterator<Entry<String, Map<String, Serializable>>> entries = autoExecutionServices.entrySet().iterator(); entries.hasNext(); ){
				final Entry<String, Map<String, Serializable>> entry = entries.next();
				final String actionName = entry.getKey();
				final Map<String, Serializable> config = entry.getValue();
				Object startTimeO = config.get("startTime");
				Object intervalTimeO = config.get("intervalTime");
				String providerName = (String) config.get("provider");
				Integer startTime = null;
				Integer intervalTime = null;
				if(startTimeO != null){
					startTime = Integer.parseInt(startTimeO.toString());
				}if(intervalTimeO != null){
					intervalTime = Integer.parseInt(intervalTimeO.toString());
				}
				if(startTime != null){
					if(startTime > (System.currentTimeMillis() - time)){
						continue;
					}

					if(intervalTime == null ){
						if(servicesExecutionTimes.get(actionName) >= 1){
							servicesExecutionTimes.remove(actionName);
							entries.remove();
							continue;
						}
					}else if((System.currentTimeMillis() - time - startTime) / intervalTime < servicesExecutionTimes.get(actionName)){
						continue;
					}
				}else{
					if(intervalTime == null){
						if(servicesExecutionTimes.get(actionName) >= 1){
							servicesExecutionTimes.remove(actionName);
							entries.remove();
							continue;
						}
					}else if((System.currentTimeMillis() - time) / intervalTime < servicesExecutionTimes.get(actionName)){
						continue;					  
					}
				}
				servicesExecutionTimes.put(actionName, servicesExecutionTimes.get(actionName) + 1);
				Action action = new Action(actionName);
				IActionDescription actionD = null;
				if(providerName == null){
					actionD = thisAgent.searchAction(action);
				}else{
					List<IActionDescription> actions = thisAgent.searchAllActions(action);
					for(IActionDescription a : actions){
						if(a.getProviderDescription().getName().equals(providerName)){
							actionD = a;
						}
					}
				}
				if(actionD == null){
					log.warn("Action: " + actionName + " not found");
					continue;
				}
				List<Serializable> params = (List<Serializable>) config.get("params");
				Serializable[] p = null;
				if(params != null){
					List<Class<?>> paramTypes = null;
					try {
						paramTypes = actionD.getInputTypes();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					p = new Serializable[params.size()];
					for(int i = 0; i < params.size(); i++){
						Class<?> inputType = paramTypes.get(i);
						String inputTypeAsString = inputType.toString();
						if(inputTypeAsString.equals("int") || inputTypeAsString.equals("class java.lang.Integer")){
							p[i] = (Serializable) Integer.parseInt((String)params.get(i));
						}else if(inputTypeAsString.equals("double") || inputTypeAsString.equals("class java.lang.Double")){
							p[i] = (Serializable) Double.parseDouble((String)params.get(i));
						}else if(inputTypeAsString.equals("boolean") || inputTypeAsString.equals("class java.lang.Boolean")){
							p[i] = (Serializable) Boolean.parseBoolean((String)params.get(i));
						}else{
							p[i] = (Serializable) inputType.cast(params.get(i));
						}

					}
				}
				invoke(actionD, p);

			}
		}
	}

   /**
    * {@inheritDoc}
    */
   public final void setAutoExecutionServices(Map<String, Map<String, Serializable>> autoExecutionServices) {
      this.autoExecutionServices = autoExecutionServices;
      servicesExecutionTimes = new HashMap<String, Integer>();
      for(String actionName : autoExecutionServices.keySet()){
    	  servicesExecutionTimes.put(actionName, 0);
      }
   }

   /**
    * {@inheritDoc}
    */
   public final Map<String, Map<String, Serializable>> getAutoExecutionServices() {
      return autoExecutionServices;

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
    * @param newUseRemoteExecutor <code>true</code> if a remote executor will be used
    */
   public final void setUseRemoteExecutor(boolean newUseRemoteExecutor) {
      useRemoteExecutor = newUseRemoteExecutor;
   }

}
