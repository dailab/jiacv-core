package de.dailab.jiactng.agentcore;

import java.lang.management.ManagementFactory;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * This testcase tests the JMX based management interface of an agent.
 *
 * @author Jan Keiser
 */
public class AgentMBeanTest extends TestCase implements NotificationListener {

	private ObjectName agent = null;
	private MBeanServer mbs = null;
	private ClassPathXmlApplicationContext context = null;
	private String currentLifecycleState = "";
	private String previousLifecycleState = "";
	private SimpleAgentNode node = null;

	/**
	 * Sets up the test environment. It enables the JMX interface, starts the application and
	 * registers as notification listener for changes of the agent's lifecycle state.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("jmx.invoke.getters", "");
		context = new ClassPathXmlApplicationContext(
			"de/dailab/jiactng/agentcore/agentTests.xml");
		node = (SimpleAgentNode) context.getBean("myPlatform");
		agent = new ObjectName("de.dailab.jiactng.agentcore:type=Agent,name=TestAgent");
		mbs = ManagementFactory.getPlatformMBeanServer();
		AttributeChangeNotificationFilter filter = new AttributeChangeNotificationFilter();
		filter.enableAttribute("LifecycleState");
		mbs.addNotificationListener(agent, this, filter, null);
	}

	/**
	 * Tears down the test environment. It deregisters as notification listener, shuts down 
	 * the agent node and closes the application context.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (mbs.isRegistered(agent)) {
			mbs.removeNotificationListener(agent, this);
		}
		node.shutdown();
		mbs = null;
		agent = null;
		context.close();
		context = null;
		currentLifecycleState = "";
		previousLifecycleState = "";
		node = null;
	}
	
	
	/**
	 * Implementation of the interface NotificationListener. It notes the last two notifications.
	 */
	public void handleNotification(Notification notification, Object handback) {
		previousLifecycleState = currentLifecycleState;
		currentLifecycleState = (String) ((AttributeChangeNotification) notification).getNewValue();
	}


	/**
	 * Tests if the agent can be correctly removed by using the JMX interface (remove).
	 * @see #doAction(String)
	 */
	public void testRemove() {
		doAction("remove");
		assertEquals("AgentMBean.remove doesn't deregister management of agent", false, mbs.isRegistered(agent));
		assertEquals("AgentMBean.remove doesn't remove agent from agent node", true, node.findAgents().isEmpty());
		checkNotifications("CLEANING_UP", "CLEANED_UP");
	}
	
	/**
	 * Tests if you get the correct name of the agent by using the JMX interface.
	 */
	public void testGetName() {
		String name = "";
		try {
			name = (String) mbs.getAttribute(agent, "Name");
		} catch (Exception e) {
			fail("Error while getting agent's name");
		}
		assertEquals("AgentMBean.getName is wrong", "TestAgent", name);
	}
	
/*
	public void testGetAgentNodeUUID() {
		fail("Not yet implemented");
	}	
*/
	
	/**
	 * Tests if the agent can change its lifecycle state by using the JMX interface (stop, cleanup, init, start).
	 * @see #doAction(String)
	 * @see #checkNotifications(String,String)
	 * @see #checkState(String)
	 */
	public void testSetLifecycleState() {
		doAction("stop");
		checkNotifications("STOPPING", "STOPPED");
		checkState("STOPPED");
	    
		doAction("cleanup");
		checkNotifications("CLEANING_UP", "CLEANED_UP");
		checkState("CLEANED_UP");
	    
		doAction("init");
		checkNotifications("INITIALIZING", "INITIALIZED");
		checkState("INITIALIZED");
	    
		doAction("start");
		checkNotifications("STARTING", "STARTED");
		checkState("STARTED");
	}

	/**
	 * Tests if you get the lifecycle state STARTED of the agent by using the JMX interface.
	 * @see #checkState(String)
	 */
	public void testGetLifecycleState() {
		checkState("STARTED");
	}


	/**
	 * Calls an action of the agent without parameters by using the JMX interface.
	 * @param action the name of the action (method)
	 */
	protected void doAction(String action) {
		try {
			mbs.invoke(agent, action, new Object[] {}, new String[] {});
		} catch (Exception e) {
			fail("Error while " + action + " agent");
		}
	}

	/**
	 * Checks the actual state of the agent and the state getting from the JMX interface.
	 * @param intendedState the intended state of the agent
	 */
	protected void checkState(String intendedState) {
		// check actual state
		String state = ((IAgent) node.findAgents().toArray()[0]).getState().toString();
		assertEquals("Wrong lifecycle state of agent", intendedState, state);		

		// check JMX interface
		state = "";
		try {
			state = (String) mbs.getAttribute(agent, "LifecycleState");
		} catch (Exception e) {
			fail("Error while getting agent's state");
		}
		assertEquals("AgentMBean.getLifecycleState is wrong", intendedState, state);		
	}

	/**
	 * Checks the last two notifications about change of agent's lifecycle state.
	 * @param previousState the intended state of the agent after the previous notification
	 * @param currentState the intended state of the agent after the current notification
	 */
	protected void checkNotifications(String previousState, String currentState) {
		assertEquals("Missing notification about starting agent", previousState, previousLifecycleState);
		assertEquals("Missing notification about started agent", currentState, currentLifecycleState);
	}

}
