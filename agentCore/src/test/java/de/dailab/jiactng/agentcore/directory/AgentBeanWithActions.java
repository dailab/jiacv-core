package de.dailab.jiactng.agentcore.directory;

import java.util.List;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class AgentBeanWithActions extends AbstractMethodExposingBean {

	@Expose(scope=ActionScope.GLOBAL)
	public String echo(String s) {
		return s;
	}

	@Expose(scope=ActionScope.GLOBAL)
	public String mirror(String s) {
		StringBuffer tmp = new StringBuffer();
		for (int i = s.length() - 1; i >= 0; i--) {
			tmp.append(s.charAt(i));
		}
		return tmp.toString();
	}
	
	@Expose(scope=ActionScope.AGENT)
	public String concat(String s1, String s2) {
		return s1.concat(s2);
	}
	
	final String joinGroup = "de.dailab.jiactng.agentcore.comm.ICommunicationBean#joinGroup";
	@Expose(name=joinGroup,scope=ActionScope.AGENT)
	public void joinGroup(String group) {
		//join
	}
	
	public IActionDescription searchAction(String name) {
		return thisAgent.searchAction(new Action(name));
	}
	
	public List<IActionDescription> searchAllActions(String name) {
		return thisAgent.searchAllActions(new Action(name));
	}
}
