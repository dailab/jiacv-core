package de.dailab.jiactng.agentcore.directory;

import java.util.List;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IServiceDescription;

/**
 * This interface describes the way how to access the directory.
 * It should be implemented by agents with reference to a directory
 * and by the directory itself.
 * 
 * @author axle
 */
public interface IDirectoryAccess {
	/**
	 * Registers an action within the directory.
	 * 
	 * @param actionDescription the description of the action
	 */
	void registerAction(IActionDescription actionDescription);
	
	/**
	 * Removes an action from the directory
	 * 
	 * @param actionDescription the description of the action
	 */
	void deregisterAction(IActionDescription actionDescription);
	
	/**
	 * Modifies an action description stored in the directory and
	 * characterized with the first argument with the second argument.
	 * 
	 * @param oldDescription the old description of the action
	 * @param newDescription the new description of the action
	 */
	void modifyAction(IActionDescription oldDescription, IActionDescription newDescription);
	
	/**
	 * Returns an action that matches the given action description.
	 * Usually it is the first that can be found or <code>null</code>
	 * if none could be found.
	 * 
	 * @param template an action description template that will be used 
	 *        to find such an action in the directory
	 * @return the action that matches the template or <code>null</code>
	 *         if none could be found
	 */
	IActionDescription searchAction(IActionDescription template);

	/**
	 * Returns all actions that match the given action description.
	 * 
	 * @param template an action description template that will be used 
	 *        to find such actions in the directory
	 * @return all actions that match the template or an empty list
	 *         if none could be found
	 */
	List<IActionDescription> searchAllActions(IActionDescription template);	
	
	/**
	 * Returns an action that matches the given service description.
	 * Usually it is the first that can be found or <code>null</code>
	 * if none could be found.
	 * 
	 * @param template a service description template that will be used 
	 *        to find such an action in the directory
	 * @return the action that matches the template or <code>null</code>
	 *         if none could be found
	 */
	IActionDescription searchAction(IServiceDescription template);

	/**
	 * Returns all actions that match the given service description.
	 * 
	 * @param template a service description template that will be used 
	 *        to find such actions in the directory
	 * @return all actions that match the template or an empty list
	 *         if none could be found
	 */
	List<IActionDescription> searchAllActions(IServiceDescription template);
}
