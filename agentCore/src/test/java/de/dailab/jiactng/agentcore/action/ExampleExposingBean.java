/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.util.Set;

import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ExampleExposingBean extends AbstractMethodExposingBean {
    
    public ExampleExposingBean() {
        
    }
    
    
    @Expose
    public boolean getFlag() {
        return true;
    }

    @Expose
    public void saveMessage(String str, IJiacMessage message) {
        
    }
    
    @Expose(name = "OderDochAnders")
    public void ichHeisseSo(Object foo) {
        
    }
    
    public Set<Action> getAllActionsFromMemory() {
        return memory.readAllOfType(Action.class);
    }

    public Action getActionFromMemory(String name) {
    	return memory.read(new Action(name));
    }
}
