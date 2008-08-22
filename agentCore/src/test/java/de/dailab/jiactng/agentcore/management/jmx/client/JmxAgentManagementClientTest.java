package de.dailab.jiactng.agentcore.management.jmx.client;

import java.util.ArrayList;
import java.util.Set;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;

import junit.framework.TestCase;

/**
 * This testcase tests the JMX based management client for an agent.
 *
 * @author Jan Keiser
 */
public class JmxAgentManagementClientTest extends TestCase implements NotificationListener {

	private final String nodeName = "myPlatform";
	private final String agentName = "TestAgent";
	private ClassPathXmlApplicationContext context = null;
	private SimpleAgentNode nodeRef = null;
	private JmxAgentManagementClient agentClient = null;
	private String agentId = null;
	private String currentLifecycleState = "";
	private String previousLifecycleState = "";
	private ArrayList<String> agentListNotification = null;

	/**
	 * Sets up the test environment. It enables the JMX interface, starts the application 
	 * (platform "myPlatform" with one agent "TestAgent") defined in "agentTests.xml" and 
	 * registers as listener for changes of the agent's lifecycle state and agent node's 
	 * agent list.
	 * @see ClassPathXmlApplicationContext#ClassPathXmlApplicationContext(String)
	 * @see JmxManagementClient#JmxManagementClient()
	 * @see JmxManagementClient#getAgentNodeNames()
	 * @see JmxManagementClient#getAgentManagementClient(String, String)
	 * @see JmxAgentNodeManagementClient#addAgentsListener(NotificationListener)
	 * @see JmxAgentManagementClient#addLifecycleStateListener(NotificationListener)
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
		Set<String> nodes = client.getAgentNodeNames();
		assertEquals("JmxManagementClient.getAgentNodeNames() found wrong number of agent nodes", nodes.size(), 1);
		String name = nodes.toArray(new String[0])[0];
		assertEquals("JmxManagementClient.getAgentNodeNames() is wrong", nodeName, name);

		// add listener for change of agent's lifecycle state
		agentClient = client.getAgentManagementClient(name, agentId);
		agentClient.addLifecycleStateListener(this);

		// add listener for change of agent node's agent list
		client.getAgentNodeManagementClient(name).addAgentsListener(this);
	}

	/**
	 * Tears down the test environment. It shuts down the agent node and closes the application 
	 * context.
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
	 * notifications about change of agent's lifecycle state and the last notification 
	 * about change of agent node's agent list.
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
	 * Tests if the agent can be correctly removed by using the agent management client. This includes
	 * the change to state CLEANED_UP and the removal from the agent node (incl. notification).
	 * @see JmxAgentManagementClient#removeAgent()
	 * @see #checkStateNotifications(String,String)
	 * @see #checkAgentListNotification(ArrayList)
	 */
	public void testRemove() {
		try {
			agentClient.removeAgent();
		} catch (Exception e) {
			fail("Error while removing agent");
		}
		assertEquals("JmxAgentManagementClient.removeAgent() doesn't remove agent from agent node", true, nodeRef.findAgents().isEmpty());
		checkStateNotifications("CLEANING_UP", "CLEANED_UP");
		
		// check notification
		checkAgentListNotification(new ArrayList<String>());
	}
	
	/**
	 * Tests if you get the name "TestAgent" of the agent by using the agent management client.
	 * @see JmxAgentManagementClient#getAgentName()
	 */
	public void testGetName() {
		String name = "";
		try {
			name = agentClient.getAgentName();
		} catch (Exception e) {
			fail("Error while getting agent's name");
		}
		assertEquals("JmxAgentManagementClient.getAgentName() is wrong", agentName, name);
	}
	
	/**
	 * Tests if the agent can change its lifecycle state by using the agent management client 
	 * (stop, cleanup, init, start) and notifications are sent to the listener.
	 * @see JmxAgentManagementClient#stopAgent()
	 * @see JmxAgentManagementClient#cleanupAgent()
	 * @see JmxAgentManagementClient#initAgent()
	 * @see JmxAgentManagementClient#startAgent()
	 * @see #checkStateNotifications(String,String)
	 * @see #checkState(String)
	 */
	public void testSetLifecycleState() {
		try {
			agentClient.stopAgent();
		} catch (Exception e) {
			fail("Error while stopping agent");
		}
		checkStateNotifications("STOPPING", "STOPPED");
		checkState("STOPPED");
	    
		try {
			agentClient.cleanupAgent();
		} catch (Exception e) {
			fail("Error while cleaning up agent");
		}
		checkStateNotifications("CLEANING_UP", "CLEANED_UP");
		checkState("CLEANED_UP");
	    
		try {
			agentClient.initAgent();
		} catch (Exception e) {
			fail("Error while initializing agent");
		}
		checkStateNotifications("INITIALIZING", "INITIALIZED");
		checkState("INITIALIZED");
	    
		try {
			agentClient.startAgent();
		} catch (Exception e) {
			fail("Error while starting agent");
		}
		checkStateNotifications("STARTING", "STARTED");
		checkState("STARTED");
	}

	/**
	 * Tests if you get the lifecycle state "STARTED" of the agent by using the agent management client.
	 * @see #checkState(String)
	 */
	public void testGetLifecycleState() {
		checkState("STARTED");
	}


	/**
	 * Checks the actual state of the agent and the state getting from the agent management client.
	 * @param intendedState the intended state of the agent
	 * @see JmxAgentManagementClient#getAgentState()
	 */
	protected void checkState(String intendedState) {
		// check actual state
		String state = nodeRef.findAgents().get(0).getState().toString();
		assertEquals("Wrong lifecycle state of agent", intendedState, state);		

		// check JMX interface
		state = "";
		try {
			state = agentClient.getAgentState();
		} catch (Exception e) {
			fail("Error while getting agent's state");
		}
		assertEquals("JmxAgentManagementClient.getAgentState() is wrong", intendedState, state);		
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
	 * Checks the last notification about changed agent list of the agent node.
	 * @param intendedList the intended agent list of the notification
	 */
	protected void checkAgentListNotification(ArrayList<String> intendedList) {
		assertEquals("Missing notification about changed agent list", intendedList, agentListNotification);
	}

}
