/**
 * 
 */
package de.dailab.jiactng.agentcore.comm.util;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Used for invoking a service via the web service interface
 * 
 * @author schenk
 *
 */

public interface IWSClient extends IFact {
	
	/**
	 * Is used to invoke a web service.
	 * @param argument the parameters for the service
	 * @return the result of the service usage.
	 */
	
	public Serializable invokeService(IServiceInvocationArgument argument);

}
