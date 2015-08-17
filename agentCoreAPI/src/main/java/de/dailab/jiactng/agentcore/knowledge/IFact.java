package de.dailab.jiactng.agentcore.knowledge;

import java.io.Serializable;

/**
 * The interface is meant to be the super-interface of all knowledge 
 * that can be added to and removed from the memory.
 * 
 * HOW TUPLESPACE MATCHING WORKS (as learned directly from Grzegorz Lehmann)
 * 
 * - the matcher DOES NOT use equals and/or hashcode. Those methods can and 
 *   should be implemented as they should be, i.e. transitive, reflexive, and 
 *   symmetrical, and stuff that is equal should also have the same hashcode
 * 
 * - the matcher DOES use reflection, particularly PUBLIC attributes, GETTERS
 *   and SETTERS (using the GetterSetterFinder)
 * 
 * - if an attribute has a GETTER, it MUST also have a SETTER; attributes that
 *   have JUST a GETTER with NO SETTER will be IGNORED! This is to prevent the 
 *   GetterSetterFinder from using non-getter-methods that happen to start with 
 *   "get-", e.g. long-running DB access stuff; also, memory.update requires
 *   SETTER methods next to GETTERS
 *   
 * - Thus, IFact DOES NOT WORK with final attributes, as those will have no SETTER
 * 
 * In a nutshell:
 * 
 * - EQUALS and HASHCODE should be implemented "normally", i.e. no "equals or null"
 * 
 * - all attributes MUST be PUBLIC or have GETTERS AND SETTERS
 */
public interface IFact extends Serializable {
    
	/**
     * This method DOES NOT have to be implemented to enable the
     * template matching of the memory. It can be implemented properly,
     * as equals is meant to be implemented
     * 
     * @param obj the other knowledge fact
     * @return <code>true</code> if the facts are equal.
     * @see Object#equals(Object)
     */
    boolean equals(Object obj);
    
    /**
     * This method must be implemented to 
     * remain consistent with {@link #equals(Object)}.
     * @return the hash code of this fact.
     * @see Object#hashCode()
     */
    int hashCode();
}
