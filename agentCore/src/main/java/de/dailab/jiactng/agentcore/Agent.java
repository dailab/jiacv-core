/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
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
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;
import de.dailab.jiactng.agentcore.ontology.ThisAgentDescription;
import de.dailab.jiactng.agentcore.util.IdFactory;

/**
 * Agentclass implementing the IAgent interface and therby realizing the basic
 * JIAC-TNG agent. The Agent currently holds a Memory-Component, an
 * ExecutionCycle component and a list of agentbeans.
 * 
 * @author Thomas Konnerth
 * @see de.dailab.jiactng.agentcore.IAgent
 */
public class Agent extends AbstractLifecycle implements IAgent, AgentMBean, NotificationListener {

	/**
	 * The AID (agent identifier). This property is generated and assigned
	 * automatically during agent creation. It is not intended to make sense for
	 * human readers.
	 */
	private final String agentId;

	/**
	 * Reference to the agentNode that holds this agent.
	 */
	private IAgentNode agentNode = null;

	/**
	 * The name of this agent.
	 */
	private String agentName = null;

	/**
	 * The owner of this agent.
	 */
	private String owner = null;

	/**
	 * Comment for <code>memory</code>
	 */
	protected IMemory memory = null;

	/**
	 * The list of agentbeans of this agent.
	 */
	protected final ArrayList<IAgentBean> agentBeans = new ArrayList<IAgentBean>();

	/**
	 * activity Flag (could be replaced by statecheck
	 */
	private boolean active = false;

	/**
	 * Reference to the Object that handles the executionCycle
	 */
	private IExecutionCycle execution = null;

	/**
	 * Future for the executionCycle of this agent. Used to store and cancel the
	 * executionThread.
	 */
	private Future<?> executionFuture = null;

	/**
	 * Be nice timer for calling the executionCycle.
	 */
	private int executionInterval = 50;

	/**
	 * Timeout after which the execution of a bean will be stopped and the agent
	 * as well. TODO do something more intelligent, possibly recover the bean
	 * without stopping the agent.
	 */
	private long beanExecutionTimeout = 5 * 60 * 1000;

	private ArrayList<Action> actionList = null;

	/**
	 * The id of the start time notification.
	 */
	private Integer startTimeId = null;

	/**
	 * The id of the stop time notification.
	 */
	private Integer stopTimeId = null;

	/**
	 * Client for accessing the agent node timer.
	 */
	private JmxAgentNodeTimerManagementClient timerClient = null;

	/**
	 * Public default constructor, creating the agent identifier.
	 */
	public Agent() {
		agentId = IdFactory.createAgentId(this.hashCode());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMemory(IMemory memory) {
		// disable management of old memory
		if (isManagementEnabled() && (this.memory != null)) {
			this.memory.disableManagement();
		}

		// change memory
		this.memory = memory;
		memory.setThisAgent(this);

		// enable management of new memory
		if (isManagementEnabled() && (this.memory != null)) {
			this.memory.enableManagement(_manager);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IAgentBean> getAgentBeans() {
		return Collections.unmodifiableList(agentBeans);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAgentBeans(List<IAgentBean> agentbeans) {
		// disable management of all old agent beans
		if (isManagementEnabled() && (this.agentBeans != null)) {
			for (IAgentBean ab : this.agentBeans) {
				ab.disableManagement();
			}
		}

		// change agent beans
		this.agentBeans.clear();
		this.agentBeans.addAll(agentbeans);

		// set references for all new agent beans and
		// enable management of all new agent beans
		for (IAgentBean ab : this.agentBeans) {
			ab.setThisAgent(this);
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
						executionFuture = agentNode.getThreadPool().submit(
								execution);
						FutureTask<?> t = ((FutureTask<?>) executionFuture);
						try {
							t.get(beanExecutionTimeout, TimeUnit.MILLISECONDS);
						} catch (TimeoutException to) {
							System.err.print("this: " + agentName);
							to.printStackTrace();
							t.cancel(true);
							this.stop();
							log.error("ExecutionCycle did not return: ", to);
						}
					} else {
						break;
					}
				}
			} catch (Exception e) {
				log.error("Critical error in controlcycle of agent: "
						+ agentName + ". Stopping Agent. Exception was: ",e);
				e.printStackTrace();
				try {
					this.stop();
				} catch (LifecycleException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Setter for the agentname. Called by Spring via the BeanNameAware
	 * interface.
	 * 
	 * @param name
	 *            the name of the agent.
	 * @see de.dailab.jiactng.agentcore.IAgent#setBeanName(java.lang.String)
	 */
	public void setBeanName(String name) {
		setAgentName(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void onEvent(@SuppressWarnings("unused")
	LifecycleEvent evt) {
		// TODO Auto-generated method stub

	}

	/**
	 * Stops and undeploys this agent from its agent node (incl. deregistration
	 * as JMX resource).
	 * 
	 * @throws LifecycleException
	 *             if an error occurs during stop or cleanup of this agent.
	 */
	public void remove() throws LifecycleException {
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
		if (executionFuture != null) {
			executionFuture.cancel(true);
			executionFuture = null;
		}

		// call cleanup for all agentbeans
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
		this.execution.cleanup();
		this.memory.cleanup();

		if (log != null && log.isInfoEnabled()) {
			log.info("Memory and executioncycle switched to state "
					+ LifecycleStates.CLEANED_UP);
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
			timerClient = new JmxManagementClient().getAgentNodeTimerManagementClient(agentNode.getName());
		}
		catch (MalformedObjectNameException e) {
			throw new LifecycleException("Error when initializing timer client", e);
		}

		// initialize agent elements
		this.actionList = new ArrayList<Action>();

		if (log != null && log.isInfoEnabled()) {
			log.info("Trying to initalize memory and executioncycle");
		}

		this.memory.init();
		this.memory.write(new ThisAgentDescription(this.agentId,
				this.agentName, LifecycleStates.INITIALIZING.name(),
				CommunicationAddressFactory
						.createMessageBoxAddress(this.agentNode.getUUID() + '/'
								+ this.agentId), this.agentNode.getUUID()));

		this.execution.setMemory(memory);
		this.execution.init();

		if (log != null && log.isInfoEnabled()) {
			log.info("Memory and executioncycle switched to state "
					+ LifecycleStates.INITIALIZED);
		}

		// call init for all agentbeans
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
			IAgentDescription myDescription= getAgentDescription();
			if (ab instanceof IEffector) {
				List<? extends Action> acts = ((IEffector) ab).getActions();
				if (acts != null) {
					for (Action item : acts) {
						item.setProviderDescription(myDescription);
						if(item.getProviderBean() == null) {
							item.setProviderBean((IEffector) ab);
						}
						memory.write(item);
						actionList.add(item);
					}
				}
			}
		}

		updateState(LifecycleStates.INITIALIZED);
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
		this.execution.start();

		if (log != null && log.isInfoEnabled()) {
			log.info("Memory and executioncycle switched to state "
					+ LifecycleStates.STARTED);
		}

		// call start for all agentbeans
		for (IAgentBean a : this.agentBeans) {
			try {
				setBeanState(a, LifecycleStates.STARTED);
			} catch (LifecycleException e) {
				handleBeanException(a, e, LifecycleStates.STARTED);
			}
		}

		synchronized (this) {
			active = true;
		}
		updateState(LifecycleStates.STARTED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doStop() throws LifecycleException {

		if (log != null && log.isInfoEnabled()) {
			log.info("Trying to stop memory and executioncycle");
		}

		synchronized (this) {
			active = false;
			if (executionFuture != null) {
				executionFuture.cancel(false);
			}

		}

		this.memory.stop();
		this.execution.stop();

		if (log != null && log.isInfoEnabled()) {
			log.info("Memory and executioncycle switched to state "
					+ LifecycleStates.STOPPED);
		}

		// call stop for all agentbeans
		for (IAgentBean a : this.agentBeans) {
			try {
				setBeanState(a, LifecycleStates.STOPPED);
			} catch (LifecycleException e) {
				handleBeanException(a, e, LifecycleStates.STOPPED);
			}
		}

		updateState(LifecycleStates.STOPPED);
	}

	/**
	 * Utility-Method for handling bean exections during lifecycle changes.
	 * 
	 * @param a
	 *            the bean that threw the exception
	 * @param e
	 *            the actual exception
	 * @param state
	 *            the state to which the bean should have changed.
	 */
	private void handleBeanException(IAgentBean a, LifecycleException e,
			LifecycleStates state) {

		printCriticalMessage("Agentbean: \'" + a.getBeanName()
				+ "\' could not switch to state: \'" + state
				+ "\'! \n  Exception was: ", e);
		try {
			a.handleLifecycleException(e, state);
			setBeanState(a, state);
			printCriticalMessage("Recovery for Agentbean: \'" + a.getBeanName()
					+ "\' successful.", null);
		} catch (Exception newEx) {
			printCriticalMessage("Recovery for Agentbean: \'" + a.getBeanName()
					+ "\' failed, removing Bean.", newEx);
			// TODO: probably remove bean
		}

	}

	/**
	 * Delivers a message to the logging system or to the console, if the
	 * logging-system is not yet initiated. An optional exception can be
	 * submitted.
	 * 
	 * @param message
	 *            the message to print
	 * @param e
	 *            an optional exception
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
	 *            the new state
	 */
	private void updateState(ILifecycle.LifecycleStates newState) {
		memory.update(new ThisAgentDescription(), new ThisAgentDescription(
				null, null, newState.name(), null, this.getAgentNode().getUUID()));
	}

	/**
	 * Gets the lifecycle state of this agent by reading the agent description
	 * within the agent's memory.
	 * 
	 * @return the current lifecycle state of this agent
	 */
	public LifecycleStates getAgentState() {
		return LifecycleStates.valueOf(memory.read(new ThisAgentDescription())
				.getState());
	}

	/**
	 * Sets the lifecycle state of an agent bean by invoking the corresponding
	 * method of interface <code>ILifecycle</code>. It also updates the bean
	 * description within the agent's memory.
	 * 
	 * @param bean
	 *            the agent bean
	 * @param newState
	 *            the intended state of the agent bean (must be one of
	 *            CLEANED_UP, INITIALIZED, STOPPED or STARTED).
	 * @throws LifecycleException
	 *             if the corresponding lifecycle method throws an exception.
	 * @see ILifecycle
	 * 
	 */
	public void setBeanState(IAgentBean bean, LifecycleStates newState)
			throws LifecycleException {
	    
		String beanName = bean.getBeanName();

		if (log != null && log.isInfoEnabled()) {
			log.info("Trying to switch bean: " + bean.getBeanName()
					+ " to " + newState.toString());
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
		if (log != null && log.isInfoEnabled())
			log.info("Bean " + bean.getBeanName() + " switched to state: "
					+ newState.toString());
		this.memory.update(new AgentBeanDescription(beanName, null),
				new AgentBeanDescription(null, newState.name()));
	}

	/**
	 * Gets the lifecycle state of an agent bean by reading the bean description
	 * within the agent's memory.
	 * 
	 * @param beanName
	 *            the name of the agent bean
	 * @return the current lifecycle state of the agent bean
	 */
	public LifecycleStates getBeanState(String beanName) {
		return LifecycleStates.valueOf(this.memory.read(
				new AgentBeanDescription(beanName, null)).getState());
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAgentName() {
		return this.agentName;
	}

	/**
	 * Setter for attribute <code>AgentName</code>. It also sends a
	 * notification to JMX listeners.
	 * 
	 * @param agentname
	 *            the new name of the agent
	 * @see #setBeanName(java.lang.String)
	 */
	public void setAgentName(String agentname) {
		String oldName = this.agentName;
		this.agentName = agentname;

		// send notification
		Notification n = new AttributeChangeNotification(this,
				sequenceNumber++, System.currentTimeMillis(),
				"Name of agent changed", "AgentName", "java.lang.String",
				oldName, agentname);
		sendNotification(n);
	}

	/**
	 * {@inheritDoc}
	 */
	public ExecutorService getThreadPool() {
		return agentNode.getThreadPool();
	}

	/**
	 * Gets the execution cycle of this agent.
	 * 
	 * @return the execution cycle of this agent
	 */
	public IExecutionCycle getExecution() {
		return execution;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setExecution(IExecutionCycle execution) {
		// disable management of old execution cycle
		if (isManagementEnabled() && (this.execution != null)) {
			this.execution.disableManagement();
		}

		// change execution cycle
		this.execution = execution;
		this.execution.setThisAgent(this);

		// enable management of new execution cycle
		if (isManagementEnabled() && (this.execution != null)) {
			this.execution.enableManagement(_manager);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IAgentNode getAgentNode() {
		return agentNode;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAgentNode(IAgentNode agentNode) {
		// update management
		if (isManagementEnabled()) {
			Manager manager = _manager;
			disableManagement();
			this.agentNode = agentNode;
			enableManagement(manager);
		} else {
			this.agentNode = agentNode;
		}

		// update logger
		setLog(this.agentNode.getLog(this));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * {@inheritDoc}
	 */
	public Log getLog(IAgentBean bean) {
		if (agentNode == null) {
			return null;
		}
		return agentNode.getLog(this, bean);
	}

	/**
	 * {@inheritDoc}
	 */
	public Log getLog(IAgentBean owner, String extension) {
		if (agentNode == null) {
			return null;
		}
		return agentNode.getLog(this, owner, extension);
	}

	/**
	 * Returns the timeout after which the execution of a bean will be stopped.
	 * 
	 * @return the timeout in milliseconds
	 */
	public long getBeanExecutionTimeout() {
		return beanExecutionTimeout;
	}

	/**
	 * Sets the timeout after which the execution of a bean will be stopped.
	 * 
	 * @param beanExecutionTimeout
	 *            the timeout in milliseconds
	 */
	public void setBeanExecutionTimeout(long beanExecutionTimeout) {
		this.beanExecutionTimeout = beanExecutionTimeout;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAgentId() {
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
	public List<String> getAgentBeanNames() {
		ArrayList<String> ret = new ArrayList<String>();
		for (IAgentBean bean : getAgentBeans()) {
			ret.add(bean.getBeanName());
		}
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Action> getActionList() {
		return Collections.unmodifiableList(actionList);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setActionList(List<Action> actionList) {
		this.actionList = new ArrayList<Action>();
		this.actionList.addAll(actionList);
	}

	/**
	 * Getter for attribute "ActionNames" of the managed agent.
	 * 
	 * @return name of actions provided by this agent
	 */
	public List<String> getActionNames() {
		ArrayList<String> ret = new ArrayList<String>();
		for (Action action : getActionList()) {
			ret.add(action.getName());
		}
		return ret;
	}

	/**
	 * Getter for attribute "MemoryData" of the managed agent.
	 * 
	 * @return implementation of the memory of this agent
	 */
	public CompositeData getMemoryData() {
		if (memory == null) {
			return null;
		}
		String[] itemNames = new String[] { "class", "matcher", "updater" };
		try {
			CompositeType type = new CompositeType(
					"javax.management.openmbean.CompositeDataSupport",
					"Memory information", itemNames, new String[] {
							"Implementation of the memory instance",
							"Implementation of the matcher instance",
							"Implementation of the updater instance" },
					new OpenType[] { SimpleType.STRING, SimpleType.STRING,
							SimpleType.STRING });
			return new CompositeDataSupport(type, itemNames, new Object[] {
					memory.getClass().getName(),
					(memory.getMatcher() == null) ? null : memory.getMatcher()
							.getClass().getName(),
					(memory.getUpdater() == null) ? null : memory.getUpdater()
							.getClass().getName() });
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
	public String getExecutionCycleClass() {
		if (execution == null) {
			return null;
		}
		return execution.getClass().getName();
	}

	/**
	 * Registers the agent and all its resources for management
	 * 
	 * @param manager
	 *            the manager to be used for registration
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
			System.err.println("WARNING: Unable to register agent "
					+ getAgentName() + " of agent node "
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

		// register execution cycle for management
		if (execution != null) {
			execution.enableManagement(manager);
		}

		super.enableManagement(manager);
	}

	/**
	 * Deregisters the agent and all its resources from management.
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}

		// deregister memory from management
		if (memory != null) {
			memory.disableManagement();
		}

		// deregister execution cycle from management
		if (execution != null) {
			execution.disableManagement();
		}

		// deregister agent beans from management
		for (IAgentBean ab : this.agentBeans) {
			ab.disableManagement();
		}

		// deregister agent from management
		try {
			_manager.unregisterAgent(this);
		} catch (Exception e) {
			System.err.println("WARNING: Unable to deregister agent "
					+ getAgentName() + " of agent node "
					+ getAgentNode().getName() + " as JMX resource.");
			System.err.println(e.getMessage());
		}

		super.disableManagement();
	}

	/**
	 * Getter for the executionInterval timer
	 * 
	 * @return the be nice timer between to calls to the executionCycle
	 */
	public int getExecutionInterval() {
		return executionInterval;
	}

	/**
	 * Setter for the executionInterval timer
	 * 
	 * @param executionInterval
	 *            the be nice timer between to calls to the executionCycle
	 */
	public void setExecutionInterval(int executionInterval) {
		this.executionInterval = executionInterval;
	}

    /**
	 * {@inheritDoc}
	 */
	public Long getStartTime() throws InstanceNotFoundException {
		if (startTimeId == null) {
			return null;
		}
		try {
			return timerClient.getDate(startTimeId).getTime();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

    /**
	 * {@inheritDoc}
	 */
	public void setStartTime(Long startTime) throws InstanceNotFoundException {
		// add listener if needed
		if ((startTimeId == null) && (stopTimeId == null) && (startTime != null)) {
			try {
				timerClient.addTimerNotificationListener(this);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		// remove old timer notification
		if (startTimeId != null) {
			try {
				timerClient.removeNotification(startTimeId);
			}
			catch (IOException e) {
				e.printStackTrace();
			}			
		}

		// remove listener if no longer needed
		if ((startTimeId != null) && (stopTimeId == null) && (startTime == null)) {
			try {
				timerClient.removeTimerNotificationListener(this);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (ListenerNotFoundException e) {
				e.printStackTrace();
			}
		}

		// add new timer notification
		if (startTime != null) {
			try {
				startTimeId = timerClient.addNotification(null, null, null, new Date(startTime));
			}
			catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

    /**
	 * {@inheritDoc}
	 */
	public Long getStopTime() throws InstanceNotFoundException {
		if (stopTimeId == null) {
			return null;
		}
		try {
			return timerClient.getDate(stopTimeId).getTime();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

    /**
	 * {@inheritDoc}
	 */
	public void setStopTime(Long stopTime) throws InstanceNotFoundException {
		// add listener if needed
		if ((startTimeId == null) && (stopTimeId == null) && (stopTime != null)) {
			try {
				timerClient.addTimerNotificationListener(this);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		// remove old timer notification
		if (stopTimeId != null) {
			try {
				timerClient.removeNotification(stopTimeId);
			}
			catch (IOException e) {
				e.printStackTrace();
			}			
		}

		// remove listener if no longer needed
		if ((startTimeId == null) && (stopTimeId != null) && (stopTime == null)) {
			try {
				timerClient.removeTimerNotificationListener(this);
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			catch (ListenerNotFoundException e) {
				e.printStackTrace();
			}
		}

		// add new timer notification
		if (stopTime != null) {
			try {
				stopTimeId = timerClient.addNotification(null, null, null, new Date(stopTime));
			}
			catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

	/**
	 * Handles notifications about start and stop time.
	 * @param notification the received notification.
	 * @param handback the corresponding user data.
	 */
	public void handleNotification(Notification notification, Object handback) {
		if (notification instanceof TimerNotification) {
			Integer id = ((TimerNotification) notification).getNotificationID();
			if (id.equals(startTimeId)) {
				try {
					start();
				}
				catch (LifecycleException e) {
					e.printStackTrace();
				}
			}
			if (id.equals(stopTimeId)) {
				try {
					stop();
				}
				catch (LifecycleException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ///////////////////////////////////
	// TODO
	// ///////////////////////////////////
	//
	// - addAgentBean method
	// - removeAgentBean method
	//   -> suggestion: use the add, addAll, remove, and removeAll methods of List.
	//      Then we do not have to render the list unmodifiable...
	//   -> the (add|remove)All methods are convenient to install/remove new beans and synchronize their state changes between them
	// - setBeanState method should be renamed and moved to the AbstractLifecycle and ILifecycle
}
