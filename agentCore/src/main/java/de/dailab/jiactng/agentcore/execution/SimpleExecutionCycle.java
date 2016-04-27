/*
 * Created on 21.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.execution;

import java.util.ArrayList;
import java.util.Set;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.SessionEvent;

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
  public void run() {
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

              processSessionTimeout(session, doAction);
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

}
