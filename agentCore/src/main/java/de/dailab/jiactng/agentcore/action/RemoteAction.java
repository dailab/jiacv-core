package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * Wrapper for advertising actions to other agents.
 * 
 * @author axle

 */
public class RemoteAction implements IFact {
	/** The action that will be offered remotely.*/
	private Action action;
	
	/** The agent that offers the action for remote use.*/
	private AgentDescription agentDescription;
	
	public RemoteAction(Action action, AgentDescription agentDescription) {
		this.action = action;
		this.agentDescription = agentDescription;
	}

	/**
	 * @return the action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * @return the agentDescription
	 */
	public AgentDescription getAgentDescription() {
		return agentDescription;
	}

	/**
	 * @param agentDescription the agentDescription to set
	 */
	public void setAgentDescription(AgentDescription agentDescription) {
		this.agentDescription = agentDescription;
	}

    @Override
    public String toString() {
        return "RemoteAction for " + action.toString();
    }
}
