package de.dailab.jiactng.agentcore;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

import de.dailab.jiactng.agentcore.management.jmx.JmxManager;

import junit.framework.TestCase;

public class SimpleAgentNodeMBeanTest extends TestCase implements NotificationListener {

	private final String nodeName = "myPlatform";
	private final String agentName = "TestAgent";
	private final String newAgentName = "NewAgent";
	private MBeanServer mbs = null;
	private ObjectName node = null;
	private ObjectName agent = null;
	private ObjectName newAgent = null;
	private ClassPathXmlApplicationContext context = null;
	private String currentLifecycleState = "";
	private String previousLifecycleState = "";
	private String registrationNotification = "";
	private ArrayList agentListNotification = null;
	private SimpleAgentNode nodeRef = null;
	private JmxManager manager = null;

	/**
	 * Sets up the test environment. It enables the JMX interface, registers as listener
	 * for agent node's (de)registration, starts the application (agent node "myPlatform" 
	 * with one agent "TestAgent") defined in "agentTests.xml" and registers as listener 
	 * for changes of the agent node's lifecycle state.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("jmx.invoke.getters", "");
		mbs = ManagementFactory.getPlatformMBeanServer();
		manager = new JmxManager();
		node = manager.getMgmtNameOfAgentNode(nodeName);
		agent = manager.getMgmtNameOfAgent(nodeName, agentName);
		newAgent = manager.getMgmtNameOfAgent(nodeName, newAgentName);

		// add listener for (de)registration of the agent node
		MBeanServerNotificationFilter msnf = new MBeanServerNotificationFilter();
		msnf.disableAllObjectNames();
		msnf.enableObjectName(node);
		mbs.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), this, msnf, null);

		// start application
		context = new ClassPathXmlApplicationContext(
			"de/dailab/jiactng/agentcore/agentTests.xml");
		nodeRef = (SimpleAgentNode) context.getBean("myPlatform");
		ArrayList<String> agentList = new ArrayList<String>();
		agentList.add("TestAgent");
		agentListNotification = agentList;

		// add listener for change of agent node's lifecycle state and agent list
		AttributeChangeNotificationFilter acnf = new AttributeChangeNotificationFilter();
		acnf.enableAttribute("LifecycleState");
		acnf.enableAttribute("Agents");
		mbs.addNotificationListener(node, this, acnf, null);
	}

	/**
	 * Tears down the test environment. It deregisters as listener for changes of the 
	 * agent node's lifecycle state, shuts down the agent node, deregisters as listener
	 * for agent node's (de)registration and closes the application context.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (mbs.isRegistered(node)) {
			mbs.removeNotificationListener(node, this);
			nodeRef.shutdown();
		}
		mbs.removeNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), this);
		context.close();
		context = null;
		currentLifecycleState = null;
		previousLifecycleState = null;
		registrationNotification = null;
		agentListNotification = null;
		nodeRef = null;
		node = null;
		agent = null;
		newAgent = null;
		manager = null;
		mbs = null;
	}


	/**
	 * Implementation of the interface NotificationListener. It notes the last two 
	 * notifications about change of agent node's lifecycle state and the last notification 
	 * about (de)registration of the agent node.
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

	
/*
	public void testGetUUID() {
		fail("Not yet implemented");
	}
*/
	
	/**
	 * Tests if you get the name "myPlatform" of the agent node by using the JMX interface.
	 */
	public void testGetName() {
		String name = "";
		try {
			name = (String) manager.getAttributeOfAgentNode(nodeName, "Name");
		} catch (Exception e) {
			fail("Error while getting agent node's name");
		}
		assertEquals("SimpleAgentNodeMBean.getName is wrong", "myPlatform", name);
	}

	/**
	 * Tests if you get the host (name/IP of local host) of the agent node by using the JMX interface.
	 */
	public void testGetHost() {
		// get host directly
		String intendedHost = "";
		try {
			intendedHost = InetAddress.getLocalHost().toString();
		} catch (UnknownHostException uhe) {}
		
		// get host by JMX
		String host = "";
		try {
			host = (String) manager.getAttributeOfAgentNode(nodeName, "Host");
		} catch (Exception e) {
			if (!intendedHost.equals("")) {
				fail("Error while getting agent node's host");
			}
		}
		
		assertEquals("SimpleAgentNodeMBean.getHost is wrong", intendedHost, host);
	}

	/**
	 * Tests if you get the owner (system property "user.name") of the agent node by using the 
	 * JMX interface.
	 */
	public void testGetOwner() {
		String owner = "";
		try {
			owner = (String) manager.getAttributeOfAgentNode(nodeName, "Owner");
		} catch (Exception e) {
			fail("Error while getting agent node's owner");
		}
		assertEquals("SimpleAgentNodeMBean.getOwner is wrong", System.getProperty("user.name"), owner);
	}

	/**
	 * Tests if you get the agents (one agent "TestAgent") of the agent node by using the 
	 * JMX interface.
	 */
	public void testGetAgents() {
		ArrayList agents = new ArrayList();
		try {
			agents = (ArrayList) manager.getAttributeOfAgentNode(nodeName, "Agents");
		} catch (Exception e) {
			fail("Error while getting agent node's agents");
		}
		ArrayList<String> intendedAgents = new ArrayList<String>();
		intendedAgents.add("TestAgent");
		assertEquals("SimpleAgentNodeMBean.getAgents is wrong", intendedAgents, agents);
	}

	/**
	 * Tests if you can add new agents to the agent node by using the JMX interface. This
	 * includes the registration as JMX resource, the change to state STARTED and the
	 * update of agent node's list of agents (inclusive notification).
	 * 
	 * @see #checkAgentListNotification(ArrayList)
	 */
	public void testAddAgents() {
		try {
			manager.invokeAgentNode(nodeName, "addAgents", new Object[] {"de/dailab/jiactng/agentcore/agentCreationTest"}, new String[] {"java.lang.String"});
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error while adding new agents to the agent node");
		}
		assertEquals("SimpleAgentNodeMBean.addAgents doesn't register management of new agent", true, mbs.isRegistered(newAgent));
		assertEquals("SimpleAgentNodeMBean.addAgents doesn't add new agent to agent node", 2, nodeRef.findAgents().size());
		assertEquals("SimpleAgentNodeMBean.addAgents doesn't start new agent", "STARTED", nodeRef.findAgents().get(1).getState().toString());
		
		// check notification
		ArrayList<String> agentList = new ArrayList<String>();
		agentList.add("TestAgent");
		agentList.add("NewAgent");
		checkAgentListNotification(agentList);
	}

	/**
	 * Tests if the agent node can be correctly killed by using the JMX interface. This 
	 * includes the previous registration as JMX resource, the change to state CLEANED_UP 
	 * and the deregistration as JMX resource.
	 * @see #doAction(String)
	 * @see #checkRegistrationNotification(String)
	 * @see #checkStateNotifications(String,String)
	 */
	public void testShutdown() {
		checkRegistrationNotification("JMX.mbean.registered");
		doAction("shutdown");
		assertEquals("SimpleAgentNodeMBean.shutdown doesn't deregister management of agent node", false, mbs.isRegistered(node));
		assertEquals("SimpleAgentNodeMBean.shutdown doesn't deregister management of agents", false, mbs.isRegistered(agent));
		checkStateNotifications("CLEANING_UP", "CLEANED_UP");
		checkRegistrationNotification("JMX.mbean.unregistered");
	}

	/**
	 * Tests if the agent node can change its lifecycle state by using the JMX interface 
	 * (stop, cleanup, init, start) and notifications are sent to the listener. The lifecycle
	 * state of the agent will also be changed.
	 * @see #doAction(String)
	 * @see #checkStateNotifications(String,String)
	 * @see #checkState(String)
	 * @see #checkAgentState(String)
	 */
	public void testSetLifecycleState() {
		doAction("stop");
		checkStateNotifications("STOPPING", "STOPPED");
		checkState("STOPPED");
		checkAgentState("STOPPED");
		
		doAction("cleanup");
		checkStateNotifications("CLEANING_UP", "CLEANED_UP");
		checkState("CLEANED_UP");
		checkAgentState("CLEANED_UP");
	    
		doAction("init");
		checkStateNotifications("INITIALIZING", "INITIALIZED");
		checkState("INITIALIZED");
		checkAgentState("INITIALIZED");
	    
		doAction("start");
		checkStateNotifications("STARTING", "STARTED");
		checkState("STARTED");
		checkAgentState("STARTED");
	}

	/**
	 * Tests if you get the lifecycle state "STARTED" of the agent node by using the JMX interface.
	 * @see #checkState(String)
	 */
	public void testGetLifecycleState() {
		checkState("STARTED");
	}


	/**
	 * Calls an action of the agent node without parameters by using the JMX interface.
	 * @param action the name of the action (method)
	 */
	protected void doAction(String action) {
		try {
			manager.invokeAgentNode(nodeName, action, new Object[] {}, new String[] {});
		} catch (Exception e) {
			fail("Error while " + action + " agent node");
		}
	}

	/**
	 * Checks the actual state of the agent node and the state getting from the JMX interface.
	 * @param intendedState the intended state of the agent node
	 */
	protected void checkState(String intendedState) {
		// check actual state
		String state = nodeRef.getState().toString();
		assertEquals("Wrong lifecycle state of agent node", intendedState, state);		

		// check JMX interface
		state = "";
		try {
			state = (String) manager.getAttributeOfAgentNode(nodeName, "LifecycleState");
		} catch (Exception e) {
			fail("Error while getting agent node's state");
		}
		assertEquals("SimpleAgentNodeMBean.getLifecycleState is wrong", intendedState, state);		
	}

	/**
	 * Checks the actual state of the agent node's agent.
	 * @param intendedState the intended state of the agent
	 */
	protected void checkAgentState(String intendedState) {
		String state = nodeRef.findAgents().get(0).getState().toString();
		assertEquals("Wrong lifecycle state of agent", intendedState, state);		
	}

	/**
	 * Checks the last two notifications about change of agent node's lifecycle state.
	 * @param previousState the intended state of the agent node after the previous notification
	 * @param currentState the intended state of the agent node after the current notification
	 */
	protected void checkStateNotifications(String previousState, String currentState) {
		assertEquals("Missing notification about agent node's state " + previousState, previousState, previousLifecycleState);
		assertEquals("Missing notification about agent node's state " + currentState, currentState, currentLifecycleState);
	}

	/**
	 * Checks the last registration notification of the agent node.
	 * @param intendedType the intended type of the registration notification
	 */
	protected void checkRegistrationNotification(String intendedType) {
		assertEquals("Missing notification about agent node " + intendedType, intendedType, registrationNotification);
	}

	/**
	 * Checks the last notification about changed agent list of the agent node.
	 * @param intendedList the intended agent list of the notification
	 */
	protected void checkAgentListNotification(ArrayList intendedList) {
		assertEquals("Missing notification about changed agent list", intendedList, agentListNotification);
	}

}
