package de.dailab.jiactng.agentcore.comm.broker;

import de.dailab.jiactng.agentcore.AbstractAgentNodeBeanMBean;

/**
 * Management interface of the message broker.
 * @author Jan Keiser
 */
public interface ActiveMQBrokerMBean extends AbstractAgentNodeBeanMBean {

	/**
	 * Get TTL of the network connectors.
	 * @return the networkTTL.
	 */
	int getNetworkTTL();

	/**
	 * Set TTL of the network connectors. 
	 * @param networkTTL the networkTTL to set.
	 * @throws Exception if re-adding of a network connector fails.
	 */
	void setNetworkTTL(int networkTTL) throws Exception;

	/**
     * Indicates whether messages are stored in a data base or not.
	 * @return <code>true</code> if messages are stored and <code>false</code> otherwise.
	 */
	boolean getPersistent();

}
