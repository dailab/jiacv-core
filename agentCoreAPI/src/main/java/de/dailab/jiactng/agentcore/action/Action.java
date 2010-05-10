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

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import de.dailab.jiactng.agentcore.action.scope.ActionScope;
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
	private String name;

	/** The component that holds the funtionality for this action */
	private transient IEffector providerBean;

	/** The class names of the input-parameters of this action */
	private List<String> inputTypeNames;

	/** The class names of the results of this action */
	private List<String> resultTypeNames;

	private IAgentDescription providerDescription;
	
	/** The scope of the action, i.e. which agent will it know or can it use.*/
	private ActionScope scope = null;
	
	/**
	 * This constructor is used to create an action template
	 */
	public Action() {
		this(null, null, (List<Class<?>>) null, (List<Class<?>>) null);
	}

	/**
	 * This constructor is used to create an action template with only the name
	 * specified.
	 * @param name the name of the action template
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

	/**
	 * Constructor. Creates a new action-declaration.
	 * @param name the name of the action
	 * @param providerBean the component that holds the functionality of this action
	 * @param inputTypes the classes of the input-parameters of this action
	 * @param resultTypes the classes of the results of this action
	 */
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
		name = action.getName();
		providerBean = action.getProviderBean();

		// we can exchange references here, because the lists are immutable
		inputTypeNames = action.getInputTypeNames();
		resultTypeNames = action.getResultTypeNames();
	}

	/**
	 * {@inheritDoc}
	 */
	public final DoAction createDoAction(Serializable[] newParams, ResultReceiver source) {
		return new DoAction(this, source, newParams);
	}

	/**
	 * {@inheritDoc}
	 */
	public final DoAction createDoAction(Serializable[] newParams, ResultReceiver source,
			Long timeToLive) {
		return new DoAction(this, source, newParams, timeToLive.longValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public final DoAction createDoAction(Session parent, Serializable[] newParams, ResultReceiver source) {
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
	public final ActionResult createActionResult(DoAction source, Serializable[] results) {
		final ActionResult ret = new ActionResult(source, results);
		ret.setMetaData(source.getMetaData());
		return ret;
	}

	/**
	 * Getter for the name.
	 * 
	 * @return a string representing the name of this action.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Getter for the input-parameter class names.
	 * 
	 * @return an array containing the class names of the parameters in correct
	 *         order.
	 */
	public final List<String> getInputTypeNames() {
		return inputTypeNames;
	}

	/**
	 * Getter for the input-parameter classes.
	 * 
	 * @return an array containing the classes of the parameters in correct
	 *         order.
	 * @throws ClassNotFoundException if one of the classes is unknown.
	 */
	public final List<Class<?>> getInputTypes() throws ClassNotFoundException {
		if (inputTypeNames != null) {
			final List<Class<?>> list = new ArrayList<Class<?>>();
			for (String type : inputTypeNames) {
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

	/**
	 * Getter for the description of the agent, which provides this action.
	 * 
	 * @return the description of the providing agent.
	 */
	public final IAgentDescription getProviderDescription() {
		return providerDescription;
	}

	/**
	 * Getter for the result class names.
	 * 
	 * @return an array containing the class names of the return-values in correct
	 *         order.
	 */
	public final List<String> getResultTypeNames() {
		return resultTypeNames;
	}

	/**
	 * Getter for the result classes.
	 * 
	 * @return an array containing the classes of the return-values in correct
	 *         order.
	 * @throws ClassNotFoundException if one of the classes is unknown.
	 */
	public final List<Class<?>> getResultTypes() throws ClassNotFoundException {
		if (resultTypeNames != null) {
			final List<Class<?>> list = new ArrayList<Class<?>>();
			for (String type : resultTypeNames) {
				list.add(getClassForName(type));
			}
			return list;
		} else {
			return null;
		}
	}

	/**
	 * Sets the name of this action.
	 * @param newName
	 *            the name to set
	 */
	public final void setName(String newName) {
		name = newName;
	}

	/**
	 * Sets the input types of this action.
	 * @param newInputTypeNames
	 *            the parameters to set
	 */
	public final void setInputTypeNames(List<String> newInputTypeNames) {
		if (newInputTypeNames != null) {
			final List<String> copy = new ArrayList<String>();
			copy.addAll(newInputTypeNames);
			inputTypeNames = Collections.unmodifiableList(copy);
		} else {
			inputTypeNames = null;
		}
	}

	/**
	 * Sets the input types of this action.
	 * @param newInputTypes
	 *            the parameters to set
	 */
	public final void setInputTypes(List<Class<?>> newInputTypes) {
		if (newInputTypes != null) {
			final List<String> copy = new ArrayList<String>();
			for (Class<?> type : newInputTypes) {
				copy.add(type.getName());
			}
			inputTypeNames = Collections.unmodifiableList(copy);
		} else {
			inputTypeNames = null;
		}
	}

	/**
	 * Sets the agent bean which provides this action.
	 * @param newProviderBean
	 *            the providerBean to set
	 */
	public final void setProviderBean(IEffector newProviderBean) {
		providerBean = newProviderBean;
	}

	/**
	 * Sets the description of the agent which provides this action.
	 * @param newProviderDescription description of the providing agent
	 */
	public final void setProviderDescription(
			IAgentDescription newProviderDescription) {
		providerDescription = newProviderDescription;
	}

	/**
	 * Sets the result types of this action.
	 * @param newResultTypeNames
	 *            the results to set
	 */
	public final void setResultTypeNames(List<String> newResultTypeNames) {
		if (newResultTypeNames != null) {
			final List<String> copy = new ArrayList<String>();
			copy.addAll(newResultTypeNames);
			resultTypeNames = Collections.unmodifiableList(copy);
		} else {
			resultTypeNames = null;
		}
	}

	/**
	 * Sets the result types of this action.
	 * @param newResultTypes
	 *            the results to set
	 */
	public final void setResultTypes(List<Class<?>> newResultTypes) {
		if (newResultTypes != null) {
			final List<String> copy = new ArrayList<String>();
			for (Class<?> type : newResultTypes) {
				copy.add(type.getName());
			}
			resultTypeNames = Collections.unmodifiableList(copy);
		} else {
			resultTypeNames = null;
		}
	}

	/**
	 * Returns the hash code of the action class, 
	 * thus it is the same hash code for all actions.
	 * @return the hash code of the action class
	 */
	@Override
	public int hashCode() {
		final int hash = Action.class.hashCode();
		// hash ^= _name != null ? _name.hashCode() : 0;
		return hash;
	}

	/**
	 * Checks the equality of two actions. The actions are equal
	 * if their names, input types, and result types are equal or null.
	 * The actions are not equal if they are provided by different agents.
	 * @param obj the other action
	 * @return the result of the equality check
	 * @see EqualityChecker#equalsOrNull(Object, Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Action)) {
			return false;
		}

		if(this == obj) {
			return true;
		}
		
		final Action other = (Action) obj;
		
		final IAgentDescription myAgent = this.getProviderDescription();
		final IAgentDescription otherAgent = other.getProviderDescription();

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
	 * Returns a multiline text which contains the name, input types,
	 * result types, the provider bean, provider agent, and scope of the action.
	 * @return a string representation of the action
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Action:\n name='").append(name).append("'");
		builder.append("\n parameters=");
		prettyPrintArray(builder, inputTypeNames);
		builder.append("\n results=");
		prettyPrintArray(builder, resultTypeNames);
    builder.append("\n bean=");
    builder.append(this.providerBean);
		
		builder.append("\n provider =");
		if (providerDescription != null) {
			builder.append(providerDescription.getName()+"("+providerDescription.getAid()+")");
		} else {
		  builder.append(providerDescription);
		}
		builder.append("\n scope=").append(scope);
		builder.append("\n");
		
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
			for (final Iterator<String> iter = list.iterator(); iter.hasNext();) {
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

	/**
	 * {@inheritDoc}
	 */
	public final ActionScope getScope() {
		return scope;
	}

	/**
	 * Sets the scope of this action. Usually, when the scope has been
	 * changed, it is not a bad idea to notify the directory about the
	 * change.
	 * 
	 * @param newScope the scope of this action.
	 * @see ActionScope
	 */
	public final void setScope(ActionScope newScope) {
		scope = newScope;
	}

	private String[] getItemNames() {
		return new String[] {
	    		ITEMNAME_NAME, 
	    		ITEMNAME_INPUTTYPES, 
	    		ITEMNAME_RESULTTYPES, 
	    		ITEMNAME_SCOPE, 
	    		ITEMNAME_BEAN, 
	    		ITEMNAME_AGENT
	    };
	}

	   /**
	    * Gets the type of JIAC action descriptions based on JMX open types.
	    * 
	    * @return A composite type containing action name, input types, result types, scope, provider bean, and provider agent.
	    * @throws OpenDataException
	    *             if an error occurs during the creation of the type.
	    * @see javax.management.openmbean.CompositeType
	    */
	   public OpenType<?> getDescriptionType() throws OpenDataException {
	      final OpenType<?>[] itemTypes = new OpenType<?>[] {
	    		  SimpleType.STRING, 
	    		  new ArrayType<SimpleType<String>>(SimpleType.STRING, false), 
	    		  new ArrayType<SimpleType<String>>(SimpleType.STRING, false), 
	    		  SimpleType.STRING, 
	    		  SimpleType.STRING,
	    		  (providerDescription != null)? providerDescription.getDescriptionType():SimpleType.VOID,
	      };

	      // use names of action items as their description
	      final String[] itemDescriptions = getItemNames();

	      // create and return open type of a JIAC action
	      return new CompositeType(this.getClass().getName(), "standard JIAC-TNG action", getItemNames(), itemDescriptions, itemTypes);
	   }

	   /**
	    * Gets the description of this JIAC action based on JMX open types.
	    * 
	    * @return Composite data containing action name, input types, result types, scope, provider bean, and provider agent.
	    * @throws OpenDataException
	    *             if an error occurs during the creation of the data.
	    * @see javax.management.openmbean.CompositeData
	    */
	   public Object getDescription() throws OpenDataException {
	      final Object[] itemValues = new Object[] {
	    		  name,
	    		  inputTypeNames.toArray(new String[resultTypeNames.size()]),
	    		  resultTypeNames.toArray(new String[resultTypeNames.size()]),
	    		  (scope != null)? scope.toString():null,
	    		  (providerBean != null)? providerBean.getBeanName():null,
	    		  (providerDescription != null)? providerDescription.getDescription():null
	      };

	      final CompositeType type = (CompositeType) getDescriptionType();
	      return new CompositeDataSupport(type, getItemNames(), itemValues);
	   }
}
