/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.action;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.action.annotation.Expose;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ExampleExposingBean extends AbstractMethodExposingBean {
    private final Log _log;
    
    public ExampleExposingBean() {
        _log= LogFactory.getLog(getClass());
    }
    
    @Override
    protected Log getLog() {
        return _log;
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
}
