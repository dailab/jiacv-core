/*
 * $Id: IActionDescription.java 22434 2009-03-13 10:10:59Z axle $ 
 */
package de.dailab.jiactng.agentcore.ontology;

import java.util.Collections;
import java.util.List;

import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * This interface specifies a generic action description.
 * 
 * @author Marcel Patzlaff
 * @version $Revision: 22434 $
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
     * This method returns the type names of the input
     * parameters that the action accepts.
     * 
     * <p>
     * The type list cannot be modified.
     * </p>
     *
     * @see Collections#unmodifiableList(List)
     * @return      the unmodifiable list of input parameter type names
     *              or <code>null</code> if not set
     */
    List<String> getInputTypeNames();
    
    /**
     * This method returns the result type names of the described
     * action.
     * 
     * <p>
     * The type list cannot be modified.
     * </p>
     * 
     * @see Collections#unmodifiableList(List)
     * @return      the unmodifiable list of result type names or
     *              <code>null</code> if not set
     */
    List<String> getResultTypeNames();
    
    /**
     * This method returns the types of the input
     * parameters that the action accepts.
     * 
     * @return      a created list of input parameter types
     *              or <code>null</code> if corresponding list of parameter type names is not set
	 * @throws ClassNotFoundException if one of the classes is unknown.
     */
    List<Class<?>> getInputTypes() throws ClassNotFoundException;
    
    /**
     * This method returns the result types of the described
     * action.
     * 
     * @return      a created list of result types or
     *              <code>null</code> if corresponding list of result type names is not set
	 * @throws ClassNotFoundException if one of the classes is unknown.
     */
    List<Class<?>> getResultTypes() throws ClassNotFoundException;
    
    /**
     * This method returns the agent description of the
     * agent that provides the action.
     * 
     * @return      the agent description of the action provider
     *              or <code>null</code> if not set
     */
    IAgentDescription getProviderDescription();
    
    /**
     * Return the scope of the action.
     * 
     * @return the scope of the action
     * @see ActionScope
     */
    ActionScope getScope();
}
