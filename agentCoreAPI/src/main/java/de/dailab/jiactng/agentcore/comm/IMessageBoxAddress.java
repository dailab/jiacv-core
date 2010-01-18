/*
 * $Id: IMessageBoxAddress.java 16194 2008-02-11 14:38:34Z marcel $ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * Specifies an address type for point-to-point communication.
 * <p>
 * This interface is <b>not</b> intended to be subclassed by clients.
 * </p>
 * 
 * @author Marcel Patzlaff
 * @version $Revision: 16194 $
 */
public interface IMessageBoxAddress extends ICommunicationAddress {

	/**
	 * Gets the corresponding unbounded address.
	 */
    IMessageBoxAddress toUnboundAddress();
}
