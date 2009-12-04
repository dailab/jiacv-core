/**
 * 
 */
package de.dailab.jiactng.agentcore.security;

/**
 * Responsible for requesting security policy decision and enforcing the
 * decision.
 * 
 * Implementations of this interface MUST contact a policy decision point (PDP)
 * before they take any action on security related requests. A policy
 * enforcement point (PEP) only issues requests and enforces decision, but does
 * not decides anything by itself.
 * 
 * Interaction between PEP and PDP depend on the specific implementations.
 * 
 * Each agent bean should contain a PEP member element which should defer the
 * communication with PDP and global enforcement to PEP agent bean. Only bean
 * specific enforcement should be performed by bean PEP element.
 * 
 * @author bsufka
 * 
 * @see PolicyDecisionResult
 */
public interface PolicyEnforcementPoint {

	/**
	 * Issues a a policy decision request to a PDP.
	 * 
	 * Implementations of this method select the actual transport mechanisms for
	 * issuing the request.
	 * 
	 * @param request
	 *            A policy decision request, must not be <code>null</code>.
	 * 
	 * @return <code>true</code>, if the request issued. <code>false</code>
	 *         if request could not be transmitted to PDP. In this case the PEP
	 *         SHOULD handle this as a negative decision result.
	 * 
	 * @throws SecurityException
	 *             If a security violation occurred, during the process of
	 *             issuing the request.
	 */
	boolean issuePolicyRequest(final PolicyDecisionRequest request)
			throws SecurityException;

	/**
	 * Enforces a policy decision request.
	 * 
	 * @param result
	 *            A policy decision result for a previously issued decision
	 *            request. Must not be <code>null</code>.
	 * 
	 * @throws SecurityException
	 *             If an error occurred during the decision enforcement.
	 */
	void enfrocePolicyDecision(final PolicyDecisionResult result)
			throws SecurityException;
}
