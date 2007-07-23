/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.net.URI;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Specifies an address for inter-agent communication.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface ICommunicationAddress extends IFact {
    /**
     * Returns the <code>String</code> that was used to create
     * this communication address
     * 
     * @return
     */
    String getName();
    
    /**
     * Checks whether the destination this address references exists.
     * 
     * @return
     */
    boolean exists();
    boolean isBoundToTransport();
    URI toURI();
    <T extends ICommunicationAddress> T toUnboundAddress();
}
