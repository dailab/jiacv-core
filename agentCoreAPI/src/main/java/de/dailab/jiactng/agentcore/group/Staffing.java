package de.dailab.jiactng.agentcore.group;

import java.util.List;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentRole;

public class Staffing {
	/** The agent that play the roles.*/
	private IAgent agent;
	
	/** The roles that are played by this agent.*/
	private List<IAgentRole> agentroles;

	/**
	 * Return the agent that staffs these roles.
	 * 
	 * @return the agent that staffs these roles
	 */
	public IAgent getAgent() {
		return agent;
	}

	/**
	 * The agent that staffs these roles.
	 * 
	 * @param agent the agent that staffs these roles
	 */
	public void setAgent(IAgent agent) {
		this.agent = agent;
	}

	/**
	 * The roles that are staffed by this agent.
	 * 
	 * @return the roles that are staffed by this agent
	 */
	public List<IAgentRole> getAgentroles() {
		return agentroles;
	}

	public void setAgentroles(List<IAgentRole> agentroles) {
		this.agentroles = agentroles;
	}
}
