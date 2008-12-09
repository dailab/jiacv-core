/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.execution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.SessionEvent;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * A simple ExecutionCycle implementation. This class executes active agentbeans (those agentbeans where the
 * <code>executionInterval</code> is set to a value greater than 0) and takes care of action requests (<code>DoAction</code>)
 * and their results (<code>ActionResult</code>).
 * 
 * @author Thomas Konnerth
 * @author axle
 * 
 * @see IExecutionCycle
 * @see DoAction
 * @see ActionResult
 */
public class SimpleExecutionCycle extends AbstractExecutionCycle {

  private Set<ActionResult> pendingResults = new HashSet<ActionResult>();

  /**
   * Run-method for the execution cycle. The method iterates over the list of agentbeans and calls the execute method of
   * each <i>active</i> agentbean.
   * 
   * This method also takes care of new <code>DoAction</code>s and <code>ActionResult</code>s.
   * 
   * The <code>SimpleExecutionCycle</code> only executes agentbeans and handles DoActions and ActionResults when it
   * has reached <code>LifecycleStates.STARTED</code>.
   * 
   * @see de.dailab.jiactng.agentcore.execution.IExecutionCycle#run()
   * @see de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates
   */
  public void run() {
    // check if lifecycle has been started --> execute if STARTED
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
      boolean executionDone = false;
      if (minBean != null) {
        executionDone = true;
        try {
          minBean.execute();
        } catch (Exception ex) {
          log.error("Error when executing bean \'" + minBean.getBeanName() + "\'", ex);
        }

        // reschedule bean
        minBean.setNextExecutionTime(now + minBean.getExecuteInterval());
      }
      // else {
      // log.debug("No active beans to execute");
      // }
      updateWorkload(EXECUTION, executionDone);

      // process one doAction
      // TODO: check if read can be used
      DoAction act = memory.remove(new DoAction(null, null, null, null));

      boolean actionPerformed = false;
      if (act != null) {
        actionPerformed = true;
        synchronized (this) {
          performDoAction(act);
        }
      }
      updateWorkload(DO_ACTION, actionPerformed);

      // process one actionResult
      // TODO: check if read can be used
      Set<ActionResult> resultSet = memory.removeAll(new ActionResult(null, null));
      int countNew = 0;
      for (ActionResult ar : resultSet) {
        synchronized (this) {
          pendingResults.add(ar);
          countNew++;
        }
      }

      boolean resultProcessed = false;
      if (!pendingResults.isEmpty()) {
        resultProcessed = true;
        synchronized (this) {
          ActionResult actionResult = pendingResults.iterator().next();
          processResult(actionResult);
          pendingResults.remove(actionResult);
        }
      }
      updateWorkload(ACTION_RESULT, resultProcessed);

      // ActionResult actionResult = memory.remove(new ActionResult(null,
      // null));
      // if (actionResult != null) {
      // synchronized (this) {
      // processResult(actionResult);
      // }
      // }

      /*
       * Session-Cleanup
       * 
       * If Session has a timeout
       */
      synchronized (memory) {
        Set<Session> sessions = memory.readAll(new Session());
        for (Session session : sessions) {
          if (session.isTimeout()) {
            // session has timeout
            ArrayList<SessionEvent> history = session.getHistory();

            // Does Session is related to DoAction?
            boolean doActionFound = false;
            for (SessionEvent event : history) {
              if (event instanceof DoAction) {
                // doAction found
                doActionFound = true;
                DoAction doAction = (DoAction) event;
                if (doAction.getAction() instanceof Action) {
                  // Got an Action, so let's cancel this
                  // doAction

                  Action action = (Action) doAction.getAction();
                  log.debug("canceling DoAction " + doAction);

                  ActionResult result = null;
                  if ((action == null)) {
                    log.warn("Found doAction with missing action:" + doAction);
                  } else if (action.getProviderBean() == null) {
                    log.warn("Found doAction with missing providerBean:" + action);
                  } else {
                    result = action.getProviderBean().cancelAction(doAction);
                  }

                  if (session.getSource() != null) {
                    log.debug("sending timeout Result to source of Session " + session);
                    ResultReceiver receiver = session.getSource();

                    if (result == null) {
                      result = new ActionResult(doAction, new TimeoutException("DoAction has timeout"));
                    }
                    receiver.receiveResult(result);
                  } else {
                    log.warn("Session without Source: DoAction has to be canceled due to sessiontimeout " + doAction);
                  }
                }
              }
            }

            if (!doActionFound) {
              log.warn("Session with no DoAction was deleted due to timeout. Session: " + session);
            }
            // last but not least remove timeout session from memory
            memory.remove(session);
          }
        }
      }

    }

    // reject execution if SimpleExecutionCycle hasn't been started
    else {
      log.error("Trying to run SimpleExecutionCycle in state " + getState());
    }
  }

}
