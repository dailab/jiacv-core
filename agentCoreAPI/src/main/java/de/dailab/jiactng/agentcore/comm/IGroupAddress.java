/*
 * $Id: IGroupAddress.java 16194 2008-02-11 14:38:34Z marcel $ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * It defines a logical communication address to support grouping of agents.
 * <p>
 * This interface is <b>not</b> intended to be subclassed by clients.
 * </p>
 * 
 * @author Marcel Patzlaff
 * @version $Revision: 16194 $
 */
public interface IGroupAddress extends ICommunicationAddress {

	/**
	 * Gets the corresponding unbounded address.
	 */
    IGroupAddress toUnboundAddress();
}
