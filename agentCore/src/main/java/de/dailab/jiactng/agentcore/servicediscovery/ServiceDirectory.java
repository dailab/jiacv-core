package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.knowledge.Memory;

/**
 * ServiceDirectoryBean soll Beschreibungen aller Services die auf einem AgentNode existieren, verfügbar halten. Dazu
 * wird die CommBean benutzt. Es soll der memory benutzt werden.
 * 
 * @author janko
 */
public class ServiceDirectory extends AbstractAgentBean implements IServiceDirectory {

	public ServiceDirectory() {
		super();
		memory = new Memory();
	}

	public void doInit() throws Exception {
		memory.init();
	}

	public void doStart() throws Exception {
		memory.start();
	}

	public void doStop() throws Exception {
		memory.stop();
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

}
