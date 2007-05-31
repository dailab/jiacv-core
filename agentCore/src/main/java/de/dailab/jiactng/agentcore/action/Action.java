/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
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
	private transient IEffector providerBean;

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
	 * Copying constructor
	 * 
	 * @param action
	 *            the action to create an action from
	 */
	public Action(Action action) {
		this.name = action.getName();
		this.providerBean = action.getProviderBean();
		if (action.parameters != null) {
			this.parameters = new Class[action.getParameters().length];
			for (int i = 0; i < action.getParameters().length; i++) {
				this.parameters[i] = action.getParameters()[i];
			}
		}
		if (action.getResults() != null) {
			this.results = new Class[action.getResults().length];
			for (int i = 0; i < action.getResults().length; i++) {
				this.results[i] = action.getResults()[i];
			}
		}
	}

	/**
	 * Creates a new DoAction-object for this action. The resulting object can
	 * be written to the memory to trigger the action.
	 * 
	 * @see de.dailab.jiactng.agentcore.action.DoAction
	 * @param newParams
	 *            the input-parameters that should be used when executing the
	 *            action.
	 * @param source
	 *            the caller of the action.
	 * @return a new DoAction-object that can be used (by writing it to the
	 *         memory) to call the action.
	 */
	public DoAction createDoAction(Object[] newParams, ResultReceiver source) {
		return new DoAction(this, source, newParams);
	}

	/**
	 * Creates a new Result-object for this action. The resulting object can be
	 * written to the memory to return the results of the action. *
	 * 
	 * @see de.dailab.jiactng.agentcore.action.ActionResult
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
	public ActionResult createActionResult(Session resultOf, Object[] results,
			Object source) {
		return new ActionResult(this, resultOf, results, source);
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

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Class[] parameters) {
		this.parameters = parameters;
	}

	/**
	 * @param providerBean
	 *            the providerBean to set
	 */
	public void setProviderBean(IEffector providerBean) {
		this.providerBean = providerBean;
	}

	/**
	 * @param results
	 *            the results to set
	 */
	public void setResults(Class[] results) {
		this.results = results;
	}

}
