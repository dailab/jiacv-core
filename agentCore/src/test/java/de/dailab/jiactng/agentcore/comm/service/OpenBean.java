/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class OpenBean extends AbstractMethodExposingBean {
    @Override
    public void doInit() throws Exception {
        super.doInit();
        
        ServiceBean serviceBean= null;
        for(IAgentBean bean : thisAgent.getAgentBeans()) {
            if(bean instanceof ServiceBean) {
                serviceBean= (ServiceBean) bean;
                break;
            }
        }
        
        if(serviceBean == null) {
            log.error("this agent has not serviceBean");
        }
        
        for(Action action : getActions()) {
            serviceBean.offerAction(action);
        }
    }
    
    @Expose
    public void log(String message) {
        log.debug("I was forced to log something:: '" + message + "'");
    }
    
    @Expose
    public long getCurrentTime() {
        log.debug("somebody want MY time... tztz");
        return System.currentTimeMillis();
    }
}
