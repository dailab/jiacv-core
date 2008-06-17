package de.dailab.jiactng.agentcore.comm.wp.helpclasses;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

/**
 * Wrapperclass to connect an action stored within the Directory with a (logical) creationtime
 * so making it possible to filter them out of the Directory
 * 
 * @author Martin Loeffelholz
 *
 */
@SuppressWarnings("serial")
public class ActionData implements IFact{
	private IActionDescription _action = null;
	private Long _creationTime = null;
	private Boolean _isLocal = null;
	private IAgentDescription _providerDescription = null; 

	public ActionData(long creationtime){
		_creationTime = new Long(creationtime);
	}

	/**
	 * standard Constructor method
	 */
	public ActionData(){
		_creationTime = null;
	}

	/**
	 * sets the (logical) time of creation of the <code>ActionData</code>
	 * 
	 * @param creationTime (logical) time of creation
	 */
	public void setCreationTime(Long creationTime){
		_creationTime = creationTime;
	}

	/**
	 * gets the (logical) time of creation of this instance of <code>ActionData</code>
	 * @return
	 */
	public Long getCreationTime(){
		return _creationTime;
	}

	/**
	 * sets an ActionDescription to be stored within this <code>ActionData</code>
	 * 
	 * @param action implements IActionDescription
	 */
	public void setActionDescription(IActionDescription action){
		_action = action;
	}

	/**
	 * gets the ActionDescription stored within this <code>ActionData</code>
	 * 
	 * @return IActionDescription stored within this <code>ActionData</code>
	 */
	public IActionDescription getActionDescription(){
		return _action;
	}
	
	public void setIsLocal( Boolean isLocal){
		_isLocal = isLocal;
	}
	
	public Boolean getIsLocal(){
		return _isLocal;
	}
	
	/**
	 * returns Stringrepresentation of this <code>ActionData</code>
	 */
	public String toString(){
		String thisString = "ActionData: ";
		if (_action != null){
			thisString += "Action.name= " + _action.getName() + ";";
		}
		if (_creationTime != null){
			thisString += "CreationTime=" + _creationTime + ";";
		}
		
		return thisString;
	}

	public IAgentDescription getProviderDescription() {
		return _providerDescription;
	}

	public void setProviderDescription(IAgentDescription description) {
		_providerDescription = description;
	}

}
