/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IActionInvocationPreparer {
    void setResultReceiver(ResultReceiver receiver);
}
