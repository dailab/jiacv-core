/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import de.dailab.jiactng.agentcore.action.RemoteAction;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
class RemoteActionContext {
    final RemoteAction remoteAction;
    final ICommunicationAddress providerAddress;
    
    RemoteActionContext(RemoteAction remoteAction, ICommunicationAddress providerAddress) {
        this.remoteAction= remoteAction;
        this.providerAddress= providerAddress;
    }
}
