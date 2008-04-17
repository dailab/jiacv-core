/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractAgentNodeBean extends AbstractLifecycle implements IAgentNodeBean, AbstractAgentNodeBeanMBean {
    protected IAgentNode agentNode;
    protected Manager manager = null;
    
    private String _beanName;

    public final void setAgentNode(IAgentNode agentNode) {
        this.agentNode = agentNode;
        setLog(agentNode.getLog(this));
    }
    
    public final String getBeanName() {
        return _beanName;
    }

    public final void setBeanName(String beanName) {
        _beanName = beanName;
    }

    /**
     * Deregisters the node bean and all its resources from management
     */
    public void disableManagement() {
        // do nothing if management already disabled
        if (!isManagementEnabled()) {
            return;
        }

        // deregister node bean from management
        try {
            manager.unregisterAgentNodeResource(agentNode.getName(), "agentNodeBean", getBeanName());
        } catch (Exception e) {
            System.err.println("WARNING: Unable to deregister node bean " + _beanName + " of node "
                    + agentNode.getName() + " as JMX resource.");
            System.err.println(e.getMessage());
        }

        manager = null;
    }

    /**
     * Registers the node bean and all its resources for management
     * 
     * @param manager
     *            the manager responsible for this agentbean.
     */
    public void enableManagement(Manager manager) {
        // do nothing if management already enabled
        if (isManagementEnabled()) {
            return;
        }

        // register agentNode bean for management
        try {
            manager.registerAgentNodeResource(agentNode.getName(), "agentNodeBean", getBeanName(), this);
        } catch (Exception e) {
            System.err.println("WARNING: Unable to register node bean " + _beanName + " of node " + agentNode.getName()
                    + " as JMX resource.");
            System.err.println(e.getMessage());
        }

        this.manager = manager;
    }

    /**
     * Checks wether the management of this object is enabled or not.
     * 
     * @return true if the management is enabled, otherwise false
     */
    public boolean isManagementEnabled() {
        return manager != null;
    }

    @Override
    public void doCleanup() throws Exception {}

    @Override
    public void doInit() throws Exception {}

    @Override
    public void doStart() throws Exception {}

    @Override
    public void doStop() throws Exception {}
}
