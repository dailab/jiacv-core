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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is a clone of
 * {@link org.apache.activemq.transport.discovery.multicast.MulticastDiscoveryAgentFactory}
 * !
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class SourceAwareMulticastDiscoveryAgentFactory extends DiscoveryAgentFactory {
    private static final Log LOG = LogFactory.getLog(SourceAwareMulticastDiscoveryAgentFactory.class);

    protected DiscoveryAgent doCreateDiscoveryAgent(URI uri) throws IOException {
        try {

            if (LOG.isTraceEnabled()) {
                LOG.trace("doCreateDiscoveryAgent: uri = " + uri.toString());
            }

            SourceAwareMulticastDiscoveryAgent samda = new SourceAwareMulticastDiscoveryAgent();

            samda.setDiscoveryURI(uri);

            // allow MDA's params to be set via query arguments
            // (e.g., multicast://default?group=foo
            Map<String, String> options = URISupport.parseParameters(uri);
            IntrospectionSupport.setProperties(samda, options);

            return samda;
        } catch (Throwable e) {
            throw IOExceptionSupport.create("Could not create discovery agent: " + uri, e);
        }
    }
}