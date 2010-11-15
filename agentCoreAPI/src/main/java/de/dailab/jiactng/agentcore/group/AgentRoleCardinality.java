package de.dailab.jiactng.agentcore.group;

import de.dailab.jiactng.agentcore.IAgentRole;

/**
 * Relationship between an agentrole and quantity.
 * 
 * @author axle
 */
public class AgentRoleCardinality {
	/** The agentrole.*/
	private IAgentRole agentrole;
	
	/** The actual number of agents the play this role.*/
	private int cardinality = 0;
	
	/** The desired minimum number of agents that play this role.*/
	private int minCardinality = 0;
	
	/** The maximum allowed number of agents that play this role.*/
	private int maxCardinality = -1;

	/**
	 * Returns the agentrole.
	 * 
	 * @return the agentrole
	 */
	public IAgentRole getAgentrole() {
		return agentrole;
	}

	/**
	 * Sets the agentrole.
	 * 
	 * @param agentrole
	 */
	public void setAgentrole(IAgentRole agentrole) {
		this.agentrole = agentrole;
	}

	/**
	 * Returns the actual number of agents that play this agentrole.
	 * 
	 * @return the actual number of agents that play this agentrole
	 */
	public int getCardinality() {
		return cardinality;
	}

	/**
	 * Sets the actual number of agents that play this agentrole.
	 * 
	 * @param cardinality the actual number of agents that play this agentrole
	 */
	public void setCardinality(int cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * Restriction that limits the minimal number of agents that play this agentrole.
	 * 
	 * @return the minimal number of agent that play that role.
	 */
	public int getMinCardinality() {
		return minCardinality;
	}

	/**
	 * Restriction that limits the minimal number of agents that play this agentrole.
	 * 
	 * @param minCardinality  minimal number of agents that play this agentrole. default: 0
	 */ 
	public void setMinCardinality(int minCardinality) {
		this.minCardinality = minCardinality;
	}

	/**
	 * Restriction that limits the maximum number of agents that play this agentrole.
	 * -1 is unlimited.
	 * 
	 * @return the maximum number of agents that play this agentrole
	 */
	public int getMaxCardinality() {
		return maxCardinality;
	}

	/**
	 * Restriction that limits the maximum number of agents that play this agentrole.
	 * -1 is unlimited, default: -1.
	 * 
	 * @param maxCardinality 
	 * 				the maximum number of agents that play this agentrole.
	 * 				-1 is unlimited, default: -1.
	 */
	public void setMaxCardinality(int maxCardinality) {
		this.maxCardinality = maxCardinality;
	}
}
