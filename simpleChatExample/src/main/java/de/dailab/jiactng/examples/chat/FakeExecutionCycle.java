/*
 * $Id$ 
 */
package de.dailab.jiactng.examples.chat;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IExecutionCycle;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class FakeExecutionCycle extends AbstractAgentBean implements IExecutionCycle {
    public void setAgent(IAgent agent) {
        setThisAgent(agent);
    }

    public void run() {

    }
}
