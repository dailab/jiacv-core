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
import java.util.List;
import java.lang.management.ManagementFactory;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;


/**
 * Test class for the NodeConfigurationMonitor.
 *
 * @author Silvan Kaiser
 */
public class NodeConfigurationMonitorBeanTest extends TestCase {
	
	
	// constants
	private static final String nodeName = "NodeConfigurationMonitorTestPlatform";
	
	// variables
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
	private static int testcount = 2; //Stores the number of tests in this testcase. Manual counter for deciding when to tear down the test agent node
	
	/**
	 * The file specified by this string is used as spring configuration for the new agent
	 * to be deployed in this test.
	 **/
	public static String NODE_SPRINGCONFIGFILE = "de/dailab/jiactng/agentcore/conf/agentTests.xml"; // 
	
	/**
	 * The file specified by this string is used as spring configuration for the new agent
	 * to be deployed in this test.
	 **/
	public static String TEST_SPRINGCONFIGFILE = "de/dailab/jiactng/agentcore/conf/LoadReporterAgent.xml";
	
	/**
	 * The owner of the agent added by this Test. Should be the user running this test
	 */
	public static String OWNER_STRING = System.getProperty("user.name");
	
	private JmxManagementClient jmxclient = null;
	private JmxAgentNodeManagementClient nodeclient = null;
	private JmxAgentManagementClient agentclient = null;
	
	String deployedagent = null; // stores the agentID of the agent to be deployed on the tests agent node.
	
	
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
			
			logger.debug("Setting up, this is the first test in this TestCase.");
			
			System.setProperty("jmx.invoke.getters", "");
			manager = new JmxManager();
			node = manager.getMgmtNameOfAgentNode(nodeName);
			mbs = ManagementFactory.getPlatformMBeanServer();
			
			// start node
			//this.startAgentNode(NODE_SPRINGCONFIGFILE);
			
			// enable Filter
			AttributeChangeNotificationFilter acnf = new AttributeChangeNotificationFilter();
			acnf.enableAttribute("Agents");
		} else {
			logger.debug("Skipping SetUP, this is not the first test in this testcase.");
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
			//logger.debug("This is the last test in this testcase, tearing down Agent Node.");
			//this.shutdownAgentNode();
			manager = null;
		} else {
			//logger.debug("This is not the last Test in this testcase, skipping tear down of the Agent Node.");
		}
	}
	
	/**
	 * Starts up the agent node used for these tests.
	 *
	 * @param configfile The path to the configuration file to use for the agent node.
	 **/
	protected void startAgentNode(String configfile){
		// Init properties
		System.setProperty("jiactng.rootconfigfile", configfile);
			
		// start application
		context = new ClassPathXmlApplicationContext(configfile);
		logger.debug("Agent Node has been created in context: " + context.toString());
		nodeRef = (SimpleAgentNode) context.getBean(nodeName);
		try {
			// connect to local JMX interface
			jmxclient = new JmxManagementClient();
			nodeclient = jmxclient.getAgentNodeManagementClient(nodeName);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Starts up the autosaved agent node config used for these tests.
	 *
	 * @param configfile The path to the configuration file to use for the agent node.
	 **/
	protected void startAgentNode(){
		// construct file name
		String configfile = NodeConfigurationMonitorBean.produceAutosaveConfigurationFileName(NODE_SPRINGCONFIGFILE);

		// start application
		context = new ClassPathXmlApplicationContext(configfile);
		logger.debug("Agent Node has been created in context: " + context.toString());
		nodeRef = (SimpleAgentNode) context.getBean(nodeName);
		try {
			// connect to local JMX interface
			jmxclient = new JmxManagementClient();
			nodeclient = jmxclient.getAgentNodeManagementClient(nodeName);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Closes down the agent node used for these tests.
	 **/
	protected void shutdownAgentNode(){
		try {
			nodeclient.shutdownAgentNode();
		} catch (Exception e){
			e.printStackTrace();
		}
		context.close();
		context = null;
		agentListNotification = null;
		agentId = null;
		nodeRef = null;
		nodeclient = null;
		jmxclient = null;
	}
		
	
	// Testing Methods
	
	/**
	 * Tests wether or not the NodeConfigurationMonitorBean of the tests Agent Node
	 * has initialized and started up.
	 **/
	
	public void testNodeConfigurationMonitorBeanStartup(){
		testcount--; // This is a test, reduce count
		
		// start node
		this.startAgentNode(NODE_SPRINGCONFIGFILE);
		
		try {
			String ncmbstate = nodeclient.getAgentNodeState();
			logger.debug("ncmbstate is " + ncmbstate);
			if (ncmbstate.compareTo("STARTED") == 0) {
				logger.debug("NodeConfigurationMonitorBean started successfully. Test passed.");
			} else {
				assert false;
			}
		} catch (Exception e){
			e.printStackTrace();
			assert false;
		}
		
		// shutdown Agent Node
		this.shutdownAgentNode();
	}
	
	/**
	 * Tests if an autosave configuration file is saved when shutting down the test Agent Node.
	 * So far the content of file is not evaluated, only existence and readability of the file.
	 **/
	public void testNodeConfigurationMonitorBeanShutdown(){
		testcount--; // This is a test, reduce count
		
		// start Agent Node
		this.startAgentNode(NODE_SPRINGCONFIGFILE);
		
		// execute test
		// shutdown Agent Node
		this.shutdownAgentNode();
		// try to find test Agent Nodes autosave config file
		String autofilename = NODE_SPRINGCONFIGFILE;
		autofilename = autofilename.substring(0,autofilename.indexOf(".xml"));
		logger.debug("autofilename truncated to: " + autofilename);
		File autoconfigfile = new File(autofilename + "_autosave.xml");
		autoconfigfile = new File(autoconfigfile.getName()); // Weird construction to get a handle without path, only the pure file name.
		logger.debug("Searching for: " + autoconfigfile.getPath());
		assert autoconfigfile.exists();
		assert autoconfigfile.canRead();
		
		logger.debug("NodeConfigurationMonitorBean shutdown successful. Test passed.");
		
	}
	
	
	/**
	 * Deploys an Agent on the test node, shuts down the node and reloads the autosave configuration. Checks
	 * wether or not the new deployed agent is still active on the node afterwards.
	 **/
	public void DEACTIVATEDtestSaveAndReloadSingleDeployment(){
		testcount--; // This is a test, reduce count
		
		// start Agent Node
		this.startAgentNode(NODE_SPRINGCONFIGFILE);
		
		String result = this.deployTestAgent();
		if (result != null){
			logger.debug("Test-Agent deployed successfully: " + result);
		} else {
			logger.debug("Unable to deploy Test-Agent.");
			assert false;
		}
		// shutdown the node to trigger autosave for the configuration
		this.shutdownAgentNode();
		logger.debug("First instance of the agent node has been shut down.");
		
		// restart the node in order to load the autosaved configuration
		logger.debug("Restarting agent node as second instance.");
		try {
			// try to find test Agent Nodes autosave config file
			String autofilename = NODE_SPRINGCONFIGFILE;
			autofilename = autofilename.substring(0,autofilename.indexOf(".xml"));
			File autoconfigfile = new File(autofilename + "_autosave.xml");
			this.startAgentNode(autoconfigfile.getName()); // Weird construction to get a name without path, only the pure file name.
		} catch (Exception e){
			e.printStackTrace();	
			assert false;
		}
		
		try {
			// connect to local JMX interface
			jmxclient = new JmxManagementClient();
			agentclient = jmxclient.getAgentManagementClient(nodeName, deployedagent);
			if (agentclient == null){
				logger.error("Unable to find added agent in restarted Agent Node, test failed.");
			} else {
				logger.debug("Connected to agentclient " + agentclient + " for testing agent state.");
			}
			// check if agent is up and running
			if (agentclient.getAgentState().compareTo("ACTIVE") == 0){
				logger.debug("Test testSaveAndReloadSingleDeployment successful.");
				assert true;
			} else {
				logger.warn("Deployed agent could not be found active on the 2nd instance of the tests agent node, therefore test fails.");
				assert false;	
			}
		} catch (Exception e) {
			e.printStackTrace();
			assert false;
		}
	}
	
	/**
	 * Auxiliary method for deploying a new agent on the test agent node.
	 *
	 * @return Returns the agentID that was itself returned from the agent deployment, null in case of errors.
	 **/ 
	public String deployTestAgent(){
		try {
			
			// Prepare Data for test agent
			// Spring Konfiguration einlesen			
			// File myspringconfig = new File(TEST_SPRINGCONFIGFILE);
			JARClassLoader jcl = new JARClassLoader();
			
			// get content of agent configuration file
			InputStream configInputStream = jcl.getResourceAsStream(TEST_SPRINGCONFIGFILE);
			
			logger.debug("Found config file " + TEST_SPRINGCONFIGFILE);
			
			// Get the size of the file
			long length = configInputStream.available();
			
			if (length > Integer.MAX_VALUE) {
				throw new IOException("File too large");
			}
			
			// Create the byte array to hold the data
			byte[] configuration = new byte[(int)length];
			
			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < configuration.length && (numRead=configInputStream.read(configuration, offset, configuration.length-offset)) >= 0) {
				offset += numRead;
			}
			
			// Ensure all the bytes have been read in
			if (offset < configuration.length) {
				throw new IOException("Could not completely read file " + TEST_SPRINGCONFIGFILE);
			}
			
			configInputStream.close();
			
			// create dummy list of application-specific jars
			ArrayList<JARMemory> libraries = new ArrayList<JARMemory>();
			
			// Testagenten installieren
			List<String> l = nodeclient.addAgents(configuration, libraries, OWNER_STRING);
			if (l.isEmpty()){
				return null;
			} else {
				deployedagent = l.get(0);
				return deployedagent;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
}
