/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractAgentNodeBean extends AbstractLifecycle implements IAgentNodeBean, AbstractAgentNodeBeanMBean {
    protected IAgentNode agentNode;
    protected Manager manager = null;
    protected Log log;
    
    private String _beanName;

    public final void setAgentNode(IAgentNode agentNode) {
        this.agentNode = agentNode;
        log= LogFactory.getLog(agentNode.getName() + "." + getBeanName());
    }
    
    public final String getBeanName() {
        return _beanName;
    }

    public final void setBeanName(String beanName) {
        _beanName = beanName;
    }

	/**
	 * Gets information about the logger of this bean.
	 * 
	 * @return information about levels of the logger
	 */
	public CompositeData getLog() {
		if (log == null) {
			return null;
		}
		String[] itemNames = new String[] { "DebugEnabled", "ErrorEnabled",
				"FatalEnabled", "InfoEnabled", "TraceEnabled", "WarnEnabled" };
		try {
			CompositeType type = new CompositeType(log.getClass().getName(),
					"Logger information", itemNames, itemNames, new OpenType[] {
							SimpleType.BOOLEAN, SimpleType.BOOLEAN,
							SimpleType.BOOLEAN, SimpleType.BOOLEAN,
							SimpleType.BOOLEAN, SimpleType.BOOLEAN });
			return new CompositeDataSupport(type, itemNames, new Object[] {
					log.isDebugEnabled(), log.isErrorEnabled(),
					log.isFatalEnabled(), log.isInfoEnabled(),
					log.isTraceEnabled(), log.isWarnEnabled() });
		} catch (OpenDataException e) {
			e.printStackTrace();
			return null;
		}
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
