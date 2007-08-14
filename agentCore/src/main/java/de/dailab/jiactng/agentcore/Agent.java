/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.management.Manager;
import de.dailab.jiactng.agentcore.ontology.AgentBeanDescription;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
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
public class Agent extends AbstractLifecycle implements IAgent, AgentMBean {

	/**
	 * The AID (agent identifier). This property is generated and assigned
	 * automatically during agent creation. It is not intended to make sense for
	 * human readers.
	 */
	private final String agentId;

	/**
	 * Reference to the agentnode that holds this agent.
	 */
	private IAgentNode agentNode = null;

	/**
	 * The log-instance for this agent.
	 */
	private Log agentLog = null;

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
	private ArrayList<IAgentBean> agentBeans = null;

	/**
	 * Synchronization object for the Thread
	 */
	private Boolean syncObj = new Boolean(true);

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
	private Future executionFuture = null;

	/**
	 * Timeout after which the execution of a bean will be stopped and the agent
	 * as well. TODO do something more intelligent, possibly recover the bean
	 * without stopping the agent.
	 */
	private long beanExecutionTimeout = 5*60*1000;

	private ArrayList<Action> actionList = null;

	/** The manager of the agent node */
	protected Manager _manager = null;

	/**
	 * Getter for the memory-component
	 * 
	 * @return a reference to the IMemory implementation of this agent.
	 * 
	 * public IMemory getMemory() { return memory; }
	 */

	/**
	 * Public default constructor, creating the agent identifier.
	 */
	public Agent() {
		agentId = IdFactory.createAgentId(this.hashCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#setMemory(de.dailab.jiactng.agentcore.knowledge.IMemory)
	 */
	public void setMemory(IMemory memory) {
		this.memory = memory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#getAgentBeans()
	 */
	public ArrayList<IAgentBean> getAgentBeans() {
		return agentBeans;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#setAgentBeans(java.util.ArrayList)
	 */
	public void setAgentBeans(ArrayList<IAgentBean> agentbeans) {
		this.agentBeans = agentbeans;

		// set references for all agent beans
		for (IAgentBean ab : this.agentBeans) {
			ab.setThisAgent(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#run()
	 */
	public void run() {
		while (active) {
			try {
				synchronized (syncObj) {
					executionFuture = agentNode.getThreadPool().submit(
							execution);
					FutureTask t = ((FutureTask) executionFuture);
					try {
						t.get(beanExecutionTimeout, TimeUnit.MILLISECONDS);
					} catch (TimeoutException to) {
						System.err.print("this: " + agentName);
						to.printStackTrace();
						t.cancel(true);
						this.stop();
						agentLog.error("ExecutionCycle did not return");
					}
				}
			} catch (Exception e) {
				agentLog.error("Critical error in controlcycle of agent: "
						+ agentName + ". Stopping Agent.");
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
	 * @see de.dailab.jiactng.agentcore.IAgent#setBeanName(java.lang.String)
	 */
	public void setBeanName(String arg0) {
		// update management
		if (isManagementEnabled()) {
			Manager manager = _manager;
			disableManagement();
			this.agentName = arg0;
			enableManagement(manager);
		} else {
			this.agentName = arg0;
		}

		// update logger
		if (agentNode != null) {
			this.agentLog = agentNode.getLog(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#onEvent(de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent)
	 */
	public void onEvent(LifecycleEvent evt) {
		// TODO Auto-generated method stub

	}

	/**
	 * Stops and undeploys this agent from its agent node (incl. deregistration
	 * as JMX resource).
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#doCleanup()
	 */
	@Override
	public void doCleanup() throws LifecycleException {
		synchronized (syncObj) {
			if (executionFuture != null) {
				executionFuture.cancel(true);
				executionFuture = null;
			}
		}

		// call cleanup for all agentbeans
		for (IAgentBean a : this.agentBeans) {
			try {
				a.cleanup();
				setBeanState(a.getBeanName(), LifecycleStates.CLEANED_UP);
			} catch (LifecycleException e) {
				handleBeanException(a, e, LifecycleStates.CLEANING_UP);
			}
		}
		this.memory.removeAll(new Action(null, null, null, null));

		// update state information in agent's memory
		updateState(LifecycleStates.CLEANED_UP);

		this.actionList = null;
		this.execution.cleanup();
		this.memory.cleanup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#doInit()
	 */
	@Override
	public void doInit() throws LifecycleException {
		// initialize agent elements
		this.actionList = new ArrayList<Action>();
		this.memory.init();
		this.memory.write(new ThisAgentDescription(this.agentId,
				this.agentName, LifecycleStates.INITIALIZING.name(), null));

		this.execution.setAgent(this);
		this.execution.init();
		((AbstractAgentBean) this.execution).setMemory(memory);

		// call init for all agentbeans
		for (IAgentBean ab : this.agentBeans) {
			try {
				ab.setMemory(memory);
				ab.init();
				if (ab instanceof ILifecycle) {
					ab.addLifecycleListener(this);
				}
				memory.write(new AgentBeanDescription(ab.getBeanName(),
						LifecycleStates.INITIALIZED.name()));
				setBeanState(ab.getBeanName(), LifecycleStates.INITIALIZED);
			} catch (LifecycleException e) {
				handleBeanException(ab, e, LifecycleStates.INITIALIZING);
			}

			// if bean is effector, add all actions to memory
			if (ab instanceof IEffector) {
				ArrayList<? extends Action> acts = ((IEffector) ab)
						.getActions();
				for (Action item : acts) {
					memory.write(item);
					actionList.add(item);
				}
			}
		}

		updateState(LifecycleStates.INITIALIZED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#doStart()
	 */
	@Override
	public void doStart() throws LifecycleException {

		this.memory.start();
		this.execution.start();

		// call start for all agentbeans
		for (IAgentBean a : this.agentBeans) {
			try {
				a.start();
				setBeanState(a.getBeanName(), LifecycleStates.STARTED);
			} catch (LifecycleException e) {
				handleBeanException(a, e, LifecycleStates.STARTING);
			}
		}

		synchronized (syncObj) {
			active = true;
		}
		updateState(LifecycleStates.STARTED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#doStop()
	 */
	@Override
	public void doStop() throws LifecycleException {

		this.memory.stop();
		this.execution.stop();

		// call stop for all agentbeans
		for (IAgentBean a : this.agentBeans) {
			try {
				a.stop();
				setBeanState(a.getBeanName(), LifecycleStates.STOPPED);
			} catch (LifecycleException e) {
				handleBeanException(a, e, LifecycleStates.STOPPING);
			}
		}
		synchronized (syncObj) {
			active = false;
			if (executionFuture != null) {
				executionFuture.cancel(false);
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
		e.printStackTrace();
	}

	/**
	 * Utility-Method that updates the state of the agent in the Memory
	 * 
	 * @param newState
	 *            the new state
	 */
	private void updateState(ILifecycle.LifecycleStates newState) {
		memory.update(new ThisAgentDescription(null, null, null, null),
				new ThisAgentDescription(null, null, newState.name(), null));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#getAgentState()
	 */
	public LifecycleStates getAgentState() {
		return LifecycleStates.valueOf(memory.read(
				new ThisAgentDescription(null, null, null, null)).getState());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBeanState(String beanName, LifecycleStates newState) {
		this.memory.update(new AgentBeanDescription(beanName, null),
				new AgentBeanDescription(null, newState.name()));
	}

	/**
	 * {@inheritDoc}
	 */
	public LifecycleStates getBeanState(String beanName) {
		return LifecycleStates.valueOf(this.memory.read(
				new AgentBeanDescription(beanName, null)).getState());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#getAgentName()
	 */
	public String getAgentName() {
		return this.agentName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#getThreadPool()
	 */
	public ExecutorService getThreadPool() {
		return agentNode.getThreadPool();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#getExecution()
	 */
	public IExecutionCycle getExecution() {
		return execution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#setExecution(de.dailab.jiactng.agentcore.IExecutionCycle)
	 */
	public void setExecution(IExecutionCycle execution) {
		this.execution = execution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#getAgentNode()
	 */
	public IAgentNode getAgentNode() {
		return agentNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#getName()
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getName()
	 */
	public String getName() {
		return agentName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#setAgentNode(de.dailab.jiactng.agentcore.IAgentNode)
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
		this.agentLog = this.agentNode.getLog(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#setName(java.lang.String)
	 */
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#getOwner()
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getOwner()
	 */
	public String getOwner() {
		return owner;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgent#setOwner(java.lang.String)
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Log getLog(IAgentBean bean) {
		if (agentNode == null) {
			return null;
		}
		return agentNode.getLog(this, bean);
	}
    
	public Log getLog(IAgentBean owner, String extension) {
		if (agentNode == null) {
			return null;
		}
        return agentNode.getLog(this, owner, extension);
    }

    /**
	 * @see de.dailab.jiactng.agentcore.AgentMBean#getAgentNodeUUID()
	 */
	public String getAgentNodeUUID() {
		return agentNode.getUUID();
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
	public void setExecutionTimeout(long beanExecutionTimeout) {
		this.beanExecutionTimeout = beanExecutionTimeout;
	}

	/**
	 * Returns the agent identifier.
	 * 
	 * @return the agent identifier of this agent
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
		return memory.read(new ThisAgentDescription(null, null, null, null));
	}

	public ArrayList<String> getAgentBeanNames() {
		ArrayList<String> ret = new ArrayList<String>();
		for (IAgentBean bean : getAgentBeans()) {
			ret.add(bean.getBeanName());
		}
		return ret;
	}

	public ArrayList<Action> getActionList() {
		return actionList;
	}

	public void setActionList(ArrayList<Action> actionList) {
		this.actionList = actionList;
	}

	/**
	 * Getter for attribute "ActionNames" of the managed agent.
	 * @return name of actions provided by this agent
	 */
	public ArrayList<String> getActionNames() {
		ArrayList<String> ret = new ArrayList<String>();
		for (Action action : getActionList()) {
			ret.add(action.getName());
		}
		return ret;		
	}

	/**
	 * Getter for attribute "Logger" of the managed agent.
	 * @return information about the logger of this agent
	 */
	public CompositeData getLogger() {
		if (agentLog == null) {
			return null;
		}
		String[] itemNames = new String[] {"class", "debug", "error", "fatal", "info", "trace", "warn"};
		try {
			CompositeType type = new CompositeType("javax.management.openmbean.CompositeDataSupport", "Logger information", itemNames, new String[] {"Implementation of the logger instance", "debug", "error", "fatal", "info", "trace", "warn"}, new OpenType[] {SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN, SimpleType.BOOLEAN});
			return new CompositeDataSupport(type, itemNames, new Object[] {agentLog.getClass().getName(), agentLog.isDebugEnabled(), agentLog.isErrorEnabled(), agentLog.isFatalEnabled(), agentLog.isInfoEnabled(), agentLog.isTraceEnabled(), agentLog.isWarnEnabled()});
		}
		catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}		
	}

	/**
	 * Getter for attribute "MemoryData" of the managed agent.
	 * @return implementation of the memory of this agent
	 */
	public CompositeData getMemoryData() {
		if (memory == null) {
			return null;
		}
		String[] itemNames = new String[] {"class", "matcher", "updater"};
		try {
			CompositeType type = new CompositeType("javax.management.openmbean.CompositeDataSupport", "Memory information", itemNames, new String[] {"Implementation of the memory instance", "Implementation of the matcher instance", "Implementation of the updater instance"}, new OpenType[] {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING});
			return new CompositeDataSupport(type, itemNames, new Object[] {memory.getClass().getName(), (memory.getMatcher() == null)? null:memory.getMatcher().getClass().getName(), (memory.getUpdater() == null)? null:memory.getUpdater().getClass().getName()});
		}
		catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}		
	}

	/**
	 * Getter for attribute "ExecutionCycleClass" of the managed agent.
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
     * @param manager the manager to be used for registration
	 */
	public void enableManagement(Manager manager) {
		// do nothing if management already enabled
		if (isManagementEnabled()) {
			return;
		}
		
		// register agent for management
		try {
			manager.registerAgent(this);
		}
		catch (Exception e) {
			System.err.println("WARNING: Unable to register agent " + getAgentName() + " of agent node " + getAgentNode().getName() + " as JMX resource.");
			System.err.println(e.getMessage());					
		}

		// register agent beans for management
		for (IAgentBean ab : this.agentBeans) {
			ab.enableManagement(manager);
		}
		
		_manager = manager;
	}
	  
	/**
	 * Deregisters the agent and all its resources from management
	 * @param manager
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}
		
		// deregister agent beans from management
		for (IAgentBean ab : this.agentBeans) {
			ab.disableManagement();
		}
		
		// deregister agent from management
		try {
			_manager.unregisterAgent(this);
		}
		catch (Exception e) {
			System.err.println("WARNING: Unable to deregister agent " + getAgentName() + " of agent node " + getAgentNode().getName() + " as JMX resource.");
			System.err.println(e.getMessage());					
		}
		
		_manager = null;
	}

	/**
	 * Checks wether the management of this object is enabled or not.
	 * @return true if the management is enabled, otherwise false
	 */
	public boolean isManagementEnabled() {
		return _manager != null;
	}
	  
}
