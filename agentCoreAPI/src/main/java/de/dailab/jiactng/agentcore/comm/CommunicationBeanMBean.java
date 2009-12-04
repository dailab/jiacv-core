package de.dailab.jiactng.agentcore.comm;

import javax.management.openmbean.CompositeData;

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

	/**
	 * Gets information about the selectors of all listeners for each address.
	 * @return composite data where the items are the addresses and the values are the
	 * lists of the selectors of the corresponding listeners.
	 */
	public CompositeData getSelectorsOfAddresses();
}
