/*
 * $Id$ 
 */
package de.dailab.jiac.net.discovery.multicast;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import org.apache.activemq.transport.discovery.DiscoveryAgent;
import org.apache.activemq.transport.discovery.DiscoveryAgentFactory;
import org.apache.activemq.util.IOExceptionSupport;
import org.apache.activemq.util.IntrospectionSupport;
import org.apache.activemq.util.URISupport;

/**
 * This class is a clone of
 * {@link org.apache.activemq.transport.discovery.multicast.MulticastDiscoveryAgentFactory}!
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class MulticastDiscoveryAgentFactory extends DiscoveryAgentFactory {

    @SuppressWarnings("unchecked")
    protected DiscoveryAgent doCreateDiscoveryAgent(URI uri) throws IOException {
        try {
            Map options = URISupport.parseParamters(uri);
            MulticastDiscoveryAgent rc = new MulticastDiscoveryAgent();
            rc.setGroup(uri.getHost());
            IntrospectionSupport.setProperties(rc, options);
            return rc;
            
        } catch (Throwable e) {
            throw IOExceptionSupport.create("Could not create discovery agent: " + uri, e);
        }
    }
}
