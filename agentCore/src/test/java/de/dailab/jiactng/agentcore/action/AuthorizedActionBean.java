package de.dailab.jiactng.agentcore.action;

import de.dailab.jiactng.agentcore.action.scope.ActionScope;

public class AuthorizedActionBean extends AbstractMethodExposingBean {

	public static final String ACTION_EXAMPLE1 = "AuthorizedActionBean.example1";
	public static final String ACTION_EXAMPLE2 = "AuthorizedActionBean.example2";
	public static final String ACTION_EXAMPLE3 = "AuthorizedActionBean.example3";

    @Expose(name = ACTION_EXAMPLE1, scope=ActionScope.NODE)
	public String example1() {
    	return getOriginalUser();
	}

    @Expose(name = ACTION_EXAMPLE2, scope=ActionScope.NODE)
	public String example2() {
    	return getOriginalUser();
	}

    @Expose(name = ACTION_EXAMPLE3, scope=ActionScope.NODE)
	public String example3() {
    	return getOriginalUser();
	}

    private String getOriginalUser() {
    	// test for getting the correct session
    	// sleep 3 seconds at begin and end of action to ensure that multiple sessions exist
    	try {wait(3000);} catch (Exception e) {}
    	final String user = getSession().getOriginalUser();
    	try {wait(3000);} catch (Exception e) {}
		return user;
    }
}
