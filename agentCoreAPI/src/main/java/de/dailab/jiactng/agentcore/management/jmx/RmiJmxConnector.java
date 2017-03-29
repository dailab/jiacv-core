/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.management.jmx;

/**
 * This class represents the configuration of JMX connector servers, which are
 * based on the RMI protocol. If the port but not the host of a RMI registry
 * is specified, the address of localhost will be used. If the host but not 
 * the port of a RMI registry is specified, the default port will be used.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class RmiJmxConnector extends JmxConnector {
    private String _registryHost;
    private int _registryPort;

    /**
     * Constructor.
     */
    public RmiJmxConnector() {
        super("rmi");
    }

    /**
     * Checks whether JMX connectors based on this configuration will use
     * a RMI registry.
     * @return <code>true</code> if the port or host of a registry is specified
     */
    public final boolean useRmiRegistry() {
        return (_registryPort > 0) || (_registryHost != null);
    }

    /**
     * Returns the host of the RMI registry to be used by the JMX connector
     * servers.
     * @return the registry host or <code>null</code> if not specified
     */
    public final String getRegistryHost() {
        return _registryHost;
    }

    /**
     * Sets the host of the RMI registry to be used by the JMX connector
     * servers.
     * @param registryHost the registry host
     */
    public final void setRegistryHost(String registryHost) {
        _registryHost= registryHost;
    }

    /**
     * Returns the port of the RMI registry to be used by the JMX connector
     * servers.
     * @return the registry port or <code>0</code> if not specified
     */
    public final int getRegistryPort() {
        return _registryPort;
    }

    /**
     * Sets the port of the RMI registry to be used by the JMX connector
     * servers.
     * @param registryPort the registry port
     */
    public final void setRegistryPort(int registryPort) {
        _registryPort= registryPort;
    }
}
