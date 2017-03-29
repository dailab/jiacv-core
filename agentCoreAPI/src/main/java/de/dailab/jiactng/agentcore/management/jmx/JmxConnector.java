/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.management.jmx;

import javax.management.remote.JMXAuthenticator;

/**
 * This abstract class represents the configuration of JMX connector servers 
 * for different kinds of protocols.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class JmxConnector {
    private final String _protocol;
    private String _path;
    private int _port;
    private JMXAuthenticator _authenticator;
    private String _interface;
    private String _privateKeyFile;

    /**
     * Constructor.
     * @param protocol the protocol, which will be used for JMX connections
     */
    protected JmxConnector(String protocol) {
        _protocol= protocol;
    }

    /**
     * Checks whether JMX connectors based on this configuration will use
     * a RMI registry.
     * @return usage of a RMI registry
     */
    public abstract boolean useRmiRegistry();

    /**
     * Gets the path of the JMX URL used by the connector servers.
     * @return the JMX URL path to be used
     */
    public final String getPath() {
        return _path;
    }

    /**
     * Sets the path of the JMX URL used by the connector servers.
     * @param path the JMX URL path to be used
     */
    public final void setPath(String path) {
        _path= path;
    }

    /**
     * Gets the protocol of the JMX URL used by the connector servers.
     * @return the JMX URL protocol to be used
     */
    public final String getProtocol() {
        return _protocol;
    }

    /**
     * Gets the port of the JMX URL used by the connector servers.
     * @return the JMX URL protocol to be used
     */
    public final int getPort() {
        return _port;
    }

    /**
     * Sets the port of the JMX URL used by the connector servers.
     * @param port the JMX URL port to be used
     */
    public final void setPort(int port) {
        _port= port;
    }

    /**
     * Gets the authenticator used by the connector servers to accept or 
     * reject incoming connection requests.
     * @return the authenticator object
     */
    public final JMXAuthenticator getAuthenticator() {
        return _authenticator;
    }

    /**
     * Sets the authenticator used by the connector servers to accept or 
     * reject incoming connection requests.
     * @param authenticator the authenticator object
     */
    public final void setAuthenticator(JMXAuthenticator authenticator) {
        _authenticator= authenticator;
    }

    /**
     * Gets the network interface for which the JMX connector server will be
     * created. If this interface is not specified, a connector server for 
     * each active network interface will be provided.
     * @return the name of the network interface
     */
    public final String getInterface() {
        return _interface;
    }

    /**
     * Sets the network interface for which the JMX connector server will be
     * created. If this interface is not specified, a connector server for 
     * each active network interface will be provided.
     * @param ifc the name of the network interface
     */
    public final void setInterface(String ifc) {
        _interface= ifc;
    }

    /**
     * Gets the filename of the private key used by the connector servers to 
     * encrypt the JMX URL or parts of it. This enables clients to ensure that
     * the JMX URL belongs to the expected owner by using its public key.
     * @return the name of the private key file
     */
    public String getPrivateKeyFile() {
		return _privateKeyFile;
	}

    /**
     * Sets the filename of the private key used by the connector servers to 
     * encrypt the JMX URL or parts of it. This enables clients to ensure that
     * the JMX URL belongs to the expected owner by using its public key.
     * @param privateKeyFile the name of the private key file
     */
    public void setPrivateKeyFile(String privateKeyFile) {
		_privateKeyFile = privateKeyFile;
	}
}
