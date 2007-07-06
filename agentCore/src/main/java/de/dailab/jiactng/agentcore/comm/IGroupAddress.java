/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * It defines a logical communication address to support grouping of agents.
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public interface IGroupAddress extends ICommunicationAddress {
    
    /**
     * Checks whether the group referenced by this address is a closed
     * group.
     * 
     * @return  <code>true</code> if this address references a closed group
     *          and <code>false</code> otherwise.
     */
    boolean isClosed();
}
