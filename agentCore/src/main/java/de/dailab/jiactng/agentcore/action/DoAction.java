package de.dailab.jiactng.agentcore.action;

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
    private final static Object[] EMPTY_OBJECTS= new Object[0];

	/** The input-parameters for the action-call */
	private Object[] params;

	private String owner;
	
	/**
	 * Constructor for a new action-call. The created object should be written
	 * to the agents memory to trigger the action execution. Note that it may be
	 * usefull to store the session object from this object after creation, as it
	 * will be your only method of retrieving the results of the action-call.
	 * 
	 * @param thisAction
	 *            the action that shall be called.
	 * @param source
	 *            the source of the action-call.
	 * @param params
	 *            the input-parameters for the call.
	 */
	public DoAction(Action thisAction, ResultReceiver source, Object[] params) {
		this(new Session(source), thisAction, source, params);
	}

	/**
	 * Constructor for a new action-call. The created object should be written
	 * to the agents memory to trigger the action execution. Note that it may be
	 * usefull to store the session object from this object after creation, as it
	 * will be your only method of retrieving the results of the action-call.
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
	public DoAction(Session session, Action thisAction, ResultReceiver source, Object[] params) {
		super(session, thisAction, source);
		if (session != null) session.addToSessionHistory(this);
		setParams(params);
	}
	/**
	 * Getter for the input-parameters of the action-call.
	 * 
	 * @return an array containing the parameters for the action-call.
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * @param params the params to set
	 */
	public void setParams(Object[] params) {
		this.params = params == null ? EMPTY_OBJECTS : params;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
    
    public String typeCheck() {
        Class[] types= getAction().getParameters();
        
        if(types.length != params.length) {
            return "type length is '" + types.length + "' but param length is '" + params.length + "'";
        }
        
        for(int i= 0; i < types.length; ++i) {
            if(!types[i].isInstance(params[i])) {
                return "param" + i + " '" + params[i] + "' mismatch the type '" + types[i] + "'";
            }
        }
        
        return null;
    }
}
