package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * The interface represents the description of an agentnode.
 * 
 * @author axle
 */
public interface IAgentNodeDescription extends IFact {
	
	/**
	 * Returns the unique identifier of the agentnode.
	 * 
	 * @return the UUID of the agentnode
	 */
	public String getUUID();
}
