/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ProviderBean extends AbstractMethodExposingBean {
    @Expose(name = "log")
    public void log(String message) {
        log.debug("I was forced to log something:: '" + message + "'");
    }
    
    @Expose(name = "getCurrentTime")
    public long getCurrentTime() {
        log.debug("somebody want MY time... tztz");
        return System.currentTimeMillis();
    }
}
