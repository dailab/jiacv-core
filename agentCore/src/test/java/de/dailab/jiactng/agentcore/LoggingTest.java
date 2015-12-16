package de.dailab.jiactng.agentcore;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.JIACTestForJUnit3;

public class LoggingTest extends JIACTestForJUnit3 {
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
