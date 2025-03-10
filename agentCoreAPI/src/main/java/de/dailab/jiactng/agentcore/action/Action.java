/*
 * Created on 27.02.2007
 */
package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	/** the names of the input parameters of this action */
	private List<String> inputNames;
	
	/** The class names of the input-parameters of this action */
	private List<String> inputTypeNames;

	/** The class names of the results of this action */
	private List<String> resultTypeNames;

	private IAgentDescription providerDescription;

	/** The scope of the action, i.e. which agent will it know or can it use. */
	private ActionScope scope;
	
	/** The IRI link to a semantic service description 	 */
	private String semanticServiceDescriptionIRI;
	
	/** tags (labels, categories) of this action, used for matching, optional */
	private List<String> tags;
	
	/** human readable description of this action, optional */
	private String documentation;

	/**
	 * The type of this action. This field is designed to store information about the type of the action. By default,
	 * this field is null. If the provider publishes distinct actions, this field will be set by the provider.
	 */
	private String actionType = null;

	/**
	 * This constructor is used to create an action template
	 */
	public Action() {
		this(null, null, null, null, null);
	}

	/**
	 * This constructor is used to create an action template with only the name specified.
	 * 
	 * @param name
	 *           the name of the action template
	 */
	public Action(final String name) {
		this(name, null, null, null, null);
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
    public Action(final String name, final IEffector providerBean, final List<Class<?>> inputTypes,
            final List<Class<?>> resultTypes, final ActionScope scope) {
        this.setName(name);
        this.setProviderBean(providerBean);
        this.setInputTypes(inputTypes);
        if (inputTypes != null) {
            this.inputTypeNames = new ArrayList<>();
            for (Class<?> type : inputTypes) {
                this.inputTypeNames.add(type.getName());
            }
        }
        this.setResultTypes(resultTypes);
        if (resultTypes != null) {
            this.resultTypeNames = new ArrayList<>();
            for (Class<?> type : resultTypes) {
                this.resultTypeNames.add(type.getName());
            }
        }
        this.setScope(scope);
    }

	/**
	 * Copying constructor
	 * 
	 * @param action
	 *           the action to create an action from
	 */
	public Action(final IActionDescription action) {
		this.name = action.getName();
		this.providerBean = action.getProviderBean();
		this.scope = action.getScope();
		this.actionType = action.getActionType();
		this.providerDescription = action.getProviderDescription();
		this.semanticServiceDescriptionIRI = action.getSemanticServiceDescriptionIRI();
		this.documentation = action.getDocumentation();

		// we can exchange references here, because the lists are immutable
		this.inputTypeNames = action.getInputTypeNames();
		this.resultTypeNames = action.getResultTypeNames();
		this.inputNames = action.getInputNames();
		this.tags = action.getTags();
	}

	/**
	 * Creates an action description from JMX composite data.
	 * 
	 * @param descr
	 *           the action description based on JMX open types.
	 */
	public Action(final CompositeData descr) {
		this.name = (String) descr.get(IActionDescription.ITEMNAME_NAME);
		this.inputNames = Arrays.asList((String[]) descr.get(IActionDescription.ITEMNAME_INPUTNAMES));
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
		this.semanticServiceDescriptionIRI = (String) descr.get(IActionDescription.ITEMNAME_SEMURI);
		this.tags = Arrays.asList((String[]) descr.get(IActionDescription.ITEMNAME_TAGS));
		this.documentation = (String) descr.get(IActionDescription.ITEMNAME_DOC);
		this.actionType = (String) descr.get(IActionDescription.ITEMNAME_TYPE);
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
		return new DoAction(this, source, newParams, timeToLive);
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
	public final List<String> getInputNames() {
		return this.inputNames;
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
			final List<Class<?>> list = new ArrayList<>();
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
	public String getSemanticServiceDescriptionIRI() {
		return this.semanticServiceDescriptionIRI;
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
			final List<Class<?>> list = new ArrayList<>();
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
	 * {@inheritDoc}
	 */
	public final void setInputNames(List<String> newInputNames) {
		this.inputNames = newInputNames != null
				? Collections.unmodifiableList(new ArrayList<>(newInputNames))
				: null;
	}
	
	/**
	 * Sets the input types of this action.
	 * 
	 * @param newInputTypeNames
	 *           the parameters to set
	 */
	public final void setInputTypeNames(final List<String> newInputTypeNames) {
		if (newInputTypeNames != null) {
			final List<String> copy = new ArrayList<>();
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
			final List<String> copy = new ArrayList<>();
			for (final Class<?> type : newInputTypes) {
				copy.add(type.getName());
			}
			this.inputTypeNames = Collections.unmodifiableList(copy);
		}
		else {
			this.inputTypeNames = null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final void setTags(List<String> tags) {
		if (tags != null && ! tags.isEmpty()) {
			this.tags = Collections.unmodifiableList(new ArrayList<>(tags));
		} else {
			this.tags = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getTags() {
		return this.tags;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDocumentation() {
		return this.documentation;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated Inconsistent name of setter; problems with TupleSpace/GetterSetterFinder, use {@link #setSemanticServiceDescriptionIRI(String)} instead
	 */
	@Deprecated
	public void setSemanticServiceDescriptionURI(String iri) {
		this.semanticServiceDescriptionIRI = iri;
		
	}

	public void setSemanticServiceDescriptionIRI(String iri) {
		this.semanticServiceDescriptionIRI = iri;
		
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
			final List<String> copy = new ArrayList<>();
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
			final List<String> copy = new ArrayList<>();
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
		int hash = Action.class.hashCode();
		hash ^= name != null ? name.hashCode() : 0;
		return hash;
	}

	/**
	 * Checks the equality of two actions. The actions are equal if their 
	 * provider, names, input types, and result types are equal.
	 * 
	 * @param obj
	 *           the other action
	 * @return the result of the equality check
	 * @see EqualityChecker#equalsOrNull(Object, Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (! (obj instanceof Action)) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		final Action other = (Action) obj;

		final IAgentDescription myAgent = this.getProviderDescription();
		final IAgentDescription otherAgent = other.getProviderDescription();

		if ((myAgent != null) && (otherAgent != null)) {
			if (! EqualityChecker.equals(myAgent.getAid(), otherAgent.getAid())) {
				return false;
			}
		}

		return EqualityChecker.equals(this.getName(), other.getName())
		      && EqualityChecker.equals(this.getDocumentation(), other.getDocumentation())
		      && EqualityChecker.equals(this.getInputNames(), other.getInputNames())
		      && EqualityChecker.equals(this.getInputTypeNames(), other.getInputTypeNames())
		      && EqualityChecker.equals(this.getResultTypeNames(), other.getResultTypeNames())
		      && EqualityChecker.equals(this.getTags(), other.getTags())
		      && EqualityChecker.equals(this.getSemanticServiceDescriptionIRI(), other.getSemanticServiceDescriptionIRI())
		      && EqualityChecker.equals(this.getActionType(), other.getActionType());
	}
	
	@Override
	public boolean matches(final IActionDescription template) {
		if (this == template || template == null) {
			return true;
		}

		final IAgentDescription myAgent = this.getProviderDescription();
		if (myAgent != null && ! myAgent.matches(template.getProviderDescription())) {
			return false;
		}
		
		if (template.getTags() != null && ! template.getTags().isEmpty()) {
			if (this.getTags() == null || ! template.getTags().stream()
					.allMatch(t -> this.getTags().contains(t))) {
				return false;
			}
		}

		return EqualityChecker.equalsOrOtherNull(this.getName(), template.getName()) &&
				EqualityChecker.equalsOrOtherNull(this.getDocumentation(), template.getDocumentation()) &&
				EqualityChecker.equalsOrOtherNull(this.getInputNames(), template.getInputNames()) && 
				EqualityChecker.equalsOrOtherNull(this.getInputTypeNames(), template.getInputTypeNames()) && 
				EqualityChecker.equalsOrOtherNull(this.getResultTypeNames(), template.getResultTypeNames()) && 
				EqualityChecker.equalsOrOtherNull(this.getSemanticServiceDescriptionIRI(), template.getSemanticServiceDescriptionIRI()) &&
				EqualityChecker.equalsOrOtherNull(this.getActionType(), template.getActionType());
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
		builder.append("\n paramNames=").append(this.inputNames);
		builder.append("\n parameters=").append(this.inputTypeNames);
		builder.append("\n results=").append(this.resultTypeNames);
		builder.append("\n bean=");
		builder.append(this.providerBean);

		builder.append("\n provider=");
		if (this.providerDescription != null) {
			builder.append(this.providerDescription.getName()).append("(").append(this.providerDescription.getAid()).append(")");
		}
		else {
			builder.append(this.providerDescription);
		}
		builder.append("\n scope=").append(this.scope);
		builder.append("\n IRI=").append(this.semanticServiceDescriptionIRI);
		builder.append("\n tags=").append(this.tags);
		builder.append("\n documentation=").append(this.documentation);
		builder.append("\n action type=").append(this.actionType);
		builder.append("\n");

		return builder.toString();
	}

	private Class<?> getClassForName(final String type) throws ClassNotFoundException {
        switch (type) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
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

	/**
	 * Gets the type of JIAC action descriptions based on JMX open types.
	 * 
	 * @return A composite type containing action name, input names, input types, result types, scope, provider bean, provider
	 *         agent, semantic URI, tags, documentation, and action type.
	 * @throws OpenDataException
	 *            if an error occurs during the creation of the type.
	 * @see javax.management.openmbean.CompositeType
	 */
	public OpenType<?> getDescriptionType() throws OpenDataException {
		final OpenType<?>[] itemTypes = new OpenType<?>[] {
			SimpleType.STRING, 
			new ArrayType<SimpleType<String>>(SimpleType.STRING, false),
			new ArrayType<SimpleType<String>>(SimpleType.STRING, false),
			new ArrayType<SimpleType<String>>(SimpleType.STRING, false), 
			SimpleType.STRING, 
			SimpleType.STRING,
			(this.providerDescription != null) ? this.providerDescription.getDescriptionType() : SimpleType.VOID,
			SimpleType.STRING,
			new ArrayType<SimpleType<String>>(SimpleType.STRING, false), 
			SimpleType.STRING,
			SimpleType.STRING
		};

		// use names of action items as their description
		final String[] itemDescriptions = IActionDescription.getItemNames();

		// create and return open type of a JIAC action
		return new CompositeType(this.getClass().getName(), "standard JIAC-TNG action", IActionDescription.getItemNames(), itemDescriptions, itemTypes);
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
		final Object[] itemValues = new Object[] { 
			name, 
			inputNames != null ? inputNames.toArray(new String[inputNames.size()]) : null,
			inputTypeNames != null ? inputTypeNames.toArray(new String[inputTypeNames.size()]) : null,
			resultTypeNames != null ? resultTypeNames.toArray(new String[resultTypeNames.size()]) : null, 
			scope != null ? scope.toString() : null,
			providerBean != null ? providerBean.getBeanName() : null,
			providerDescription != null ? providerDescription.getDescription() : null,
			semanticServiceDescriptionIRI,
			tags != null ? tags.toArray(new String[tags.size()]) : null,
			documentation,
			actionType
		};

		final CompositeType type = (CompositeType) this.getDescriptionType();
		return new CompositeDataSupport(type, IActionDescription.getItemNames(), itemValues);
	}
}
