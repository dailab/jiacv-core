package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.dailab.jiactng.agentcore.comm.CommBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.comm.protocol.IServiceProtocol;
import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;

/**
 * ServiceDirectoryBean soll Beschreibungen aller Services die auf einem AgentNode existieren, verfügbar halten. Dazu
 * wird die CommBean benutzt. Es soll der memory benutzt werden.
 * 
 * @author janko
 */
public class ServiceDirectory extends AbstractLifecycle implements IServiceDirectory, Runnable {
	Log log = LogFactory.getLog(getClass());
	
	// um auf die topic zu schreiben
	CommBean _commBean;
	// zum speichern der Servicebeschreibungen
	ServiceDirectoryMemory memory;
	
	/* um die CommBean zu initialisieren, aus dem Namen wird die Adresse gebildet */
	String _agentNodeName;

	/* Time in milliseconds between publishment of services */
	int _publishTimer = 10000;

	public ServiceDirectory() {
		super();
		memory = new ServiceDirectoryMemory();
	}

	public void doInit() throws Exception {
		if (_commBean != null) {
			_commBean.setAgentNodeName(_agentNodeName);
			_commBean.init();
		}
	}

	public void doStart() throws Exception {}

	public void doStop() throws Exception {}

	public void doCleanup() throws Exception {
		if (_commBean != null) {
			_commBean.cleanup();
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
	 * @return ein String als Schlüssel für den ServiceDirectory-Thread
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
		for (Iterator iter = serviceList.iterator(); iter.hasNext();) {
			IServiceDescription serviceDesc = (IServiceDescription) iter.next();
			// nur services publishen, deren verfallsdatum in zukunft liegt.
			Date actualDate = new Date();
			if (serviceDesc.getExpireDate().after(actualDate)) {
				long timeDiff = serviceDesc.getExpireDate().getTime()-actualDate.getTime();
				log.debug("service:"+serviceDesc.getName()+" has "+timeDiff+" ms to live.");
				_commBean.publish(createJiacMessage(serviceDesc));
			}
		}
	}

	/**
	 * bastelt JiacNachrciht zusammen. Sie soll an eine ServicTopic gehen.
	 * 
	 * @param serviceDesc die zu verschiekende Servicebeschreibung
	 * @return die Nachricht die per JMS verschickt wird.
	 */
	private JiacMessage createJiacMessage(IServiceDescription serviceDesc) {
		IJiacContent content = new ObjectContent(serviceDesc);
		JiacMessage msg = new JiacMessage(IServiceProtocol.PUB_SERVICE, content, null, _commBean.getAddress(), _commBean
																						.getCommunicator().getSender().getDefaultReplyDestination());
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
	 * @return
	 */
	public Set<ServiceDescription> findServiceByName(String name) {
		Set<ServiceDescription> serviceDescriptionList = memory.readAll(new ServiceDescription(null, null, name, null,
																						null, null, null, null, null, null, null));
		return serviceDescriptionList;
	}

	/**
	 * Liefert die Anzahl der registrierten Services zurück
	 * 
	 * @return Liefert die Anzahl der registrierten Services zurück
	 */
	public int getServiceNumber() {
		return memory.readAllOfType(IServiceDescription.class).size();
	}

	/**
	 * threadsafe ??
	 * 
	 * @return all webservices, stored in this servicedirectory
	 */
	public List<IServiceDescription> getAllWebServices() {
		List<IServiceDescription> webServices = new ArrayList<IServiceDescription>();
		Set<ServiceDescription> serviceDescriptionList = memory.readAll(new ServiceDescription(null, null, null, null,
																						null, null, null, null, null, null, null));
		for (Iterator iter = serviceDescriptionList.iterator(); iter.hasNext();) {
			IServiceDescription serviceDesc = (IServiceDescription) iter.next();
			if (serviceDesc.isWebService()) {
				webServices.add(serviceDesc);
			}
		}
		return webServices;
	}

	/**
	 * threadsafe ??
	 * 
	 * @return all webservices, stored in this servicedirectory
	 */
	public List<IServiceDescription> getAllServices() {
		List<IServiceDescription> services = new ArrayList<IServiceDescription>();
		Set<ServiceDescription> serviceDescriptionList = memory.readAll(new ServiceDescription(null, null, null, null,
																						null, null, null, null, null, null, null));
		// gefundene services in ne Liste kopieren
		for (Iterator iter = serviceDescriptionList.iterator(); iter.hasNext();) {
			IServiceDescription serviceDesc = (IServiceDescription) iter.next();
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

	public CommBean getCommBean() {
		return _commBean;
	}

	public void setCommBean(CommBean commBean) {
		_commBean = commBean;
	}

	public int getPublishTimer() {
		return _publishTimer;
	}

	public void setPublishTimer(int publishTimer) {
		_publishTimer = publishTimer;
	}

	public String getAgentNodeName() {
		return _agentNodeName;
	}

	public void setAgentNodeName(String agentNodeName) {
		_agentNodeName = agentNodeName;
	}

}
