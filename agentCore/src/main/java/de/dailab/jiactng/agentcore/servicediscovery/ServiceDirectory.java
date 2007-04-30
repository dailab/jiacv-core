package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * ServiceDirectoryBean soll Beschreibungen aller Services die auf einem AgentNode existieren, verfügbar halten. Dazu
 * wird die CommBean benutzt. Es soll der memory benutzt werden.. - weil der nicht funktioniert ist mockmässig eine
 * eigene Hashmapklasse implmentiert, die nach namen speichert.
 * 
 * @author janko
 */
public class ServiceDirectory implements IServiceDirectory {

	HashMap2 _hashMap;

	public ServiceDirectory() {
		_hashMap = new HashMap2();
	}

	/**
	 * Registriert n service
	 * 
	 * @param serviceDescription
	 */
	public boolean registerService(IServiceDescription serviceDescription) {
		// memory.write(serviceDescription);
		return (_hashMap.put(serviceDescription.getName(), serviceDescription) != null);
	}

	/**
	 * entfernt ein service aus dem directory
	 * 
	 * @param serviceDescription
	 */
	public boolean deRegisterService(IServiceDescription serviceDescription) {
		// memory.remove(serviceDescription);
		return (_hashMap.remove(serviceDescription) != null);
	}

	/**
	 * Sucht einen Service im Directory anhand des ServiceNamens
	 * 
	 * @param name
	 * @return
	 */
	public Set<ServiceDescription> findServiceByName(String name) {
		// Set<ServiceDescription> serviceDescriptionList = memory.readAll(new ServiceDescription(null, null, name, null,
		// null, null, null, null, null, null, null));
		// achtung hier wird das Object in der testimplementierung zurückgeliefert.
		Set<ServiceDescription> hashMap_Set = (Set<ServiceDescription>) _hashMap.findByKey(name);
		return hashMap_Set;
	}

	/**
	 * Liefert die Anzahl der registrierten Services zurück
	 * 
	 * @return Liefert die Anzahl der registrierten Services zurück
	 */
	public int getServiceNumber() {
		// return memory.readAllOfType(IServiceDescription.class).size();
		return 0;
	}

	/**
	 * returns all webservices, stored in this servicedirectory
	 */
	public List<IServiceDescription> getAllWebServices() {
		return _hashMap.getAllWebServices();
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

	/*********************************************************************************************************************
	 * Temporäre Hilfsklasse zum speichern von Sets in nem Hash HashMap2 - eine HashMap mit Sets als Werten
	 * 
	 * @author janko
	 */
	class HashMap2 {
		HashMap _hash;

		public HashMap2() {
			_hash = new HashMap();
		}

		/**
		 * @param key Schlüssel unter dem gespeichert wird
		 * @param value der Wert der gespeichert werden soll
		 * @return Das Object das gespeichert wurde
		 */
		public Object put(Object key, Object value) {
			if (_hash.containsKey(key)) {
				// name gibts schon
				Set set = (Set) _hash.get(key);
				return set.add(value);
			} else {
				Set set = new HashSet();
				set.add(value);
				return _hash.put(key, set);
			}
		}

		public Object remove(Object object) {
			for (Iterator iter = _hash.values().iterator(); iter.hasNext();) {
				Set set = (HashSet) iter.next();
				if (set.contains(object)) {
					return set.remove(object);
				}
			}
			return null;
		}

		/**
		 * Findet alle Objekte die zum gegebenen Key gespeichert wurden
		 * 
		 * @param key
		 * @return ein Set von Objekten
		 */
		public Set findByKey(Object key) {
			return (Set) _hash.get(key);
		}

		/**
		 * Geht alle hashwerte durch - das sind sets. Jedes set wird ebenefalls durchlaufen und geguckt ob es ein Webservice ist.
		 * Alle gefundenen WebServices werden in Liste gesmmelt und zurückgegeben. 
		 * @return eine Liste mit IServiceDescriptions, die Webservices beschreiben, kann leer sein
		 */
		public List<IServiceDescription> getAllWebServices() {
			List<IServiceDescription> serviceDescList = new ArrayList<IServiceDescription>();
			for (Iterator iter = _hash.values().iterator(); iter.hasNext();) {
				Set serviceSet = (HashSet) iter.next();
				for (Iterator iterator = serviceSet.iterator(); iterator.hasNext();) {
					IServiceDescription service = (IServiceDescription) iterator.next();
					if (service.isWebService()) {
						serviceDescList.add(service);
					}
				}
			}
			return serviceDescList;
		}
		
	}

}
