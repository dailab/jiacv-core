/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

/**
 * It defines a logical communication address to support grouping of agents.
 * <p>
 * This interface is <b>not</b> intended to be subclassed by clients.
 * </p>
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface IGroupAddress extends ICommunicationAddress {
    IGroupAddress toUnboundAddress();
}
