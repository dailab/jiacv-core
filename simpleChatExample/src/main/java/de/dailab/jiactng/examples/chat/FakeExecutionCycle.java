/*
 * $Id$ 
 */
package de.dailab.jiactng.examples.chat;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.execution.IExecutionCycle;
import java.util.List;

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
    
    public void setAutoExecutionServices(List<String> actionIds) {
      ;
	  }
	    
	  public List<String> getAutoExecutionServices() {
	    return null;
	    
	  }
	    
	  public void setAutoExecutionType(boolean continous) {
	    ;
	  }
	    
	  public boolean getAutoExecutionType() {
	    return true;
	  }
}
