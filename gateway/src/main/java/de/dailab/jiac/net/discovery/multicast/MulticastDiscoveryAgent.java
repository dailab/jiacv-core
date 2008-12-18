/*
 * $Id$ 
 */
package de.dailab.jiac.net.discovery.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.activemq.command.DiscoveryEvent;
import org.apache.activemq.transport.discovery.DiscoveryAgent;
import org.apache.activemq.transport.discovery.DiscoveryListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is mainly a clone of
 * {@link org.apache.activemq.transport.discovery.multicast.MulticastDiscoveryAgent}!
 * 
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class MulticastDiscoveryAgent implements DiscoveryAgent,Runnable{
    private static final Log log=LogFactory.getLog(MulticastDiscoveryAgent.class);
    public static final String DEFAULT_DISCOVERY_URI_STRING="multicast://239.255.2.3:6155";
    private static final String TYPE_SUFFIX="JIAC_NETGW-0.";
    private static final String ALIVE="alive.";
    private static final String DEAD="dead.";
    private static final String DELIMITER = "%";
    private static final int BUFF_SIZE=8192;
    private static final int DEFAULT_IDLE_TIME=500;
    private static final int HEARTBEAT_MISS_BEFORE_DEATH=4;
    
    protected DiscoveryListener discoveryListener;
    
    private int _timeToLive=1;
    private boolean _loopBackMode=false;
    private Map<String, AtomicLong> _services= new ConcurrentHashMap<String, AtomicLong>();
    private Map<String, String> _brokers= new ConcurrentHashMap<String, String>();
    private String _group="default";
    private String _brokerName;
    private URI _discoveryURI;
    private InetAddress _inetAddress;
    private SocketAddress _sockAddress;
    private String _selfService;
    
    private MulticastSocket _mcast;
    private Thread _runner;
    private long _keepAliveInterval= DEFAULT_IDLE_TIME;
    private long _lastAdvertizeTime= 0;
    private AtomicBoolean _started= new AtomicBoolean(false);
    private boolean _reportAdvertizeFailed= true;
    
    private final Executor executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
        public Thread newThread(Runnable runable) {
            Thread t= new Thread(runable, "Multicast Discovery Agent Notifier");
            t.setDaemon(true);
            return t;
        }            
    });

    /**
     * Set the discovery listener
     * 
     * @param listener
     */
    public void setDiscoveryListener(DiscoveryListener listener) {
        this.discoveryListener=listener;
    }

    /**
     * register a service
     */
    public void registerService(String name) {//throws IOException {
        _selfService= name;
        if (_started.get()){
            doAdvertizeSelf();
        }
    }

    /**
     * Get the group used for discovery
     * 
     * @return the group
     */
    public String getGroup(){
        return _group;
    }

    /**
     * Set the group for discovery
     * 
     * @param group
     */
    public void setGroup(String group){
        _group=group;
    }

    /**
     * @return Returns the brokerName.
     */
    public String getBrokerName(){
        return _brokerName;
    }

    /**
     * @param brokerName The brokerName to set.
     */
    public void setBrokerName(String brokerName){
        if (brokerName != null){
            brokerName = brokerName.replace('.','-');
            brokerName = brokerName.replace(':','-');
            brokerName = brokerName.replace('%','-');
            _brokerName=brokerName;
        }
    }

    /**
     * @return Returns the loopBackMode.
     */
    public boolean isLoopBackMode(){
        return _loopBackMode;
    }

    /**
     * @param loopBackMode
     *            The loopBackMode to set.
     */
    public void setLoopBackMode(boolean loopBackMode){
        _loopBackMode= loopBackMode;
    }

    /**
     * @return Returns the timeToLive.
     */
    public int getTimeToLive(){
        return _timeToLive;
    }

    /**
     * @param timeToLive
     *            The timeToLive to set.
     */
    public void setTimeToLive(int timeToLive){
        _timeToLive= timeToLive;
    }

    /**
     * @return the discoveryURI
     */
    public URI getDiscoveryURI(){
        return _discoveryURI;
    }

    /**
     * Set the discoveryURI
     * 
     * @param discoveryURI
     */
    public void setDiscoveryURI(URI discoveryURI){
        _discoveryURI= discoveryURI;
    }

    public long getKeepAliveInterval(){
        return _keepAliveInterval;
    }

    public void setKeepAliveInterval(long keepAliveInterval){
        this._keepAliveInterval=keepAliveInterval;
    }

    /**
     * start the discovery agent
     * 
     * @throws Exception
     */
    public void start() throws Exception{
        if(_started.compareAndSet(false,true)){
            if(_group == null || _group.length() == 0){
                throw new IOException("You must specify a group to discover");
            }
            if (_brokerName == null || _brokerName.length() == 0){
                log.warn("brokerName not set");
            }
            String type= getType();
            if(!type.endsWith(".")){
                log.warn("The type '" + type + "' should end with '.' to be a valid Discovery type");
                type+=".";
            }
            if(_discoveryURI == null){
                _discoveryURI= new URI(DEFAULT_DISCOVERY_URI_STRING);
            }
            
            _inetAddress=InetAddress.getByName(_discoveryURI.getHost());
            _sockAddress=new InetSocketAddress(_inetAddress, _discoveryURI.getPort());
            _mcast=new MulticastSocket(_discoveryURI.getPort());
            _mcast.setLoopbackMode(_loopBackMode);
            _mcast.setTimeToLive(getTimeToLive());
            _mcast.joinGroup(_inetAddress);
            _mcast.setSoTimeout((int) _keepAliveInterval);
            _runner=new Thread(this);
            _runner.setName("MulticastDiscovery: " + _selfService);
            _runner.setDaemon(true);
            _runner.start();
            doAdvertizeSelf();
        }
    }

    /**
     * stop the channel
     * 
     * @throws Exception
     */
    public void stop() throws Exception{
        if(_started.compareAndSet(true,false)){
            doAdvertizeSelf();
            _mcast.close();
        }
    }

    public String getType(){
        return _group+"."+TYPE_SUFFIX;
    }

    public void run(){
        byte[] buf= new byte[BUFF_SIZE];
        DatagramPacket packet= new DatagramPacket(buf, 0, buf.length);
        while(_started.get()){
            doTimeKeepingServices();
            try{
                _mcast.receive(packet);
                
                InetAddress source= packet.getAddress();
                
                if(packet.getLength() > 0){
                    String str= new String(packet.getData(), packet.getOffset(), packet.getLength());
                    processData(str, source);
                }
            } catch(SocketTimeoutException se){
                // ignore
            } catch(IOException e){
                if(_started.get()) {
                    log.error("failed to process packet: "+e);
                }
            }
        }
    }

    private void processData(String str, InetAddress source){
        if (discoveryListener != null){
            if(str.startsWith(getType())){
                String payload= str.substring(getType().length());
                if(payload.startsWith(ALIVE)){
                    String brokerName= getBrokerName(payload.substring(ALIVE.length()));
                    String service= payload.substring(ALIVE.length() + brokerName.length() + 2);
                    
                    // use the address of the datagram instead
                    try {
                        URI serviceURI= new URI(service);
                        service= new URI(serviceURI.getScheme(), serviceURI.getUserInfo(), source.getHostAddress(), serviceURI.getPort(), serviceURI.getPath(), serviceURI.getQuery(), serviceURI.getFragment()).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    if(!brokerName.equals(_brokerName)){
                        processAlive(brokerName,service);
                    }
                } else{
                    String brokerName= getBrokerName(payload.substring(DEAD.length()));
                    String service= payload.substring(DEAD.length() + brokerName.length()+2);
                    
                    // use the address of the datagram instead
                    try {
                        URI serviceURI= new URI(service);
                        service= new URI(serviceURI.getScheme(), serviceURI.getUserInfo(), source.getHostAddress(), serviceURI.getPort(), serviceURI.getPath(), serviceURI.getQuery(), serviceURI.getFragment()).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    if(!brokerName.equals(_brokerName)){
                        processDead(brokerName,service);
                    }
                }
            }
        }
    }

    private void doTimeKeepingServices(){
        if(_started.get()){
            long currentTime= System.currentTimeMillis();
            if (currentTime < _lastAdvertizeTime || ((currentTime-_keepAliveInterval) > _lastAdvertizeTime)) {
                doAdvertizeSelf();
                _lastAdvertizeTime= currentTime;
            }
            doExpireOldServices();
        }
    }

    private void doAdvertizeSelf(){
        if(_selfService!=null ){
            
            String payload= getType();
            payload+= _started.get() ? ALIVE:DEAD;
            payload+= DELIMITER + _brokerName + DELIMITER;
            payload+= _selfService;
            try{
                byte[] data= payload.getBytes();
                DatagramPacket packet= new DatagramPacket(data, 0, data.length, _sockAddress);
                _mcast.send(packet);
            } catch(IOException e) {
                // If a send fails, chances are all subsequent sends will fail too.. No need to keep reporting the
                // same error over and over.
                if( _reportAdvertizeFailed ) {
                    _reportAdvertizeFailed= false;
                    log.error("Failed to advertise our service: " + payload,e);
                    if( "Operation not permitted".equals(e.getMessage()) ) {
                        log.error("The 'Operation not permitted' error has been know to be caused by improper firewall/network setup.  Please make sure that the OS is properly configured to allow multicast traffic over: "+_mcast.getLocalAddress());
                    }
                }
            }
        }
    }

    private void processAlive(String brokerName,String service){
        if(_selfService == null || !service.equals(_selfService)){
            AtomicLong lastKeepAlive= _services.get(service);
            if(lastKeepAlive == null){
                _brokers.put(service, brokerName);
                if(discoveryListener != null){
                    final DiscoveryEvent event= new DiscoveryEvent(service);
                    event.setBrokerName(brokerName);
                    
                    // Have the listener process the event async so that 
                    // he does not block this thread since we are doing time sensitive
                    // processing of events.
                    executor.execute(new Runnable() {
                        public void run() {
                            DiscoveryListener dl= MulticastDiscoveryAgent.this.discoveryListener;
                            if(dl != null){
                                dl.onServiceAdd(event);
                            }
                        }
                    });
                }
                lastKeepAlive= new AtomicLong(System.currentTimeMillis());
                _services.put(service,lastKeepAlive);
                doAdvertizeSelf();
                
            }
            lastKeepAlive.set(System.currentTimeMillis());
        }
    }

    private void processDead(String brokerName,String service){
        if(!service.equals(_selfService)){
            if(_services.remove(service) != null){
                _brokers.remove(service);
                if(discoveryListener != null){
                    final DiscoveryEvent event= new DiscoveryEvent(service);
                    event.setBrokerName(brokerName);
                    
                    // Have the listener process the event async so that 
                    // he does not block this thread since we are doing time sensitive
                    // processing of events.
                    executor.execute(new Runnable() {
                        public void run() {
                            DiscoveryListener dl= MulticastDiscoveryAgent.this.discoveryListener;
                            if(dl != null){
                                dl.onServiceRemove(event);
                            }
                        }
                    });
                }
            }
        }
    }

    private void doExpireOldServices(){
        long expireTime= System.currentTimeMillis() - (_keepAliveInterval * HEARTBEAT_MISS_BEFORE_DEATH);
        for(Iterator<Entry<String, AtomicLong>> i= _services.entrySet().iterator(); i.hasNext();){
            Map.Entry<String, AtomicLong> entry= i.next();
            AtomicLong lastHeartBeat= entry.getValue();
            if(lastHeartBeat.get() < expireTime){
                String brokerName= _brokers.get(entry.getKey());
                processDead(brokerName,entry.getKey().toString());
            }
        }
    }
    
    private String getBrokerName(String str){
        String result= null;
        int start= str.indexOf(DELIMITER);
        if (start >= 0 ){
            int end= str.indexOf(DELIMITER,start+1);
            result= str.substring(start+1, end);
        }
        return result;
    }

    public void serviceFailed(DiscoveryEvent event) {//throws IOException {
        processDead(event.getBrokerName(), event.getServiceName());
    }
}
