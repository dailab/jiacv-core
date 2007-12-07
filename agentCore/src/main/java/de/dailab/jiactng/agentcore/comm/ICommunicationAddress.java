/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import java.net.URI;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Specifies an address for inter-agent communication.
 * 
 * This interface is <strong>not</strong> intended to be subclassed by clients!
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
    
    /**
     * Checks whether this address is bound to a specific transport type.
     * Currently this information is only evaluated when addressing a
     * message box.
     * 
     * @return      <code>true</code> if this address is bound to a transport and
     *              <code>false</code> otherwise.
     */
    boolean isBoundToTransport();
    
    /**
     * This method can be used to obtain the URI which defines this communication
     * address.
     * 
     * @return      the URI for this address.
     */
    URI toURI();
    
    /**
     * If this address is bound to a specific transport then this method
     * returns the generic unbound address. Otherwise it returns a reference to
     * itself.
     * 
     * @return      the generic unbound address for this address
     * 
     * @see         IMessageBoxAddress#toUnboundAddress()
     * @see         IGroupAddress#toUnboundAddress()
     */
    ICommunicationAddress toUnboundAddress();
}
