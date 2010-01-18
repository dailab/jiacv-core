package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * Used to submit an action for execution. An agent will check its memory
 * periodically for DoAction-objects, an it will call the approriate component
 * if such an object is encountered. Note that this class is also a subclass of
 * Session to make sure executed actions can be identified and tracked.
 * 
 * @see de.dailab.jiactng.agentcore.action.Action
 * @see de.dailab.jiactng.agentcore.action.Session
 * @see de.dailab.jiactng.agentcore.environment.IEffector
 * @author Thomas Konnerth
 * 
 */
public class DoAction extends SessionEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8741204284564770003L;

	private final static Serializable[] EMPTY_OBJECTS = new Serializable[0];

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
	public DoAction(Action thisAction, ResultReceiver source, Serializable[] params) {
		this(new Session(source), thisAction, source, params);
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
	public DoAction(Action thisAction, ResultReceiver source, Serializable[] params, long timeToLive) {
		this(new Session(source, timeToLive), thisAction, source, params);
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
	public DoAction(Session session, Action thisAction, ResultReceiver source,
			Serializable[] params) {
		super(session, thisAction, source);
		setParams(params);
	}
	
	
	/**
	 * Getter for the input-parameters of the action-call.
	 * 
	 * @return an array containing the parameters for the action-call.
	 */
	public Serializable[] getParams() {
		return Arrays.copyOf(params, params.length);
	}

	/**
	 * Setter for the parameters of the Trigger.
	 * 
	 * @param params
	 *            the params to set
	 */
	public void setParams(Serializable[] params) {
		this.params = params == null ? EMPTY_OBJECTS : params;
	}

	/**
	 * Getter for the owner of this action-invocation.
	 * 
	 * @return the name of the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * Setter for the owner of this action-invocation.
	 * 
	 * @param owner
	 *            the name of the user that initiated this action.
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * Utility-method for typechecking of this trigger and it's action
	 * 
	 * @return null, if the type declaration of the corresponding action and the
	 *         parameters of this trigger are compatible, or a String describing
	 *         the mismatch otherwise.
	 */
	public String typeCheck() {
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
	   * {@inheritDoc}
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
	    sb.append("params="+Arrays.asList(params));
	    sb.append(")");
	    return sb.toString();
	}
}
