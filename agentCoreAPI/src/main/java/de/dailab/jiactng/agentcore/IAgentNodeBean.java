/*
 * $Id: IAgentNodeBean.java 19890 2008-08-29 12:11:01Z keiser $ 
 */
package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;

/**
 * Interface for all beans of an agentNode. It declares all required methods
 * for the plug-in mechanism and also insures inheritance from the
 * ILifecycle and Manageable interfaces.
 * 
 * @author Marcel Patzlaff
 * @version $Revision: 19890 $
 */
public interface IAgentNodeBean extends ILifecycle {
    /**
     * Setter for the agentNode-reference that is responsible for this
     * agentNodeBean.
     * 
     * @param agentNode     the agentNode that holds this bean
     */
    void setAgentNode(IAgentNode agentNode);
    
    /**
     * Setter for the beanName.
     * 
     * @param name
     *            the unqualified name of the bean.
     */
    void setBeanName(String name);

    /**
     * Getter for the name of this node bean
     * 
     * @return a string representing the name of this node bean.
     */
    String getBeanName();
}
