package de.dailab.jiactng.agentcore.action;

import java.io.Serializable;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.JIACTestForJUnit4;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNodeMBean;
import de.dailab.jiactng.agentcore.action.IMethodExposingBean.Expose;

/**
 * Test case for @Expose with multiple {@link Expose#returnTypes()}
 * 
 * @see ExposeMultiReturnBean
 * 
 * @author kuester
 */
public class TestExposeMultiReturn extends JIACTestForJUnit4 {

	static IAgentNode node;
	static ExposeMultiReturnBean bean;

	@SuppressWarnings("resource")
	@BeforeClass
	public static void setup() {
		final String CONFIG = "de/dailab/jiactng/agentcore/action/multiExposeTest.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG);
		node = (IAgentNode) context.getBean("MultiExposeNode");
		bean = (ExposeMultiReturnBean) node.findAgents().get(0).getAgentBeans().get(0);
	}

	@AfterClass
	public static void teardown() throws Exception {
		((SimpleAgentNodeMBean) node).shutdown();
	}

	@Test
	public void testSingle() {
		ActionResult result = bean.invokeAndWait("single");
		Assert.assertArrayEquals(new Serializable[] { "foo" }, result.getResults());
	}

	@Test
	public void testMulti() {
		ActionResult result = bean.invokeAndWait("multi");
		Assert.assertArrayEquals(new Serializable[] { "foo", 42, 3.14 }, result.getResults());
	}

	@Test
	public void testArray() {
		ActionResult result = bean.invokeAndWait("array");
		Assert.assertArrayEquals(new Serializable[] { new String[] { "foo", "bar" } }, result.getResults());
	}

}
