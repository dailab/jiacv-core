/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.ActionResultListener;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * Abstract superclass of all agentbeans. This includes core-components as well
 * as agentbeans. The class handles basic references (such as to memory and the
 * agent) and defines the methods that are necessary for (lifecycle-)management.
 * 
 * @author Thomas Konnerth
 */
public abstract class AbstractAgentBean extends AbstractLifecycle implements IAgentBean, AbstractAgentBeanMBean {

  /**
   * Interval by which the execute()-method of the bean is called. If negative,
   * the execute-method is never called.
   */
  private int       executeInterval   = -1;

  /**
   * Used by the execution cycle to determine the next execution time when
   * <code>executeInterval</code> is greater than 0.
   */
  private long      nextExecutionTime = 0;

  private HashMap<String, DoAction> searchToInvokeMap = new HashMap<String,DoAction>();
  
  /**
   * Creates an agent bean that uses lifecycle support in loose mode
   */
  public AbstractAgentBean() {
    super();
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
   * The name this bean. Note that this is the unqualified name which is
   * assigned by Spring.
   */
  protected String  beanName  = null;

  /**
   * {@inheritDoc}
   */
  public final void setThisAgent(IAgent agent) {
    // update management
    if (isManagementEnabled()) {
      Manager manager = _manager;
      disableManagement();
      this.thisAgent = agent;
      enableManagement(manager);
    } else {
      this.thisAgent = agent;
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
      Manager manager = _manager;
      disableManagement();
      this.beanName = name;
      enableManagement(manager);
    } else {
      this.beanName = name;
    }

    // update logger
    if (thisAgent != null) {
      setLog(thisAgent.getLog(this));
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
    if (log == null) {
      setLog(thisAgent.getLog(this));
    }
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
   * is only meant to be called through SimpleExecutionCycle during
   * Timeoutmanagment
   * 
   * @param doAction
   *          the action execution to be canceled
   * @return the result of the canceled action (always <code>null</code> if
   *         this method is not overwritten)
   */
  public ActionResult cancelAction(DoAction doAction) {
    return null;
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
   * {@inheritDoc}
   */
  public int getExecuteInterval() {
    return executeInterval;
  }

  /**
   * {@inheritDoc}
   */
  public void setExecuteInterval(int executeInterval) {
    try {
      if (executeInterval <= 0) {
        nextExecutionTime = 0;
        return;
      }
      // execute Interval > 0, schedule/reschedule bean
      if (nextExecutionTime > 0) {
        nextExecutionTime = nextExecutionTime - this.executeInterval + executeInterval;
      } else {
        nextExecutionTime = System.currentTimeMillis() + executeInterval;
      }
    } finally {
      this.executeInterval = executeInterval;
    }
  }

  /**
   * {@inheritDoc}
   */
  public long getNextExecutionTime() {
    return nextExecutionTime;
  }

  /**
   * {@inheritDoc}
   */
  public void setNextExecutionTime(long nextExecutionTime) {
    this.nextExecutionTime = nextExecutionTime;
  }

  /**
   * {@inheritDoc}
   */
  public void execute() {

  }

  /**
   * {@inheritDoc}
   */
  public void handleLifecycleException(LifecycleException e, @SuppressWarnings("unused")
  LifecycleStates state) {
    throw new RuntimeException(e);
  }

  /**
   * Invokes an action and waits for its result. 
   * NOTE: This method MUST NOT be used with a blocking execution cycle (e.g. SimpleExecutionCycle).
   * 
   * @param a The action to be invoked.
   * @param inputParams The values for the input parameters.
   * @return The result of the action.
   */
  protected ActionResult invokeAndWaitForResult(Action a, Serializable[] inputParams) {
	// invoke action
	ActionResultListener listener = new ActionResultListener();
	invoke(a, inputParams, listener);

	// wait for result
	synchronized (listener) {
		try {
			listener.wait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	return listener.getResult();
  }

  /**
   * Invokes an action asynchronously without handling the result.
   * @param a the action to be invoked.
   * @param inputParams the input parameters used for the action invocation.
   * @return the session id of the action invocation.
   */
  protected String invoke(Action a, Serializable[] inputParams) {
    return invoke(a, inputParams, null);
  }

  /**
   * Invokes an action asynchronously with handling the result by a given receiver.
   * @param a the action to be invoked.
   * @param inputParams the input parameters used for the action invocation.
   * @param receiver the receiver to be informed about the results.
   * @return the session id of the action invocation.
   */
  protected String invoke(Action a, Serializable[] inputParams, ResultReceiver receiver) {
    DoAction doAct = a.createDoAction(inputParams, receiver);
    memory.write(doAct);
    return doAct.getSessionId();
  }

  /**
   * Invokes an action asynchronously as part of a parent session with handling the result by a given receiver.
   * @param a the action to be invoked.
   * @param parent the session of the parent action.
   * @param inputParams the input parameters used for the action invocation.
   * @param receiver the receiver to be informed about the results.
   * @return the session id of the action invocation.
   */
  protected String invoke(Action a, Session parent, Serializable[] inputParams, ResultReceiver receiver) {
    DoAction doAct = a.createDoAction(parent, inputParams, receiver);
    memory.write(doAct);
    return doAct.getSessionId();
  }

  // protected ActionResult syncInvoke(Action a, Object[] inputParams) {
  // DoAction doAct = a.createDoAction(inputParams, null);
  // return ((Agent)thisAgent).syncInvoke(doAct);
  // }

  /**
   * Sends back the results of an action. 
   * @param origin the invocation of the action.
   * @param results the results of the action.
   */
  protected void returnResult(DoAction origin, Serializable[] results) {
    ActionResult res = ((Action) origin.getAction()).createActionResult(origin, results);
    memory.write(res);
  }

  /**
   * Sends back the failure information of an action. 
   * @param origin the invocation of the action.
   * @param failure the failure information of the action.
   */
  protected void returnFailure(DoAction origin, Serializable failure) {
    ActionResult res = new ActionResult(origin, failure);
    memory.write(res);
  }

  /**
   * Retrieves an action with a given name from the local memory.
   * @param actionName the name of the action to be searched for.
   * @return a found action matching the given name or null if no such action was found.
   */
  protected Action retrieveAction(String actionName) {
    Action retAct = memory.read(new Action(actionName));
    if (retAct == null) {
      log.warn("Could not find \'" + actionName + "\'.");
    }
    return retAct;
  }

  /**
   * Retrieves an action with a given name and provider from the local memory.
   * @param actionName the name of the action to be searched for.
   * @param provider the provider of the action to be search for.
   * @return a found action matching the given name and provider or null if no such action was found.
   */
  protected Action retrieveAction(String actionName, IAgentDescription provider) {
    Action template = new Action(actionName);
    template.setProviderDescription(provider);

    Action retAct = memory.read(template);
    if (retAct == null) {
      log.warn("Could not find \'" + actionName + "\'.");
    }
    return retAct;
  }

  /**
   * Invokes an action for the search of actions in the service directory.
   * @param template The template for searching actions.
   * @param isGlobal <code>true</code> for a global search and <code>false</code> for an agent node internal search.
   * @param timeout The maximum duration of the global search. This value will be ignored by local search.
   * @param receiver the receiver to be informed about the found actions.
   * @return the session id of the action invocation.
   * @throws RuntimeException if the agent has no directory access bean.
   */
  protected String invokeActionSearch(Action template, boolean isGlobal, long timeout, ResultReceiver receiver) {
	Action requestSearchAction = memory.read(new Action(DirectoryAccessBean.ACTION_REQUEST_SEARCH));
	if (requestSearchAction == null) {
		throw new RuntimeException("DirectoryAccessBean not available");
	}
	Serializable[] params = {template, new Boolean(isGlobal), new Long(timeout)}; 
	return invoke(requestSearchAction, params, receiver);
  }

  /**
   * Gets from the service directory a list of actions which match the given template. 
   * NOTE: This method MUST NOT be used with a blocking execution cycle (e.g. SimpleExecutionCycle).
   * 
   * @param template The template for matching actions.
   * @param isGlobal <code>true</code> for a global search and <code>false</code> for an agent node internal search.
   * @param timeout The maximum duration of the global search. This value will be ignored by local search.
   * @throws RuntimeException if the search failed or was not possible.
   * @return The list of found actions.
   */
  protected List<Action> retrieveActionsFromDirectory(Action template, boolean isGlobal, long timeout) {
	// get search action
	Action requestSearchAction = memory.read(new Action(DirectoryAccessBean.ACTION_REQUEST_SEARCH));
	if (requestSearchAction == null) {
		throw new RuntimeException("DirectoryAccessBean not available");
	}

	// invoke search action
	Serializable[] params = {template, Boolean.valueOf(isGlobal), Long.valueOf(timeout)}; 
	ActionResult result = invokeAndWaitForResult(requestSearchAction, params);
	if (result == null) {
		throw new RuntimeException("Got no result");
	}

	// get result
	Serializable[] results = result.getResults();
	if (results != null) {
		if (results.length == 1) {
			try {
				return (List<Action>) results[0];
			} catch (ClassCastException e) {
				throw new RuntimeException("Got wrong type of results");
			}
		} else {
			throw new RuntimeException("Got wrong number of results");
		}
	}

	// search failed
	Serializable failure = result.getFailure();
	if (failure != null) {
		if (failure instanceof Throwable) {
			throw new RuntimeException((Throwable) failure);
		} else if (failure instanceof String) {
			throw new RuntimeException((String) failure);
		} else {
			throw new RuntimeException("Search for actions failed");
		}
	}

	throw new RuntimeException("Got neigther results nor failure");
  }
  
  protected String invokeRemote(Action template, Session parent, Serializable[] inputParams, ResultReceiver receiver) {
    if(parent == null) {
      parent = new Session(receiver);
    }
    
    Action a = memory.read(template);
    if(a != null) {
      log.warn("Found in memory... invoking");
      return invoke(a, parent, inputParams, receiver);
    } else {
      DoAction doAct = new DoAction(parent,template, receiver, inputParams);
//      searchToInvokeMap.put(parent.getSessionId(),doAct);
//      invokeActionSearch(template,true,1000,this);
      Action requestSearchAction = memory.read(new Action(DirectoryAccessBean.ACTION_REQUEST_SEARCH));
      if (requestSearchAction == null) {
        throw new RuntimeException("DirectoryAccessBean not available");
      }
      Serializable[] params = {template, new Boolean(true), new Long(5000)};
      log.warn("invoking requestSerach with: "+template+" / "+parent.getSessionId() );
//      invoke(requestSearchAction,parent,params, new SearchResultHandler());
      
      DoAction readAction = requestSearchAction.createDoAction(params, new SearchResultHandler());
      searchToInvokeMap.put(readAction.getSessionId(),doAct);
      memory.write(readAction);      
      
      
      return parent.getSessionId();
    }
  }

  
  private class SearchResultHandler implements ResultReceiver {

    /* (non-Javadoc)
     * @see de.dailab.jiactng.agentcore.environment.ResultReceiver#receiveResult(de.dailab.jiactng.agentcore.action.ActionResult)
     */
    @Override
    public void receiveResult(ActionResult result) {
      String readSession = result.getSessionId();
      log.warn("Receveid result for: "+readSession);
      DoAction doAct = searchToInvokeMap.get(readSession);
      if((doAct != null) &&  (result.getFailure() == null)) {
        Serializable[] res = result.getResults();
        List<Action> actList = (List<Action>)res[0];
        for(Action a : actList) {
          memory.write(a);
        }
        if(actList.size()>0) {
          Action myAct = actList.get(0);
          doAct.setAction(myAct);
          memory.write(doAct);
        } else {
          log.error("No Actions found for: "+readSession);
        }
      } else {
        log.error("Encountered problem with: "+readSession);
      }
    }
    
  }
  
}
