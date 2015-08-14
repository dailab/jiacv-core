/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.ActionResultListener;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * Abstract superclass of all agentbeans. This includes core-components as well as agentbeans. The class handles basic
 * references (such as to memory and the agent) and defines the methods that are necessary for (lifecycle-)management.
 * 
 * @author Thomas Konnerth
 */
public abstract class AbstractAgentBean extends AbstractLifecycle implements IAgentBean, AbstractAgentBeanMBean,
    BeanNameAware {

  /**
   * Interval by which the execute()-method of the bean is called. If negative, the execute-method is never called.
   */
  private int  executionInterval   = -1;

  /**
   * Used by the execution cycle to determine the next execution time when <code>executeInterval</code> is greater
   * than 0.
   */
  private long nextExecutionTime = 0;

  
  public final static String TIMEOUT_MESSAGE = "Session timed out.";
  
  /**
   * string constant for not action founding
   */
  public final static String NO_ACTION_FOUND = "no action found";
  
  /**
   * Creates an agent bean that uses lifecycle support in loose mode
   */
  public AbstractAgentBean() {
    super();
    /*
     * If this class is _not_ an instance of AbstractMethodExposingBean, check 
     * whether there are Methods with the @Expose tag attached to them. This is 
     * a quite common programming mistake, as the actions are then not exposed, 
     * without mention by Eclipse or JIAC.
     */
	  if (! (this instanceof AbstractMethodExposingBean)) {
		  List<Method> methods = AbstractMethodExposingBean.getExposedPublicMethods(getClass());
		  if (! methods.isEmpty()) {
			  // logging is not yet initialized, using System.err instead
			  System.err.println("WARNING: Using @Expose Annotation in Bean " + this.getClass().getName() + " without extending AbstractMethodExposingBean!");
		  }
	  }
  }

  // /**
  // * Creates an agent bean that may use lifecycle support in strict mode. This
  // * means, that the lifecycle graph is enforced.
  // *
  // * @param strict
  // * Flag, that determines, whether the lifecylce of this agentbean
  // * should run in strict mode or not.
  // * @see ILifecycle
  // */
  // public AbstractAgentBean(boolean strict) {
  // super(strict);
  // }

  /**
   * Reference to the agent that holds this bean.
   */
  protected IAgent  thisAgent = null;

  /**
   * Reference to the memory of the agent that holds this bean.
   */
  protected IMemory memory    = null;

  /**
   * The name this bean. Note that this is the unqualified name which is assigned by Spring.
   */
  protected String  beanName  = null;

  /**
   * {@inheritDoc}
   */
  public final void setThisAgent(IAgent agent) {
    // update management
    if (isManagementEnabled()) {
      final Manager manager = _manager;
      disableManagement();
      thisAgent = agent;
      enableManagement(manager);
    } else {
      thisAgent = agent;
    }

    // update logger
    setLog(thisAgent.getLog(this));
  }

  /**
   * {@inheritDoc}
   */
  public final void setMemory(IMemory mem) {
    this.memory = mem;
  }

  /**
   * {@inheritDoc}
   */
  public final void setBeanName(String name) {
    // update management
    if (isManagementEnabled()) {
      final Manager manager = _manager;
      disableManagement();
      beanName = name;
      enableManagement(manager);
    } else {
      beanName = name;
    }

  }

  /**
   * {@inheritDoc}
   */
  public final String getBeanName() {
    return beanName;
    // return new StringBuffer(thisAgent.getAgentName()).append(".").append(
    // beanName).toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doInit() throws Exception {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doStart() throws Exception {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doStop() throws Exception {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doCleanup() throws Exception {
  }

  /**
   * is only meant to be called through SimpleExecutionCycle during Timeoutmanagment
   * 
   * @param doAction
   *          the action execution to be canceled
   * @return the result of the canceled action (always <code>null</code> if this method is not overwritten)
   */
  public ActionResult cancelAction(DoAction doAction) {
    return new ActionResult(doAction, "The cancelAction-method was is not implemented by this agentbean");
  }

  /**
   * Registers the agent bean and all its resources for management
   * 
   * @param manager
   *          the manager responsible for this agentbean.
   */
  public void enableManagement(Manager manager) {
    // do nothing if management already enabled
    if (isManagementEnabled()) {
      return;
    }

    // register agent bean for management
    try {
      manager.registerAgentBean(this, thisAgent);
    } catch (Exception e) {
      System.err.println("WARNING: Unable to register agent bean " + beanName + " of agent " + thisAgent.getAgentName()
          + " of agent node " + thisAgent.getAgentNode().getName() + " as JMX resource.");
      System.err.println(e.getMessage());
    }

    super.enableManagement(manager);
  }

  /**
   * Deregisters the agent bean and all its resources from management.
   */
  public void disableManagement() {
    // do nothing if management already disabled
    if (!isManagementEnabled()) {
      return;
    }

    // deregister agent bean from management
    try {
      _manager.unregisterAgentBean(this, thisAgent);
    } catch (Exception e) {
      System.err.println("WARNING: Unable to deregister agent bean " + beanName + " of agent "
          + thisAgent.getAgentName() + " of agent node " + thisAgent.getAgentNode().getName() + " as JMX resource.");
      System.err.println(e.getMessage());
    }

    super.disableManagement();
  }

  /**
   * @deprecated
   * {@inheritDoc}
   */
    public final int getExecuteInterval() {
        if (log != null) {
            log.warn("Method \'getExecuteInterval\' is deprecated - please use \'getExecutionInterval\' instead.");
        } else {
            System.err
                    .println("Method \'getExecuteInterval\' is deprecated - please use \'getExecutionInterval\' instead.");
        }
        return getExecutionInterval();
    }

  /**
   * @deprecated
   * {@inheritDoc}
   */
  public final void setExecuteInterval(int newExecuteInterval) {
      if(log != null) {
          log.warn("Property \'executeInterval\' is deprecated - please use \'executionInterval\' instead.");
      } else {
          System.err.println("Property \'executeInterval\' is deprecated - please use \'executionInterval\' instead.");
      }
      setExecutionInterval(newExecuteInterval);
  }

  /**
   * {@inheritDoc}
   */
  public final int getExecutionInterval() {
    return executionInterval;
  }

  /**
   * {@inheritDoc}
   */
  public final void setExecutionInterval(int newExecutionInterval) {
    try {
      if (newExecutionInterval <= 0) {
        nextExecutionTime = 0;
        return;
      }
      // new execute Interval > 0, schedule/reschedule bean
      if (nextExecutionTime > 0) {
        nextExecutionTime = nextExecutionTime - executionInterval + newExecutionInterval;
      } else {
        nextExecutionTime = System.currentTimeMillis() + newExecutionInterval;
      }
    } finally {
      executionInterval = newExecutionInterval;
    }
  }  
  
  /**
   * {@inheritDoc}
   */
  public final long getNextExecutionTime() {
    return nextExecutionTime;
  }

  /**
   * {@inheritDoc}
   */
  public final void setNextExecutionTime(long newNextExecutionTime) {
    nextExecutionTime = newNextExecutionTime;
  }

  /**
   * {@inheritDoc}
   */
  public void execute() {

  }

  /**
   * {@inheritDoc}
   */
  public void handleLifecycleException(LifecycleException e, LifecycleStates state) {
    throw new RuntimeException(e);
  }

  /**
   * Invokes an action with default timeout and waits for its result. NOTE: This method MUST NOT be used with a blocking
   * execution cycle (e.g. SimpleExecutionCycle).
   * 
   * @param a
   *          The action to be invoked.
   * @param inputParams
   *          The values for the input parameters.
   * @return The result of the action.
   * @see #invokeAndWaitForResult(IActionDescription, Serializable[], Long)
   * @see Session#DEFAULT_TIMETOLIVE
   */
  protected final ActionResult invokeAndWaitForResult(IActionDescription a, Serializable[] inputParams) {
    return invokeAndWaitForResult(a, inputParams, Long.valueOf(Session.DEFAULT_TIMETOLIVE));
  }

  /**
   * Invokes an action with given timeout and waits for its result. NOTE: This method MUST NOT be used with a blocking
   * execution cycle (e.g. SimpleExecutionCycle).
   * 
   * @param a
   *          The action to be invoked.
   * @param inputParams
   *          The values for the input parameters.
   * @param timeout
   *          the timeout in milliseconds after this DoAction fails.
   * @return The result of the action.
   * @see #invoke(IActionDescription, Serializable[], ResultReceiver, Long)
   * @see ActionResultListener#getResult()
   */
  protected final ActionResult invokeAndWaitForResult(IActionDescription a, Serializable[] inputParams, Long timeout) {
    // invoke action
    final ActionResultListener listener = new ActionResultListener();
    invoke(a, inputParams, listener, timeout);

    // wait for result
    synchronized (listener) {
      if (listener.getResult() == null) {
        try {
          listener.wait();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    ActionResult result = listener.getResult();
    if(result.getResults() != null && result.getResults().length == 0){
    	log.warn("Action: " + a.getName() + " has no return parameter");
    }
    return result;
  }

  /**
   * Invokes an action asynchronously with default timeout and without handling the result.
   * 
   * @param a
   *          the action to be invoked.
   * @param inputParams
   *          the input parameters used for the action invocation.
   * @return the session id of the action invocation.
   * @see #invoke(IActionDescription, Serializable[], ResultReceiver)
   * @see Session#DEFAULT_TIMETOLIVE
   */
  protected final String invoke(IActionDescription a, Serializable[] inputParams) {
    return invoke(a, inputParams, null);
  }

  /**
   * Invokes an action asynchronously with default timeout and with handling the result by a given receiver.
   * 
   * @param a
   *          the action to be invoked.
   * @param inputParams
   *          the input parameters used for the action invocation.
   * @param receiver
   *          the receiver to be informed about the results.
   * @return the session id of the action invocation.
   * @see IActionDescription#createDoAction(Serializable[], ResultReceiver)
   * @see Session#DEFAULT_TIMETOLIVE
   */
  protected final String invoke(IActionDescription a, Serializable[] inputParams, ResultReceiver receiver) {
    return invoke(a, inputParams, receiver, Long.valueOf(Session.DEFAULT_TIMETOLIVE));
  }

  /**
   * Invokes an action asynchronously with given timeout and with handling the result by a given receiver.
   * 
   * @param a
   *          the action to be invoked.
   * @param inputParams
   *          the input parameters used for the action invocation.
   * @param receiver
   *          the receiver to be informed about the results.
   * @param timeOut
   *          a timeout in milliseconds after the DoAction fails.
   * @return the session id of the action invocation.
   * @see IActionDescription#createDoAction(Serializable[], ResultReceiver, Long)
   */
  protected final String invoke(IActionDescription a, Serializable[] inputParams, ResultReceiver receiver,
      final Long timeOut) {
    final DoAction doAct = a.createDoAction(inputParams, receiver, timeOut);
    memory.write(doAct);
    return doAct.getSessionId();
  }

  /**
   * Invokes an action asynchronously as part of a parent session with default timeout and with handling the result by a
   * given receiver.
   * 
   * @param a
   *          the action to be invoked.
   * @param parent
   *          the session of the parent action.
   * @param inputParams
   *          the input parameters used for the action invocation.
   * @param receiver
   *          the receiver to be informed about the results.
   * @return the session id of the action invocation.
   * @see IActionDescription#createDoAction(Session, Serializable[], ResultReceiver)
   * @see Session#DEFAULT_TIMETOLIVE
   */
  protected final String invoke(IActionDescription a, Session parent, Serializable[] inputParams,
      ResultReceiver receiver) {
    final DoAction doAct = a.createDoAction(parent, inputParams, receiver);
    memory.write(doAct);
    return doAct.getSessionId();
  }
  
  /**
   * Searches for an action and invokes it with default timeout and waits for its result. If the result is failed than another action will be searched
   * 
   * NOTE: This method MUST NOT be used with a blocking
   * execution cycle (e.g. SimpleExecutionCycle).
   * 
   * @param template
   *          Template of an action which is searched.
   * @param inputParams
   *          The values for the input parameters.
   * @return The result of the action.
   * @see Session#DEFAULT_TIMETOLIVE
   */
  protected final ActionResult invokeWithBacktracking(IActionDescription template, Serializable[] inputParams){
	 return invokeWithBacktracking(template, inputParams, Long.valueOf(Session.DEFAULT_TIMETOLIVE));
  }
  
  /**
   * Searches for an action and invokes it with given timeout and waits for its result. If the result is failed than another action will be searched
   * 
   * NOTE: This method MUST NOT be used with a blocking
   * execution cycle (e.g. SimpleExecutionCycle).
   * 
   * @param template
   *          Template of an action which is searched.
   * @param inputParams
   *          The values for the input parameters.
   * @param timeOut
   *          the timeout in milliseconds after this DoAction fails.
   * @return The result of the action.
   */
  protected final ActionResult invokeWithBacktracking(IActionDescription template, Serializable[] inputParams, final Long timeOut){
	  List<IActionDescription> actionDescriptions = thisAgent.searchAllActions(template);

	  ActionResult actionResult = null;
	  for(IActionDescription actionDescription : actionDescriptions){
          final ActionResultListener listener = new ActionResultListener();
		  invoke(actionDescription, inputParams, listener, timeOut);
		  synchronized (listener) {
		      if(listener.getResult() == null) {
		        try {
		          listener.wait();
		        } catch (Exception e) {
		          e.printStackTrace();
		        }
		      }
		    }
		  actionResult = listener.getResult();
		  if(actionResult != null && actionResult.getFailure() == null){
			if(actionResult.getResults() != null && actionResult.getResults().length == 0){
				log.warn("Action: " + template.getName() + " has no return parameter");
			}
		    return actionResult;
		  }
	  }
	  return new ActionResult(new DoAction(template, null, inputParams, timeOut), NO_ACTION_FOUND);
  }
  
	/**
	 * Find and invoke action, throw exception in case of failures, return result.
	 * 
	 * This is intended as a simpler way for finding actions, synchronously invoking 
	 * those actions, and checking the results for failures, all in one go. It will 
	 * just find and invoke the first action by that name (if any), no further checks 
	 * for e.g. action provider or backtracking. If you need any of those, you are 
	 * obviously a more seasoned JIAC developer and can use one of the other methods.
	 * 
	 * @param actionName	the name of the action to invoke
	 * @param timeout		the timeout in milliseconds; if <= 0, use the default
	 * @param parameters	parameters of the action, if any
	 * @return 				the action result values (the complete array, not just the first one)
	 * @throws Exception	in case anything went wrong (action not found, or failure)
	 */
	protected Serializable[] invokeAction(String actionName, long timeout, Serializable... parameters) throws Exception {
	    IActionDescription action = thisAgent.searchAction(new Action(actionName));
	    if (action == null) {
	        throw new Exception("Could not find Action with name " + actionName);
	    }
	    if (timeout <= 0) {
	    	timeout = Session.DEFAULT_TIMETOLIVE;
	    }
	    ActionResult result = invokeAndWaitForResult(action, parameters, timeout);
	    if (result.getFailure() != null) {
	    	if (result.getFailure() instanceof Exception) {
	    		throw (Exception) result.getFailure();
	    	} else {
	    		throw new Exception("Action returned with Failure" + result.getFailure().toString());
	    	}
	    }
	    return result.getResults();
	}

  // protected ActionResult syncInvoke(Action a, Object[] inputParams) {
  // DoAction doAct = a.createDoAction(inputParams, null);
  // return ((Agent)thisAgent).syncInvoke(doAct);
  // }

  /**
   * Sends back the results of an action.
   * 
   * @param origin
   *          the invocation of the action.
   * @param results
   *          the results of the action.
   */
  protected final void returnResult(DoAction origin, Serializable[] results) {
    final ActionResult res = ((Action) origin.getAction()).createActionResult(origin, results);
    memory.write(res);
  }

  /**
   * Sends back the failure information of an action.
   * 
   * @param origin
   *          the invocation of the action.
   * @param failure
   *          the failure information of the action.
   */
  protected final void returnFailure(DoAction origin, Serializable failure) {
    final ActionResult res = new ActionResult(origin, failure);
    memory.write(res);
  }

  /**
   * Retrieves an action with a given name from the local memory.
   * 
   * @param actionName
   *          the name of the action to be searched for.
   * @return a found action matching the given name or null if no such action was found.
   */
  protected final Action retrieveAction(String actionName) {
    final Action retAct = memory.read(new Action(actionName));
    if (retAct == null) {
      log.warn("Local memory does not contain \'" + actionName + "\'.");
    }
    return retAct;
  }

  /**
   * Retrieves an action with a given name and provider from the local memory.
   * 
   * @param actionName
   *          the name of the action to be searched for.
   * @param provider
   *          the provider of the action to be search for.
   * @return a found action matching the given name and provider or null if no such action was found.
   */
  protected final Action retrieveAction(String actionName, IAgentDescription provider) {
    final Action template = new Action(actionName);
    template.setProviderDescription(provider);

    final Action retAct = memory.read(template);
    if (retAct == null) {
      log.warn("Local memory does not contain \'" + actionName + "\' with provider " + provider + ".");
    }
    return retAct;
  }

  /**
   * Sends an attribute change notification to JMX listeners.
   * 
   * @param attributeName
   *          Attribute Name
   * @param attributeType
   *          Attribute Type
   * @param oldValue
   *          old value (before change)
   * @param newValue
   *          new value (after change)
   */
  protected final void sendAttributeChangeNotification(String attributeName, String attributeType, Object oldValue,
      Object newValue) {
    final Notification n = new AttributeChangeNotification(this, sequenceNumber++, System.currentTimeMillis(),
        "AgentBean Property " + attributeName + " changed", attributeName, attributeType, oldValue, newValue);
    sendNotification(n);
  }

}
