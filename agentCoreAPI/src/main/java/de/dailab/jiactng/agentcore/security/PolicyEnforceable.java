/**
 * 
 */
package de.dailab.jiactng.agentcore.security;

import de.dailab.jiactng.agentcore.IAgentBean;

/**
 * Indicates that an agent bean supports the enforcement of a (security) policy
 * through a policy enforcement point (PEP).
 * 
 * Implementations of this interface should be agent beans.
 * 
 * @author bsufka
 * 
 * @see PolicyDecisionRequest
 * @see PolicyDecisionResult
 * @see PolicyEnforcementPoint
 * @see IAgentBean
 */
public interface PolicyEnforceable {

	/**
	 * Setter for the policy enforcement point.
	 * 
	 * @param pep
	 *            A policy enforcement point, must not be <code>null</code>.
	 * 
	 * @throws SecurityException
	 *             If an error occurred setting the PEP.
	 */
	void setPolicyEnforcementPoint(PolicyEnforcementPoint pep)
			throws SecurityException;
}
