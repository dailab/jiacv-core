/**
 * 
 */
package de.dailab.jiactng.agentcore.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * @author moekon
 * 
 */
public class SessionTestBean extends AbstractAgentBean implements IEffector, ResultReceiver {

  private Action                            ACTION_A        = null;
  private Action                            ACTION_B        = null;
  private Action                            ACTION_C        = null;

  public final static String                ACTION_NAME_A   = "ACTION_NAME_A";
  public final static String                ACTION_NAME_B   = "ACTION_NAME_B";
  public final static String                ACTION_NAME_C   = "ACTION_NAME_C";

  private boolean                           offerActionA    = true;
  private boolean                           offerActionB    = true;
  private boolean                           offerActionC    = true;

  private boolean                           waitWithActionA = false;
  private boolean                           waitWithActionB = false;
  private boolean                           waitWithActionC = false;

  private long                              waitTimer       = 0;

  private String                            finalString     = null;

  private HashMap<Session, Stack<DoAction>> sessionStack;

  @Override
  public void doInit() throws Exception {
    sessionStack = new HashMap<Session, Stack<DoAction>>();

    ArrayList<Class<?>> inputParamsA = new ArrayList<Class<?>>();
    ArrayList<Class<?>> outputParamsA = new ArrayList<Class<?>>();
    inputParamsA.add(String.class);
    outputParamsA.add(String.class);
    this.ACTION_A = new Action(ACTION_NAME_A, this, inputParamsA, outputParamsA, ActionScope.AGENT);

    ArrayList<Class<?>> inputParamsB = new ArrayList<Class<?>>();
    ArrayList<Class<?>> outputParamsB = new ArrayList<Class<?>>();
    inputParamsB.add(String.class);
    outputParamsB.add(String.class);
    this.ACTION_B = new Action(ACTION_NAME_B, this, inputParamsB, outputParamsB, ActionScope.AGENT);

    ArrayList<Class<?>> inputParamsC = new ArrayList<Class<?>>();
    ArrayList<Class<?>> outputParamsC = new ArrayList<Class<?>>();
    inputParamsC.add(String.class);
    outputParamsC.add(String.class);
    this.ACTION_C = new Action(ACTION_NAME_C, this, inputParamsC, outputParamsC, ActionScope.NODE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.environment.IEffector#getActions()
   */
  @Override
  public List<? extends IActionDescription> getActions() {
    ArrayList<Action> ret = new ArrayList<Action>();
    if (offerActionA) {
      ret.add(ACTION_A);
    }
    if (offerActionB) {
      ret.add(ACTION_B);
    }
    if (offerActionC) {
      ret.add(ACTION_C);
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.dailab.jiactng.agentcore.environment.IEffector#doAction(de.dailab.jiactng.agentcore.action.DoAction)
   */
  @Override
  public void doAction(DoAction doAction) throws Exception {
    String execActName = doAction.getAction().getName();

    if (ACTION_NAME_A.equals(execActName) && offerActionA) {
      processActionA(doAction);

    } else if (ACTION_NAME_B.equals(execActName) && offerActionB) {
      processActionB(doAction);

    } else if (ACTION_NAME_C.equals(execActName) && offerActionC) {
      processActionC(doAction);

    } else {
      log.error("Unknown Action: " + execActName);
    }

  }

  /**
   * @param doAction
   */
  private void processActionA(DoAction doAction) {
    Session session = doAction.getSession();

    Serializable[] inputParams = doAction.getParams();
    String param = (String) inputParams[0];

    if (waitWithActionA) {
      try {
        Thread.sleep(waitTimer);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    param = param + "+A";

    IActionDescription template = new Action(ACTION_NAME_B);
    IActionDescription callAction = memory.read(template);
    if (callAction == null) {
      callAction = thisAgent.searchAction(template);
    }

    invoke(callAction, session, new Serializable[] { param }, this);

    Stack<DoAction> currentStack = sessionStack.get(session);
    if (currentStack == null) {
      currentStack = new Stack<DoAction>();
    }
    currentStack.push(doAction);
    sessionStack.put(session, currentStack);
  }

  /**
   * @param doAction
   */
  private void processActionB(DoAction doAction) {
    Session session = doAction.getSession();

    Serializable[] inputParams = doAction.getParams();
    String param = (String) inputParams[0];

    if (waitWithActionB) {
      try {
        Thread.sleep(waitTimer);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    param = param + "+B";

    IActionDescription template = new Action(ACTION_NAME_C);
    IActionDescription callAction = memory.read(template);
    if (callAction == null) {
      callAction = thisAgent.searchAction(template);
    }

    invoke(callAction, session, new Serializable[] { param }, this);

    Stack<DoAction> currentStack = sessionStack.get(session);
    if (currentStack == null) {
      currentStack = new Stack<DoAction>();
    }
    currentStack.push(doAction);
    sessionStack.put(session, currentStack);
  }

  /**
   * @param doAction
   */
  private void processActionC(DoAction doAction) {
    Session session = doAction.getSession();

    Serializable[] inputParams = doAction.getParams();
    String param = (String) inputParams[0];

    if (waitWithActionC) {
      try {
        Thread.sleep(waitTimer);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    param = param + "+C";

    returnResult(doAction, new Serializable[] { param });
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * de.dailab.jiactng.agentcore.environment.ResultReceiver#receiveResult(de.dailab.jiactng.agentcore.action.ActionResult
   * )
   */
  @Override
  public void receiveResult(ActionResult result) {
    IActionDescription action = result.getAction();

    if (action == null) {
      log.error("No action in actionresult: " + result);
      return;
    }

    String actName = action.getName();

    if (ACTION_NAME_A.equals(actName)) {
      processActionAResult(result);

    } else if (ACTION_NAME_B.equals(actName)) {
      processActionBResult(result);

    } else if (ACTION_NAME_C.equals(actName)) {
      processActionCResult(result);

    } else {
      log.error("Got Result for unknown Action: " + result);
    }

  }

  /**
   * @param result
   */
  private void processActionAResult(ActionResult result) {
    Session session = result.getSession();

    Stack<DoAction> actionStack = sessionStack.get(session);
    if (actionStack == null) {
      log.error("Result for unknown session: " + session);
      return;
    }

    DoAction doActToAnswer = actionStack.pop();
    if (doActToAnswer == null) {
      log.error("Could not find DoAction for session: " + session);
      return;
    }

    if (result.getFailure() != null) {
      log.warn("Action failed: " + result.getAction().getName());
      returnFailure(doActToAnswer, result.getFailure());

    } else {
      returnResult(doActToAnswer, result.getResults());
    }
  }

  /**
   * @param result
   */
  private void processActionBResult(ActionResult result) {
    Session session = result.getSession();

    Stack<DoAction> actionStack = sessionStack.get(session);
    if (actionStack == null) {
      log.error("Result for unknown session: " + session);
      return;
    }

    DoAction doActToAnswer = actionStack.pop();
    if (doActToAnswer == null) {
      log.error("Could not find DoAction for session: " + session);
      return;
    }

    if (result.getFailure() != null) {
      log.warn("Action failed: " + result.getAction().getName());
      returnFailure(doActToAnswer, result.getFailure());

    } else {
      returnResult(doActToAnswer, result.getResults());
    }
  }

  /**
   * @param result
   */
  private void processActionCResult(ActionResult result) {
    Session session = result.getSession();

    Stack<DoAction> actionStack = sessionStack.get(session);
    if (actionStack == null) {
      log.error("Result for unknown session: " + session);
      return;
    }

    DoAction doActToAnswer = actionStack.pop();
    if (doActToAnswer == null) {
      log.error("Could not find DoAction for session: " + session);
      return;
    }

    if (result.getFailure() != null) {
      log.warn("Action failed: " + result.getAction().getName());
      returnFailure(doActToAnswer, result.getFailure());

    } else {
      returnResult(doActToAnswer, result.getResults());
    }
  }

  @Override
  public ActionResult cancelAction(DoAction doAction) {
    log.warn("Canceled Action: " + doAction);

    return new ActionResult(doAction, "Action canceled");
  }

  public boolean isOfferActionA() {
    return this.offerActionA;
  }

  public void setOfferActionA(boolean offerActionA) {
    this.offerActionA = offerActionA;
  }

  public boolean isOfferActionB() {
    return this.offerActionB;
  }

  public void setOfferActionB(boolean offerActionB) {
    this.offerActionB = offerActionB;
  }

  public boolean isOfferActionC() {
    return this.offerActionC;
  }

  public void setOfferActionC(boolean offerActionC) {
    this.offerActionC = offerActionC;
  }

  public boolean isWaitWithActionA() {
    return this.waitWithActionA;
  }

  public void setWaitWithActionA(boolean waitWithActionA) {
    this.waitWithActionA = waitWithActionA;
  }

  public boolean isWaitWithActionB() {
    return this.waitWithActionB;
  }

  public void setWaitWithActionB(boolean waitWithActionB) {
    this.waitWithActionB = waitWithActionB;
  }

  public boolean isWaitWithActionC() {
    return this.waitWithActionC;
  }

  public void setWaitWithActionC(boolean waitWithActionC) {
    this.waitWithActionC = waitWithActionC;
  }

  public long getWaitTimer() {
    return this.waitTimer;
  }

  public void setWaitTimer(long waitTimer) {
    this.waitTimer = waitTimer;
  }

  private class TestReceiver implements ResultReceiver {

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.dailab.jiactng.agentcore.environment.ResultReceiver#receiveResult(de.dailab.jiactng.agentcore.action.ActionResult
     * )
     */
    @Override
    public void receiveResult(ActionResult result) {
      if (result.getFailure() != null) {
        setFinalString(result.getFailure().toString());
      } else {
        setFinalString((String) result.getResults()[0]);
      }
    }

  }

  public void startAction(String actionName, String param, long timeOut) {
    TestReceiver receiver = new TestReceiver();

    Action template = new Action(actionName);

    IActionDescription act = memory.read(template);

    if (act == null) {
      act = thisAgent.searchAction(template);
    }

    invoke(act, new Serializable[] { param }, receiver, timeOut);

  }

  public String getFinalString() {
    return this.finalString;
  }

  public void setFinalString(String finalString) {
    this.finalString = finalString;
  }

}
