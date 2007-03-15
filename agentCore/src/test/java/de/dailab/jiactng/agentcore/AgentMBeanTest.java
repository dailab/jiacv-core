package de.dailab.jiactng.agentcore;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import javax.management.relation.MBeanServerNotificationFilter;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * This testcase tests the JMX based management interface of an agent.
 *
 * @author Jan Keiser
 */
public class AgentMBeanTest extends TestCase implements NotificationListener {

	private ObjectName node = null;
	private ObjectName agent = null;
	private MBeanServer mbs = null;
	private ClassPathXmlApplicationContext context = null;
	private String currentLifecycleState = "";
	private String previousLifecycleState = "";
	private String registrationNotification = "";
	private ArrayList agentListNotification = null;
	private SimpleAgentNode nodeRef = null;

	/**
	 * Sets up the test environment. It enables the JMX interface, registers as listener
	 * for agent's (de)registration, starts the application (platform "myPlatform" with 
	 * one agent "TestAgent") defined in "agentTests.xml" and registers as listener for
	 * changes of the agent's lifecycle state and agent node's agent list.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("jmx.invoke.getters", "");
		node = new ObjectName("de.dailab.jiactng.agentcore:type=SimpleAgentNode,name=myPlatform");
		agent = new ObjectName("de.dailab.jiactng.agentcore:type=Agent,name=TestAgent");
		mbs = ManagementFactory.getPlatformMBeanServer();

		// add listener for (de)registration of the agent
		MBeanServerNotificationFilter msnf = new MBeanServerNotificationFilter();
		msnf.disableAllObjectNames();
		msnf.enableObjectName(agent);
		mbs.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), this, msnf, null);

		// start application
		context = new ClassPathXmlApplicationContext(
			"de/dailab/jiactng/agentcore/agentTests.xml");
		nodeRef = (SimpleAgentNode) context.getBean("myPlatform");
		ArrayList<String> agentList = new ArrayList<String>();
		agentList.add("TestAgent");
		agentListNotification = agentList;

		// add listener for change of agent's lifecycle state
		AttributeChangeNotificationFilter acnf = new AttributeChangeNotificationFilter();
		acnf.enableAttribute("LifecycleState");
		mbs.addNotificationListener(agent, this, acnf, null);

		// add listener for change of agent node's agent list
		AttributeChangeNotificationFilter acnf2 = new AttributeChangeNotificationFilter();
		acnf2.enableAttribute("Agents");
		mbs.addNotificationListener(node, this, acnf2, null);
	}

	/**
	 * Tears down the test environment. It deregisters as listener for changes of the 
	 * agent's lifecycle state and agent node's agent list, shuts down the agent node, 
	 * deregisters as listener for agent's (de)registration and closes the application 
	 * context.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (mbs.isRegistered(agent)) {
			mbs.removeNotificationListener(agent, this);
		}
		mbs.removeNotificationListener(node, this);
		nodeRef.shutdown();
		mbs.removeNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), this);
		mbs = null;
		agent = null;
		context.close();
		context = null;
		currentLifecycleState = null;
		previousLifecycleState = null;
		registrationNotification = null;
		agentListNotification = null;
		nodeRef = null;
	}
	
	
	/**
	 * Implementation of the interface NotificationListener. It notes the last two 
	 * notifications about change of agent's lifecycle state and the last notification 
	 * about (de)registration of the agent and about change of agent node's agent list.
	 */
	public void handleNotification(Notification notification, Object handback) {
		if (notification instanceof AttributeChangeNotification) {
			if (((AttributeChangeNotification)notification).getAttributeName().equals("LifecycleState")) {
				previousLifecycleState = currentLifecycleState;
				currentLifecycleState = (String) ((AttributeChangeNotification) notification).getNewValue();
			} else if (((AttributeChangeNotification)notification).getAttributeName().equals("Agents")) {
				agentListNotification = (ArrayList) ((AttributeChangeNotification) notification).getNewValue();
			}
		} else if (notification instanceof MBeanServerNotification) {
			registrationNotification = notification.getType();
		}
	}


	/**
	 * Tests if the agent can be correctly removed by using the JMX interface. This includes
	 * the previous registration as JMX resource, the change to state CLEANED_UP, the 
	 * deregistration as JMX resource and the removal from the agent node (incl. notification).
	 * @see #doAction(String)
	 * @see #checkRegistrationNotification(String)
	 * @see #checkStateNotifications(String,String)
	 * @see #checkAgentListNotification(ArrayList)
	 */
	public void testRemove() {
		checkRegistrationNotification("JMX.mbean.registered");
		doAction("remove");
		assertEquals("AgentMBean.remove doesn't deregister management of agent", false, mbs.isRegistered(agent));
		assertEquals("AgentMBean.remove doesn't remove agent from agent node", true, nodeRef.findAgents().isEmpty());
		checkStateNotifications("CLEANING_UP", "CLEANED_UP");
		checkRegistrationNotification("JMX.mbean.unregistered");
		
		// check notification
		checkAgentListNotification(new ArrayList());
	}
	
	/**
	 * Tests if you get the name "TestAgent" of the agent by using the JMX interface.
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
	 * Tests if the agent can change its lifecycle state by using the JMX interface 
	 * (stop, cleanup, init, start) and notifications are sent to the listener.
	 * @see #doAction(String)
	 * @see #checkStateNotifications(String,String)
	 * @see #checkState(String)
	 */
	public void testSetLifecycleState() {
		doAction("stop");
		checkStateNotifications("STOPPING", "STOPPED");
		checkState("STOPPED");
	    
		doAction("cleanup");
		checkStateNotifications("CLEANING_UP", "CLEANED_UP");
		checkState("CLEANED_UP");
	    
		doAction("init");
		checkStateNotifications("INITIALIZING", "INITIALIZED");
		checkState("INITIALIZED");
	    
		doAction("start");
		checkStateNotifications("STARTING", "STARTED");
		checkState("STARTED");
	}

	/**
	 * Tests if you get the lifecycle state "STARTED" of the agent by using the JMX interface.
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
		String state = nodeRef.findAgents().get(0).getState().toString();
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
	protected void checkStateNotifications(String previousState, String currentState) {
		assertEquals("Missing notification about agent's state " + previousState, previousState, previousLifecycleState);
		assertEquals("Missing notification about agent's state " + currentState, currentState, currentLifecycleState);
	}

	/**
	 * Checks the last registration notification of the agent.
	 * @param intendedType the intended type of the registration notification
	 */
	protected void checkRegistrationNotification(String intendedType) {
		assertEquals("Missing notification about agent " + intendedType, intendedType, registrationNotification);
	}

	/**
	 * Checks the last notification about changed agent list of the agent node.
	 * @param intendedList the intended agent list of the notification
	 */
	protected void checkAgentListNotification(ArrayList intendedList) {
		assertEquals("Missing notification about changed agent list", intendedList, agentListNotification);
	}

}
