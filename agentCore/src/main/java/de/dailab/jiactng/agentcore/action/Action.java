/*
 * Created on 27.02.2007
 */
package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;
import de.dailab.jiactng.agentcore.util.EqualityChecker;

/**
 * Describes an action that can be used within an agent. This is only the action-declaration.
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

	/** The component that holds the functionality for this action */
	private transient IEffector providerBean = null;

	/** The class names of the input-parameters of this action */
	private List<String> inputTypeNames;

	/** The class names of the results of this action */
	private List<String> resultTypeNames;

	private IAgentDescription providerDescription;

	/** The scope of the action, i.e. which agent will it know or can it use. */
	private ActionScope scope;

	/**
	 * The type of this action. This field is designed to store information about the type of the action. By default,
	 * this field is null. If the provider publishes distinct actions, this field will be set by the provider.
	 */
	private String actionType = null;

	/**
	 * This constructor is used to create an action template
	 */
	public Action() {
		this(null, null, (List<Class<?>>) null, (List<Class<?>>) null, null);
	}

	/**
	 * This constructor is used to create an action template with only the name specified.
	 * 
	 * @param name
	 *           the name of the action template
	 */
	public Action(final String name) {
		this(name, null, (List<Class<?>>) null, (List<Class<?>>) null, null);
	}

	/**
	 * Constructor. Creates a new action-declaration with agent internal scope.
	 * 
	 * @param name
	 *           the name of the action
	 * @param providerBean
	 *           the component that holds the functionality of this action
	 * @param inputTypes
	 *           the classes of the input-parameters of this action
	 * @param resultTypes
	 *           the classes of the results of this action
	 */
	public Action(final String name, final IEffector providerBean, final Class<?>[] inputTypes, final Class<?>[] resultTypes) {
		this(name, providerBean, inputTypes != null ? Arrays.asList(inputTypes) : null, resultTypes != null ? Arrays.asList(resultTypes)
		      : null);
	}

	/**
	 * Constructor. Creates a new action-declaration with agent internal scope.
	 * 
	 * @param name
	 *           the name of the action
	 * @param providerBean
	 *           the component that holds the functionality of this action
	 * @param inputTypes
	 *           the classes of the input-parameters of this action
	 * @param resultTypes
	 *           the classes of the results of this action
	 */
	public Action(final String name, final IEffector providerBean, final List<Class<?>> inputTypes, final List<Class<?>> resultTypes) {
		this(name, providerBean, inputTypes, resultTypes, ActionScope.AGENT);
	}

	/**
	 * Constructor. Creates a new action-declaration.
	 * 
	 * @param name
	 *           the name of the action
	 * @param providerBean
	 *           the component that holds the functionality of this action
	 * @param inputTypes
	 *           the classes of the input-parameters of this action
	 * @param resultTypes
	 *           the classes of the results of this action
	 * @param scope
	 *           the scope of this action
	 */
	public Action(final String name, final IEffector providerBean, final List<Class<?>> inputTypes, final List<Class<?>> resultTypes,
	      final ActionScope scope) {
		this.setName(name);
		this.setProviderBean(providerBean);
		this.setInputTypes(inputTypes);
		this.setResultTypes(resultTypes);
		this.setScope(scope);
	}

	/**
	 * Copying constructor
	 * 
	 * @param action
	 *           the action to create an action from
	 */
	public Action(final Action action) {
		this.name = action.getName();
		this.providerBean = action.getProviderBean();
		this.scope = action.getScope();

		// we can exchange references here, because the lists are immutable
		this.inputTypeNames = action.getInputTypeNames();
		this.resultTypeNames = action.getResultTypeNames();
	}

	/**
	 * Creates an action description from JMX composite data.
	 * 
	 * @param descr
	 *           the action description based on JMX open types.
	 */
	public Action(final CompositeData descr) {
		this.name = (String) descr.get(IActionDescription.ITEMNAME_NAME);
		this.inputTypeNames = Arrays.asList((String[]) descr.get(IActionDescription.ITEMNAME_INPUTTYPES));
		this.resultTypeNames = Arrays.asList((String[]) descr.get(IActionDescription.ITEMNAME_RESULTTYPES));
		final String actionScope = (String) descr.get(IActionDescription.ITEMNAME_SCOPE);
		if (actionScope != null) {
			this.scope = ActionScope.valueOf(actionScope);
		}
		final CompositeData provider = (CompositeData) descr.get(IActionDescription.ITEMNAME_AGENT);
		if (provider != null) {
			this.providerDescription = new AgentDescription(provider);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final DoAction createDoAction(final Serializable[] newParams, final ResultReceiver source) {
		return new DoAction(this, source, newParams);
	}

	/**
	 * {@inheritDoc}
	 */
	public final DoAction createDoAction(final Serializable[] newParams, final ResultReceiver source, final Long timeToLive) {
		return new DoAction(this, source, newParams, timeToLive.longValue());
	}

	/**
	 * {@inheritDoc}
	 */
	public final DoAction createDoAction(final Session parent, final Serializable[] newParams, final ResultReceiver source) {
		return new DoAction(parent, this, source, newParams);
	}

	/**
	 * Creates a new Result-object for this action. The resulting object can be written to the memory to return the
	 * results of the action. *
	 * 
	 * @param source
	 *           the entity that created the results of the action (usually the providing component)
	 * @param results
	 *           the results that come from executing the action.
	 * 
	 * @see de.dailab.jiactng.agentcore.action.ActionResult
	 * @return a new ActionResult-object that can be used (by writing it to the memory) to return the results of the
	 *         action.
	 */
	public final ActionResult createActionResult(final DoAction source, final Serializable[] results) {
		final ActionResult ret = new ActionResult(source, results);
		ret.setMetaData(source.getMetaData());
		return ret;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<String> getInputTypeNames() {
		return this.inputTypeNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<Class<?>> getInputTypes() throws ClassNotFoundException {
		if (this.inputTypeNames != null) {
			final List<Class<?>> list = new ArrayList<Class<?>>();
			for (final String type : this.inputTypeNames) {
				list.add(this.getClassForName(type));
			}
			return list;
		}
		else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final IEffector getProviderBean() {
		return this.providerBean;
	}

	/**
	 * {@inheritDoc}
	 */
	public final IAgentDescription getProviderDescription() {
		return this.providerDescription;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<String> getResultTypeNames() {
		return this.resultTypeNames;
	}

	/**
	 * {@inheritDoc}
	 */
	public final List<Class<?>> getResultTypes() throws ClassNotFoundException {
		if (this.resultTypeNames != null) {
			final List<Class<?>> list = new ArrayList<Class<?>>();
			for (final String type : this.resultTypeNames) {
				list.add(this.getClassForName(type));
			}
			return list;
		}
		else {
			return null;
		}
	}

	/**
	 * Sets the name of this action.
	 * 
	 * @param newName
	 *           the name to set
	 */
	public final void setName(final String newName) {
		this.name = newName;
	}

	/**
	 * Sets the input types of this action.
	 * 
	 * @param newInputTypeNames
	 *           the parameters to set
	 */
	public final void setInputTypeNames(final List<String> newInputTypeNames) {
		if (newInputTypeNames != null) {
			final List<String> copy = new ArrayList<String>();
			copy.addAll(newInputTypeNames);
			this.inputTypeNames = Collections.unmodifiableList(copy);
		}
		else {
			this.inputTypeNames = null;
		}
	}

	/**
	 * Sets the input types of this action.
	 * 
	 * @param newInputTypes
	 *           the parameters to set
	 */
	public final void setInputTypes(final List<Class<?>> newInputTypes) {
		if (newInputTypes != null) {
			final List<String> copy = new ArrayList<String>();
			for (final Class<?> type : newInputTypes) {
				copy.add(type.getName());
			}
			this.inputTypeNames = Collections.unmodifiableList(copy);
		}
		else {
			this.inputTypeNames = null;
		}
	}

	public String getActionType() {
		return this.actionType;
	}

	public void setActionType(final String actionType) {
		this.actionType = actionType;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setProviderBean(final IEffector newProviderBean) {
		this.providerBean = newProviderBean;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setProviderDescription(final IAgentDescription newProviderDescription) {
		this.providerDescription = newProviderDescription;
	}

	/**
	 * Sets the result types of this action.
	 * 
	 * @param newResultTypeNames
	 *           the results to set
	 */
	public final void setResultTypeNames(final List<String> newResultTypeNames) {
		if (newResultTypeNames != null) {
			final List<String> copy = new ArrayList<String>();
			copy.addAll(newResultTypeNames);
			this.resultTypeNames = Collections.unmodifiableList(copy);
		}
		else {
			this.resultTypeNames = null;
		}
	}

	/**
	 * Sets the result types of this action.
	 * 
	 * @param newResultTypes
	 *           the results to set
	 */
	public final void setResultTypes(final List<Class<?>> newResultTypes) {
		if (newResultTypes != null) {
			final List<String> copy = new ArrayList<String>();
			for (final Class<?> type : newResultTypes) {
				copy.add(type.getName());
			}
			this.resultTypeNames = Collections.unmodifiableList(copy);
		}
		else {
			this.resultTypeNames = null;
		}
	}

	/**
	 * Returns the hash code of the action class, thus it is the same hash code for all actions.
	 * 
	 * @return the hash code of the action class
	 */
	@Override
	public int hashCode() {
		final int hash = Action.class.hashCode();
		// hash ^= _name != null ? _name.hashCode() : 0;
		return hash;
	}

	/**
	 * Checks the equality of two actions. The actions are equal if their names, input types, and result types are equal
	 * or null. The actions are not equal if they are provided by different agents.
	 * 
	 * @param obj
	 *           the other action
	 * @return the result of the equality check
	 * @see EqualityChecker#equalsOrNull(Object, Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null || !(obj instanceof Action)) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		final Action other = (Action) obj;

		final IAgentDescription myAgent = this.getProviderDescription();
		final IAgentDescription otherAgent = other.getProviderDescription();

		if ((myAgent != null) && (otherAgent != null)) {
			if (!EqualityChecker.equalsOrNull(myAgent.getAid(), otherAgent.getAid())) {
				return false;
			}
		}

		return EqualityChecker.equalsOrNull(this.getName(), other.getName())
		      && EqualityChecker.equalsOrNull(this.getInputTypeNames(), other.getInputTypeNames())
		      && EqualityChecker.equalsOrNull(this.getResultTypeNames(), other.getResultTypeNames());
	}

	/**
	 * Returns a multiline text which contains the name, input types, result types, the provider bean, provider agent,
	 * and scope of the action.
	 * 
	 * @return a string representation of the action
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Action:\n name='").append(this.name).append("'");
		builder.append("\n parameters=");
		this.prettyPrintArray(builder, this.inputTypeNames);
		builder.append("\n results=");
		this.prettyPrintArray(builder, this.resultTypeNames);
		builder.append("\n bean=");
		builder.append(this.providerBean);

		builder.append("\n provider =");
		if (this.providerDescription != null) {
			builder.append(this.providerDescription.getName() + "(" + this.providerDescription.getAid() + ")");
		}
		else {
			builder.append(this.providerDescription);
		}
		builder.append("\n scope=").append(this.scope);
		builder.append("\n");

		return builder.toString();
	}

	/*
	 * Utility-method for a nicely formatted output
	 */
	private void prettyPrintArray(final StringBuilder builder, final List<String> list) {
		if (list == null) {
			builder.append("null");
		}
		else {
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

	private Class<?> getClassForName(final String type) throws ClassNotFoundException {
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
		return this.scope;
	}

	/**
	 * Sets the scope of this action. Usually, when the scope has been changed, it is not a bad idea to notify the
	 * directory about the change.
	 * 
	 * @param newScope
	 *           the scope of this action.
	 * @see ActionScope
	 */
	public final void setScope(final ActionScope newScope) {
		this.scope = newScope;
	}

	private String[] getItemNames() {
		return new String[] { IActionDescription.ITEMNAME_NAME, IActionDescription.ITEMNAME_INPUTTYPES,
		      IActionDescription.ITEMNAME_RESULTTYPES, IActionDescription.ITEMNAME_SCOPE, IActionDescription.ITEMNAME_BEAN,
		      IActionDescription.ITEMNAME_AGENT };
	}

	/**
	 * Gets the type of JIAC action descriptions based on JMX open types.
	 * 
	 * @return A composite type containing action name, input types, result types, scope, provider bean, and provider
	 *         agent.
	 * @throws OpenDataException
	 *            if an error occurs during the creation of the type.
	 * @see javax.management.openmbean.CompositeType
	 */
	public OpenType<?> getDescriptionType() throws OpenDataException {
		final OpenType<?>[] itemTypes = new OpenType<?>[] { SimpleType.STRING, new ArrayType<SimpleType<String>>(SimpleType.STRING, false),
		      new ArrayType<SimpleType<String>>(SimpleType.STRING, false), SimpleType.STRING, SimpleType.STRING,
		      (this.providerDescription != null) ? this.providerDescription.getDescriptionType() : SimpleType.VOID, };

		// use names of action items as their description
		final String[] itemDescriptions = this.getItemNames();

		// create and return open type of a JIAC action
		return new CompositeType(this.getClass().getName(), "standard JIAC-TNG action", this.getItemNames(), itemDescriptions, itemTypes);
	}

	/**
	 * Gets the description of this JIAC action based on JMX open types.
	 * 
	 * @return Composite data containing action name, input types, result types, scope, provider bean, and provider
	 *         agent.
	 * @throws OpenDataException
	 *            if an error occurs during the creation of the data.
	 * @see javax.management.openmbean.CompositeData
	 */
	public Object getDescription() throws OpenDataException {
		final Object[] itemValues = new Object[] { this.name, this.inputTypeNames.toArray(new String[this.resultTypeNames.size()]),
		      this.resultTypeNames.toArray(new String[this.resultTypeNames.size()]), (this.scope != null) ? this.scope.toString() : null,
		      (this.providerBean != null) ? this.providerBean.getBeanName() : null,
		      (this.providerDescription != null) ? this.providerDescription.getDescription() : null };

		final CompositeType type = (CompositeType) this.getDescriptionType();
		return new CompositeDataSupport(type, this.getItemNames(), itemValues);
	}
}
