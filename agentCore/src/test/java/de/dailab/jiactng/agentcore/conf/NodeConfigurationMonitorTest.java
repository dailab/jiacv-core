package de.dailab.jiactng.agentcore.conf;

import junit.framework.TestCase;


import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.util.jar.JARMemory;

import de.dailab.jiactng.agentcore.management.jmx.JmxManager;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentNodeManagementClient;
import de.dailab.jiactng.agentcore.util.jar.JARClassLoader;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;

import java.util.ArrayList;
import java.lang.management.ManagementFactory;



/**
 * Test class for the NodeConfigurationMonitor.
 *
 * @author Silvan Kaiser
 */
public class NodeConfigurationMonitorTest extends TestCase {
	
	
	// constants
	private static final String nodeName = "NodeConfigurationMonitorTestPlatform";
	private static String agentId = null;
	private static ObjectName node = null;
	private static MBeanServer mbs = null;
	private static ClassPathXmlApplicationContext context = null;
	private static ArrayList<String> agentListNotification = null;
	private static SimpleAgentNode nodeRef = null;
	private static JmxManager manager = null;
	
	static  Logger logger;
	static  Level myloglevel=Level.DEBUG;
	
	 
	 
	private static boolean doinit = true; // stores info about wether or not this is the first test in this testcase
	private static int testcount = 1; //Stores the number of tests in this testcase. Manual counter for deciding when to tear down the test agent node
	
	/**
	 * The file specified by this string is used as spring configuration for the new agent
	 * to be deployed in this test.
	 **/
	public static String TEST_SPRINGCONFIGFILE = "de/dailab/jiactng/agentcore/conf/agentTests.xml";
	
	/**
	 * The owner of the agent added by this Test. Should be the user running this test
	 */
	public static String OWNER_STRING = System.getProperty("user.name");
	
	
	
	
	/**
	 * Sets up the test environment. It enables the JMX interface, registers as listener
	 * for agent node's (de)registration, starts the application (agent node "myTestPlatform" 
	 * with one agent "TestAgent") defined in "agentTests.xml" and registers as listener 
	 * for changes of the agent node's lifecycle state.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		
		
		if (doinit){
			doinit = false; // initialize now but no more afterwards
			
			// init logging
			logger = Logger.getLogger("SchedulerMethodExposingBeanTest");
			logger.setLevel(myloglevel);
			
			logger.debug("Setting up Agent Node, this is the first test in this TestCase.");
			
			System.setProperty("jmx.invoke.getters", "");
			manager = new JmxManager();
			node = manager.getMgmtNameOfAgentNode(nodeName);
			mbs = ManagementFactory.getPlatformMBeanServer();
			
			// start application
			context = new ClassPathXmlApplicationContext(TEST_SPRINGCONFIGFILE);
			nodeRef = (SimpleAgentNode) context.getBean(nodeName);
			// agentId = nodeRef.getAgents().get(0); // Ersten Agenten auf dem Agent Node auslesen
			// ArrayList<String> agentList = new ArrayList<String>();
			// agentList.add(agentId);
			// agentListNotification = agentList;
			
			// logger.debug("Started agent " + agentId + " at Node " + nodeName);
			
			
			// add listener for change of agent node's agent list
			AttributeChangeNotificationFilter acnf = new AttributeChangeNotificationFilter();
			acnf.enableAttribute("Agents");
			// mbs.addNotificationListener(node, this, acnf, null);
		} else {
			logger.debug("Skipping Agent Node SetUP, this is not the first test in this testcase.");
		}
		
	}
	
	/**
	 * Tears down the test environment. It deregisters as listener for changes of the 
	 * agent node's lifecycle state, shuts down the agent node, deregisters as listener
	 * for agent node's (de)registration and closes the application context.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		
		if (testcount==0){
			logger.debug("This is the last test in this testcase, tearing down Agent Node.");
			
			/**
			 if ((mbs != null) && (node != null)){
			 if (mbs.isRegistered(node)) {
			 mbs.removeNotificationListener(node, this);
			 nodeRef.shutdown();
			 } else {
			 logger.warn(node + " was not registered in " + mbs + " when tearing down test.");
			 }
			 mbs = null;
			 node = null;
			 }
			 **/
			context.close();
			context = null;
			agentListNotification = null;
			agentId = null;
			nodeRef = null;
			manager = null;
		} else {
			logger.debug("This is not the last Test in this testcase, skipping tear down of the Agent Node.");
		}
	}
	
	
	public void testTest(){
		assert true;
	}
	
}
