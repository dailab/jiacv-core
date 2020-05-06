package de.dailab.jiactng.agentcore.action;

import java.util.HashMap;
import java.util.Map;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class AuthorizationBean extends AbstractMethodExposingBean {

	public static Map<String,String> tokens = new HashMap<String,String>() {{
		put("user1","ae2f01");
		put("user2","ae2f02");
	}};

	@Expose(name="AuthorizationBean.authorize",scope=ActionScope.AGENT)
	public String authorize(String userToken, IActionDescription action) {
		// get name of user from token
		String user = null;
		for (Map.Entry<String,String> entry: tokens.entrySet()) {
			if (entry.getValue().equals(userToken)) {
				user = entry.getKey();
			}
		}

		// user1 is allowed to use example1
		if (action.getName().equals(AuthorizedActionBean.ACTION_EXAMPLE1)) {
			if ((user != null) && user.equals("user1")) {
				return user;
			} else {
				return null;
			}
		}
		// user2 is allowed to use example2
		else if (action.getName().equals(AuthorizedActionBean.ACTION_EXAMPLE2)) {
			if ((user != null) && user.equals("user2")) {
				return user;
			} else {
				return null;
			}

		}
		// example3 can also be used by unknown users
		return "";
	}

}
