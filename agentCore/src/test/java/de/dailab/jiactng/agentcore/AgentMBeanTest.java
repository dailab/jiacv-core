package de.dailab.jiactng.agentcore;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import javax.management.openmbean.CompositeData;
import javax.management.relation.MBeanServerNotificationFilter;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.management.jmx.JmxManager;

import junit.framework.TestCase;

/**
 * This testcase tests the JMX based management interface of an agent.
 *
 * @author Jan Keiser
 */
public class AgentMBeanTest extends TestCase implements NotificationListener {

	private final String nodeName = "myNode";
	private final String agentName = "TestAgent";
	private ObjectName node = null;
	private ObjectName agent = null;
	private MBeanServer mbs = null;
	private ClassPathXmlApplicationContext context = null;
	private String currentLifecycleState = "";
	private String previousLifecycleState = "";
	private String registrationNotification = "";
	private ArrayList<String> agentListNotification = null;
	private SimpleAgentNode nodeRef = null;
	private String agentId = null;
	private JmxManager manager = null;

	/**
	 * Sets up the test environment. It enables the JMX interface, registers as listener
	 * for agent's (de)registration, starts the application (platform "myNode" with 
	 * one agent "TestAgent") defined in "agentTests.xml" and registers as listener for
	 * changes of the agent's lifecycle state and agent node's agent list.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("jmx.invoke.getters", "");
		manager = new JmxManager();
		mbs = ManagementFactory.getPlatformMBeanServer();

		// add listener for (de)registration of the agent
		MBeanServerNotificationFilter msnf = new MBeanServerNotificationFilter();
		msnf.enableAllObjectNames();
		mbs.addNotificationListener(new ObjectName("JMImplementation:type=MBeanServerDelegate"), this, msnf, null);

		// start application
		context = new ClassPathXmlApplicationContext(
			"de/dailab/jiactng/agentcore/agentTests.xml");
		nodeRef = (SimpleAgentNode) context.getBean(nodeName);
		node = manager.getMgmtNameOfAgentNode(nodeRef.getUUID());
		agentId = ((IAgent) context.getBean(agentName)).getAgentId();
		agent = manager.getMgmtNameOfAgent(nodeRef.getUUID(), agentId);
		ArrayList<String> agentList = new ArrayList<String>();
		agentList.add(agentId);
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
		agentId = null;
		nodeRef = null;
		manager = null;
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
				agentListNotification = (ArrayList<String>) ((AttributeChangeNotification) notification).getNewValue();
			}
		} else if (notification instanceof MBeanServerNotification) {
			ObjectName bean = ((MBeanServerNotification)notification).getMBeanName();
			if (bean.getDomain().equals("de.dailab.jiactng") && 
					(bean.getKeyPropertyList().size()==4) &&
					bean.getKeyPropertyList().containsKey("agent")) {
				// an agent was (de)registered
				registrationNotification = notification.getType();
			}
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
		checkAgentListNotification(new ArrayList<String>());
	}

	/**
	 * Tests if you get the name "TestAgent" of the agent by using the JMX interface.
	 */
	public void testGetName() {
		try {
			String name = (String) manager.getAttributeOfAgent(nodeRef.getUUID(), agentId, "AgentName");
			assertEquals("AgentMBean.getAgentName is wrong", agentName, name);
		} catch (Exception e) {
			fail("Error while getting agent's name");
		}
	}

	/**
	 * Tests if you get the name "dummyBean" for the agent bean of the agent by using the JMX interface.
	 */
	public void testGetAgentBeanNames() {
		try {
			List<String> names = (List<String>) manager.getAttributeOfAgent(nodeRef.getUUID(), agentId, "AgentBeanNames");
			assertEquals("AgentMBean.getAgentBeanNames is wrong", Arrays.asList(new String[] {"dummyBean"}), names);
		} catch (Exception e) {
			fail("Error while getting names of agent beans of the agent");
		}
	}

	/**
	 * Tests if you get an empty list as names of actions of the agent by using the JMX interface.
	 */
	public void testGetActionNames() {
		try {
			List<String> names = (List<String>) manager.getAttributeOfAgent(nodeRef.getUUID(), agentId, "ActionNames");
			assertEquals("AgentMBean.getActionNames is wrong", new ArrayList<String>(), names);
		} catch (Exception e) {
			fail("Error while getting names of actions of the agent");
		}
	}

	/**
	 * Tests if you get "de.dailab.jiactng.agentcore.knowledge.Memory" as class, "org.sercho.masp.space.ReflectiveObjectMatcher" as matcher, and "org.sercho.masp.space.ReflectiveObjectUpdater" as updater of the agent's memory by using the JMX interface.
	 */
	public void testGetMemoryData() {
		try {
			CompositeData data = (CompositeData) manager.getAttributeOfAgent(nodeRef.getUUID(), agentId, "MemoryData");
			assertEquals("AgentMBean.getMemoryData returns wrong class item", "de.dailab.jiactng.agentcore.knowledge.Memory", data.get("class"));
			assertEquals("AgentMBean.getMemoryData returns wrong matcher item", "org.sercho.masp.space.ReflectiveObjectMatcher", data.get("matcher"));
			assertEquals("AgentMBean.getMemoryData returns wrong updater item", "org.sercho.masp.space.ReflectiveObjectUpdater", data.get("updater"));
		} catch (Exception e) {
			fail("Error while getting agent's memory data");
		}
	}

	/**
	 * Tests if you get "de.dailab.jiactng.agentcore.execution.SimpleExecutionCycle" as class name of the agent's execution cycle by using the JMX interface.
	 */
	public void testGetExecutionCycleClass() {
		try {
			String name = (String) manager.getAttributeOfAgent(nodeRef.getUUID(), agentId, "ExecutionCycleClass");
			assertEquals("AgentMBean.getExecutionCycleClass is wrong", "de.dailab.jiactng.agentcore.execution.SimpleExecutionCycle", name);
		} catch (Exception e) {
			fail("Error while getting class of agent's execution cycle");
		}
	}

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
			manager.invokeAgent(nodeRef.getUUID(), agentId, action, new Object[] {}, new String[] {});
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
			state = (String) manager.getAttributeOfAgent(nodeRef.getUUID(), agentId, "LifecycleState");
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
		assertEquals("Missing notification about agent registration", intendedType, registrationNotification);
	}

	/**
	 * Checks the last notification about changed agent list of the agent node.
	 * @param intendedList the intended agent list of the notification
	 */
	protected void checkAgentListNotification(ArrayList<String> intendedList) {
		assertEquals("Missing notification about changed agent list", intendedList, agentListNotification);
	}

}
