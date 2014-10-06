package de.dailab.jiactng.agentcore.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import de.dailab.jiactng.agentcore.AbstractAgentBean;

/**
 * Quickly build a configuration of bean(s), which set on generic agent, which
 * run on a default agent node. It offers the possibility to test a
 * functionality of bean, with minor complexity.
 * 
 * 
 * usage:
 * 
 * new AgentQuickStarter(Bean1.class);
 * 
 * Creates one agent with name "QuickBeanStarterAgent", which runs on a default
 * agent node with name "QuickBeanStarterNode". The agent contains the bean
 * "Bean1". "Bean1" has the execution interval with default value 1000ms.
 * 
 * new AgentQuickStarter(3, 800, "BeanNode", "Bean123Agent", Bean1.class,
 * Bean2.class, Bean3.class).start();
 * 
 * Creates three agents with name "Bean123Agent", which runs on a default node
 * with name "BeanNode". The generic agents each contain "Bean1", "Bean2" and
 * "Bean3". Each bean has the execution interval 800ms.
 * 
 * Cleaned up by kuester
 * 
 * TODO Problems: Not sure whether the node is started correctly. - 2011-10-28
 * 16:27:17,440 WARN [a-15187de812be.null doInit 228] - no transports available
 * yet! - Agents are started, Actions are advertised, but can not be invoked!
 * FIXED commBean wasn't properly prepared
 * 
 * - sometimes, ASGARD shows two agents Jakob told me, that is a bug in ASGARD.
 * 
 * @author jd
 */
public class AgentQuickStarter {

	/** execution interval of the agent bean(s), default is 1 second */
	private int executeInterval = 1000;

	/** The list of given agent beans */
	private Class<? extends AbstractAgentBean>[] agentBeans;

	/** The default agent name */
	private String agentName = "QuickBeanStarterAgent";

	/** The default node name */
	private String nodeName = "QuickBeanStarterNode";

	/** The number of generic agents, default is one */
	private int numberOfAgents = 1;

	private static final String TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
			+ "<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">\n\n"
			+ "<!-- This is a Spring application context. There are imports to other Spring\n"
			+ "\tconfigurations and configured beans. -->\n"
			+ "<beans>\n\n"
			+ "\t<import resource=\"classpath:de/dailab/jiactng/agentcore/conf/Agent.xml\" />\n"
			+ "\t<import resource=\"classpath:de/dailab/jiactng/agentcore/conf/AgentNode.xml\" />\n\n\n"
			+ "\t<bean name=\"[NODENAME]\" parent=\"NodeWithJMX\">\n"
			+ "\t\t<property name=\"agents\">\n"
			+ "\t\t\t<list>\n"
			+ "\t\t\t\t<!-- [PUT_REF_AGENTS_HERE] -->\n"
			+ "\t\t\t</list>\n"
			+ "\t\t</property>\n"
			+ "\t</bean>\n\n"
			+ "\t<bean name=\"[AGENTNAME]\" parent=\"SimpleAgent\" singleton=\"false\" >\n"
			+ "\t\t<property name=\"agentBeans\">\n"
			+ "\t\t\t<list>\n"
			+ "\t\t\t\t<!-- [PUT_REF_BEANS_HERE] -->\n"
			+ "\t\t\t</list>\n"
			+ "\t\t</property>\n"
			+ "\t</bean>\n"
			+ "<!-- [PUT_BEAN_DEF_HERE] -->\n" + "</beans>";

	/**
	 * Simple constructor for a list of agent beans
	 * 
	 * @param agentBeans
	 *            The list of agent-beans which are added to the generic agent
	 */
	public AgentQuickStarter(Class<? extends AbstractAgentBean>... agentBeans) {

		if (agentBeans.length > 0) {
			this.agentBeans = agentBeans;
		} else {
			System.err.println("Given agentbean list is empty!");
		}
	}

	/**
	 * Advanced constructor for a list of agent beans
	 * 
	 * @param numberOfAgents
	 *            number of needed generic agents.
	 * @param executeInterval
	 *            execution interval of bean(s) in milliseconds.
	 * @param customNodeName
	 *            The custom name of the default node.
	 * @param customAgentName
	 *            The custom name of the generic agent.
	 * @param agentBeans
	 *            The list of agent-beans which are added to the generic
	 *            agent(s).
	 */
	public AgentQuickStarter(int numberOfAgents, int executeInterval,
			String customNodeName, String customAgentName,
			Class<? extends AbstractAgentBean>... agentBeans) {

		this(agentBeans);
		this.nodeName = customNodeName;
		this.agentName = customAgentName;
		this.executeInterval = executeInterval;
		this.numberOfAgents = numberOfAgents;

		// Method[] methods = agentBeans[0].getClass().getMethods();
		// for (int i = 0; i < methods.length; i++) {
		// if(methods[i].getName().startsWith("set")){
		// System.out.println(methods[i].getName());
		// methods[i].getReturnType().equals()
		// }
		// }
	}

	/**
	 * Start the Node and the Agent after preparation
	 */
	public void start() {
		/* capsuling the configuration parameters */
		ConfigParamters paramters = new ConfigParamters(numberOfAgents,
				executeInterval, nodeName, agentName);
		/* create temporary file */
		try {
		    File tempFile = File.createTempFile("temp", ".xml");
			tempFile.deleteOnExit();
			
			/* copy template file and prepare the new spring configuration */
			TemplateHandler templateHandler = new TemplateHandler();
			templateHandler.copyTemplateFileInTempFile(tempFile);
			templateHandler.prepareConfiguration(tempFile, paramters, agentBeans);
			
			/* start the node with created spring configuration */
			new FileSystemXmlApplicationContext("file:" + tempFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This class is capsule of all given parameters. They are used to create a
	 * temporary spring configuration
	 * 
	 * @author jd
	 * 
	 */
	private class ConfigParamters {

		/** The number of agents */
		private int numberOfAgents;

		/** The execute interval of agent bean(s) */
		private int executionInterval;

		/** The name of the agent node */
		private String nodeName;

		/** The name of the agent(s) */
		private String agentName;

		/**
		 * Constructs a new config parameters object
		 * 
		 * @param numberOfAgents
		 * @param executionInterval
		 * @param nodeName
		 * @param agentName
		 */
		public ConfigParamters(int numberOfAgents, int executionInterval,
				String nodeName, String agentName) {
			this.numberOfAgents = numberOfAgents;
			this.executionInterval = executionInterval;
			this.nodeName = nodeName;
			this.agentName = agentName;
		}

		public int getNumberOfAgents() {
			return numberOfAgents;
		}

		public int getExecutionInterval() {
			return executionInterval;
		}

		public String getNodeName() {
			return nodeName;
		}

		public String getAgentName() {
			return agentName;
		}

	}

	private class TemplateHandler {

		/**
		 * Copy the template file content in a temporary file
		 * 
		 * @param templateFileStream
		 * @param tempFile
		 */
		public void copyTemplateFileInTempFile(File tempFile) {
			/* Write template in temporary file */
			BufferedWriter bfw = null;
			try {
				bfw = new BufferedWriter(new FileWriter(tempFile));
				bfw.write(TEMPLATE);
				bfw.flush();
			} catch (IOException e3) {
				e3.printStackTrace();
			} finally {
				try {
				    if(bfw != null) {
				        bfw.close();
				    }
				} catch (IOException e4) {
					e4.printStackTrace();
				}
			}
		}

		/**
		 * Modify the temporary configuration file, with the result that the
		 * given parameters and beans are defined in the configuration.
		 * 
		 * @param newConfigurationFile
		 * @param paramters
		 * @param beans
		 */
		public void prepareConfiguration(File newConfigurationFile,
				ConfigParamters paramters,
				Class<? extends AbstractAgentBean>[] beans) {
			ConfigXmlParser parser = new ConfigXmlParser(newConfigurationFile,
					paramters, beans);
			if (!parser.prepareConfig()) {
				System.err.println("Configuration Failed");
			}

		}
	}

	class ConfigXmlParser {

		static final String ELEMENT_BEAN_LABEL = "bean";
		static final String ELEMENT_BEANS_LABEL = "beans";
		static final String ELEMENT_PROPERTY_LABEL = "property";

		static final String ATTRIBUT_CLASS_LABEL = "class";
		static final String ATTRIBUT_NAME_LABEL = "name";

		static final String ATTRIBUT_VALUE_LABEL = "value";
		static final String ATTRIBUT_SINGELTON_LABEL = "singleton";

		static final String PROPERTY_ATTRIBUT_EXECINTERVAL_NAME_VALUE = "executeInterval";
		static final String PROPERTY_ATTRIBUT_LOGLEVEL_NAME_VALUE = "logLevel";

		File configFile;
		ConfigParamters paramters;
		Class<? extends AbstractAgentBean>[] beans;

		public ConfigXmlParser(File configFile, ConfigParamters paramters,
				Class<? extends AbstractAgentBean>[] beans) {
			this.configFile = configFile;
			this.paramters = paramters;
			this.beans = beans;
		}

		public boolean prepareConfig() {

			try {
				DocumentBuilderFactory fact = DocumentBuilderFactory
						.newInstance();
				fact.setValidating(true);
				fact.setIgnoringElementContentWhitespace(true);
				DocumentBuilder builder;

				builder = fact.newDocumentBuilder();

				Document doc = builder.parse(configFile);

				if (!processPreperation(doc)) {
					return false;
				} else {
					writeDocumentSerializer(doc);
				}

			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}

		private boolean processPreperation(Document doc) {

			// create bean definitions
			NodeList springBeansList = doc
					.getElementsByTagName(ELEMENT_BEANS_LABEL);

			if (springBeansList.getLength() > 1) {
				return false;
			} else {
				/* write bean definitions */
				Node beansNode = springBeansList.item(0);
				for (int i = 0; i < beans.length; i++) {

					/*
					 * create: ' <bean name="[beans[i].getSimpleName()]"
					 * class="[beans[i].getName()]" > ... </bean> '
					 */
					Element beanDef = doc.createElement(ELEMENT_BEAN_LABEL);
					beanDef.setAttribute(ATTRIBUT_NAME_LABEL,
							beans[i].getSimpleName());
					beanDef.setAttribute(ATTRIBUT_CLASS_LABEL,
							beans[i].getName());
					beanDef.setAttribute(ATTRIBUT_SINGELTON_LABEL, "false");

					/*
					 * create: ' <property name="executeInterval"
					 * value="[paramters.getExecutionInterval()]" /> '
					 */
					Element property1 = doc
							.createElement(ELEMENT_PROPERTY_LABEL);
					property1.setAttribute(ATTRIBUT_NAME_LABEL,
							PROPERTY_ATTRIBUT_EXECINTERVAL_NAME_VALUE);
					property1.setAttribute(ATTRIBUT_VALUE_LABEL,
							"" + paramters.getExecutionInterval());
					beanDef.appendChild(property1);

					/* create: ' <property name="logLevel" value="info" /> ' */
					Element property2 = doc
							.createElement(ELEMENT_PROPERTY_LABEL);
					property2.setAttribute(ATTRIBUT_NAME_LABEL,
							PROPERTY_ATTRIBUT_LOGLEVEL_NAME_VALUE);
					property2.setAttribute(ATTRIBUT_VALUE_LABEL, "INFO");
					beanDef.appendChild(property2);

					beansNode.appendChild(beanDef);
				}

			}
			/* Add ref beans to Agent(s) and replace agent name */
			NodeList springBeanList = doc
					.getElementsByTagName(ELEMENT_BEAN_LABEL);
			Node agentDef = null;
			/* search for Agent definition */
			final int len = springBeanList.getLength();
			for (int l = 0; l < len; l++) {
				NamedNodeMap attrList = springBeanList.item(l).getAttributes();
				String nameTextContent = attrList.getNamedItem(
						ATTRIBUT_NAME_LABEL).getTextContent();
				String parentTextContent = attrList.getNamedItem("parent")
						.getTextContent();
				if (nameTextContent.equals("[AGENTNAME]")
						&& parentTextContent.equals("SimpleAgent")) {
					agentDef = springBeanList.item(l);
					break;
				}
			}

			if (agentDef == null) {
				return false;
			} else {
				/* replace name value */
				agentDef.getAttributes().getNamedItem(ATTRIBUT_NAME_LABEL)
						.setTextContent(paramters.getAgentName());

				/* Add ref beans to Agent */
				Node propertyChild = agentDef.getFirstChild();
				Node listChild = propertyChild.getFirstChild();

				for (int i = 0; i < beans.length; i++) {

					Element beanRef = doc.createElement("ref");
					beanRef.setAttribute(ELEMENT_BEAN_LABEL,
							beans[i].getSimpleName());
					listChild.appendChild(beanRef);

				}
				// /* Add CommunicationBean to Agent */
				// Element commBeanRef = doc.createElement("ref");
				// commBeanRef.setAttribute(ELEMENT_BEAN_LABEL,
				// "CommunicationBean");
				// listChild.appendChild(commBeanRef);

				/* Get AgentNode definition */
				Node agentNodeDef = springBeanList.item(0);
				/* replace name value */
				agentNodeDef.getAttributes().getNamedItem(ATTRIBUT_NAME_LABEL)
						.setTextContent(paramters.getNodeName());

				Node agentNodePropertyChild = agentNodeDef.getFirstChild();
				Node agentListChild = agentNodePropertyChild.getFirstChild();

				/* Add ref agents to AgentNode */
				for (int i = 0; i < paramters.getNumberOfAgents(); i++) {

					Element beanRef = doc.createElement("ref");
					beanRef.setAttribute(ELEMENT_BEAN_LABEL,
							paramters.getAgentName());
					agentListChild.appendChild(beanRef);

				}

			}
			return true;
		}

		/**
		 * Write the new spring configuration file in the temporary file.
		 * 
		 * @param doc
		 */
		private void writeDocumentSerializer(Document doc) {
			BufferedWriter writer = null;

			try {
				writer = new BufferedWriter(new FileWriter(configFile));

				DOMImplementation domImplementation = doc.getImplementation();

				if (domImplementation.hasFeature("LS", "3.0")
						&& domImplementation.hasFeature("Core", "2.0")) {

					DOMImplementationLS impl = (DOMImplementationLS) domImplementation
							.getFeature("LS", "3.0");

					LSOutput lsOutput = impl.createLSOutput();
					lsOutput.setEncoding("UTF-8");

					lsOutput.setCharacterStream(writer);

					LSSerializer lsSerializer = impl.createLSSerializer();

					DOMConfiguration domConfiguration = lsSerializer
							.getDomConfig();
					if (domConfiguration.canSetParameter("format-pretty-print",
							Boolean.TRUE)) {

						domConfiguration.setParameter("format-pretty-print",
								Boolean.TRUE);

						lsSerializer.write(doc, lsOutput);

					} else {
						throw new RuntimeException(
								"DOMConfiguration 'format-pretty-print' parameter isn't settable.");
					}
				} else {
					throw new RuntimeException(
							"DOM 3.0 LS and/or DOM 2.0 Core not supported.");
				}

			} catch (IOException e) {
				System.err.println("IO ERROR: " + e.getMessage());
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
                    if (writer != null) {
                        writer.flush();
					    writer.close();
                    }
					System.out.println("Configuration completed!");
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}
	}
}
