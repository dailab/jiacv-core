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

public class TaggedActionsTest extends JIACTestForJUnit4 {

	static IAgentNode node;
	static TaggedActionsBean bean;

	@SuppressWarnings("resource")
	@BeforeClass
	public static void setup() {
		final String CONFIG = "de/dailab/jiactng/agentcore/action/taggedActionsTest.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG);
		node = (IAgentNode) context.getBean("TaggedActionsNode");
		bean = (TaggedActionsBean) node.findAgents().get(0).getAgentBeans().get(0);
	}

	@AfterClass
	public static void teardown() throws Exception {
		((SimpleAgentNodeMBean) node).shutdown();
	}

	@Test
	public void testNoTags() {
		assertEquals("noTags", bean.matchTags("noTags"));
	}
	
	@Test
	public void testWithTags() {
		assertNull(bean.matchTags("noTags", "some", "tags"));
	}
	
	@Test
	public void testWrongTags() {
		assertNull(bean.matchTags("hasTags", "some", "tags"));
	}
	
	@Test
	public void testTagsAndName() {
		assertEquals("hasTags", bean.matchTags("hasTags", "bar", "blub", "foo"));
	}
	
	@Test
	public void testTagsWrongName() {
		assertNull(bean.matchTags("wrongName", "foo", "bar", "blub"));
	}
	
	@Test
	public void testNameOnly() {
		assertEquals("hasTags", bean.matchTags("hasTags"));
	}
	
	@Test
	public void testTagsOnly() {
		assertEquals("hasOther", bean.matchTags(null, "tags", "other"));
	}
	
	@Test
	public void moreTags() {
		assertEquals("hasTags", bean.matchTags(null, "foo", "bar"));
	}
	
	@Test
	public void missingTags() {
		assertNull(bean.matchTags(null, "other", "tags", "and", "some", "more"));
	}
	
}
