package de.dailab.jiactng.agentcore.conf;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgentNode;
import junit.framework.TestCase;

public class GenericAgentPropertiesInjection extends TestCase {

	private IAgentNode node  = null;
	ClassPathXmlApplicationContext newContext = null;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		newContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/conf/genericAgentPropertiesTest.xml");
		node = (IAgentNode) newContext.getBean("myPlatform");
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSize() {
		int agents = node.findAgents().size();
		assertEquals(10, agents);
	}

}
