/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * Specifies an address type for point-to-point communication.
 * <p>
 * This interface is <b>not</b> intended to be subclassed by clients.
 * </p>
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IMessageBoxAddress extends ICommunicationAddress {
    /**
     * Checks whether the message box referenced by this address is local
     * to the holder of this object.
     * 
     * @return  <code>true</code> if the address points to a local message box and
     *          <code>false</code> otherwise.
     */
    boolean isLocal();
    
    IMessageBoxAddress toUnboundAddress();
}
