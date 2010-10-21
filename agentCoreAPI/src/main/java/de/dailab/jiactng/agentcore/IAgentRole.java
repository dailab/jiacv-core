package de.dailab.jiactng.agentcore;

import java.util.List;

import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * This interface represents the (sub)set of capabilities of an
 * agent.  These capabilities result from AgentBeans and scripts.
 *  
 * @author axle
 */
public interface IAgentRole extends IFact {
	/**
	 * Sets the unique name of this agentrole
	 * @param name the unique name of this agentrole
	 */
	void setName(String name);
	
	/**
	 * Returns the unique name of this agentrole
	 * @return the unique name of this agentrole
	 */
	String getName();
	
	  /**
	   * Setter for the agentrole's agentbeans.
	   * 
	   * @param agentbeans the agentbeans of this agentrole.
	   */
	  void setAgentBeans(List<IAgentBean> agentbeans);
	  
	  /**
	   * Getter for a list of agentbeans of this agentrole.
	   * 
	   * @return the unmodifiable list of the agentbeans.
	   * 
	   * @see java.util.Collections#unmodifiableList(List)
	   */
	  List<IAgentBean> getAgentBeans();

	  /**
	   * Setter for scripts in this agentrole.
	   * 
	   * @param scripts the scripts of this agentrole
	   */
	  void setScripts(List<String> scripts);
	  
	  /**
	   * Returns the scripts of this agentrole.
	   * @return the scripts of this agentrole
	   */
	  List<String> getScripts();
	  
	  /**
	   * Setter for subroles of this agentrole.
	   * @param includedAgentRoles the list of subroles that this agentrole inherits
	   */
	  void setIncludedAgentRoles(List<IAgentRole> includedAgentRoles);
	  
	  /**
	   * Getter for inherited agentroles.
	   * @return the subroles of this agentrole
	   */
	  List<IAgentRole> getIncludedAgentRoles();
	  
	  /**
	   * The setter for an interpreter that interprets the scripts.
	   * If no interpreter is set, scripts will be ignored
	   * @param interpreter the interpreter
	   */
	  void setInterpreter(IEffector interpreter);
	  
	  /**
	   * Returns all actions that are provided by agentbeans and scripts of this agentrole.
	   * TODO: clarify, whether the method also returns actions of included roles
	   * @return the actions that are provided by agentbeans and scripts of this agentrole
	   */
	  List<IActionDescription> getActions();
}
