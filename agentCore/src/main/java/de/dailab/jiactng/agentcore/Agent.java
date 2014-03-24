/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.AttributeChangeNotification;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.timer.TimerNotification;

import org.apache.commons.logging.Log;
import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.directory.IDirectory;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.execution.IExecutionCycle;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentNodeTimerManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient;
import de.dailab.jiactng.agentcore.ontology.AgentBeanDescription;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.AgentGroupDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;
import de.dailab.jiactng.agentcore.ontology.ThisAgentDescription;
import de.dailab.jiactng.agentcore.util.IdFactory;
import de.dailab.jiactng.agentcore.util.jar.JAR;
import de.dailab.jiactng.agentcore.util.jar.JARClassLoader;

/**
 * Agent class realizing the basic JIAC-TNG agent by implementing the IAgent interface. The Agent currently holds
 * a Memory-Component, an ExecutionCycle component and a list of agent beans.
 * 
 * @author Thomas Konnerth
 * @see de.dailab.jiactng.agentcore.IAgent
 */
public class Agent extends AbstractLifecycle implements IAgent, AgentMBean, BeanNameAware, NotificationListener {

  /** The default execution interval is 5. */
  public static final int                   DEFAULT_EXECUTION_INTERVAL     = 5;

  /** The default bean execution timeout is 300,000 milliseconds (5 minutes). */
  public static final long                  DEFAULT_BEAN_EXECUTION_TIMEOUT = 5 * 60 * 1000;

  /** The delay for automatic execution of services is 3,000 milliseconds. */
  public static final long                  AUTO_EXECUTION_DELAY           = 3000;

  /** The delay for agent start time is 3,000 milliseconds. */
  public static final long                  AGENT_STARTTIME_DELAY          = 3000;

  /** The interval for continuous service execution is 30,000 milliseconds. */
  public static final long                  CONTINUOUS_EXECUTION_INTERVAL  = 30 * 1000;

  /**
   * The AID (agent identifier). This property is generated and assigned automatically during agent creation. It is not
   * intended to make sense for human readers.
   */
  private final String                      agentId;

  /**
   * Reference to the agentNode that holds this agent.
   */
  private transient IAgentNode              agentNode                      = null;

  /**
   * Reference to the agent's specific class loader.
   */
  private transient JARClassLoader          classLoader                    = null;

  /**
   * The name of this agent.
   */
  private String                            agentName                      = null;

  /**
   * The owner of this agent.
   */
  private String                            owner                          = null;

  /**
   * Comment for <code>memory</code>
   */
  protected IMemory                         memory                         = null;

  /** Reference to directory, if any. */
  private transient IDirectory              directory                      = null;

  /**
   * The list of agent beans of this agent.
   */
  protected final ArrayList<IAgentBean>     agentBeans                     = new ArrayList<IAgentBean>();

  private List<IAgentRole>                  agentRoles                     = new ArrayList<IAgentRole>();
  
  /**
   * activity Flag (could be replaced by state check)
   */
  private boolean                           active                         = false;

  /**
   * Reference to the Object that handles the executionCycle
   */
  private IExecutionCycle                   execution                      = null;

  /**
   * Reference to the Object that handles the executionCycle
   */
  private ICommunicationBean                communication                  = null;

  /**
   * Future for the executionCycle of this agent. Used to store and cancel the executionThread.
   */
  private Future<?>                         executionFuture                = null;

  /**
   * Be nice timer for calling the executionCycle.
   */
  private int                               executionInterval              = DEFAULT_EXECUTION_INTERVAL;

  /**
   * Timeout after which the execution of a bean will be stopped and the agent as well. TODO do something more
   * intelligent, possibly recover the bean without stopping the agent.
   */
  private long                              beanExecutionTimeout           = DEFAULT_BEAN_EXECUTION_TIMEOUT;

  /**
   * List of the agents actions
   */
  private ArrayList<IActionDescription>     actionList                     = null;

  /**
   * The id of the start time notification.
   */
  private Integer                           startTimeId                    = null;

  /**
   * start time property cache required to make start time spring-configurable
   */
  private Long                              startTime                      = null;

  /**
   * The id of the stop time notification.
   */
  private Integer                           stopTimeId                     = null;

  /**
   * start time property cache required to make start time spring-configurable
   */
  private Long                              stopTime                       = null;

  /**
   * The spring configuration XML snippet for this agent. Currently only written if agents are added by
   * SimpleAgentNode.addAgent()
   */
  private byte[]                            springConfigXml                = null;

  /**
   * Client for accessing the agent node timer.
   */
  private JmxAgentNodeTimerManagementClient timerClient                    = null;


  /**
   * Public default constructor, creating the agent identifier.
   */
  public Agent() {
    agentId = IdFactory.createAgentId(this.hashCode());
  }

  /**
   * Constructor for spring-based agent node persistency, creates an agent with a given ID. Note: You should not try to
   * assign agentIds yourself, but always use the {@link Agent#Agent() Agent()} constructor to create a new agent.
   * 
   * @param agentID
   *          AgentID to assign.
   */
  public Agent(String agentID) {
    this.agentId = agentID;
  }

  /**
   * {@inheritDoc}
   */
  public final void setMemory(IMemory newMemory) {
    // disable management of old memory
    if (isManagementEnabled() && (memory != null)) {
      memory.disableManagement();
    }

    // change memory
    memory = newMemory;

    // enable management of new memory
    if (isManagementEnabled() && (memory != null)) {
      memory.enableManagement(_manager);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final List<IAgentBean> getAgentBeans() {
    return Collections.unmodifiableList(agentBeans);
  }

  /**
   * {@inheritDoc}
   */
  public final void setAgentBeans(List<IAgentBean> agentbeans) {
    // add agent beans
	addAgentBeans(agentbeans);
  }

  private final void addAgentBeans(List<IAgentBean> agentbeans) {
	this.agentBeans.addAll(agentbeans);

	// enable management of all new agent beans
	for (IAgentBean ab : this.agentBeans) {
	  if (isManagementEnabled()) {
	    ab.enableManagement(_manager);
	  }
	}	  
  }

  /**
   * {@inheritDoc}
   */
  public void run() {
    while (true) {
      try {
        Thread.sleep(executionInterval);
        synchronized (this) {
          if (active) {
            executionFuture = agentNode.getThreadPool().submit(execution);
            final FutureTask<?> t = ((FutureTask<?>) executionFuture);
            try {
              t.get(beanExecutionTimeout, TimeUnit.MILLISECONDS);
            } catch (TimeoutException to) {
              log.error("ExecutionCycle did not return: ", to);
              t.cancel(true);
              this.stop();
            }
          } else {
            break;
          }
        }
      } catch (Exception e) {
        if ((log != null) && (log.isErrorEnabled())) {
          log.error("Critical error in controlcycle of agent: " + getAgentName() + ". Stopping Agent! Exception: ", e);
        } else {
          System.err.println("Critical error in controlcycle of agent: " + getAgentName()
              + ". Stopping Agent. Exception: ");
          e.printStackTrace();
        }

        try {
          this.stop();
        } catch (LifecycleException lex) {
          printCriticalMessage("Agent " + getAgentName() + " could not be stopped because of:", lex);
        }
      }
    }
  }

  /**
   * Setter for the agent name. Called by Spring via the BeanNameAware interface.
   * 
   * @param name
   *          the name of the agent.
   * @see de.dailab.jiactng.agentcore.IAgent#setBeanName(java.lang.String)
   */
  public final void setBeanName(String name) {
    setAgentName(name);
  }

  /**
   * {@inheritDoc}
   */
  public void onEvent(LifecycleEvent evt) {
    // TODO Auto-generated method stub

  }

  /**
   * Stops and undeploys this agent from its agent node (incl. unregister as JMX resource).
   * 
   * @throws LifecycleException
   *           if an error occurs during stop or cleanup of this agent.
   */
  public final void remove() throws LifecycleException {
    // clean up agent
    stop();
    cleanup();

    // remove agent from the agent list of the agent node
    if (agentNode != null) {
      agentNode.removeAgent(this);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doCleanup() throws LifecycleException {
    synchronized (this) {
      if (executionFuture != null) {
        executionFuture.cancel(true);
        executionFuture = null;
      }
    }

    // cleanup all agent beans
    for (IAgentBean a : this.agentBeans) {
      try {
        setBeanState(a, LifecycleStates.CLEANED_UP);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.CLEANED_UP);
      }
    }

    if (log != null && log.isInfoEnabled()) {
      log.info("Trying to cleanup memory and executioncycle");
    }
    this.memory.removeAll(new Action());

    // update state information in agent's memory
    updateState(LifecycleStates.CLEANED_UP);

    this.actionList = null;
    if (communication != null) {
      this.communication.cleanup();
    }
    this.execution.cleanup();
    this.memory.cleanup();

    if (log != null && log.isInfoEnabled()) {
      log.info("Memory and executioncycle switched to state " + LifecycleStates.CLEANED_UP);
    }

    timerClient = null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doInit() throws LifecycleException {
    // initialize timer client
    try {
      timerClient = new JmxManagementClient().getAgentNodeTimerManagementClient(agentNode.getUUID());
    } catch (MalformedObjectNameException e) {
      throw new LifecycleException("Error when initializing timer client", e);
    }

    // initialize agent elements
    this.actionList = new ArrayList<IActionDescription>();

    if (log != null && log.isInfoEnabled()) {
      log.info("Trying to initalize memory and executioncycle");
    }

    this.memory.init();
    this.memory.write(new ThisAgentDescription(this.agentId, this.agentName, this.owner, LifecycleStates.INITIALIZING.name(),
        CommunicationAddressFactory.createMessageBoxAddress(this.agentNode.getUUID() + '/' + this.agentId),
        this.agentNode.getUUID()));

    this.execution.setMemory(memory);
    this.execution.init();

    if (communication != null) {
      this.communication.setMemory(memory);
      this.communication.init();
    }

    if (log != null && log.isInfoEnabled()) {
      log.info("Memory and executioncycle switched to state " + LifecycleStates.INITIALIZED);
    }

    final IAgentDescription myDescription = getAgentDescription();

    // initialize all agent beans
    for (IAgentBean ab : this.agentBeans) {
      try {
        ab.setMemory(memory);
        ab.addLifecycleListener(this);

        // memory.write(new AgentBeanDescription(ab.getBeanName(),
        // LifecycleStates.INITIALIZED.name()));
        setBeanState(ab, LifecycleStates.INITIALIZED);
      } catch (LifecycleException e) {
        handleBeanException(ab, e, LifecycleStates.INITIALIZED);
      }

      // if bean is effector, add all actions to memory
      if (ab instanceof IEffector) {
        final List<? extends IActionDescription> acts = ((IEffector) ab).getActions();
        if (acts != null) {
          for (IActionDescription item : acts) {
            item.setProviderDescription(myDescription);
            if (item.getProviderBean() == null) {
              item.setProviderBean((IEffector) ab);
            }
            memory.write(item);
            actionList.add(item);
          }
        }
      }
    }

    if (this.communication != null) {
      final List<? extends IActionDescription> acts = ((IEffector) this.communication).getActions();
      if (acts != null) {
        for (IActionDescription item : acts) {
          item.setProviderDescription(myDescription);
          if (item.getProviderBean() == null) {
            item.setProviderBean((IEffector) ((IEffector) this.communication));
          }
          memory.write(item);
          actionList.add(item);
        }
      }
    }

    for (IAgentRole role : this.agentRoles) {
      memory.write(role);
    }

    updateState(LifecycleStates.INITIALIZED);

    if ((startTime != null) && (stopTime != null)) {
      try {
        registerStartTime(startTime);
        registerStopTime(stopTime);
      } catch (InstanceNotFoundException e) {
        throw new LifecycleException("Error when initializing start/stoptime", e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doStart() throws LifecycleException {
    if (log != null && log.isInfoEnabled()) {
      log.info("Trying to start memory and executioncycle");
    }

    this.memory.start();

    if (log != null && log.isInfoEnabled()) {
      log.info("Memory and executioncycle switched to state " + LifecycleStates.STARTED);
    }

    if (communication != null) {
      this.communication.start();
    }

    // call start for all agent beans
    for (IAgentBean a : this.agentBeans) {
      try {
        setBeanState(a, LifecycleStates.STARTED);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.STARTED);
      }
    }

    this.execution.start();

    if (execution.getAutoExecutionServices() != null) {
      try {
        // add listener if needed
        if ((startTimeId == null) && (stopTimeId == null)) {
          try {
            timerClient.addTimerNotificationListener(this);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }

      } catch (InstanceNotFoundException e) {
        e.printStackTrace();
      }
    }

    synchronized (this) {
      active = true;
    }
    updateState(LifecycleStates.STARTED);

    Set<AgentGroupDescription> groupDescriptions = memory.readAllOfType(AgentGroupDescription.class);
    if (log != null && log.isInfoEnabled()) {
    	log.info("Groups: " + groupDescriptions.size());
    }
    if (communication != null) {
	    for (AgentGroupDescription description: groupDescriptions) {
	    	try {
				communication.joinGroup(CommunicationAddressFactory.createGroupAddress(description.getName()));
			    if (log != null && log.isInfoEnabled()) {
			    	log.info("Joined group: " + description.getName());
			    }
			} catch (CommunicationException e) {
				e.printStackTrace();
			}
	    }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doStop() throws LifecycleException {
    // call stop for all agent beans
    for (IAgentBean a : this.agentBeans) {
      try {
        setBeanState(a, LifecycleStates.STOPPED);
      } catch (LifecycleException e) {
        handleBeanException(a, e, LifecycleStates.STOPPED);
      }
    }

    if (log != null && log.isInfoEnabled()) {
      log.info("Trying to stop memory and executioncycle");
    }

    synchronized (this) {
      active = false;
      if (executionFuture != null) {
        executionFuture.cancel(false);
      }

    }

    this.execution.stop();

    if (communication != null) {
      this.communication.stop();
    }

    if (log != null && log.isInfoEnabled()) {
      log.info("Memory and executioncycle switched to state " + LifecycleStates.STOPPED);
    }

    this.memory.stop();

    updateState(LifecycleStates.STOPPED);
  }

  /**
   * Utility-Method for handling bean executions during life-cycle changes.
   * 
   * @param a
   *          the bean that threw the exception
   * @param e
   *          the actual exception
   * @param state
   *          the state to which the bean should have changed.
   */
  private void handleBeanException(IAgentBean a, LifecycleException e, LifecycleStates state) {

    printCriticalMessage("Agentbean: \'" + a.getBeanName() + "\' could not switch to state: \'" + state
        + "\'! \n  Exception was: ", e);
    try {
      a.handleLifecycleException(e, state);
      setBeanState(a, state);
      printCriticalMessage("Recovery for Agentbean: \'" + a.getBeanName() + "\' successful.", null);
    } catch (Exception newEx) {
      printCriticalMessage("Recovery for Agentbean: \'" + a.getBeanName() + "\' failed, removing Bean.", newEx);
      // TODO: probably remove bean
    }

  }

  /**
   * Delivers a message to the logging system or to the console, if the logging-system is not yet initiated. An optional
   * exception can be submitted.
   * 
   * @param message
   *          the message to print
   * @param e
   *          an optional exception
   */
  private void printCriticalMessage(String message, Exception e) {
    if (log != null) {
      log.error(message, e);
    } else {
      System.err.println(message);
      if (e != null) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Utility-Method that updates the state of the agent in the Memory
   * 
   * @param newState
   *          the new state
   */
  private void updateState(ILifecycle.LifecycleStates newState) {
    memory.update(new ThisAgentDescription(), new ThisAgentDescription(null, null, null, newState.name(), null, this
        .getAgentNode().getUUID()));
  }

  /**
   * Gets the life-cycle state of this agent by reading the agent description within the agent's memory.
   * 
   * @return the current life-cycle state of this agent
   */
  public final LifecycleStates getAgentState() {
    return LifecycleStates.valueOf(memory.read(new ThisAgentDescription()).getState());
  }

  /**
   * Sets the life-cycle state of an agent bean by invoking the corresponding method of interface <code>ILifecycle</code>
   * . It also updates the bean description within the agent's memory.
   * 
   * @param bean
   *          the agent bean
   * @param newState
   *          the intended state of the agent bean (must be one of CLEANED_UP, INITIALIZED, STOPPED or STARTED).
   * @throws LifecycleException
   *           if the corresponding life-cycle method throws an exception.
   * @see ILifecycle
   * 
   */
  public final void setBeanState(IAgentBean bean, LifecycleStates newState) throws LifecycleException {

    final String beanName = bean.getBeanName();

    if (log != null && log.isInfoEnabled()) {
      log.info("Trying to switch bean: " + bean.getBeanName() + " to " + newState.toString());
    }

    switch (newState) {
      case CLEANED_UP:
        bean.cleanup();
        break;
      case INITIALIZED:
        bean.init();
        break;
      case STOPPED:
        bean.stop();
        break;
      case STARTED:
        bean.start();
        break;
      default:
        return;
    }
    if (log != null && log.isInfoEnabled()) {
      log.info("Bean " + bean.getBeanName() + " switched to state: " + newState.toString());
    }
    memory.update(new AgentBeanDescription(beanName, null), new AgentBeanDescription(null, newState.name()));
  }

  /**
   * Gets the life-cycle state of an agent bean by reading the bean description within the agent's memory.
   * 
   * @param beanName
   *          the name of the agent bean
   * @return the current life-cycle state of the agent bean
   */
  public final LifecycleStates getBeanState(String beanName) {
    return LifecycleStates.valueOf(this.memory.read(new AgentBeanDescription(beanName, null)).getState());
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
        "Agent property " + attributeName + " changed", attributeName, attributeType, oldValue, newValue);
    sendNotification(n);
  }

  /**
   * {@inheritDoc}
   */
  public final String getAgentName() {
    return agentName;
  }

  /**
   * Setter for attribute <code>AgentName</code>. It also sends a notification to JMX listeners.
   * 
   * @param agentname
   *          the new name of the agent
   * @see #setBeanName(java.lang.String)
   */
  public final void setAgentName(String agentname) {
    final String oldName = this.agentName;
    this.agentName = agentname;

    // update memory
    switch (memory.getState()) {
    case INITIALIZED:
    case STARTING:
    case STARTED:
    case STOPPING:
    case STOPPED:
        memory.update(new ThisAgentDescription(), new ThisAgentDescription(null, agentname, null, null, null, null));
        break;
    default:        
        // do nothing
    }

    // send notification
    sendAttributeChangeNotification("AgentName", "java.lang.String", oldName, agentname);
  }

  /**
   * {@inheritDoc}
   */
  public final ExecutorService getThreadPool() {
    return agentNode.getThreadPool();
  }

  /**
   * Gets the execution cycle of this agent.
   * 
   * @return the execution cycle of this agent
   */
  public final IExecutionCycle getExecution() {
    return execution;
  }

  /**
   * Gets the communication bean of this agent.
   * 
   * @return the CommunicationBean of this agent
   */
  public final ICommunicationBean getCommunication() {
    return communication;
  }

  /**
   * Getter for the memory of this agent.
   * 
   * @return the memory instance of this agent.
   */
  public final IMemory getMemory() {
    return memory;
  }

  /**
   * {@inheritDoc}
   */
  public final void setExecution(IExecutionCycle newExecution) {
    // disable management of old execution cycle
    if (isManagementEnabled() && (execution != null)) {
      execution.disableManagement();
    }

    // change execution cycle
    execution = newExecution;

    // enable management of new execution cycle
    if (isManagementEnabled() && (execution != null)) {
      execution.enableManagement(_manager);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void setCommunication(ICommunicationBean newCommunication) {
    // disable management of old execution cycle
    if (isManagementEnabled() && (communication != null)) {
      communication.disableManagement();
    }

    // change execution cycle
    communication = newCommunication;

    // enable management of new execution cycle
    if (isManagementEnabled() && (communication != null)) {
      communication.enableManagement(_manager);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final IAgentNode getAgentNode() {
    return agentNode;
  }

  /**
   * {@inheritDoc}
   */
  public final void setAgentNode(IAgentNode newAgentNode) {
    // update management
    if (isManagementEnabled()) {
      final Manager manager = _manager;
      disableManagement();
      agentNode = newAgentNode;
      enableManagement(manager);
    } else {
      agentNode = newAgentNode;
    }

    // update logger
    setLog(agentNode.getLog(this));
    this.memory.setThisAgent(this);
    this.execution.setThisAgent(this);
    if (communication != null) {
      this.communication.setThisAgent(this);
    }
    for (IAgentBean iab : this.agentBeans) {
      iab.setThisAgent(this);
    }
  }

  /**
   * {@inheritDoc}
   */
  public final String getOwner() {
    return owner;
  }

  /**
   * {@inheritDoc}
   */
  public final void setOwner(String newOwner) {
    final String oldOwner = owner;
    owner = newOwner;

    // update memory
    switch (memory.getState()) {
    case INITIALIZED:
    case STARTING:
    case STARTED:
    case STOPPING:
    case STOPPED:
    	memory.update(new ThisAgentDescription(), new ThisAgentDescription(null, null, owner, null, null, null));
    	break;
    default:
        // do nothing
    }

    // send notification
    sendAttributeChangeNotification("owner", "java.lang.String", oldOwner, owner);
  }

  /**
   * {@inheritDoc}
   */
  public final Log getLog(IAgentBean bean) {
    if (agentNode == null) {
      return null;
    }
    return agentNode.getLog(this, bean);
  }

  /**
   * {@inheritDoc}
   */
  public final Log getLog(IAgentBean bean, String extension) {
    if (agentNode == null) {
      return null;
    }
    return agentNode.getLog(this, bean, extension);
  }

  /**
   * Returns the timeout after which the execution of a bean will be stopped.
   * 
   * @return the timeout in milliseconds
   */
  public final long getBeanExecutionTimeout() {
    return beanExecutionTimeout;
  }

  /**
   * Sets the timeout after which the execution of a bean will be stopped.
   * 
   * @param newBeanExecutionTimeout
   *          the timeout in milliseconds
   */
  public final void setBeanExecutionTimeout(long newBeanExecutionTimeout) {
    beanExecutionTimeout = newBeanExecutionTimeout;
  }

  /**
   * {@inheritDoc}
   */
  public final String getAgentId() {
    return agentId;
  }

  /**
   * Returns the agent description of this agent.
   * 
   * @return the agent description of this agent
   */
  public AgentDescription getAgentDescription() {
    return memory.read(new ThisAgentDescription());
  }

  /**
   * {@inheritDoc}
   */
  public final List<String> getAgentBeanNames() {
    final ArrayList<String> ret = new ArrayList<String>();
    for (IAgentBean bean : getAgentBeans()) {
      ret.add(bean.getBeanName());
    }
    return ret;
  }

  /**
   * {@inheritDoc}
   */
  public final List<IActionDescription> getActionList() {
    final ArrayList<IActionDescription> tempList = new ArrayList<IActionDescription>();
    for (IAgentBean iab : agentBeans) {
      if (iab instanceof IEffector) {
        tempList.addAll(((IEffector) iab).getActions());
      }
    }

    if (communication != null) {
      tempList.addAll(((IEffector) communication).getActions());
    }

    for (IActionDescription a : tempList) {
      a.setProviderDescription(getAgentDescription());
    }
    actionList = tempList;
    return Collections.unmodifiableList(actionList);
  }

  /**
   * {@inheritDoc}
   */
  public final void setActionList(List<IActionDescription> newActionList) {
    actionList = new ArrayList<IActionDescription>();
    actionList.addAll(newActionList);
  }

  /**
   * Getter for attribute "ActionNames" of the managed agent.
   * 
   * @return name of actions provided by this agent
   */
  public final List<String> getActionNames() {
    final ArrayList<String> ret = new ArrayList<String>();
    for (IActionDescription action : getActionList()) {
      ret.add(action.getName());
    }
    return ret;
  }

  /**
   * Getter for attribute "MemoryData" of the managed agent.
   * 
   * @return implementation of the memory of this agent
   */
  public final CompositeData getMemoryData() {
    if (memory == null) {
      return null;
    }
    final String[] itemNames = new String[] { "class", "matcher", "updater" };
    try {
      final CompositeType type = new CompositeType("javax.management.openmbean.CompositeDataSupport",
          "Memory information", itemNames, new String[] { "Implementation of the memory instance",
              "Implementation of the matcher instance", "Implementation of the updater instance" }, new OpenType[] {
              SimpleType.STRING, SimpleType.STRING, SimpleType.STRING });
      return new CompositeDataSupport(type, itemNames, new Object[] { memory.getClass().getName(),
          (memory.getMatcher() == null) ? null : memory.getMatcher().getClass().getName(),
          (memory.getUpdater() == null) ? null : memory.getUpdater().getClass().getName() });
    } catch (OpenDataException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Getter for attribute "ExecutionCycleClass" of the managed agent.
   * 
   * @return implementation of the execution cycle of this agent
   */
  public final String getExecutionCycleClass() {
    if (execution == null) {
      return null;
    }
    return execution.getClass().getName();
  }

  /**
   * Registers the agent and all its resources for management
   * 
   * @param manager
   *          the manager to be used for registration
   */
  public void enableManagement(Manager manager) {
    // do nothing if management already enabled
    if (isManagementEnabled()) {
      return;
    }

    // register agent for management
    try {
      manager.registerAgent(this);
    } catch (Exception e) {
      System.err.println("WARNING: Unable to register agent " + getAgentName() + " of agent node "
          + getAgentNode().getName() + " as JMX resource.");
      System.err.println(e.getMessage());
    }

    // register agent beans for management
    for (IAgentBean ab : this.agentBeans) {
      ab.enableManagement(manager);
    }

    // register memory for management
    if (memory != null) {
      memory.enableManagement(manager);
    }

    // register communication for management
    if (communication != null) {
      communication.enableManagement(manager);
    }

    // register execution cycle for management
    if (execution != null) {
      execution.enableManagement(manager);
    }

    super.enableManagement(manager);
  }

  /**
   * Unregisters the agent and all its resources from management.
   */
  public void disableManagement() {
    // do nothing if management already disabled
    if (!isManagementEnabled()) {
      return;
    }

    // unregister memory from management
    if (memory != null) {
      memory.disableManagement();
    }

    // unregister communication from management
    if (communication != null) {
      communication.disableManagement();
    }

    // unregister execution cycle from management
    if (execution != null) {
      execution.disableManagement();
    }

    // unregister agent beans from management
    for (IAgentBean ab : this.agentBeans) {
      ab.disableManagement();
    }

    // unregister agent from management
    try {
      _manager.unregisterAgent(this);
    } catch (Exception e) {
      System.err.println("WARNING: Unable to deregister agent " + getAgentName() + " of agent node "
          + getAgentNode().getName() + " as JMX resource.");
      System.err.println(e.getMessage());
    }

    super.disableManagement();
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
    final int oldInterval = executionInterval;
    executionInterval = newExecutionInterval;
    sendAttributeChangeNotification("executionInterval", "java.lang.int", Integer.valueOf(oldInterval),
        Integer.valueOf(newExecutionInterval));
  }

  /**
   * {@inheritDoc}
   */
  public final Long getStartTime() throws InstanceNotFoundException {
    if (startTimeId == null) {
      return null;
    }
    try {
      return Long.valueOf(timerClient.getDate(startTimeId).getTime());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } catch (NullPointerException e) {
      return startTime;
    }
  }

  /**
   * Adds the start time listener and notifications for start time. Required to enable spring configuration of
   * start/stop-time.
   * 
   * @param regStartTime
   *          the designated start time
   */
  private void registerStartTime(Long regStartTime) throws InstanceNotFoundException {
    if ((startTimeId == null) && (stopTimeId == null) && (regStartTime != null)) {
      try {
        timerClient.addTimerNotificationListener(this);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // remove old timer notification
    if (startTimeId != null) {
      try {
        timerClient.removeNotification(startTimeId);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // remove listener if no longer needed
    if ((startTimeId != null) && (stopTimeId == null) && (regStartTime == null)) {
      try {
        timerClient.removeTimerNotificationListener(this);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ListenerNotFoundException e) {
        e.printStackTrace();
      }
    }

    // add new timer notification
    if (regStartTime != null) {
      try {
        startTimeId = timerClient.addNotification(null, null, null, new Date(regStartTime.longValue()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void setStartTime(Long newStartTime) throws InstanceNotFoundException {
    // add listener if needed
    if ((newStartTime != null) && (newStartTime.longValue() <= (System.currentTimeMillis() + AGENT_STARTTIME_DELAY))) {
      newStartTime = Long.valueOf(System.currentTimeMillis() + AGENT_STARTTIME_DELAY);
    }
    startTime = newStartTime;
    final Long oldStartTime = getStartTime();

    if (this.memory.getState() != LifecycleStates.UNDEFINED) {
      if (this.getAgentState() != LifecycleStates.UNDEFINED) {
        registerStartTime(newStartTime);
        sendAttributeChangeNotification("startTime", "java.lang.Long", oldStartTime, getStartTime());
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public final Long getStopTime() throws InstanceNotFoundException {
    if (stopTimeId == null) {
      return null;
    }
    try {
      return Long.valueOf(timerClient.getDate(stopTimeId).getTime());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } catch (NullPointerException e) {
      return stopTime;
    }
  }

  private void registerStopTime(Long regStopTime) throws InstanceNotFoundException {
    if ((startTimeId == null) && (stopTimeId == null) && (regStopTime != null)) {
      try {
        timerClient.addTimerNotificationListener(this);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // remove old timer notification
    if (stopTimeId != null) {
      try {
        timerClient.removeNotification(stopTimeId);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // remove listener if no longer needed
    if ((startTimeId == null) && (stopTimeId != null) && (regStopTime == null)) {
      try {
        timerClient.removeTimerNotificationListener(this);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ListenerNotFoundException e) {
        e.printStackTrace();
      }
    }

    // add new timer notification
    if (regStopTime != null) {
      try {
        stopTimeId = timerClient.addNotification(null, null, null, new Date(regStopTime.longValue()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public final void setStopTime(Long newStopTime) throws InstanceNotFoundException {
    // add listener if needed
    stopTime = newStopTime;
    final Long oldStopTime = getStopTime();
    if (this.memory.getState() != LifecycleStates.UNDEFINED) {
      if (this.getAgentState() != LifecycleStates.UNDEFINED) {
        registerStopTime(newStopTime);
        sendAttributeChangeNotification("stopTime", "java.lang.Long", oldStopTime, getStopTime());
      }
    }

  }

  /**
   * Handles notifications about start and stop time.
   * 
   * @param notification
   *          the received notification.
   * @param handback
   *          the corresponding user data.
   */
  public void handleNotification(Notification notification, Object handback) {
    if (notification instanceof TimerNotification) {
      final Integer id = ((TimerNotification) notification).getNotificationID();
      if (id.equals(startTimeId) && !getAgentState().equals(LifecycleStates.STARTED)
          && !getAgentState().equals(LifecycleStates.STARTING)) {
        try {
          start();
        } catch (LifecycleException e) {
          e.printStackTrace();
        }
      }
      if (id.equals(stopTimeId) && !getAgentState().equals(LifecycleStates.STOPPED)
          && !getAgentState().equals(LifecycleStates.STOPPING)) {
        try {
          stop();
        } catch (LifecycleException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final Map<String, Map<String, Serializable>> getAutoExecutionServices() {
    if (this.execution != null) {
      return this.execution.getAutoExecutionServices();
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setAutoExecutionServices(Map<String, Map<String, Serializable>> autoExecutionServices) {
	  this.execution.setAutoExecutionServices(autoExecutionServices);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final byte[] getSpringConfigXml() {
    if (springConfigXml != null) {
      return Arrays.copyOf(springConfigXml, springConfigXml.length);
    }
    return springConfigXml;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void setSpringConfigXml(byte[] springConfig) {
    if (springConfig != null) {
      this.springConfigXml = Arrays.copyOf(springConfig, springConfig.length);
    } else {
      this.springConfigXml = null;
    }
  }

  /*********************************************************************************************************************
   * DirectoryAccess *
   ********************************************************************************************************************/

  /**
   * Get the access to the directory.
   * 
   * @return the directory
   */
  public final IDirectory getDirectory() {
    return directory;
  }

  /**
   * Set the access to the directory
   * 
   * @param newDirectory
   *          the directory
   */
  public final void setDirectory(IDirectory newDirectory) {
    directory = newDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void deregisterAction(IActionDescription actionDescription) {
    if (directory != null) {
      directory.deregisterAction(actionDescription);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void modifyAction(IActionDescription oldDescription, IActionDescription newDescription) {
    if (directory != null) {
      directory.modifyAction(oldDescription, newDescription);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void registerAction(IActionDescription actionDescription) {
    if (directory != null) {
      directory.registerAction(actionDescription);
    } else {
      log.warn("Agent has no reference to directory.");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final IActionDescription searchAction(IActionDescription template) {
    IActionDescription myAction = null;
    if (memory != null) {
      myAction = memory.read(template);
    }
    if (myAction == null && directory != null) {
      myAction = directory.searchAction(template);
    }
    return myAction;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final List<IActionDescription> searchAllActions(IActionDescription template) {
    Set<IActionDescription> actionDescriptions = new HashSet<IActionDescription>();
    if (memory != null) {
      actionDescriptions.addAll(memory.readAll(template));
    }

    if (directory != null) {
      actionDescriptions.addAll(directory.searchAllActions(template));
    }
    return new ArrayList<IActionDescription>(actionDescriptions);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void deregisterAgent(String aid) {
    if (directory != null) {
      directory.deregisterAgent(aid);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void modifyAgent(IAgentDescription agentDescription) {
    if (directory != null) {
      directory.modifyAgent(agentDescription);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void registerAgent(IAgentDescription agentDescription) {
    if (directory != null) {
      directory.registerAgent(agentDescription);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final IAgentDescription searchAgent(IAgentDescription template) {
    if (directory != null) {
      return directory.searchAgent(template);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final List<IAgentDescription> searchAllAgents(IAgentDescription template) {
    if (directory != null) {
      return directory.searchAllAgents(template);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public final <T> T findAgentBean(Class<T> type) {
    if (type == null) {
      throw new IllegalArgumentException("Cannot find AgentBean for null-type");
    }
    IAgentBean ret = null;
    synchronized (this.agentBeans) {
      for (IAgentBean iab : this.agentBeans) {
        if (type.isInstance(iab)) {
          ret = iab;
          break;
        }
      }
    }

    return type.cast(ret);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<IAgentRole> getRoles() {
    return Collections.unmodifiableList(agentRoles);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setRoles(List<IAgentRole> roles) {
    this.agentRoles = collectIncludedRoles(roles);

    // add agentBeans from roles to this agent
    for (IAgentRole role : this.agentRoles) {
      addAgentBeans(role.getAgentBeans());
    }
  }

  private List<IAgentRole> collectIncludedRoles(List<IAgentRole> roles) {
    ArrayList<IAgentRole> ret = new ArrayList<IAgentRole>();

    if (roles != null) {
      for (IAgentRole role : roles) {
        if (role != null) {
          ret.add(role);
          if (role.getIncludedAgentRoles() != null) {
            ret.addAll(collectIncludedRoles(role.getIncludedAgentRoles()));
          }
        }
      }
    }

    return ret;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JARClassLoader getClassLoader() {
	  return classLoader;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setClassLoader(JARClassLoader cl) {
	  classLoader = cl;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getJarNames() {
	if (classLoader == null) {
		return null;
	}

	ArrayList<String> names = new ArrayList<String>();
	JAR[] jars = classLoader.getJARs();
	for (int i=0; i<jars.length; i++) {
		String jarName = jars[i].getJarName();
		names.add(jarName.substring(Math.max(jarName.lastIndexOf("/"), jarName.lastIndexOf("\\")) + 1));
	}
	return names;
  }

  /**
   * {@inheritDoc}
   */
  public void loadClass(String className) throws ClassNotFoundException {
	  if (classLoader == null) {
		  agentNode.loadClass(className);
	  } else {
		  classLoader.loadClass(className);
	  }
  }

  // ///////////////////////////////////
  // TODO
  // ///////////////////////////////////
  //
  // - addAgentBean method
  // - removeAgentBean method
  // -> suggestion: use the add, addAll, remove, and removeAll methods of List.
  // Then we do not have to render the list unmodifiable...
  // -> the (add|remove)All methods are convenient to install/remove new beans and synchronize their state changes
  // between them
  // - setBeanState method should be renamed and moved to the AbstractLifecycle and ILifecycle
}
