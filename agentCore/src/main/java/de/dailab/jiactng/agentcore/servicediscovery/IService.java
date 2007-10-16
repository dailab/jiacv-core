package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.List;

import de.dailab.jiactng.agentcore.environment.IEffector;

/**
 * Ein Interface fuer Services. Frage: sollen die IEffector erweitern ? Oder nur IAgentBean ?
 * @author janko
 * ‰‰hh, was ?
 */
public interface IService extends IEffector {

	/**
	 * Ruft einen Service auf
	 * @param serviceName Name des Service (oder lieber Id?)
	 * @return ein Array den Rueckgabewerten
	 */
	public ServiceParameter[] invoke(String serviceName);
	
	/**
	 * Liefert ne Liste mit ServiceDescriptions, den einzelnen operationen des Service
	 * @return
	 */
	public List<IServiceDescription> getServiceDescriptions();
}
