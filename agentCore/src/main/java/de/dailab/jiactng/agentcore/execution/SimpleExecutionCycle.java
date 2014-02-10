/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.SessionEvent;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * A simple ExecutionCycle implementation. This class executes active agentbeans (those agentbeans where the
 * <code>executionInterval</code> is set to a value greater than 0) and takes care of action requests (
 * <code>DoAction</code>) and their results (<code>ActionResult</code>).
 * 
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see IExecutionCycle
 * @see DoAction
 * @see ActionResult
 */
public final class SimpleExecutionCycle extends AbstractExecutionCycle {

  private static final Session      SESSION_TEMPLATE      = new Session(null, null, null, null);

  private static final ActionResult ACTIONRESULT_TEMPLATE = new ActionResult(null, null);

  private static final DoAction     DOACTION_TEMPLATE     = new DoAction(null, null, null, null);

  /**
   * Run-method for the execution cycle. The method iterates over the list of agentbeans and calls the execute method of
   * each <i>active</i> agentbean.
   * 
   * This method also takes care of new <code>DoAction</code>s and <code>ActionResult</code>s.
   * 
   * The <code>SimpleExecutionCycle</code> only executes agentbeans and handles DoActions and ActionResults when it has
   * reached <code>LifecycleStates.STARTED</code>.
   * 
   * @see de.dailab.jiactng.agentcore.execution.IExecutionCycle#run()
   * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
   */
  public final void run() {
    // check if lifecycle has been started --> execute if STARTED
    if (getState() == LifecycleStates.STARTED) {

      // execute one Beans execute Method
      processBeanExecutes();

      // process one DoAction
      processDoActions();

      // process one ActionResult
      processActionResults();

      processSessionTimeouts();
      
      processAutoExecutionServices();

    }

    // reject execution if SimpleExecutionCycle hasn't been started
    else {
      log.error("Trying to run SimpleExecutionCycle in state " + getState());
    }
  }

  /**
   * Find one bean whose execute Method is due and execute it.
   */
  private void processBeanExecutes() {
    IAgentBean minBean = null;
    long minExecutionTime = Long.MAX_VALUE;
    final long now = System.currentTimeMillis();
    for (IAgentBean bean : thisAgent.getAgentBeans()) {
      // check bean's state, if not started --> reject
      if (bean.getState() != LifecycleStates.STARTED) {
        continue;
      }

      // check if bean has cyclic behavior, if not --> reject
      if (bean.getExecutionInterval() <= 0) {
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
    boolean executionDone = false;
    if (minBean != null) {
      executionDone = true;
      try {
        minBean.execute();
      } catch (Exception ex) {
        log.error("Error when executing bean \'" + minBean.getBeanName() + "\'", ex);
      }

      // reschedule bean
      minBean.setNextExecutionTime(now + minBean.getExecutionInterval());
    }

    updateWorkload(EXECUTION, executionDone);
  }

  /**
   * Process one DoAction from the Memory
   */
  private void processDoActions() {
    final DoAction act = memory.remove(DOACTION_TEMPLATE);

    boolean actionPerformed = false;
    if (act != null) {
      actionPerformed = true;
      synchronized (this) {
        performDoAction(act);
      }
    }
    updateWorkload(DO_ACTION, actionPerformed);
  }

  /**
   * Process one ActionResult from the Memory
   */
  private void processActionResults() {
    final ActionResult result = memory.remove(ACTIONRESULT_TEMPLATE);
    
    boolean resultProcessed = false;
    if(result != null) {
      resultProcessed = true;
      synchronized(this) {
        processResult(result);
      }
    }
    
    updateWorkload(ACTION_RESULT, resultProcessed);
  }

  /**
   * Session-Cleanup
   * 
   * If Session has a timeout
   */
  private void processSessionTimeouts() {
    synchronized (memory) {
      final Set<Session> sessions = memory.readAll(SESSION_TEMPLATE);
      for (Session session : sessions) {
        if (session.isTimeout()) {
          // session has timeout
          log.warn(TIMEOUT_MESSAGE + session);

          final ArrayList<SessionEvent> history = session.getHistory();

          // Does Session is related to DoAction?
          boolean doActionFound = false;
          for (SessionEvent event : history) {
            if (event instanceof DoAction) {
              // doAction found
              doActionFound = true;
              final DoAction doAction = (DoAction) event;
              memory.remove(doAction);

              if (doAction.getAction() instanceof Action) {
                // Got an Action, so let's cancel this doAction

                final Action action = (Action) doAction.getAction();
                log.info("Canceling DoAction " + doAction);

                ActionResult result = null;
                if ((action == null)) {
                  log.warn("Found doAction with missing action:" + doAction);
                } else if (action.getProviderBean() == null) {
                  log.warn("Found doAction with missing providerBean:" + action);
                } else {
                  result = action.getProviderBean().cancelAction(doAction);
                }

                // if no result was created, use TimeoutExecption as default result
                if (result == null) {
                  result = new ActionResult(doAction, new TimeoutException(TIMEOUT_MESSAGE));
                }
                
                if ((doAction.getSource() != null) && (doAction.getSource() instanceof ResultReceiver)) {
                  log.debug("sending timeout Result to source of DoAction " + doAction);
                  final ResultReceiver receiver = (ResultReceiver)doAction.getSource();

                  receiver.receiveResult(result);
                } else {
                  log.warn("DoAction without ResultReceiver-Source: DoAction had to be canceled due to sessiontimeout " + doAction);
                }
              }
            }
          }

          if (!doActionFound) {
            // Such a Session should not exist, but who knows... 
            log.warn("Session with no DoAction was deleted due to timeout. Session: " + session);
          }
          // last but not least remove timeout session from memory
          memory.remove(session);
        }
      }
    }
  }
  
  private void processAutoExecutionServices(){
	  if(autoExecutionServices != null){
		  List<String> trash = new ArrayList<String>();
		  for(String actionName : autoExecutionServices.keySet()){
			  Map<String, Serializable> config = autoExecutionServices.get(actionName);
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
						  trash.add(actionName);
						  continue;
					  }
				  }else if((System.currentTimeMillis() - time - startTime) / intervalTime < servicesExecutionTimes.get(actionName)){
					  continue;
				  }
			  }else{
				 if(intervalTime == null){
					 if(servicesExecutionTimes.get(actionName) >= 1){
						 trash.add(actionName);
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
		  for(String actionName : trash){
			  autoExecutionServices.remove(actionName);
			  servicesExecutionTimes.remove(actionName);
		  }
	  }
  }

}
