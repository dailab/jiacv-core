package de.dailab.jiactng.agentcore.directory;

import java.util.List;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;


/**
 * An interface for all directory implementations.
 * 
 * @author axle
 */
public interface IDirectory extends IDirectoryAccess, ILifecycleListener {
	/**
	 * Registers an agent in the directory. If the given agent
	 * has already been registered the registered agent will be modified.
	 * 
	 * @param agentDescription the agent to be registered
	 * @see #modifyAgent(IAgentDescription)
	 */
	public void registerAgent(IAgentDescription agentDescription);
	
	/**
	 * Unregisters the given agent from the directory.
	 * 
	 * @param aid agent identifier of the agent to remove
	 */
	public void deregisterAgent(String aid);
	
	/**
	 * Modifies an agent in the directory. If the agent has not yet been
	 * registered it will be registered in the directory.
	 * 
	 * @param agentDescription the agent to modify
	 * @see #registerAgent(IAgentDescription)
	 */
	public void modifyAgent(IAgentDescription agentDescription);
	
	/**
	 * Searches for an agent that matches the given template. Returns
	 * the first match or <code>null</code> if no match found.
	 * 
	 * @param agentDescription the agent description template
	 * @return the first match that matches the given template
	 *         or <code>null</code> if no match found. 
	 */
	public IAgentDescription searchAgent(IAgentDescription template);

	/**
	 * Searches for agents that match the given template. Returns
	 * all known agents that match or an empty list.
	 * 
	 * @param agentDescription the agent description template
	 * @return all agents that match the given template
	 *         or an empty set if no match found.
	 * @see List
	 */
	public List<IAgentDescription> searchAllAgents(IAgentDescription template);
}
