package de.dailab.jiactng.agentcore;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

public class LoggingTest extends TestCase {
	public void testLogging() {
		new ClassPathXmlApplicationContext(
		"de/dailab/jiactng/agentcore/loggingTest.xml");
	}
}
