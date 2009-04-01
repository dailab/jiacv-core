package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.comm.wp.DirectoryAccessBean;
import de.dailab.jiactng.agentcore.environment.IEffector;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * Abstract class of all agentbeans, which are able to authorize action invocations. 
 * The class allows to configure an optional authorization action, which will be executed at the beginning
 * of all action invocations. The invoked action will only be performed if this authorization action 
 * sends a success.
 * 
 * @author Jan Keiser
 */
public abstract class AbstractActionAuthorizationBean extends AbstractAgentBean
		implements IEffector, ResultReceiver, AbstractActionAuthorizationBeanMBean {

	/**
	 * Name of action to be used for authorization.
	 */
	private String authorizationActionName = null;

	/**
	 * Action to be used for authorization.
	 */
	private Action authorizationAction = null;

	/**
	 * Map of running authorization invocations.
	 */
	private HashMap<String, DoAction> _doActionAuthorizations = new HashMap<String, DoAction>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doStart() throws Exception {
		super.doStart();

    List<IActionDescription> foundActs = thisAgent.searchAllActions(new Action(authorizationActionName));
    if((foundActs != null)&& (foundActs.size()>=1)) {
      authorizationAction = (Action)foundActs.get(0);
    } else {
      invokeActionSearch(new Action(authorizationActionName), false, 0, this);
    }
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doStop() throws Exception {
		super.doStop();

		authorizationAction = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAuthorizationActionName() {
		return authorizationActionName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAuthorizationActionName(String authorizationActionName) {
		this.authorizationActionName = authorizationActionName;
		authorizationAction = null;

//		// search for authorization action
//		if ((authorizationActionName != null) && getState().equals(LifecycleStates.STARTED)) {
//			invokeActionSearch(authorizationActionName, false, 0, this);
//		}
		
		if((thisAgent!=null)&& (LifecycleStates.STARTED.equals(thisAgent.getState()))) {
		  List<IActionDescription> foundActs = thisAgent.searchAllActions(new Action(authorizationActionName));
		  log.error("FOUND: "+foundActs);
		  if((foundActs != null)&& (foundActs.size()>=1)) {
		    authorizationAction = (Action)foundActs.get(0);
		  } else {
		    invokeActionSearch(new Action(authorizationActionName), false, 0, this);
		  }
		}
		
	}

	/**
	 * Get the action to be used for authorization.
	 * @return the action to be used for authorization.
	 */
	protected Action getAuthorizationAction() {
		return authorizationAction;
	}

	/**
	 * {@inheritDoc}
	 */
	public void receiveResult(ActionResult result) {
		// handle result of search for authorization action
		if (result.getAction().getName().equals(DirectoryAccessBean.ACTION_REQUEST_SEARCH)) {
			Serializable[] results = result.getResults();
			if (results != null) {
				if (results.length == 1) {
					try {
						List<Action> actions = (List<Action>) results[0];
						if (actions.isEmpty()) {
							log.warn("Found no action");
						}
						else if (actions.get(0).getName().equals(authorizationActionName)) {
							authorizationAction = actions.get(0);
						}
					} catch (ClassCastException e) {
						log.error("Got wrong type of search results");
					}
				} else {
					log.error("Got wrong number of search results");
				}
			}
			else {
				log.error("Search for action failed");
			}
		}

		// handle result of authorization
		DoAction doAction = _doActionAuthorizations.remove(result.getSessionId());
		if (doAction != null) {
			Serializable[] results = result.getResults();
			if (results != null) {
				if (results.length == 1) {
					try {
						if ((String) results[0] !=null) {
							// authorization successful => invoke service
							try {
							  doAction.getSession().setOriginalUser((String) results[0]);
								doAction(doAction);
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
						else {
							// user is not authorized
							returnFailure(doAction, "Not authorized");
						}
					} catch (ClassCastException e) {
						log.error("Got wrong type of authorization results");
						returnFailure(doAction, "Unable to authorize");
					}
				} else {
					log.error("Got wrong number of authorization results");
					returnFailure(doAction, "Unable to authorize");
				}
			}
			else {
				log.error("Authorization action failed");
				returnFailure(doAction, "Unable to authorize");
			}
		}
	}

	/**
	 * Starts the authorization action if defined in property "authorizationActionName".
	 * @param doAction the action invocation for which the authorization will be executed.
	 * @throws Exception thrown by the invoked action without authorization.
	 */
	public final void authorize(DoAction doAction) throws Exception {
		if (authorizationActionName == null) {
			// no authorization => invoke action
			doAction(doAction);
		}
		else if (authorizationAction == null) {
			// authorization action not found
			returnFailure(doAction, "Unable to authorize");
		}
		else if ((doAction.getSession() == null) || (doAction.getSession().getUserToken() == null)) {
			// token of original user unknown
			returnFailure(doAction, "Unknown user token");
		}
		else {
			// start authorization
			String sessionId = invoke(authorizationAction, doAction.getSession(), new Serializable[] {doAction.getSession().getUserToken(), doAction.getAction()}, this);
			_doActionAuthorizations.put(sessionId, doAction);
		}
	}
}
