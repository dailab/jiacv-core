package de.dailab.jiactng.agentcore.comm.V1;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.comm.CommBean;

public class SpringCommTest1 extends TestCase {
	ClassPathXmlApplicationContext ctx = null;

	CommBean instance = null;

	public SpringCommTest1(String testName) {
		super(testName);
		ctx = new ClassPathXmlApplicationContext(new String[] { "de/dailab/jiactng/agentcore/comm/SpringCommTest1.xml" });

		instance = (CommBean) ctx.getBean("commBean1");
	}

	// einfahc starten und 30 sekunden am leben halten
	public static void main(String args[]) {
		SpringCommTest1 sp1 = new SpringCommTest1("test");
		try {
			Thread.sleep(30000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
