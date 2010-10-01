/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.management.jmx;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class RmiJmxConnector extends JmxConnector {
    private String _registryHost;
    private int _registryPort;
    
    public RmiJmxConnector() {
        super("rmi");
    }
    
    public final String getRegistryHost() {
        return _registryHost;
    }
    public final void setRegistryHost(String registryHost) {
        _registryHost= registryHost;
    }
    public final int getRegistryPort() {
        return _registryPort;
    }
    public final void setRegistryPort(int registryPort) {
        _registryPort= registryPort;
    }
}
