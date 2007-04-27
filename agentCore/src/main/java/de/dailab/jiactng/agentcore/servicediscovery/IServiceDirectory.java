/**
 * 
 */
package de.dailab.jiactng.agentcore.servicediscovery;

import java.util.List;

import de.dailab.jiactng.agentcore.comm.description.IServiceDescription;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Interface for service discovery of the jiac tng.
 * It contains the main methods for registering, deregistering and searching services.
 * @author schenk
 *
 */

public interface IServiceDirectory extends IFact {
	
	/**
	 * Registers the service description in the service directory of jiac tng.
	 * @param description the service description
	 * @return true, if service registration was successful, otherwise false.
	 */
	
	public boolean registerService(IServiceDescription description);
	
	
	/**
	 * Deregisters the service description from the service directory of jiac tng.
	 * @param description the description of the service
	 * @return true, if service deregistration was successful, otherwise false.
	 */

	public boolean deRegisterService(IServiceDescription description);
	
	
	/**
	 * Searches for a service with the attributes given in the service search request.
	 * @param request an object describing the requested service
	 * @return a List of IServiceDescriptions as search result. 
	 */
	
	public List<IServiceDescription> searchService(IServiceSearchRequest request) ;
	
	
	/**
	 * Returns all web services registered in the jiac tng service directory.
	 * @return a List of IServiceDescriptions
	 */
	
	public List<IServiceDescription> getAllWebServices();
	
}
