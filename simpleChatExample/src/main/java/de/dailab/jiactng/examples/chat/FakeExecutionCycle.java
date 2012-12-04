/*
 * $Id$ 
 */
package de.dailab.jiactng.examples.chat;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.execution.IExecutionCycle;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
    
    public void setAutoExecutionServices(Map<String, Map<String, Serializable>> autoExecutionServices) {
      ;
	  }
	    
	  public Map<String, Map<String, Serializable>> getAutoExecutionServices() {
	    return null;
	    
	  }
}
