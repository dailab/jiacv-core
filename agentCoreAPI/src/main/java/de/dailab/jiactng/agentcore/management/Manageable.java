package de.dailab.jiactng.agentcore.management;

/**
 * Interface for all resources which are manageable by other local objects
 * or remote applications. The manager should be used to register and
 * deregister the resource.
 * @author Jan Keiser
 * @see Manager
 */
public interface Manageable {

	  /**
	   * Registers itself and all resources for management.
	   * @param manager the manager to be used for registration of resources
	   */
	  void enableManagement(Manager manager);
	  
	  /**
	   * Deregisters itself and all its resources from management.
	   */
	  void disableManagement();

	  /**
	   * Checks wether the management of this object is enabled or not.
	   * @return true if the management is enabled, otherwise false
	   */
	  boolean isManagementEnabled();
	  
}
