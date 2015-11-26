package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.util.jar.JARMemory;

import junit.framework.TestCase;

/**
 * This testcase tests the JMX based management client for an agent node.
 *
 * @author Jan Keiser
 */
public class JmxAgentNodeManagementClientTest extends TestCase implements NotificationListener {

	private final String nodeName = "myNode";
	private final String agentName = "TestAgent";
	private final String newAgentName = "NewAgent";
	private ClassPathXmlApplicationContext context = null;
	private SimpleAgentNode nodeRef = null;
	private JmxAgentNodeManagementClient nodeClient = null;
	private String currentLifecycleState = "";
	private String previousLifecycleState = "";
	private ArrayList<String> agentListNotification = null;
	private String agentId = null;

	/**
	 * Sets up the test environment. It starts the application (agent node "myNode" 
	 * with one agent "TestAgent") defined in "agentTests.xml" and registers as listener 
	 * for changes of the agent node's lifecycle state by using the management client.
	 * @see ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(String)
	 * @see JmxManagementClient#JmxManagementClient()
	 * @see JmxManagementClient#getAgentNodeUUIDs()
	 * @see JmxManagementClient#getAgentNodeManagementClient(String)
	 * @see JmxAgentNodeManagementClient#addAgentsListener(NotificationListener)
	 * @see JmxAgentNodeManagementClient#addLifecycleStateListener(NotificationListener)
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// start application
		context = new ClassPathXmlApplicationContext(
			"de/dailab/jiactng/agentcore/agentTests.xml");
		nodeRef = (SimpleAgentNode) context.getBean(nodeName);
		agentId = ((IAgent) context.getBean(agentName)).getAgentId();
		ArrayList<String> agentList = new ArrayList<String>();
		agentList.add(agentId);
		agentListNotification = agentList;

		// get management client and check for agent node
		JmxManagementClient client = new JmxManagementClient();
		Set<String> nodes = client.getAgentNodeUUIDs();
		assertTrue("JmxManagementClient.getAgentNodeUUIDs() did not found test node", nodes.contains(nodeRef.getUUID()));

		// add listener for change of agent node's lifecycle state and agent list
		nodeClient = client.getAgentNodeManagementClient(nodeRef.getUUID());
		nodeClient.addAgentsListener(this);
		nodeClient.addLifecycleStateListener(this);
	}

	/**
	 * Tears down the test environment. It shuts down the agent node and closes the 
	 * application context.
	 * @see SimpleAgentNode#shutdown()
	 * @see ClassPathXmlApplicationContext#close()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		nodeRef.shutdown();
		context.close();
		context = null;
		currentLifecycleState = null;
		previousLifecycleState = null;
		agentListNotification = null;
		agentId = null;
		nodeRef = null;
	}


	/**
	 * Implementation of the interface NotificationListener. It notes the last two 
	 * notifications about change of agent node's lifecycle state.
	 * @see AttributeChangeNotification#getAttributeName()
	 * @see AttributeChangeNotification#getNewValue()
	 */
	public void handleNotification(Notification notification, Object handback) {
		if (notification instanceof AttributeChangeNotification) {
			if (((AttributeChangeNotification)notification).getAttributeName().equals("LifecycleState")) {
				previousLifecycleState = currentLifecycleState;
				currentLifecycleState = (String) ((AttributeChangeNotification) notification).getNewValue();
			} else if (((AttributeChangeNotification)notification).getAttributeName().equals("Agents")) {
				agentListNotification = (ArrayList<String>) ((AttributeChangeNotification) notification).getNewValue();
			}
		}
	}

	/**
	 * Tests if you get the host (name/IP of local host) of the agent node by using the
	 * agent node management client.
	 * @see JmxAgentNodeManagementClient#getHost()
	 * @see InetAddress#getLocalHost()
	 */
	public void testGetHost() {
		// get host directly
		String intendedHost = "";
		try {
			intendedHost = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException uhe) {}
		
		// get host by JMX
		String host = "";
		try {
			host = nodeClient.getHost();
		} catch (Exception e) {
			if (!intendedHost.equals("")) {
				fail("Error while getting agent node's host");
			}
		}
		
		assertEquals("JmxAgentNodeManagementClient.getHost() is wrong", intendedHost, host);
	}

	/**
	 * Tests if you get the platform name of the agent node by using the
	 * agent node management client.
	 * @see JmxAgentNodeManagementClient#getPlatformName()
	 */
	public void testGetPlatformName() {
		String platform = "";
		try {
			platform = nodeClient.getPlatformName();
		} catch (Exception e) {
			fail("Error while getting agent node's platform");
		}
		assertEquals("JmxAgentNodeManagementClient.getPlatformName() is wrong", null, platform);
	}

	/**
	 * Tests if you get the owner (system property "user.name") of the agent node by using the 
	 * agent node management client.
	 * @see JmxAgentNodeManagementClient#getAgentNodeOwner()
	 */
	public void testGetOwner() {
		String owner = "";
		try {
			owner = nodeClient.getAgentNodeOwner();
		} catch (Exception e) {
			fail("Error while getting agent node's owner");
		}
		assertEquals("JmxAgentNodeManagementClient.getAgentNodeOwner() is wrong", System.getProperty("user.name"), owner);
	}

	/**
	 * Tests if you get the agents (one agent "TestAgent") of the agent node by using the 
	 * agent node management client.
	 * @see JmxAgentNodeManagementClient#getAgents()
	 */
	public void testGetAgents() {
		List<String> agents = new ArrayList<String>();
		try {
			agents = nodeClient.getAgents();
		} catch (Exception e) {
			fail("Error while getting agent node's agents");
		}
		ArrayList<String> intendedAgents = new ArrayList<String>();
		intendedAgents.add(agentId);
		assertEquals("JmxAgentNodeManagementClient.getAgents() is wrong", intendedAgents, agents);
	}

	/**
	 * Tests if you can add new agents to the agent node by using the agent node management client.
	 * This includes the change to state STARTED and the update of agent node's list of agents 
	 * (inclusive notification).
	 * @see JmxAgentNodeManagementClient#addAgents(byte[], List, String)
	 * @see #checkAgentListNotification(ArrayList)
	 */
	public void testAddAgents() {
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("de/dailab/jiactng/agentcore/agentCreationTest.xml");
			byte[] config = new byte[is.available()];
			is.read(config);
			is.close();
			nodeClient.addAgents(config, new ArrayList<JARMemory>(), System.getProperty("user.name"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error while adding new agents to the agent node");
		}

		// get identifier and management name of the new agent
		String newAgentId = null;
		for (IAgent a : nodeRef.findAgents()) {
			if (a.getAgentName().equals(newAgentName)) {
				newAgentId = a.getAgentId();
			}
		}

		assertEquals("JmxAgentNodeManagementClient.addAgents() doesn't add new agent to agent node", 2, nodeRef.findAgents().size());
		assertEquals("JmxAgentNodeManagementClient.addAgents() creates new agent in wrong state", "UNDEFINED", nodeRef.findAgents().get(1).getState().toString());
		
		// check notification
		ArrayList<String> agentList = new ArrayList<String>();
		agentList.add(agentId);
		agentList.add(newAgentId);
		checkAgentListNotification(agentList);
	}

	/**
	 * Tests if the agent node can be correctly killed by using the agent node management client.
	 * This includes the change to state CLEANED_UP.
	 * @see JmxAgentNodeManagementClient#shutdownAgentNode()
	 * @see #checkStateNotifications(String,String)
	 */
	public void testShutdown() {
		try {
			nodeClient.shutdownAgentNode();
		} catch (Exception e) {
			fail("Error while shutting down agent node");
		}
		checkStateNotifications("CLEANING_UP", "CLEANED_UP");
	}

	/**
	 * Tests if the agent node can change its lifecycle state by using the agent node management
	 * client (stop, cleanup, init, start) and notifications are sent to the listener. The lifecycle
	 * state of the agent will also be changed.
	 * @see JmxAgentNodeManagementClient#stopAgentNode()
	 * @see JmxAgentNodeManagementClient#cleanupAgentNode()
	 * @see JmxAgentNodeManagementClient#initAgentNode()
	 * @see JmxAgentNodeManagementClient#startAgentNode()
	 * @see #checkStateNotifications(String,String)
	 * @see #checkState(String)
	 * @see #checkAgentState(String)
	 */
	public void testSetLifecycleState() {
		try {
			nodeClient.stopAgentNode();
		} catch (Exception e) {
			fail("Error while stopping agent node");
		}
		checkStateNotifications("STOPPING", "STOPPED");
		checkState("STOPPED");
		checkAgentState("STOPPED");
		
		try {
			nodeClient.cleanupAgentNode();
		} catch (Exception e) {
			fail("Error while cleaning up agent node");
		}
		checkStateNotifications("CLEANING_UP", "CLEANED_UP");
		checkState("CLEANED_UP");
		checkAgentState("CLEANED_UP");
	    
		try {
			nodeClient.initAgentNode();
		} catch (Exception e) {
			fail("Error while initializing agent node");
		}
		checkStateNotifications("INITIALIZING", "INITIALIZED");
		checkState("INITIALIZED");
		checkAgentState("INITIALIZED");
	    
		try {
			nodeClient.startAgentNode();
		} catch (Exception e) {
			fail("Error while starting agent node");
		}
		checkStateNotifications("STARTING", "STARTED");
		checkState("STARTED");
		checkAgentState("STARTED");
	}

	/**
	 * Tests if you get the lifecycle state "STARTED" of the agent node by using the 
	 * agent node management client.
	 * @see #checkState(String)
	 */
	public void testGetLifecycleState() {
		checkState("STARTED");
	}


	/**
	 * Checks the actual state of the agent node and the state getting from the 
	 * agent node management client.
	 * @param intendedState the intended state of the agent node
	 * @see JmxAgentNodeManagementClient#getAgentNodeState()
	 */
	protected void checkState(String intendedState) {
		// check actual state
		String state = nodeRef.getState().toString();
		assertEquals("Wrong lifecycle state of agent node", intendedState, state);		

		// check JMX interface
		state = "";
		try {
			state = nodeClient.getAgentNodeState();
		} catch (Exception e) {
			fail("Error while getting agent node's state");
		}
		assertEquals("JmxAgentNodeManagementClient.getAgentNodeState() is wrong", intendedState, state);		
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
	 * Checks the last notification about changed agent list of the agent node.
	 * @param intendedList the intended agent list of the notification
	 */
	protected void checkAgentListNotification(ArrayList<String> intendedList) {
		assertEquals("Missing notification about changed agent list", intendedList, agentListNotification);
	}

}
