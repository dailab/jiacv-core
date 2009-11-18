package de.dailab.ccact.tools.agentunit;

//imports


import java.io.File;
import java.io.Serializable;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

import de.dailab.ccact.tools.agentunit.InvokeActionBean;


/**
 * This class is designed to create a testable environment for JUnit tests. 
 * The constructor receives a configuration file parameter where the Agent Node configuration
 * to be used in the tests is specified. An Agent Node of that typ is instantiated and
 * a special agent added to it. A bean in that agent provides direct synchronous access to 
 * the actions available on the Agent Node with the method: 
 * <code>invoke({@link String}, {@link Serializable}[])</code>. <br>
 * Finally a <code>stop()</code> method can be called 
 * to shut down the Agent Node previously instantiated.
 * 
 * Usage:<br>
 * Apply this class as a helper in your JUnit tests by instantiating in your test case,
 * calling to the actions to be tested afterwards and finally stopping the ActionTesterNode class.
 * 
 * @author Michael Burkhardt
 * @see File
 * @see Serializable
 * @see SimpleAgentNode
 * @see ClassPathXmlApplicationContext
 */
public class ActionTesterNode {

	// const
	private static String INVOKE_ACTION_AGENT_CONFIGURATION_FILE = "de/dailab/ccact/tools/agentunit/invoke_action_agent_config.xml";

	// var
	private String spring_config;

	private ClassPathXmlApplicationContext context;
	private SimpleAgentNode agentNode;
	private InvokeActionBean invokeactionbean;
	private Log log;

	/**
	 * Creates an ActionTesterNode from the given configuration file
	 * @param spring_node_config_param path to an JIAC AgentNode configuration file containting an Agent Node description castable to the SimpleAgentNode class.
	 * @param nodename The name of the Agent Node specified in spring_node_config_param 
	 */
	public ActionTesterNode(String spring_node_config_param, String nodename) {
		spring_config = spring_node_config_param;
		context = new ClassPathXmlApplicationContext(spring_config);
		context.start();

		// fetch Agent Node handle
		agentNode = (SimpleAgentNode) context.getBean(nodename);

		log = LogFactory.getLog(ActionTesterNode.class);

		((Log4JLogger) log).getLogger().setLevel(Level.toLevel( agentNode.getLogLevel() ));

		// Add InvokeActionAgent to AgentNode
		try {
			FileInputStream cfis = new FileInputStream(new File(INVOKE_ACTION_AGENT_CONFIGURATION_FILE));
			byte[] byteconfig = new byte[cfis.available()];
			cfis.read(byteconfig);
			List<String> createdagents = agentNode.addAgents(byteconfig, null,agentNode.getOwner());
			String invokeactionagentid = createdagents.get(0); // Only one agent added, therefore no search implemented

			// fetch ServiceDeployAgentBean
			List<IAgent> iagentslist= agentNode.findAgents();
			Iterator<IAgent> ialit = iagentslist.iterator();
			IAgent invokeactionagent;
			while (ialit.hasNext()){
				invokeactionagent = ialit.next();
				if (invokeactionagent.getAgentId().equalsIgnoreCase(invokeactionagentid)){
					List<IAgentBean> beanList = invokeactionagent.getAgentBeans();
					for (IAgentBean iab : beanList) {
						if (iab instanceof InvokeActionBean) {
							invokeactionbean = (InvokeActionBean) iab;
						}
					}
				}
			}
		} catch (Exception e){
			log.error("ActionTesterNode cannot be initalized!", e);
		}

	}




	public Serializable[] invoke(String jadlServiceName, Serializable[] serviceParameter) {

		try {
			return invokeactionbean.invokeAction(jadlServiceName, serviceParameter);
		}
		catch (Exception e) {
			return new Serializable[]{};
		}

	}

	public void stop() {

		try {
			agentNode.shutdown();
		}
		catch (LifecycleException e) {
			log.error("exception shutting down platform", e);
		}

		context.stop();

	}

}