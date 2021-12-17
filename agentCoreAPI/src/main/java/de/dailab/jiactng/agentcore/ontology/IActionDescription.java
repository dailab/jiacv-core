/*
 * $Id: IActionDescription.java 22434 2009-03-13 10:10:59Z axle $ 
 */
package de.dailab.jiactng.agentcore.ontology;

import java.io.Serializable;
import java.util.List;

import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.management.jmx.JmxDescriptionSupport;

/**
 * This interface specifies a generic action description.
 * 
 * @author Marcel Patzlaff
 * @version $Revision: 22434 $
 */
public interface IActionDescription extends IFact, JmxDescriptionSupport {

	/** Item name which can be used to get the name of the JMX-based action description. */
	String ITEMNAME_NAME = "name";

	/** Item name which can be used to get the input names of the JMX-based action description. */
	String ITEMNAME_INPUTNAMES = "input names";

	/** Item name which can be used to get the input types of the JMX-based action description. */
	String ITEMNAME_INPUTTYPES = "input types";

	/** Item name which can be used to get the result types of the JMX-based action description. */
	String ITEMNAME_RESULTTYPES = "result types";

	/** Item name which can be used to get the scope of the JMX-based action description. */
	String ITEMNAME_SCOPE = "scope";

	/** Item name which can be used to get the providing agent bean of the JMX-based action description. */
	String ITEMNAME_BEAN = "bean";

	/** Item name which can be used to get the providing agent of the JMX-based action description. */
	String ITEMNAME_AGENT = "agent";
	
	/** Item name which can be used to get the semantic service description URI of the JMX-based action description. */
	String ITEMNAME_SEMURI = "semuri";

	/** Item name which can be used to get the tags of the JMX-based action description. */
	String ITEMNAME_TAGS = "tags";

	/** Item name which can be used to get the documentation of the JMX-based action description. */
	String ITEMNAME_DOC = "documentation";

	/** Item name which can be used to get the type of the JMX-based action description. */
	String ITEMNAME_TYPE = "action type";

	/**
	 * Gets the list of item names of a JMX-based action description.
	 * @return the list if item names
	 */
	static String[] getItemNames() {
		return new String[] { IActionDescription.ITEMNAME_NAME, IActionDescription.ITEMNAME_INPUTNAMES, 
			  IActionDescription.ITEMNAME_INPUTTYPES, IActionDescription.ITEMNAME_RESULTTYPES, IActionDescription.ITEMNAME_SCOPE, 
			  IActionDescription.ITEMNAME_BEAN, IActionDescription.ITEMNAME_AGENT, IActionDescription.ITEMNAME_SEMURI,
		      IActionDescription.ITEMNAME_TAGS, IActionDescription.ITEMNAME_DOC, IActionDescription.ITEMNAME_TYPE};
	}

	/**
	 * This method returns the name of the action this description refers to.
	 * 
	 * @return the name or <code>null</code> if not set
	 */
	String getName();
	
	/**
	 * Sets the name of the action.
	 * 
	 * @param newName the name of the action this description refers to
	 */
	void setName(String newName);

	/**
	 * This method returns the names of the input parameters that the action accepts.
	 *
	 * Note: Actual input parameter names are only available when compiled with Java 8
	 * with compiler flag '-parameters'. Otherwise, names are 'arg0', 'arg1', ....
	 *
	 * @return the unmodifiable list of input parameter names, or null if not set
	 */
	List<String> getInputNames();
	
	/**
	 * Sets the names of the input parameters that the action accepts
	 *
	 * @param newInputNames	list of new input parameter names
	 */
	void setInputNames(List<String> newInputNames);
	
	/**
	 * This method returns the type names of the input parameters that the action accepts.
	 * 
	 * <p>
	 * The type list cannot be modified.
	 * </p>
	 * 
	 * @see java.util.Collections#unmodifiableList(List)
	 * @return the unmodifiable list of input parameter type names or <code>null</code> if not set
	 */
	List<String> getInputTypeNames();
	
	/**
	 * Sets the type names of the input parameters that the action accepts
	 * 
	 * @param newInputTypeNames an unmodifiable list of input parameter type names
	 */
	void setInputTypeNames(List<String> newInputTypeNames);

	/**
	 * This method returns the result type names of the described action.
	 * 
	 * <p>
	 * The type list cannot be modified.
	 * </p>
	 * 
	 * @see java.util.Collections#unmodifiableList(List)
	 * @return the unmodifiable list of result type names or <code>null</code> if not set
	 */
	List<String> getResultTypeNames();
	
	/**
	 * Sets the type names of the result parameters that the action accepts
	 * 
	 * @param newResultTypeNames an unmodifiable list of result parameter type names
	 */
	void setResultTypeNames(List<String> newResultTypeNames);

	/**
	 * This method returns the types of the input parameters that the action accepts.
	 * 
	 * @return a created list of input parameter types or <code>null</code> if corresponding list of parameter type names
	 *         is not set
	 * @throws ClassNotFoundException
	 *            if one of the classes is unknown.
	 */
	List<Class<?>> getInputTypes() throws ClassNotFoundException;

	/**
	 * Sets the types of the input parameters that the action accepts
	 * 
	 * @param newInputTypes a created list of input parameter types or <code>null</code> if corresponding list of parameter type names
	 *         is not set
	 */
	void setInputTypes(List<Class<?>> newInputTypes);
	
	/**
	 * This method returns the result types of the described action.
	 * 
	 * @return a created list of result types or <code>null</code> if corresponding list of result type names is not set
	 * @throws ClassNotFoundException
	 *            if one of the classes is unknown.
	 */
	List<Class<?>> getResultTypes() throws ClassNotFoundException;
	
	/**
	 * Sets the return types of the described action
	 * 
	 * @param newResultTypes a created list of result types or <code>null</code> if corresponding list of result type names is not set
	 */
	void setResultTypes(final List<Class<?>> newResultTypes);

	/**
	 * This method returns the agent description of the agent that provides the action.
	 * 
	 * @return the agent description of the action provider or <code>null</code> if not set
	 */
	IAgentDescription getProviderDescription();

	/**
	 * Return the scope of the action.
	 * 
	 * @return the scope of the action
	 * @see ActionScope
	 */
	ActionScope getScope();
	
	/**
	 * Sets the scope of the action
	 * 
	 * @param newScope scope of the action
	 * @see ActionScope
	 */
	void setScope(ActionScope newScope);

	/**
	 * Getter for the component that holds the functionality of this action.
	 * 
	 * @return a life-reference to the component.
	 */
	IEffector getProviderBean();

	/**
	 * Returns the type of this action. If the provider publishes an action based on a distinct technology, this method
	 * returns a string representation of the applied technology, otherwise this method returns <code>null</code>.
	 * 
	 * @return a string representation of the applied technology on which the action implementation is based on.
	 */
	String getActionType();
	
	/**
	 * Returns a String which holds an IRI to a semantic service description. Actually, this service description should be
	 * an OWL-S conform description. This attribute is not mandatory. 
	 * 
	 * @return
	 */
	String getSemanticServiceDescriptionIRI();

	/**
	 * Sets the type of this action. If the provider publishes an action based on a distinct technology, the provider
	 * could set a string representation of the applied technology.
	 * 
	 * @param type
	 *           a string representation of the applied technology on which the action implementation is based on.
	 */
	void setActionType(String type);
	
	
	/**
	 * Sets the IRI link to the semantic service description of the respective action.
	 * 
	 * @param iri a string representation of the IRI to a semantic service description 
	 */
	void setSemanticServiceDescriptionURI(String iri);

	/**
	 * Set the tags (labels, categories) of this action.
	 * 
	 * @param tags	list of tags for this action
	 */
	void setTags(List<String> tags);
	
	/**
	 * Get the tags (labels, categories) of this action.
	 * 
	 * @return	list of tags of this action
	 */
	List<String> getTags();
	
	/**
	 * Set the documentation of this action.
	 * 
	 * @param documentation	new documentation
	 */
	void setDocumentation(String documentation);
	
	/**
	 * Get the documentation of this action.
	 * 
	 * @return	current documentation
	 */
	String getDocumentation();
	
	/**
	 * Sets the agent bean which provides this action.
	 * 
	 * @param newProviderBean
	 *           the providerBean to set
	 */
	void setProviderBean(IEffector newProviderBean);

	/**
	 * Sets the description of the agent which provides this action.
	 * 
	 * @param newProviderDescription
	 *           description of the providing agent
	 */
	void setProviderDescription(IAgentDescription newProviderDescription);

	/**
	 * Creates a new DoAction-object for this action. The resulting object can be written to the memory to trigger the
	 * action.
	 * 
	 * @param newParams
	 *           the input-parameters that should be used when executing the action.
	 * @param source
	 *           the caller of the action.
	 * @return a new DoAction-object that can be used (by writing it to the memory) to call the action.
	 */
	DoAction createDoAction(Serializable[] newParams, ResultReceiver source);

	/**
	 * Creates a new DoAction-object for this action. The resulting object can be written to the memory to trigger the
	 * action.
	 * 
	 * @param newParams
	 *           the input-parameters that should be used when executing the action.
	 * @param source
	 *           the caller of the action.
	 * @param timeToLive
	 *           timeout of the action request in milliseconds.
	 * @return a new DoAction-object that can be used (by writing it to the memory) to call the action.
	 */
	DoAction createDoAction(Serializable[] newParams, ResultReceiver source, Long timeToLive);

	/**
	 * Creates a new DoAction-object for this action. The resulting object can be written to the memory to trigger the
	 * action.
	 * 
	 * @param parent
	 *           the session which creates this doAction.
	 * @param newParams
	 *           the input-parameters that should be used when executing the action.
	 * @param source
	 *           the caller of the action.
	 * @return a new DoAction-object that can be used (by writing it to the memory) to call the action.
	 */
	DoAction createDoAction(Session parent, Serializable[] newParams, ResultReceiver source);
	

	/**
	 * This method is supposed to be used instead of equals in DirectoryAgentNodeBean.
	 * An action matches a template if all the attributes in the template are either 
	 * equal to those in the action, or null.
	 * 
	 * @param template	the other action (the template)
	 * @return			whether this action matches the template
	 */
	boolean matches(IActionDescription template);
}
