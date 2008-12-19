/*
 * $Id$ 
 */
package de.dailab.jiac.net.discovery;

import java.net.InetAddress;

import org.apache.activemq.command.DiscoveryEvent;

/**
 * Extends the discovery event with information about the source.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class SourceAwareDiscoveryEvent extends DiscoveryEvent {
    private final InetAddress _source;
    
    public SourceAwareDiscoveryEvent(InetAddress source) {
        super();
        _source= source;
    }

    public SourceAwareDiscoveryEvent(String serviceName, InetAddress source) {
        super(serviceName);
        _source= source;
    }
    
    public InetAddress getSource() {
        return _source;
    }
}
