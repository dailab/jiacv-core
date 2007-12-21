package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.util.EqualityChecker;

/**
 * This is a wrapper for a <code>DoAction</code>.
 * 
 * @author axle
 */
public class DoRemoteAction implements IFact {
	private DoAction action;
	
	public DoRemoteAction (DoAction action) {
		this.action = action;
	}

	/**
	 * @return the action
	 */
	public DoAction getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(DoAction action) {
		this.action = action;
	}

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        
        if(obj == null || !(obj instanceof DoRemoteAction)) {
            return false;
        }
        
        DoRemoteAction other= (DoRemoteAction) obj;
        return EqualityChecker.equals(action, other.action);
    }

    @Override
    public int hashCode() {
        return DoRemoteAction.class.hashCode() ^ (action != null ? action.hashCode() : 0);
    }
}
