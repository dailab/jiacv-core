package de.dailab.jiactng.agentcore.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.JIACTestForJUnit4;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNodeMBean;

/**
 * Test matching actions with and without tags; actions are just matched, not invoked.
 * Each test is performed once using memory matching (using reflection) and once using
 * the directory and Action#matches.
 * 
 * XXX Actually, I did not expect this to with with memory matching, as tags should
 * match without regarding the order, and also if not all the tags are specified in
 * the template, but apparently that's exactly what Memory#read is doing...
 *
 * @author kuester
 */
public class TaggedActionsTest extends JIACTestForJUnit4 {

	static IAgentNode node;
	static TaggedActionsBean bean1;
	static TaggedActionsCallerBean bean2;

	@SuppressWarnings("resource")
	@BeforeClass
	public static void setup() {
		final String CONFIG = "de/dailab/jiactng/agentcore/action/taggedActionsTest.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG);
		node = (IAgentNode) context.getBean("TaggedActionsNode");
		bean1 = (TaggedActionsBean) node.findAgents().get(0).getAgentBeans().get(0);
		bean2 = (TaggedActionsCallerBean) node.findAgents().get(1).getAgentBeans().get(0);
	}

	@AfterClass
	public static void teardown() throws Exception {
		((SimpleAgentNodeMBean) node).shutdown();
	}

	@Test
	public void testNoTags() {
		// matching without tags still works
		checkMatch("noTags", "noTags");
	}
	
	@Test
	public void testWithTags() {
		// with tags in template, action without tags is not matched
		checkNull("noTags", "some", "tags");
	}
	
	@Test
	public void testWrongTags() {
		// action with tags is not matched by wrong tags
		checkNull("hasTags", "some", "tags");
	}
	
	@Test
	public void testTagsAndName() {
		// action is matched with matching tags and name; order of tags does not matter
		checkMatch("hasTags", "hasTags", "bar", "blub", "foo");
	}
	
	@Test
	public void testTagsWrongName() {
		// tags alone do not match the action if the name is wrong
		checkNull("wrongName", "foo", "bar", "blub");
	}
	
	@Test
	public void testNameOnly() {
		// action with tags is name-matched even if no tags provided in template
		checkMatch("hasTags", "hasTags");
	}
	
	@Test
	public void testTagsOnly() {
		// action with tags is tag-matched if no name is provided
		checkMatch("hasOther", null, "tags", "other");
	}
	
	@Test
	public void moreTags() {
		// action is matched even if the action has more tage then the template
		checkMatch("hasTags", null, "foo", "bar");
	}
	
	@Test
	public void missingTags() {
		// action is not matched if template has more tags than the action
		checkNull(null, "other", "tags", "and", "some", "more");
	}
	
	private void checkNull(String name, String... tags) {
		assertNull(bean1.matchTags(name, tags));
		assertNull(bean2.matchTags(name, tags));
	}
	
	private void checkMatch(String match, String name, String... tags) {
		assertEquals(match, bean1.matchTags(name, tags));
		assertEquals(match, bean2.matchTags(name, tags));
	}
	
}
