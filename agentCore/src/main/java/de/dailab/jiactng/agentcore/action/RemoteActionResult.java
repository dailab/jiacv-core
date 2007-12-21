package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.util.EqualityChecker;

/** 
 * This class is a wrapper for <code>ActionResult</code>.
 * @author axle
 */
public class RemoteActionResult implements IFact {

	/** The <code>ActionResult</code> to transport*/
	private ActionResult result;
	
	public RemoteActionResult(ActionResult result) {
		this.result = result;
	}

	/**
	 * @return the result
	 */
	public ActionResult getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(ActionResult result) {
		this.result = result;
	}

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        
        if(obj == null || !(obj instanceof RemoteActionResult)) {
            return false;
        }
        
        RemoteActionResult other= (RemoteActionResult) obj;
        return EqualityChecker.equals(result, other.result);
    }

    @Override
    public int hashCode() {
        return RemoteActionResult.class.hashCode() ^ (result != null ? result.hashCode() : 0);
    }
    
    
}
