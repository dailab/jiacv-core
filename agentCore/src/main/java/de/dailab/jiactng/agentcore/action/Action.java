/*
 * Created on 27.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore.action;

import java.util.Arrays;

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
	/** SerialVersionUID for Serialization */
	private static final long serialVersionUID = 6644586804009024924L;

	private final static Class[] EMPTY_CLASSES= new Class[0];
    
	/** the name of the action */
	private String name;

	/** The component that holds the funtionality for this action */
	private transient IEffector providerBean;

	/** The classes of the input-parameters of this action */
	private Class[] parameters;

	/** The classes of the results of this action */
	private Class[] results;

    /**
     * This constructor is used to create an action template
     */
    public Action() {
        this(null, null, null, null);
    }
    
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
        setParameters(parameters);
		setResults(results);
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
	 * @param results
	 *            the results that come from executing the action.
	 * @param source
	 *            the entity that created the results of the action (usually the
	 *            providing component)
	 * @return a new ActionResult-object that can be used (by writing it to the
	 *         memory) to return the results of the action.
	 */
	public ActionResult createActionResult(Session resultOf, Object[] results,
			DoAction source) {
		ActionResult ret = new ActionResult(this, resultOf, results, source);
		ret.setMetaData(source.getMetaData());
		return ret;
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
		this.parameters = parameters == null ? EMPTY_CLASSES : parameters;
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
		this.results = results == null ? EMPTY_CLASSES : results;
	}

    /**
     * {@inheritDoc}
     */
	@Override
    public int hashCode() {
        int hash= name == null ? 0 : name.hashCode();
        hash ^= Arrays.hashCode(parameters);
        hash ^= Arrays.hashCode(results);
        return hash;
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Action)) {
            return false;
        }
        
        Action other= (Action) obj;
        return  name != null && other.name != null && name.equals(other.name) &&
                Arrays.equals(parameters, other.parameters) &&
                Arrays.equals(results, other.results);
    }

    /**
     * {@inheritDoc}
     */
	@Override
    public String toString() {
        StringBuilder builder= new StringBuilder();
        builder.append("Action:\n name='").append(name).append("'");
        builder.append("\n parameters=");
        prettyPrintArray(builder, parameters);
        builder.append("\n results=");
        prettyPrintArray(builder, results);
        builder.append('\n');
        return builder.toString();
    }
    
	/*
	 * Utility-method for a nicely formatted output
	 */
    private void prettyPrintArray(StringBuilder builder, Class[] array) {
        builder.append('[');
        int last= array.length - 1;
        for(int i= 0; i <= last; ++i) {
            builder.append(array[i].getName());
            
            if(i < last) {
                builder.append("; ");
            }
        }
        
        builder.append(']');
    }
}
