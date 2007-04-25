/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Action implements IFact {

	private String name;

	private IEffector providerBean;

	private Class[] parameters;

	private Class[] results;

	public Action(String name, IEffector providerBean, Class[] parameters,
			Class[] results) {
		super();
		this.name = name;
		this.providerBean = providerBean;
		this.parameters = parameters;
		this.results = results;
	}

	public DoAction createDoAction(Object[] newParams, Object source) {
		return new DoAction(this, source, newParams);
	}

	public String getName() {
		return name;
	}

	public Class[] getParameters() {
		return parameters;
	}

	public IEffector getProviderBean() {
		return providerBean;
	}

	public Class[] getResults() {
		return results;
	}

}
