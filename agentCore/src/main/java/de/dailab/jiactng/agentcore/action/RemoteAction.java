package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.util.EqualityChecker;

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
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        
        if(obj == null || !(obj instanceof RemoteAction)) {
            return false;
        }
        
        RemoteAction other= (RemoteAction) obj;
        return  EqualityChecker.equals(action, other.action) &&
                EqualityChecker.equals(agentDescription, other.agentDescription);
    }

    @Override
    public int hashCode() {
        int hashCode= RemoteAction.class.hashCode();
        hashCode ^= action != null ? action.hashCode() : 0;
        hashCode ^= agentDescription != null ? agentDescription.hashCode() : 0;
        return hashCode;
    }

    @Override
    public String toString() {
        return "RemoteAction for " + action.toString() + " by " + agentDescription.toString();
    }
}
