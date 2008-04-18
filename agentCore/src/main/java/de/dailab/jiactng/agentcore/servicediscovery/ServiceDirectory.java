package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.CommunicationException;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport;
import de.dailab.jiactng.agentcore.comm.transport.MessageTransport.IMessageTransportDelegate;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.management.Manageable;
import de.dailab.jiactng.agentcore.management.Manager;

/**
 * ServiceDirectoryBean soll Beschreibungen aller Services die auf einem AgentNode existieren, verfuegbar halten. Dazu
 * wird die CommBean benutzt. Es soll der memory benutzt werden.
 * 
 * @author janko
 * 
 * FIXME: MessageTransport muss konfiguriert werden
 *        
 */
public class ServiceDirectory extends AbstractLifecycle implements IServiceDirectory, Runnable, Manageable, ServiceDirectoryMBean {
	private static final long serialVersionUID = 1L;

	private class ServiceDirectoryMessageDelegate implements IMessageTransportDelegate {
        public void onAsynchronousException(MessageTransport source, Exception e) {
            log.error("asynchronous error on message transport", e);
        }

        public void onMessage(MessageTransport source, IJiacMessage message, ICommunicationAddress from) {
            // TODO Auto-generated method stub
        }

        public Log getLog(String extension) {
            return LogFactory.getLog(getClass().getName() + "." + extension);
        }
    }
    
	Log log = LogFactory.getLog(getClass());
	
	// um auf die topic zu schreiben
	MessageTransport _messageTransport;
	
	// TO DO: HIER DIE GEWUENSCHTE SERVICETOPIC ERSTELLEN
	IGroupAddress _serviceTopic = CommunicationAddressFactory.createGroupAddress("ServiceTopic");
	
	// zum speichern der Servicebeschreibungen
	ServiceDirectoryMemory memory;
	
	/* Agent node of this service directory */
	IAgentNode _agentNode;

	/* Time in milliseconds between publishment of services */
	int _publishTimer = 10000;

	/** The manager of the service directory */
	private Manager _manager = null;

	public ServiceDirectory() {
		super();
		memory = new ServiceDirectoryMemory();
	}

	public void doInit() throws Exception {
		if (_messageTransport != null) {
            _messageTransport.setDefaultDelegate(new ServiceDirectoryMessageDelegate());
//			_messageTransport.setAgentNodeName(_agentNodeName);
			_messageTransport.doInit();
		}
	}

	public void doStart() throws Exception {}

	public void doStop() throws Exception {}

	public void doCleanup() throws Exception {
		if (_messageTransport != null) {
			_messageTransport.doCleanup();
		}
		memory.doCleanup();
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(_publishTimer);
				execute();
			} catch (Exception e) {
				
			}
		}
	}

	/**
	 * Methode die den Namen liefert unter der der Thread des ServiceDirectory im ThreadPool gespeichert wird
	 * 
	 * @param nodeName
	 * @return ein String als Schluessel fuer den ServiceDirectory-Thread
	 */
	public String getFutureName(String nodeName) {
		return "ServiceDirectory:" + nodeName;
	}

	/**
	 * periodisch in die topic schreiben
	 */
	private void execute() {
		log.debug("do Publish");
		doPublishServices();
	}

	/**
	 * Macht die eigentlich arbeit
	 *
	 */
	private void doPublishServices() {
		List<IServiceDescription> serviceList = getAllServices();
		for (Iterator<IServiceDescription> iter = serviceList.iterator(); iter.hasNext();) {
			IServiceDescription serviceDesc = iter.next();
			// nur services publishen, deren verfallsdatum in zukunft liegt.
			Date actualDate = new Date();
			if (serviceDesc.getExpireDate().after(actualDate)) {
				long timeDiff = serviceDesc.getExpireDate().getTime()-actualDate.getTime();
				log.debug("service:"+serviceDesc.getName()+" has "+timeDiff+" ms to live.");
                try {
                    _messageTransport.send(createJiacMessage(serviceDesc), _serviceTopic);
                } catch (CommunicationException ce) {
                    log.error("error while publishing service description", ce);
                }
			}
		}
	}

	/**
	 * bastelt JiacNachricht zusammen. Sie soll an eine ServicTopic gehen.
	 * 
	 * @param serviceDesc die zu verschickende Servicebeschreibung
	 * @return die Nachricht die per JMS verschickt wird.
	 */
	private JiacMessage createJiacMessage(IServiceDescription serviceDesc) {
		IFact content = new ObjectContent(serviceDesc);
		
		JiacMessage msg = new JiacMessage(content);
        msg.setSender(_serviceTopic);
		return msg;
	}

	/**
	 * Registriert n service
	 * 
	 * @param serviceDescription
	 * @return true
	 */
	public boolean registerService(IServiceDescription serviceDescription) {
		memory.write(serviceDescription);
		return true;
	}

	/**
	 * entfernt ein service aus dem directory
	 * 
	 * @param serviceDescription
	 * @return true
	 */
	public boolean deRegisterService(IServiceDescription serviceDescription) {
		memory.remove(serviceDescription);
		return true;
	}

	/**
	 * Sucht einen Service im Directory anhand des ServiceNamens
	 * 
	 * @param name
	 * @return description of all registered services with the given name
	 */
	public Set<ServiceDescription> findServiceByName(String name) {
		Set<ServiceDescription> serviceDescriptionList = memory.readAll(new ServiceDescription(null, null, name, null,
																						null, null, null, null, null, null));
		return serviceDescriptionList;
	}

	/**
	 * Liefert die Anzahl der registrierten Services zurueck
	 * 
	 * @return Liefert die Anzahl der registrierten Services zurueck
	 */
	public int getServiceNumber() {
		return memory.readAllOfType(IServiceDescription.class).size();
	}

//	/**
//	 * threadsafe ??
//	 * 
//	 * @return all webservices, stored in this servicedirectory
//	 */
//	public List<IServiceDescription> getAllWebServices() {
//		List<IServiceDescription> webServices = new ArrayList<IServiceDescription>();
//		Set<ServiceDescription> serviceDescriptionList = memory.readAll(new ServiceDescription(null, null, null, null,
//																						null, null, null, null, null, null));
//		for (Iterator iter = serviceDescriptionList.iterator(); iter.hasNext();) {
//			IServiceDescription serviceDesc = (IServiceDescription) iter.next();
//			if (serviceDesc.isWebService()) {
//				webServices.add(serviceDesc);
//			}
//		}
//		return webServices;
//	}
//
	/**
	 * threadsafe ??
	 * 
	 * @return all webservices, stored in this servicedirectory
	 */
	public List<IServiceDescription> getAllServices() {
		List<IServiceDescription> services = new ArrayList<IServiceDescription>();
		Set<ServiceDescription> serviceDescriptionList = memory.readAll(new ServiceDescription(null, null, null, null,
																						null, null, null, null, null, null));
		// gefundene services in ne Liste kopieren
		for (Iterator<ServiceDescription> iter = serviceDescriptionList.iterator(); iter.hasNext();) {
			IServiceDescription serviceDesc = iter.next();
			services.add(serviceDesc);
		}
		return services;
	}

	/**
	 * Searches for a service with the attributes given in the service search request.
	 * 
	 * @param request an object describing the requested service
	 * @return a List of IServiceDescriptions as search result.
	 */
	public List<IServiceDescription> searchService(IServiceSearchRequest request) {
		return null;
	}

    // TODO rename this method
	public MessageTransport getCommBean() {
		return _messageTransport;
	}

    // TODO rename this method
	public void setCommBean(MessageTransport commBean) {
		_messageTransport = commBean;
	}

	public int getPublishTimer() {
		return _publishTimer;
	}

	public void setPublishTimer(int publishTimer) {
		_publishTimer = publishTimer;
	}

	public String getAgentNodeName() {
		return _agentNode.getName();
	}

	public void setAgentNode(IAgentNode agentNode) {
		_agentNode = agentNode;
	}

	/**
	 * Information about the facts stored in the service directory memory.
	 * @return information about facts stored in service directory memory
	 */
	public CompositeData getMemory() {
	    Set<IFact> facts = memory.readAllOfType(IFact.class);
	    if (facts.isEmpty()) {
	    	return null;
	    }

	    // create map with current memory state
		Map<String,List<String>> map = new Hashtable<String,List<String>>();
	    for (IFact fact : facts) {
	    	String classname = fact.getClass().getName();
	    	List<String> values = map.get(classname);
	    	if (values == null) {
	    		values = new ArrayList<String>();
	    		map.put(classname, values);
	    	}
    		values.add(fact.toString());
	    }

	    // create composite data
	    CompositeData data = null;
	    int size = map.size();
	    String[] itemNames = new String[size];
	    OpenType[] itemTypes = new OpenType[size];
	    Object[] itemValues = new Object[size];
	    Object[] classes = map.keySet().toArray();
	    try {
	    	for (int i=0; i<size; i++) {
	    		String classname = (String) classes[i];
	    		itemNames[i] = classname;
	    		itemTypes[i] = new ArrayType(1, SimpleType.STRING);
	    		List<String> values = map.get(classname);
	    		String[] value = new String[values.size()];
	    		Iterator<String> it = values.iterator();	    		
	    		int j = 0;
	    		while (it.hasNext()) {
	    			value[j] = it.next();
	    			j++;
	    		}
	    		itemValues[i] = value;
	    	}
	    	CompositeType compositeType = new CompositeType(map.getClass().getName(), "facts stored in the service directory memory", itemNames, itemNames, itemTypes);
	    	data = new CompositeDataSupport(compositeType, itemNames, itemValues);
	    }
	    catch (OpenDataException e) {
	    	e.printStackTrace();
	    }

	    return data;		
	}

	/**
     * Registers the service directory for management.
     * @param manager the manager to be used for registration
	 */
	public void enableManagement(Manager manager) {
		// do nothing if management already enabled
		if (isManagementEnabled()) {
			return;
		}
		
		// register service directory for management
		try {
			manager.registerAgentNodeResource(getAgentNodeName(), "ServiceDirectory", this);
		}
		catch (Exception e) {
			System.err.println("WARNING: Unable to register service directory of agent node " + getAgentNodeName() + " as JMX resource.");
			System.err.println(e.getMessage());					
		}

		_manager = manager;
	}
	  
	/**
	 * Deregisters the service directory from management.
	 */
	public void disableManagement() {
		// do nothing if management already disabled
		if (!isManagementEnabled()) {
			return;
		}
		
		// deregister service directory from management
		try {
			_manager.unregisterAgentNodeResource(getAgentNodeName(), "ServiceDirectory");
		}
		catch (Exception e) {
			System.err.println("WARNING: Unable to deregister service directory of agent node " + getAgentNodeName() + " as JMX resource.");
			System.err.println(e.getMessage());					
		}
		
		_manager = null;
	}

	/**
	 * Checks wether the management of this object is enabled or not.
	 * @return true if the management is enabled, otherwise false
	 */
	public boolean isManagementEnabled() {
		return _manager != null;
	}  
}
