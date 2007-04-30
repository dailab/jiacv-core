/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * Describes an action that can be used within an agent. This is only the
 * action-declaration.
 * 
 * @see de.dailab.jiactng.agentcore.environment.IEffector
 * @see de.dailab.jiactng.agentcore.action.DoAction
 * @author moekon
 */
public class Action implements IFact {

	/** the name of the action */
	private String name;

	/** The component that holds the funtionality for this action */
	private IEffector providerBean;

	/** The classes of the input-parameters of this action */
	private Class[] parameters;

	/** The classes of the results of this action */
	private Class[] results;

	/**
	 * Constructor. Creates a new action-declaration.
	 * 
	 * @param name
	 *            the name of the action
	 * @param providerBean
	 *            the component that holds the functionality of this action
	 * @param parameters
	 *            the classes of the input-parameters of this action
	 * @param results
	 *            the classes of the results of this action
	 */
	public Action(String name, IEffector providerBean, Class[] parameters,
			Class[] results) {
		super();
		this.name = name;
		this.providerBean = providerBean;
		this.parameters = parameters;
		this.results = results;
	}

	/**
	 * Creates a new DoAction-object for this action. The resulting object can
	 * be written to the memory to trigger the action.
	 * 
	 * @see de.dailab.jiactng.agentcore.action.DoAction
	 * 
	 * @param newParams
	 *            the input-parameters that should be used when executing the
	 *            action.
	 * @param source
	 *            the caller of the action.
	 * @return a new DoAction-object that can be used (by writing it to the
	 *         memory) to call the action.
	 */
	public DoAction createDoAction(Object[] newParams, Object source) {
		return new DoAction(this, source, newParams);
	}

	/**
	 * Creates a new Result-object for this action. The resulting object can be
	 * written to the memory to return the results of the action.
	 *  *
	 * @see de.dailab.jiactng.agentcore.action.ActionResult
	 * 
	 * @param resultOf
	 *            the DoAction-object that triggered the action.
	 * @param success
	 *            flag for successful execution of the action.
	 * @param results
	 *            the results that come from executing the action.
	 * @param source
	 *            the entity that created the results of the action (usually the
	 *            providing component)
	 * @return a new ActionResult-object that can be used (by writing it to the
	 *         memory) to return the results of the action.
	 */
	public ActionResult createActionResult(DoAction resultOf, boolean success,
			Object[] results, Object source) {
		return new ActionResult(this, resultOf, success, results, source);
	}

	/**
	 * Getter for the name.
	 * 
	 * @return a string representing the name of this action.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for the input-parameter classes.
	 * 
	 * @return an array containing the classes of the parameters in correct
	 *         order.
	 */
	public Class[] getParameters() {
		return parameters;
	}

	/**
	 * Getter for the component that holds the functionality of this action
	 * 
	 * @return a life-reference to the component.
	 */
	public IEffector getProviderBean() {
		return providerBean;
	}

	/**
	 * Getter for the result classes.
	 * 
	 * @return an array containing the classes of the return-values in correct
	 *         order.
	 */
	public Class[] getResults() {
		return results;
	}

}
