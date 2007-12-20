/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.IActionInvocationHandler;
import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean.Expose;
import de.dailab.jiactng.agentcore.comm.CommunicationException;

/**
 * This interface describes the capabilities of a simple service bean.
 * <p>
 * All method declared herein are exposed as actions and can thus be used
 * via {@link IActionInvocationHandler}.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IServiceBean {
    /**
     * Offers an action to other agents.
     * 
     * @param template      the action template to expose to other agents
     */
    @Expose
    boolean offerAction(Action template);
    
    /**
     * Withdraws a previously offered action.
     * 
     * @param template      the action template to withdraw
     */
    @Expose
    boolean withdrawAction(Action template);
    
    /**
     * Request the search of a remote action for the
     * specified action template.
     * 
     * @param template      the action template for the search
     */
    @Expose
    void searchRemoteAction(Action template) throws CommunicationException;
}
