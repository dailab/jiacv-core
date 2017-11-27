package de.dailab.jiactng.agentcore.action;

import java.util.Arrays;

import de.dailab.jiactng.agentcore.ontology.IActionDescription;

/**
 * Test bean for calling tagged actions
 * This circumvents matching via the memory and enforces matching with the directory
 *
 * @author kuester
 */
public class TaggedActionsCallerBean extends AbstractMethodExposingBean {

	String matchTags(String name, String... tags) {
		Action template = new Action(name);
		template.setTags(Arrays.asList(tags));
		IActionDescription action = thisAgent.searchAction(template);
		return action != null ? action.getName() : null;
	}
	
}
