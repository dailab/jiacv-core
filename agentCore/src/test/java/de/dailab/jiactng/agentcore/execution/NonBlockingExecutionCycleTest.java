package de.dailab.jiactng.agentcore.execution;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import junit.framework.TestCase;

/**
 * This testcase tests if the non-blocking execution cycle allows the synchronously invocation of actions.
 * @author Jan Keiser
 */
public class NonBlockingExecutionCycleTest extends TestCase {

	private final String nodeName = "myPlatform";
	private ClassPathXmlApplicationContext context = null;
	private SimpleAgentNode nodeRef = null;
	private TestBean beanRef = null;

	/**
	 * Sets up the test environment. It starts the application (agent node "myPlatform" 
	 * with one agent "TestAgent") defined in "test.xml".
	 * @see ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(String)
	 */
	protected void setUp() throws Exception {
		super.setUp();

		context = new ClassPathXmlApplicationContext(
			"de/dailab/jiactng/agentcore/execution/test.xml");
		nodeRef = (SimpleAgentNode) context.getBean(nodeName);
		for (IAgentBean bean: nodeRef.findAgents().get(0).getAgentBeans()) {
			if (bean instanceof TestBean) {
				beanRef = (TestBean) bean;
			}
		}
	}

	/**
	 * Tears down the test environment. It shuts down the agent node and closes the application context.
	 * @see SimpleAgentNode#shutdown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		nodeRef.shutdown();
		context.close();
		context = null;
		nodeRef = null;
		beanRef = null;
	}

	/**
	 * Uses the test action and waits a maximum duration of 30 seconds for the result.
	 * @see TestBean#invokeTestAction()
	 * @see Future#get(long, TimeUnit)
	 */
	public void testUseActionAndWaitForResult() {
		Future<?> future = nodeRef.getThreadPool().submit(new Handler());
		try {
			future.get(30000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			future.cancel(true);
			fail("Error during synchronous invocation of the test action.");
		}
		assertNotNull("No result", beanRef.actions);
	}

	private class Handler implements Runnable {
		public void run() {
			try {
				beanRef.invokeTestAction();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
