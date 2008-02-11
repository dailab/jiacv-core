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
    IMessageBoxAddress toUnboundAddress();
}
