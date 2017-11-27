package de.dailab.jiactng.agentcore.action;

import java.util.Arrays;

import de.dailab.jiactng.agentcore.action.scope.ActionScope;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * Test bean for testing tagged actions
 *
 * @author kuester
 */
public class TaggedActionsBean extends AbstractMethodExposingBean {

	@Expose(name="noTags", scope=ActionScope.NODE)
	public void noTags(String foo, Integer bar) {
	}

	@Expose(name="hasTags", scope=ActionScope.NODE, tags= {"foo", "bar", "blub"})
	public void hasTags() {
	}
	
	@Expose(name="hasOther", scope=ActionScope.NODE, tags= {"other", "tags"})
	public void hasOther() {
	}
	
	String matchTags(String name, String... tags) {
		Action template = new Action(name);
		template.setTags(Arrays.asList(tags));
		IActionDescription action = thisAgent.searchAction(template);
		return action != null ? action.getName() : null;
	}

}
