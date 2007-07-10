/*
 * Created on 16.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import org.apache.commons.logging.Log;

import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;

/**
 * Abstract superclass of all agentbeans. This includes core-components as well
 * as agentbeans. The class handles basic references (such as to memory and the
 * agent) and defines the methods that are necessary for (lifecycle-)management.
 * 
 * @author Thomas Konnerth
 */
public abstract class AbstractAgentBean extends AbstractLifecycle implements
		IAgentBean {

	protected Log log = null;

	/**
	 * Creates an agent bean that uses lifecycle support in loose mode
	 */
	public AbstractAgentBean() {
		super();
	}

	/**
	 * Creates an agent bean that may use lifecycle support in strict mode. This
	 * means, that the lifecycle graph is enforced.
	 */
	public AbstractAgentBean(boolean strict) {
		super(strict);
	}

	/**
	 * Reference to the agent that holds this bean.
	 */
	protected IAgent thisAgent = null;

	/**
	 * Reference to the memory of the agent that holds this bean.
	 */
	protected IMemory memory = null;

	/**
	 * The name this bean. Note that this is the unqualified name which is
	 * assigned by Spring.
	 */
	protected String beanName = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentBean#setThisAgent(de.dailab.jiactng.agentcore.IAgent)
	 */
	public final void setThisAgent(IAgent agent) {
		this.thisAgent = agent;
		this.log = thisAgent.getLog(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentBean#setMemory(de.dailab.jiactng.agentcore.knowledge.IMemory)
	 */
	public final void setMemory(IMemory mem) {
		this.memory = mem;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentBean#setBeanName(java.lang.String)
	 */
	public final void setBeanName(String name) {
		this.beanName = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.AgentBeanMBean#getBeanName()
	 */
	public final String getBeanName() {
		return beanName;
		// return new StringBuffer(thisAgent.getAgentName()).append(".").append(
		// beanName).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doInit()
	 */
	public void doInit() throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStart()
	 */
	public void doStart() throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doStop()
	 */
	public void doStop() throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle#doCleanup()
	 */
	public void doCleanup() throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dailab.jiactng.agentcore.IAgentBean#execute()
	 */
	public void execute() {
	};

}
