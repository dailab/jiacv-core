/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;
import de.dailab.jiactng.agentcore.util.EqualityChecker;

/**
 * Describes an action that can be used within an agent. This is only the
 * action-declaration.
 * 
 * @see de.dailab.jiactng.agentcore.environment.IEffector
 * @see de.dailab.jiactng.agentcore.action.DoAction
 * @author moekon
 */
public class Action implements IActionDescription {
	/** SerialVersionUID for Serialization */
	private static final long serialVersionUID = 2416102010976263587L;

	/** the name of the action */
	private String _name;

	/** The component that holds the funtionality for this action */
	private transient IEffector providerBean;

	/** The class names of the input-parameters of this action */
	private List<String> _inputTypeNames;

	/** The class names of the results of this action */
	private List<String> _resultTypeNames;

	private IAgentDescription _providerDescription;

	/**
	 * This constructor is used to create an action template
	 */
	public Action() {
		this(null, null, (List<Class<?>>) null, (List<Class<?>>) null);
	}

	/**
	 * This constructor is used to create an action template with only the name
	 * specified.
	 */
	public Action(String name) {
		this(name, null, (List<Class<?>>) null, (List<Class<?>>) null);
	}

	/**
	 * Constructor. Creates a new action-declaration.
	 * 
	 * @param name
	 *            the name of the action
	 * @param providerBean
	 *            the component that holds the functionality of this action
	 * @param inputTypes
	 *            the classes of the input-parameters of this action
	 * @param resultTypes
	 *            the classes of the results of this action
	 */
	public Action(String name, IEffector providerBean, Class<?>[] inputTypes,
			Class<?>[] resultTypes) {
		this(name, providerBean, inputTypes != null ? Arrays.asList(inputTypes)
				: null, resultTypes != null ? Arrays.asList(resultTypes) : null);
	}

	public Action(String name, IEffector providerBean,
			List<Class<?>> inputTypes, List<Class<?>> resultTypes) {
		setName(name);
		setProviderBean(providerBean);
		setInputTypes(inputTypes);
		setResultTypes(resultTypes);
	}

	/**
	 * Copying constructor
	 * 
	 * @param action
	 *            the action to create an action from
	 */
	public Action(Action action) {
		_name = action.getName();
		providerBean = action.getProviderBean();

		// we can exchange references here, because the lists are immutable
		_inputTypeNames = action.getInputTypeNames();
		_resultTypeNames = action.getResultTypeNames();
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
	public DoAction createDoAction(Serializable[] newParams, ResultReceiver source) {
		return new DoAction(this, source, newParams);
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
	public DoAction createDoAction(Serializable[] newParams, ResultReceiver source,
			Long timeToLive) {
		return new DoAction(this, source, newParams, timeToLive);
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
  public DoAction createDoAction(Session parent, Serializable[] newParams, ResultReceiver source) {
    return new DoAction(parent, this, source, newParams);
  }
	
	/**
	 * Creates a new Result-object for this action. The resulting object can be
	 * written to the memory to return the results of the action. *
	 * 
	 * @param source
	 *            the entity that created the results of the action (usually the
	 *            providing component)
	 * @param results
	 *            the results that come from executing the action.
	 * 
	 * @see de.dailab.jiactng.agentcore.action.ActionResult
	 * @return a new ActionResult-object that can be used (by writing it to the
	 *         memory) to return the results of the action.
	 */
	public ActionResult createActionResult(DoAction source, Serializable[] results) {
		ActionResult ret = new ActionResult(source, results);
		ret.setMetaData(source.getMetaData());
		return ret;
	}

	/**
	 * Getter for the name.
	 * 
	 * @return a string representing the name of this action.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * Getter for the input-parameter class names.
	 * 
	 * @return an array containing the class names of the parameters in correct
	 *         order.
	 */
	public final List<String> getInputTypeNames() {
		return _inputTypeNames;
	}

	/**
	 * Getter for the input-parameter classes.
	 * 
	 * @return an array containing the classes of the parameters in correct
	 *         order.
	 * @throws ClassNotFoundException if one of the classes is unknown.
	 */
	public final List<Class<?>> getInputTypes() throws ClassNotFoundException {
		if (_inputTypeNames != null) {
			List<Class<?>> list = new ArrayList<Class<?>>();
			for (String type : _inputTypeNames) {
				list.add(getClassForName(type));
			}
			return list;
		} else {
			return null;
		}
	}

	/**
	 * Getter for the component that holds the functionality of this action
	 * 
	 * @return a life-reference to the component.
	 */
	public final IEffector getProviderBean() {
		return providerBean;
	}

	public final IAgentDescription getProviderDescription() {
		return _providerDescription;
	}

	/**
	 * Getter for the result class names.
	 * 
	 * @return an array containing the class names of the return-values in correct
	 *         order.
	 */
	public final List<String> getResultTypeNames() {
		return _resultTypeNames;
	}

	/**
	 * Getter for the result classes.
	 * 
	 * @return an array containing the classes of the return-values in correct
	 *         order.
	 * @throws ClassNotFoundException if one of the classes is unknown.
	 */
	public final List<Class<?>> getResultTypes() throws ClassNotFoundException {
		if (_resultTypeNames != null) {
			List<Class<?>> list = new ArrayList<Class<?>>();
			for (String type : _resultTypeNames) {
				list.add(getClassForName(type));
			}
			return list;
		} else {
			return null;
		}
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public final void setName(String name) {
		this._name = name;
	}

	/**
	 * @param inputTypeNames
	 *            the parameters to set
	 */
	public final void setInputTypeNames(List<String> inputTypeNames) {
		if (inputTypeNames != null) {
			List<String> copy = new ArrayList<String>();
			copy.addAll(inputTypeNames);
			_inputTypeNames = Collections.unmodifiableList(copy);
		} else {
			_inputTypeNames = null;
		}
	}

	/**
	 * @param inputTypes
	 *            the parameters to set
	 */
	public final void setInputTypes(List<Class<?>> inputTypes) {
		if (inputTypes != null) {
			List<String> copy = new ArrayList<String>();
			for (Class<?> type : inputTypes) {
				copy.add(type.getName());
			}
			_inputTypeNames = Collections.unmodifiableList(copy);
		} else {
			_inputTypeNames = null;
		}
	}

	/**
	 * @param providerBean
	 *            the providerBean to set
	 */
	public final void setProviderBean(IEffector providerBean) {
		this.providerBean = providerBean;
	}

	public final void setProviderDescription(
			IAgentDescription providerDescription) {
		_providerDescription = providerDescription;
	}

	/**
	 * @param resultTypeNames
	 *            the results to set
	 */
	public final void setResultTypeNames(List<String> resultTypeNames) {
		if (resultTypeNames != null) {
			List<String> copy = new ArrayList<String>();
			copy.addAll(resultTypeNames);
			_resultTypeNames = Collections.unmodifiableList(copy);
		} else {
			_resultTypeNames = null;
		}
	}

	/**
	 * @param resultTypes
	 *            the results to set
	 */
	public final void setResultTypes(List<Class<?>> resultTypes) {
		if (resultTypes != null) {
			List<String> copy = new ArrayList<String>();
			for (Class<?> type : resultTypes) {
				copy.add(type.getName());
			}
			_resultTypeNames = Collections.unmodifiableList(copy);
		} else {
			_resultTypeNames = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hash = Action.class.hashCode();
		hash ^= _name != null ? _name.hashCode() : 0;
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Action)) {
			return false;
		}

		if(this == obj) {
			return true;
		}
		
		Action other = (Action) obj;
		
		IAgentDescription myAgent = this.getProviderDescription();
		IAgentDescription otherAgent = other.getProviderDescription();

		if((myAgent!=null) && (otherAgent != null)) {
			if(!EqualityChecker.equalsOrNull(myAgent.getAid(), otherAgent.getAid())) {
				return false;
			}
		}

		return EqualityChecker.equalsOrNull(this.getName(), other.getName())
				&& EqualityChecker.equalsOrNull(this.getInputTypeNames(), other.getInputTypeNames())
				&& EqualityChecker.equalsOrNull(this.getResultTypeNames(), other.getResultTypeNames());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Action:\n name='").append(_name).append("'");
		builder.append("\n parameters=");
		prettyPrintArray(builder, _inputTypeNames);
		builder.append("\n results=");
		prettyPrintArray(builder, _resultTypeNames);
		if (_providerDescription != null) {
			builder.append("\n provider =");
			builder.append(_providerDescription.getName());
		}
		return builder.toString();
	}

	/*
	 * Utility-method for a nicely formatted output
	 */
	private void prettyPrintArray(StringBuilder builder, List<String> list) {
		if(list == null) {
			builder.append("null");
		} else {
			builder.append('[');
			for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
				builder.append(iter.next());
	
				if (iter.hasNext()) {
					builder.append("; ");
				}
			}
	
			builder.append(']');
		}
	}

	private Class<?> getClassForName(String type) throws ClassNotFoundException {
		if (type.equals("boolean")) {
			return boolean.class;
		}
		else if (type.equals("byte")) {
			return byte.class;
		}
		else if (type.equals("char")) {
			return char.class;
		}
		else if (type.equals("short")) {
			return short.class;
		}
		else if (type.equals("int")) {
			return int.class;
		}
		else if (type.equals("long")) {
			return long.class;
		}
		else if (type.equals("float")) {
			return float.class;
		}
		else if (type.equals("double")) {
			return double.class;
		}
		else {
			return Class.forName(type);
		}
	}
}
