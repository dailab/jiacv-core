package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBeanMBean;

/**
 * JMX-compliant management interface of communication beans.
 * @author Jan Keiser
 */
public interface CommunicationBeanMBean extends AbstractMethodExposingBeanMBean {

	/**
	 * Removes and cleans up a transport hold by this CommunicationBean.
	 * @param transportIdentifier of the transport to remove
	 */
	public void removeTransport(String transportIdentifier);
}
