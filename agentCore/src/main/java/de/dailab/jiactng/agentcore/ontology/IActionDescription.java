/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IActionDescription extends IFact {
    String getName();
    void setName(String name);
    
    Class<?>[] getInputTypes();
    void setInputTypes(Class<?>[] inputTypes);
    
    Class<?>[] getResultTypes();
    void setResultTypes(Class<?>[] resultTypes);
    
    AgentDescription getProviderDescription();
    void setProviderDescription(AgentDescription description);
}
