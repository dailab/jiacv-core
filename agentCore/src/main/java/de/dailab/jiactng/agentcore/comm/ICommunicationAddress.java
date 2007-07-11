/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Specifies an address for inter-agent communication.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface ICommunicationAddress extends IFact {
    /**
     * Returns the <code>String</code> representation for this communication
     * address.
     * 
     * @return
     */
    String getAddress();
    
    /**
     * Checks whether the destination this address references exists.
     * 
     * @return
     */
    boolean exists();
}
