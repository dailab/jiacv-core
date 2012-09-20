/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.management.jmx;

import javax.management.remote.JMXAuthenticator;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class JmxConnector {
    private final String _protocol;
    private String _path;
    private int _port;
    private JMXAuthenticator _authenticator;
    private String _interface;
    
    protected JmxConnector(String protocol) {
        _protocol= protocol;
    }
    
    public final String getPath() {
        return _path;
    }
    public final void setPath(String path) {
        _path= path;
    }
    public final String getProtocol() {
        return _protocol;
    }
    public final int getPort() {
        return _port;
    }
    public final void setPort(int port) {
        _port= port;
    }
    public final JMXAuthenticator getAuthenticator() {
        return _authenticator;
    }
    public final void setAuthenticator(JMXAuthenticator authenticator) {
        _authenticator= authenticator;
    }
    public final String getInterface() {
        return _interface;
    }
    public final void setInterface(String ifc) {
        _interface= ifc;
    }
}
