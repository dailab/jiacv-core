/*
 * $Id$ 
 */
package de.dailab.jiac.net.discovery.multicast;

import java.io.IOException;

import org.apache.activemq.command.DiscoveryEvent;
import org.apache.activemq.transport.discovery.DiscoveryAgent;
import org.apache.activemq.transport.discovery.DiscoveryListener;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class MulticastDiscoveryAgent implements DiscoveryAgent {

    /* (non-Javadoc)
     * @see org.apache.activemq.transport.discovery.DiscoveryAgent#getGroup()
     */
    public String getGroup() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.activemq.transport.discovery.DiscoveryAgent#registerService(java.lang.String)
     */
    public void registerService(String name) throws IOException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.activemq.transport.discovery.DiscoveryAgent#serviceFailed(org.apache.activemq.command.DiscoveryEvent)
     */
    public void serviceFailed(DiscoveryEvent event) throws IOException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.activemq.transport.discovery.DiscoveryAgent#setBrokerName(java.lang.String)
     */
    public void setBrokerName(String brokerName) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.activemq.transport.discovery.DiscoveryAgent#setDiscoveryListener(org.apache.activemq.transport.discovery.DiscoveryListener)
     */
    public void setDiscoveryListener(DiscoveryListener listener) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.activemq.transport.discovery.DiscoveryAgent#setGroup(java.lang.String)
     */
    public void setGroup(String group) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.activemq.Service#start()
     */
    public void start() throws Exception {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.activemq.Service#stop()
     */
    public void stop() throws Exception {
        // TODO Auto-generated method stub

    }

}
