package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class AuthorizedActionCallerBean extends AbstractAgentBean {

	public String invoke(String actionName, String user, ResultReceiver receiver) {
		final String userToken = (user == null)? "unknownToken" : AuthorizationBean.tokens.get(user); 
	    final IActionDescription action = thisAgent.searchAction(new Action(actionName));
	    final DoAction doAct = action.createDoAction(new Serializable[] {}, receiver);
	    doAct.getSession().setUserToken(userToken);
	    memory.write(doAct);
	    return doAct.getSessionId();
	}
}
