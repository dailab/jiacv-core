/*
 * $Id$ 
 */
package de.dailab.jiac.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.command.DiscoveryEvent;
import org.apache.activemq.network.Bridge;
import org.apache.activemq.network.ConduitBridge;
import org.apache.activemq.network.DemandForwardingBridge;
import org.apache.activemq.network.DiscoveryNetworkConnector;
import org.apache.activemq.network.DurableConduitBridge;
import org.apache.activemq.network.NetworkConnector;
import org.apache.activemq.transport.Transport;
import org.apache.activemq.transport.TransportFactory;
import org.apache.activemq.transport.discovery.DiscoveryAgent;
import org.apache.activemq.transport.discovery.DiscoveryAgentFactory;
import org.apache.activemq.transport.discovery.DiscoveryListener;
import org.apache.activemq.util.ServiceStopper;
import org.apache.activemq.util.ServiceSupport;

import de.dailab.jiac.net.discovery.SourceAwareDiscoveryEvent;

/**
 * This class is mainly a clone of {@link DiscoveryNetworkConnector}!
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class SourceAwareDiscoveryNetworkConnector extends NetworkConnector implements DiscoveryListener {
    protected DiscoveryAgent discoveryAgent;
    private ConcurrentHashMap<URI,Bridge> _bridges = new ConcurrentHashMap<URI,Bridge>();
    
    public SourceAwareDiscoveryNetworkConnector(URI discoveryURI) throws IOException {
        setUri(discoveryURI);
    }

    public void setUri(URI discoveryURI) throws IOException {
        setDiscoveryAgent(DiscoveryAgentFactory.createDiscoveryAgent(discoveryURI));
    }

    public void onServiceAdd(DiscoveryEvent event) {
        // Ignore events once we start stopping.
        if( isStopped() || isStopping() )
            return;
        
        String url = event.getServiceName();
        if (url != null) {

            URI uri;
            try {
                uri = new URI(url);
            }
            catch (URISyntaxException e) {
                log.warn("Could not connect to remote URI: " + url + " due to bad URI syntax: " + e, e);
                return;
            }

            // Should we try to connect to that URI?
            if (_bridges.containsKey(uri) || localURI.equals(uri) || (connectionFilter!=null && !connectionFilter.connectTo(uri))) {
                return;
            }
            
            URI connectUri= uri;
            final InetAddress source= (event instanceof SourceAwareDiscoveryEvent) ? ((SourceAwareDiscoveryEvent)event).getSource() : null;
            if(source != null) {
                boolean useSource= false;
                try {
                    InetAddress[] addresses= InetAddress.getAllByName(connectUri.getHost());
                    useSource= addresses == null || addresses.length <= 0;
                } catch (UnknownHostException uhe) {
                    useSource= true;
                }
                
                if(useSource) {
                    log.info("Could not resolve remote host: " + connectUri);
                    try {
                        connectUri= new URI(
                            uri.getScheme(),
                            uri.getUserInfo(),
                            source.getHostAddress(),
                            uri.getPort(),
                            uri.getPath(),
                            uri.getQuery(),
                            uri.getFragment()
                        );
                    } catch (URISyntaxException e) {
                        log.warn("Could not create remote URI from source: " + e.getMessage());
                        return;
                    }
                }
            } 

            if (failover) {
                try {
                    connectUri = new URI("failover:(" + connectUri+")?maxReconnectDelay=1000");
                } catch (URISyntaxException e) {
                    log.warn("Could not create failover URI: " + connectUri);
                    return;
                }
            }

            log.info("Establishing network connection between from " + localURI + " to " + connectUri);

            Transport remoteTransport;
            try {
                remoteTransport = TransportFactory.connect(connectUri);
            }
            catch (Exception e) {
                log.warn("Could not connect to remote URI: " + connectUri + ": " + e.getMessage());
                
                log.debug("Connection failure exception: "+ e, e);
                return;
            }

            Transport localTransport;
            try {
                localTransport = createLocalTransport();
            }
            catch (Exception e) {
                ServiceSupport.dispose(remoteTransport);
                log.warn("Could not connect to local URI: " + localURI + ": " + e.getMessage());
                log.debug("Connection failure exception: "+ e, e);
                return;
            }

            Bridge bridge = createBridge(localTransport, remoteTransport, event);
            _bridges.put(uri, bridge);
            try {
                bridge.start();
            }
            catch (Exception e) {
                ServiceSupport.dispose(localTransport);
                ServiceSupport.dispose(remoteTransport);
                log.warn("Could not start network bridge between: " + localURI + " and: " + uri + "(" + connectUri + ")"+ " due to: " + e);
                log.debug("Start failure exception: "+ e, e);
                
                try {
                    discoveryAgent.serviceFailed(event);
                } catch (IOException ioe) {
                    
                }
                return;
            }
        }
    }

    public void onServiceRemove(DiscoveryEvent event) {
        String url = event.getServiceName();
        if (url != null) {
            URI uri;
            try {
                uri = new URI(url);
            }
            catch (URISyntaxException e) {
                log.warn("Could not connect to remote URI: " + url + " due to bad URI syntax: " + e, e);
                return;
            }

            Bridge bridge = _bridges.remove(uri);
            if (bridge == null)
                return;

            ServiceSupport.dispose(bridge);
        }
    }

    public DiscoveryAgent getDiscoveryAgent() {
        return discoveryAgent;
    }

    public void setDiscoveryAgent(DiscoveryAgent discoveryAgent) {
        this.discoveryAgent = discoveryAgent;
        if (discoveryAgent != null) {
            this.discoveryAgent.setDiscoveryListener(this);
            this.discoveryAgent.setBrokerName(getBrokerName());
        }
    }

    public boolean isFailover() {
        return failover;
    }

    public void setFailover(boolean reliable) {
        this.failover = reliable;
    }

    protected void doStart() throws Exception {
        if (discoveryAgent == null) {
            throw new IllegalStateException("You must configure the 'discoveryAgent' property");
        }
        this.discoveryAgent.start();
        super.doStart();
    }

    protected void doStop(ServiceStopper stopper) throws Exception {
        for (Iterator<Bridge> i = _bridges.values().iterator(); i.hasNext();) {
            Bridge bridge = i.next();
            try {
                bridge.stop();
            }
            catch (Exception e) {
                stopper.onException(this, e);
            }
        }
        try {
            this.discoveryAgent.stop();
        }
        catch (Exception e) {
            stopper.onException(this, e);
        }

        super.doStop(stopper);
    }

    protected Bridge createBridge(Transport localTransport, Transport remoteTransport, final DiscoveryEvent event) {
        DemandForwardingBridge result = null;
        if (conduitSubscriptions) {
            if (dynamicOnly) {
                result = new ConduitBridge(localTransport, remoteTransport) {
                    protected void serviceLocalException(Throwable error) {
                        try {
                            super.serviceLocalException(error);
                        } finally {
                            fireServiceFailed();
                        }
                    }
                    protected void serviceRemoteException(Throwable error) {
                        try {
                            super.serviceRemoteException(error);
                        } finally {
                            fireServiceFailed();
                        }
                    }
                    public void fireServiceFailed() {
                        if( !isStopped() ) {
                            try {
                                discoveryAgent.serviceFailed(event);
                            } catch (IOException e) {
                            }
                        }
                    }
                };
            }
            else {
                result = new DurableConduitBridge(localTransport, remoteTransport) {
                    protected void serviceLocalException(Throwable error) {
                        try {
                            super.serviceLocalException(error);
                        } finally {
                            fireServiceFailed();
                        }
                    }
                    protected void serviceRemoteException(Throwable error) {
                        try {
                            super.serviceRemoteException(error);
                        } finally {
                            fireServiceFailed();
                        }
                    }
                    public void fireServiceFailed() {
                        if( !isStopped() ) {
                            try {
                                discoveryAgent.serviceFailed(event);
                            } catch (IOException e) {
                            }
                        }
                    }
                };
            }
        }
        else {
            result = new DemandForwardingBridge(localTransport, remoteTransport) {              
                protected void serviceLocalException(Throwable error) {
                    try {
                        super.serviceLocalException(error);
                    } finally {
                        fireServiceFailed();
                    }
                }
                protected void serviceRemoteException(Throwable error) {
                    try {
                        super.serviceRemoteException(error);
                    } finally {
                        fireServiceFailed();
                    }
                }
                public void fireServiceFailed() {
                    if( !isStopped() ) {
                        try {
                            discoveryAgent.serviceFailed(event);
                        } catch (IOException e) {
                        }
                    }
                }
            };
        }
        return configureBridge(result);
    }

    protected String createName() {
        return discoveryAgent.toString();
    }
}
