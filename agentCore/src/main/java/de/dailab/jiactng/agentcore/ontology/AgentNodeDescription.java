package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * The datastructure to store information about an agentnode.
 * 
 * @author axle
 */
public class AgentNodeDescription implements IAgentNodeDescription {
	private static final long serialVersionUID = -7460322120270381260L;

	public AgentNodeDescription(String uuid) {
		super();
		UUID = uuid;
	}

	/**
	 * The unique identifier for the AgentNode given
	 */
	private String UUID = null;

	/**
	 * Returns the UUID of the agentnode.
	 * 
	 * @return the UUID of the agentnode
	 */
	public String getUUID() {
		return UUID;
	}

	/**
	 * Sets the UUID of the agentnode.
	 * 
	 * @param uuid the UUID to set
	 */
	public void setUUID(String uuid) {
		UUID = uuid;
	}
}
