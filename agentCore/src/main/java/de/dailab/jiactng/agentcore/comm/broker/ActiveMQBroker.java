package de.dailab.jiactng.agentcore.comm.broker;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * 
 * @author Martin Loeffelholz
 *
 */
public class ActiveMQBroker extends AbstractLifecycle{
	/** The logger we use, if it is not set by DI, we create our own */
    protected Log _log = LogFactory.getLog(getClass());

    public static final String PROTOCOL_IP_SEPARATOR = "://";
	public static final char IP_PORT_SEPARATOR = ':';
    

    /** The embedded broker we use if no other broker is running on our host machine */
    protected BrokerService broker = new BrokerService();

	/** Agent node of this message broker */
	protected IAgentNode _agentNode = null;

	/** The manager of the message broker */
	protected Manager _manager = null;
	

	private String _name = "ActiveMQBroker";
	private Set<String> _urlList = new HashSet<String>();
	private String _discoveryMethod = "multicast";
	private String _discoveryAddress = "239.255.2.45:5555";
	private boolean _persistent = false;
	private boolean _jmx = true;
	
	public ActiveMQBroker(){
		_urlList.add("tcp://localhost:61616");
	}
	
	
	
	// Lifecyclemethods:
	public void doInit() throws Exception {
        _log.debug("initializing embedded broker");
    	broker = new BrokerService();
    	broker.setBrokerName(_name);
		broker.setUseJmx(_jmx);
		broker.setPersistent(_persistent);
		
   
    	ManagementContext context = new ManagementContext();
		context.setJmxDomainName("de.dailab.jiactng");
		context.setCreateConnector(false);
		broker.setManagementContext(context);
		
        try {
            for (String url : _urlList){
            	_log.debug("embedded broker initializing url = " + url);
            	TransportConnector connector = broker.addConnector(url);
            	if(_discoveryMethod != null && _discoveryAddress != null) {
                    connector.setDiscoveryUri(new URI(_discoveryMethod + "://" + _discoveryAddress));
                    connector.getDiscoveryAgent().setBrokerName(_name);
                }
            }
            
        } catch (Exception e) {
            _log.error(e.toString());
        }
        _log.debug("embedded broker initialized: Persistent=" + _persistent + ", UseJMX=" + _jmx);
        
        broker.start();
		_log.debug("started broker");
	}
	
	public void doStart() throws Exception {
		
	}
	
	public void doStop() throws Exception {
		
	}
	
	public void doCleanup() throws Exception {
		broker.stop();
		_log.debug("stopping broker");
	}
	
	public void setLog(Log log){
		_log = log;
	}
	
	// just for association of the broker with a node so far. No functionality
	public void setAgentNode(IAgentNode agentNode) {
		_agentNode = agentNode;
	}

	public String getAgentNodeName() {
		return _agentNode.getName();
	}
	
	// SPRING CONFIGURATORS
	public void setBrokerName(String name){
		_name = name;
	}
	
	public void setJmx(boolean jmx){
		_jmx = jmx;
	}
	
	public void setPersistent(boolean persistent){
		_persistent = persistent;
	}
	
	public void setUrlList(Set<String> urlList){
		_urlList = urlList;
	}
	
	public void setDiscoveryMethod(String discoveryMethod){
		_discoveryMethod = discoveryMethod;
	}
	
	public void setDiscoveryAddress(String discoveryAddress){
		_discoveryAddress = discoveryAddress;
	}
	
}
