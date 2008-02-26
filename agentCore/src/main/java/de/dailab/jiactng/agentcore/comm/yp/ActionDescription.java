/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.yp;

import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ActionDescription implements IActionDescription {
    protected String name;
    protected Class<?>[] inputTypes;
    protected Class<?>[] resultTypes;
    protected AgentDescription providerDescription;
    
    @Override
    public Class<?>[] getInputTypes() {
        return inputTypes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AgentDescription getProviderDescription() {
        return providerDescription;
    }

    @Override
    public Class<?>[] getResultTypes() {
        return resultTypes;
    }

    @Override
    public void setInputTypes(Class<?>[] inputTypes) {
        this.inputTypes= inputTypes;
    }

    @Override
    public void setName(String name) {
        this.name= name;
    }

    @Override
    public void setProviderDescription(AgentDescription description) {
        this.providerDescription= description;
    }

    @Override
    public void setResultTypes(Class<?>[] resultTypes) {
        this.resultTypes= resultTypes;
    }
}
