package de.dailab.ccact.tools.agentunit;

//imports

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.util.jar.JARClassLoader;
import de.dailab.jiactng.agentcore.util.jar.JARMemory;

/**
 * This class is designed to create a testable environment for JUnit tests. The constructor receives a configuration
 * file parameter where the Agent Node configuration to be used in the tests is specified. An Agent Node of that type is
 * instantiated and an ActionInvokeAgent added to it. A bean in that agent provides direct synchronous access to the
 * actions available on the Agent Node with the method: <code>invoke({@link String}, {@link Serializable}[])</code>. <br>
 * Finally a <code>stop()</code> method can be called to shut down the Agent Node after the test has been finished.
 * 
 * Usage:<br>
 * Apply this class as a helper in your JUnit tests by instantiating in your test case, calling to the actions to be
 * tested afterwards and finally stopping the ActionTesterNode class.
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
	private final String spring_config;

	/**
	 * The application context of this node.
	 */
	protected ClassPathXmlApplicationContext context;

	/**
	 * the agent node
	 */
	protected SimpleAgentNode agentNode;
	// protected IAgent invokeactionagent;
	protected InvokeActionBean invokeactionbean;

	/**
	 * default agent node logger
	 */
	protected Log log = LogFactory.getLog(ActionTesterNode.class);

	/**
	 * Creates an ActionTesterNode from the given configuration file
	 * 
	 * @param spring_node_config_param
	 *           path to an JIAC AgentNode configuration file containting an Agent Node description castable to the
	 *           SimpleAgentNode class.
	 * @param nodename
	 *           The name of the Agent Node specified in spring_node_config_param
	 */
	public ActionTesterNode(final String spring_node_config_param, final String nodename) {

		this.spring_config = spring_node_config_param;
		this.context = new ClassPathXmlApplicationContext(this.spring_config);
		this.context.start();

		// fetch Agent Node handle
		this.agentNode = (SimpleAgentNode) this.context.getBean(nodename, SimpleAgentNode.class);

		((Log4JLogger) this.log).getLogger().setLevel(Level.toLevel(this.agentNode.getLogLevel()));

		// Add InvokeActionAgent to AgentNode
		try {
			final JARClassLoader jcl = new JARClassLoader();
			// get content of agent configuration file
			final InputStream cfis = jcl.getResourceAsStream(ActionTesterNode.INVOKE_ACTION_AGENT_CONFIGURATION_FILE);

			if (this.log.isDebugEnabled()) {
				this.log.debug("Reading INVOKE_ACTION_AGENT_CONFIGURATION_FILE with up to " + cfis.available() + " bytes.");
			}

			final byte[] byteconfig = new byte[cfis.available()];
			final int bytesread = cfis.read(byteconfig, 0, cfis.available());

			if (this.log.isDebugEnabled()) {
				this.log.debug("Read " + bytesread + " bytes. The byteconfig content is:" + byteconfig.toString());
			}

			// add InvokeActionAgent
			final List<String> createdagents = this.agentNode.addAgents(byteconfig, new ArrayList<JARMemory>(), this.agentNode.getOwner());
			final String invokeactionagentid = createdagents.get(0); // Only one agent added, therefore no search
			// implemented

			final List<IAgent> iagentslist = this.agentNode.findAgents();
			final Iterator<IAgent> ialit = iagentslist.iterator();
			IAgent invokeactionagent;
			while (ialit.hasNext()) {
				invokeactionagent = ialit.next();
				if (invokeactionagent.getAgentId().equalsIgnoreCase(invokeactionagentid)) {
					invokeactionagent.init();
					invokeactionagent.start();
					this.invokeactionbean = invokeactionagent.findAgentBean(InvokeActionBean.class);
					// for (final IAgentBean iab : beanList) {
					// if (iab instanceof InvokeActionBean) {
					// this.invokeactionbean = (InvokeActionBean) iab;
					// this.log.debug("Located Bean: " + this.invokeactionbean);
					// break;
					// }
					// }
				}
			}
		}
		catch (final Exception e) {
			this.log.error("ActionTesterNode cannot be initalized!", e);
		}

	}

	/**
	 * @return the agentNode
	 */
	public SimpleAgentNode getAgentNode() {
		return this.agentNode;
	}

	public Serializable[] invoke(final String serviceName, final Serializable[] serviceParameter) {

		try {
			if (this.log.isDebugEnabled()) {
				this.log.debug("Bean is: " + this.invokeactionbean.toString());
			}
			return this.invokeactionbean.invokeAction(serviceName, serviceParameter);
		}
		catch (final RuntimeException e) {
			this.log.error(e);
			throw e;
		}

	}

	public void invokeAndForget(final String serviceName, final Serializable[] serviceParameter) {

		try {
			if (this.log.isDebugEnabled()) {
				this.log.debug("Bean is: " + this.invokeactionbean.toString());
			}
			this.invokeactionbean.invokeAndForget(serviceName, serviceParameter);
		}
		catch (final Exception e) {
			this.log.error(e);
		}

	}

	public void stop() {

		try {
			this.agentNode.shutdown();
		}
		catch (final LifecycleException e) {
			this.log.error("exception shutting down Node.", e);
		}

		this.context.stop();

	}

}
