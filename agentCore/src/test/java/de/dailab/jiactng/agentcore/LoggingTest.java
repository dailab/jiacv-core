package de.dailab.jiactng.agentcore;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class LoggingTest extends TestCase {
	public void testLogging() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
		"de/dailab/jiactng/agentcore/loggingTest.xml");
		
		// shutdown agent node
		SimpleAgentNode nodeRef = (SimpleAgentNode) context.getBean("myNode");
		try {
			nodeRef.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
