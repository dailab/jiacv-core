/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore;

import org.springframework.beans.factory.BeanNameAware;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.management.Manageable;

/**
 * Interface for all beans of an agentNode. It declares all required methods
 * for the plug-in mechanism and also insures inheritance from the
 * ILifecycle and Manageable interfaces.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IAgentNodeBean extends ILifecycle, BeanNameAware, Manageable {
    /**
     * Setter for the agentNode-reference that is responsible for this
     * agentNodeBean.
     * 
     * @param agentNode     the agentNode that holds this bean
     */
    void setAgentNode(IAgentNode agentNode);
    
    /**
     * Setter for the beanName. This method is called by Spring during
     * initialisation.
     * 
     * @param name
     *            the unqualified name of the bean.
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name);

    /**
     * Getter for the name of the agentbean
     * 
     * @return a string representing the name of the agentbean.
     */
    public String getBeanName();
}
