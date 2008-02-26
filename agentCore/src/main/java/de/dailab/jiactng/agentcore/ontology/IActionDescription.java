/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.ontology;

import java.util.Collections;
import java.util.List;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * This interface specifies a generic action description.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IActionDescription extends IFact {
    /**
     * This method returns the name of the action this description
     * refers to.
     * 
     * @return      the name or <code>null</code> if not set
     */
    String getName();
    
    /**
     * This method returns the types of the input
     * parameters that the action accepts.
     * 
     * <p>
     * The type list cannot be modified.
     * </p>
     *
     * @see Collections#unmodifiableList(List)
     * @return      the unmodifiable list of input parameter types
     *              or <code>null</code> if not set
     */
    List<Class<?>> getInputTypes();
    
    /**
     * This method returns the result types of the described
     * action.
     * 
     * <p>
     * The type list cannot be modified.
     * </p>
     * 
     * @see Collections#unmodifiableList(List)
     * @return      the unmodifiable list of return types or
     *              <code>null</code> if not set
     */
    List<Class<?>> getResultTypes();
    
    /**
     * This method returns the agent description of the
     * agent that provides the action.
     * 
     * @return      the agent description of the action provider
     *              or <code>null</code> if not set
     */
    IAgentDescription getProviderDescription();
}
