/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * Abstract superclass of all agent node beans.
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractAgentNodeBean extends AbstractLifecycle implements IAgentNodeBean, AbstractAgentNodeBeanMBean, BeanNameAware {
    protected IAgentNode agentNode;
    
    private String beanName;

    /**
     * Sets the agent node of this agent node bean.
     * @param newAgentNode the agent node
     */
    public final void setAgentNode(IAgentNode newAgentNode) {
        agentNode = newAgentNode;
        setLog(newAgentNode.getLog(this));
    }

    /**
     * Gets the name of this agent node bean.
     * @return the name of the agent node bean
     */
    public final String getBeanName() {
        return beanName;
    }

    /**
     * Sets the name of this agent node bean.
     * @param newBeanName the name of the agent node bean
     */
    public final void setBeanName(String newBeanName) {
        beanName = newBeanName;
    }

    /**
     * Deregisters the node bean and all its resources from management
     */
    public final void disableManagement() {
        // do nothing if management already disabled
        if (!isManagementEnabled()) {
            return;
        }

        // deregister node bean from management
        try {
            _manager.unregisterAgentNodeBean(this, agentNode);
        } catch (Exception e) {
            System.err.println("WARNING: Unable to deregister node bean " + beanName + " of node "
                    + agentNode.getName() + " as JMX resource.");
            System.err.println(e.getMessage());
        }

        super.disableManagement();
    }

    /**
     * Registers the node bean and all its resources for management
     * 
     * @param manager
     *            the manager responsible for this agentbean.
     */
    public final void enableManagement(Manager manager) {
        // do nothing if management already enabled
        if (isManagementEnabled()) {
            return;
        }

        // register agentNode bean for management
        try {
            manager.registerAgentNodeBean(this, agentNode);
        } catch (Exception e) {
            System.err.println("WARNING: Unable to register node bean " + beanName + " of node " + agentNode.getName()
                    + " as JMX resource.");
            System.err.println(e.getMessage());
        }

        super.enableManagement(manager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCleanup() throws Exception {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void doInit() throws Exception {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStart() throws Exception {}

    /**
     * {@inheritDoc}
     */
    @Override
    public void doStop() throws Exception {}
}
