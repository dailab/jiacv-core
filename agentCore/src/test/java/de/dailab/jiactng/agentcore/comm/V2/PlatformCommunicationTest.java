package de.dailab.jiactng.agentcore.comm.V2;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.comm.CommBean;
import de.dailab.jiactng.agentcore.comm.V1.SpringCommTest1;
import junit.framework.TestCase;

public class PlatformCommunicationTest extends TestCase {
	ClassPathXmlApplicationContext ctx = null;

	CommBean instance = null;

	public PlatformCommunicationTest(String testName) {
		super(testName);
		ctx = new ClassPathXmlApplicationContext(new String[] { "de/dailab/jiactng/agentcore/comm/V2/CommConfigV2.xml" });

		instance = (CommBean) ctx.getBean("commBean1");
	}

	// einfahc starten und 30 sekunden am leben halten
	public static void main(String args[]) {
		PlatformCommunicationTest pct = new PlatformCommunicationTest("test");
		try {
			Thread.sleep(30000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
