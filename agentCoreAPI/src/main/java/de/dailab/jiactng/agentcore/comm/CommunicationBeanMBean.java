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
	void removeTransport(String transportIdentifier);

	/**
	 * Gets information about the selectors of all listeners for each address.
	 * @return composite data where the items are the addresses and the values are the
	 * lists of the selectors of the corresponding listeners.
	 */
	CompositeData getSelectorsOfAddresses();

    /**
     * Checks, if messages of type <code>BytesMessage</code> or <code>ObjectMessage</code> will be send. 
     * @return <code>true</code> if bytes messages will be send, which can be handled by the broker 
     * 	without knowing the classes of the message content
     */
    boolean isSerialization();

    /**
     * Sets, if messages of type <code>BytesMessage</code> or <code>ObjectMessage</code> will be send.
     * @param serialization <code>true</code> if bytes messages will be send, which can be handled 
     * 	by the broker without knowing the classes of the message content
     */
    void setSerialization(boolean serialization);
}
