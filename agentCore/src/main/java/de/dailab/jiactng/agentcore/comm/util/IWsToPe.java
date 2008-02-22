package de.dailab.jiactng.agentcore.comm.util;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Used for invocation of a TNG-service via the generic web service interface.
 * @author schenk
 * 
 * @deprecated This class should be removed as specific webservice handling is not part of agentCore.
 */
@Deprecated
public interface IWsToPe extends IFact {

	/**
	 * Invokes a tng service via the protocol enabler.
	 * @param argument an object containing the operation name and the arguments.
	 * @return the result of the service usage.
	 */
	
	public Serializable invokeService(IServiceInvocationArgument argument);
	
}
