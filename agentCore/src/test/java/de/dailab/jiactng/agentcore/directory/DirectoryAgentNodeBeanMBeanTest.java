package de.dailab.jiactng.agentcore.directory;

import javax.management.openmbean.TabularData;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.management.jmx.JmxManager;

import junit.framework.TestCase;

/**
 * This testcase tests the JMX based management interface of an agent.
 *
 * @author Jan Keiser
 */
public class DirectoryAgentNodeBeanMBeanTest extends TestCase {

	private final String nodeName = "myNode";
	private final String beanName = "IDirectory";
	private ClassPathXmlApplicationContext context = null;
	private SimpleAgentNode nodeRef = null;
	private JmxManager manager = null;

	/**
	 * Sets up the test environment. It enables the JMX interface and starts the application 
	 * (platform "myNode" with one agent "TestAgent") defined in "agentTests.xml".
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("jmx.invoke.getters", "");
		manager = new JmxManager();

		// start application
		context = new ClassPathXmlApplicationContext(
			"de/dailab/jiactng/agentcore/directory/directoryTests.xml");
		nodeRef = (SimpleAgentNode) context.getBean(nodeName);
	}

	/**
	 * Tears down the test environment. It shuts down the agent node 
	 * and closes the application context.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		nodeRef.shutdown();
		context.close();
		context = null;
		nodeRef = null;
		manager = null;
	}
	
	
	/**
	 * Tests if you get the alive interval of the directory by using the JMX interface.
	 */
	public void testGetAliveInterval() {
		try {
			Long interval = (Long) manager.getAttributeOfAgentNodeBean(nodeRef.getUUID(), beanName, "AliveInterval");
			assertEquals("AgentMBean.getAliveInterval is wrong", Long.valueOf(2000), interval);
		} catch (Exception e) {
			fail("Error while getting alive interval: "+e.getLocalizedMessage());
		}
	}

	/**
	 * Tests if you get the advertise interval of the directory by using the JMX interface.
	 */
	public void testGetAdvertiseInterval() {
		try {
			Long interval = (Long) manager.getAttributeOfAgentNodeBean(nodeRef.getUUID(), beanName, "AdvertiseInterval");
			assertEquals("AgentMBean.getAdvertiseInterval is wrong", Long.valueOf(10800), interval);
		} catch (Exception e) {
			fail("Error while getting advertise interval: "+e.getLocalizedMessage());
		}
	}

	/**
	 * Tests if you get the dump state by using the JMX interface.
	 */
	public void testGetDump() {
		try {
			Boolean dump = (Boolean) manager.getAttributeOfAgentNodeBean(nodeRef.getUUID(), beanName, "Dump");
			assertEquals("AgentMBean.isDump is wrong", Boolean.valueOf(true), dump);
		} catch (Exception e) {
			fail("Error while getting dump state: "+e.getLocalizedMessage());
		}
	}

	/**
	 * Tests if you get all local actions registered in the directory by using the JMX interface.
	 */
	public void testGetLocalActions() {
		try {
			TabularData data = (TabularData) manager.getAttributeOfAgentNodeBean(nodeRef.getUUID(), beanName, "LocalActions");
			assertEquals("AgentMBean.getLocalActions is wrong", 2, data.size());
		} catch (Exception e) {
			fail("Error while getting local actions: "+e.getLocalizedMessage());
		}
	}

	/**
	 * Tests if you get all local agents registered in the directory by using the JMX interface.
	 */
	public void testGetLocalAgents() {
		try {
			TabularData data = (TabularData) manager.getAttributeOfAgentNodeBean(nodeRef.getUUID(), beanName, "LocalAgents");
			assertEquals("AgentMBean.getLocalAgents returns wrong number of agents", 1, data.size());
		} catch (Exception e) {
			fail("Error while getting local agents: "+e.getLocalizedMessage());
		}
	}

	/**
	 * Tests if you get all remote actions registered in the directory by using the JMX interface.
	 */
	public void testGetRemoteActions() {
		try {
			TabularData data = (TabularData) manager.getAttributeOfAgentNodeBean(nodeRef.getUUID(), beanName, "RemoteActions");
		} catch (Exception e) {
			fail("Error while getting remote actions: "+e.getLocalizedMessage());
		}
	}

	/**
	 * Tests if you get all remote agents registered in the directory by using the JMX interface.
	 */
	public void testGetRemoteAgents() {
		try {
			TabularData data = (TabularData) manager.getAttributeOfAgentNodeBean(nodeRef.getUUID(), beanName, "RemoteAgents");
		} catch (Exception e) {
			fail("Error while getting remote agents: "+e.getLocalizedMessage());
		}
	}

	/**
	 * Tests if you get all nodes registered in the directory by using the JMX interface.
	 */
	public void testGetKnownNodes() {
		try {
			TabularData data = (TabularData) manager.getAttributeOfAgentNodeBean(nodeRef.getUUID(), beanName, "KnownNodes");
		} catch (Exception e) {
			fail("Error while getting known nodes: "+e.getLocalizedMessage());
		}
	}

}
