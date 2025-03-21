package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * Used to submit an action for execution. An agent will check its memory
 * periodically for DoAction-objects, an it will call the appropriate component
 * if such an object is encountered. Note that this class is also a subclass of
 * Session to make sure executed actions can be identified and tracked.
 * 
 * @see IActionDescription
 * @see Session
 * @author Thomas Konnerth
 * 
 */
public class DoAction extends SessionEvent<ResultReceiver> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8741204284564770003L;

	private static final Serializable[] EMPTY_OBJECTS = new Serializable[0];

	/** The input-parameters for the action-call */
	private Serializable[] params;

	/**
	 * The owner of this action/invocation / simple implementation of a
	 * user/tracking
	 * 
	 * TODO: needs real concept and implementation
	 */
	private String owner;

	/**
	 * Constructor for a new action-call. The created object should be written
	 * to the agents memory to trigger the action execution. Note that it may be
	 * usefull to store the session object from this object after creation, as
	 * it will be your only method of retrieving the results of the action-call.
	 * 
	 * @param thisAction
	 *            the action that shall be called.
	 * @param source
	 *            the source of the action-call.
	 * @param params
	 *            the input-parameters for the call.
	 */
	public DoAction(IActionDescription thisAction, ResultReceiver source, Serializable[] params) {
		this(new Session(), thisAction, source, params);
		
	}
	
	/**
	 * Constructor for a new action-call. The created object should be written
	 * to the agents memory to trigger the action execution. Note that it may be
	 * usefull to store the session object from this object after creation, as
	 * it will be your only method of retrieving the results of the action-call.
	 * 
	 * @param thisAction
	 *            the action that shall be called.
	 * @param source
	 *            the source of the action-call.
	 * @param params
	 *            the input-parameters for the call.
	 * @param timeToLive
	 * 			  time until timeout in milliseconds
	 */
	public DoAction(IActionDescription thisAction, ResultReceiver source, Serializable[] params, long timeToLive) {
		this(new Session(timeToLive), thisAction, source, params);
	}

	/**
	 * Constructor for a new action-call. The created object should be written
	 * to the agents memory to trigger the action execution. Note that it may be
	 * usefull to store the session object from this object after creation, as
	 * it will be your only method of retrieving the results of the action-call.
	 * 
	 * @param session
	 *            the session
	 * @param thisAction
	 *            the action that shall be called.
	 * @param source
	 *            the source of the action-call.
	 * @param params
	 *            the input-parameters for the call.
	 */
	public DoAction(Session session, IActionDescription thisAction, ResultReceiver source,
			Serializable[] params) {
		super(session, thisAction, source);
		setParams(params);
	}
	
	
	/**
	 * Getter for the input-parameters of the action-call.
	 * 
	 * @return an array containing the parameters for the action-call.
	 */
	public final Serializable[] getParams() {
		return Arrays.copyOf(params, params.length);
	}

	/**
	 * Setter for the parameters of the Trigger.
	 * 
	 * @param newParams
	 *            the params to set
	 */
	public final void setParams(Serializable[] newParams) {
		params = newParams == null ? EMPTY_OBJECTS : newParams;
	}

	/**
	 * Getter for the owner of this action-invocation.
	 * 
	 * @return the name of the owner
	 */
	public final String getOwner() {
		return owner;
	}

	/**
	 * Setter for the owner of this action-invocation.
	 * 
	 * @param newOwner
	 *            the name of the user that initiated this action.
	 */
	public final void setOwner(String newOwner) {
		owner = newOwner;
	}

	/**
	 * Utility-method for typechecking of this trigger and it's action
	 * 
	 * @return null, if the type declaration of the corresponding action and the
	 *         parameters of this trigger are compatible, or a String describing
	 *         the mismatch otherwise.
	 */
	public final String typeCheck() {
		List<Class<?>> types;
		try {
			types = getAction().getInputTypes();
		}
		catch (ClassNotFoundException e) {
			return e.toString();
		}

		if (types.size() != params.length) {
			return "type length is '" + types.size()
					+ "' but param length is '" + params.length + "'";
		}

		for (int i = 0; i < types.size(); ++i) {
		    final Class<?> cit= types.get(i);
		    
		    if(cit.isPrimitive()) {
		        if(params[i] == null) {
                    return "param" + i + " '" + params[i] + "' mismatch the type '" + cit + "'";
                }
                
                final Class<?> cpt= params[i].getClass();
                if(
                        (cit == boolean.class && cpt != Boolean.class) ||
                        (cit == byte.class && cpt != Byte.class) ||
                        (cit == char.class && cpt != Character.class) ||
                        (cit == double.class && cpt != Double.class) ||
                        (cit == float.class && cpt != Float.class) ||
                        (cit == int.class && cpt != Integer.class) ||
                        (cit == long.class && cpt != Long.class) ||
                        (cit == short.class && cpt != Short.class)
                ) {
                    return "param" + i + " '" + params[i] + "' mismatch the type '" + cit + "'";
                }
		    } else if(!cit.isInstance(params[i])){
	            return "param" + i + " '" + params[i] + "' mismatch the type '" + cit + "'";
		    }
		}

		return null;
	}
	
	  /**
	   * Returns a single-line text which contains the owner, the action name and provider,
	   * and the parameters of the action request.
	   * @return a string representation of the action request
	   */
	@Override
	public String toString() {
	    final StringBuilder sb = new StringBuilder("DoAction(");
	    sb.append("owner="+owner+", ");
      if(getAction() != null) {
 	      sb.append("action="+getAction().getName()+", ");
	      sb.append("action_provider="+((getAction().getProviderDescription()!=null)?getAction().getProviderDescription().getName():null)+", ");
      } else {
        sb.append("action="+getAction()+", ");
      }
	    sb.append("params=").append(Arrays.asList(params));
	    sb.append(")");
	    return sb.toString();
	}
}
