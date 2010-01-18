package de.dailab.jiactng.agentcore.knowledge;

import java.io.Serializable;

/**
 * The interface is though to be the super interface of all knowledge add to and removed from the memory.
 */
public interface IFact extends Serializable {
    /**
     * This method have to be implemented to enable the
     * template matching of the memory.
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
