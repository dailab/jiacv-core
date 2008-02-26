/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.yp;

import java.util.List;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ActionDescription implements IActionDescription {
    protected String name;
    protected List<Class<?>> inputTypes;
    protected List<Class<?>> resultTypes;
    protected IAgentDescription providerDescription;
    
    @Override
    public List<Class<?>> getInputTypes() {
        return inputTypes;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public IAgentDescription getProviderDescription() {
        return providerDescription;
    }
    @Override
    public List<Class<?>> getResultTypes() {
        return resultTypes;
    }
}
