package de.dailab.jiactng.agentcore.conf;

// imports
import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentNodeManagementClient;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.AttributeChangeNotification;

import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.Content;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Document; 
import org.jdom.Element;
import org.jdom.JDOMFactory;


/**
 * The NodeConfigurationMonitor saves the current Spring configuration of the Agent Node
 * to harddisk in order to allow restarting the node with it's current configuration.
 *
 * @author Silvan Kaiser
 */
public class NodeConfigurationMonitorBean extends AbstractAgentNodeBean implements NotificationListener{
	
	// variables
	boolean initsuccess = true;
	Document configdocument = null;
	String configfilename = null; // stores the filename of the configuration to be monitored.
	String autoconfigfilename = null; // stores the filename of the autosave configuration to be saved.
	boolean shuttingdown = false;
	JmxManagementClient jmclient = null;
	JmxAgentNodeManagementClient nodeclient = null;
	
	HashMap<String, String> agentIDtoName = null;
	
	
	/**
	 * Adds th Initialization of the DOM object for the Agent Nodes configuration
	 * to the startup sequence for this AgentNodeBean.
	 * Reads the property spring.rootconfigfile from the system properties
	 * in order to locate the current spring root config file.
	 **/
	public void doInit() throws Exception {
		super.doInit(); // Call parent method
		
		// find config file
		configfilename = System.getProperty("jiactng.rootconfigfile");
		log.debug("Configuration file is: " + configfilename);
		URL url =  ClassLoader.getSystemResource(configfilename);
		File myconfigfilehandle = null; // declare filehandle var
		if (url != null){
			myconfigfilehandle = new File(url.getFile());
			if (myconfigfilehandle.exists()){
				log.debug("Found " + configfilename + " as root configuration file. Starting to Monitor...");
			} else {
				throw new IOException("Unable to load spring startup configuration file for Agent Node because file could not be found: " + configfilename);
			}
		} else {
			throw new IOException("Unable to load spring startup configuration file for Agent Node because of missing property.");
		}
		
		// create XML document from config file
		SAXBuilder saxb = new SAXBuilder();
		configdocument = saxb.build(myconfigfilehandle);
		log.debug("configuration file "+ configfilename + " read for monitoring starts:\n" + configdocument.getRootElement().toString() + "\n with " + configdocument.getRootElement().getContentSize() + " XML children elements.");
		
		log.debug("NodeConfigurationMonitorBean has initialized.");
	}
	
	
	/**
	 * Opens a JMX connection to the first local Agent Node found and registers
	 * the Bean as listener for add/remove agent notifications.
	 **/
	public void doStart() throws Exception {
		// Connect to local Agent Node JMX Interface
		jmclient = new JmxManagementClient();
		Set<String> nodenames = jmclient.getAgentNodeNames();
		nodeclient = jmclient.getAgentNodeManagementClient((String)((nodenames.toArray())[0])); // Connect to the first Agent Node found in the local JVM
		// add this Bean as notification listener for adding or removing agents to and from the Agent Node.
		nodeclient.addAgentsListener(this);
		
		// build up agent id to name mapping
		agentIDtoName = new HashMap<String, String>();
		for (String id: nodeclient.getAgents()) {
			JmxAgentManagementClient agentClient = jmclient.getAgentManagementClient(
					(String)((nodenames.toArray())[0]), id);
			try {
				String name = agentClient.getAgentName();
				if (name != null) {
					agentIDtoName.put(id, name);
				}
			} catch (Exception e) {
				continue;
			}
		}
		
		/**
		 * The following code was for testing purposes only
		 log.debug("Accessing JMX node client, node state is: " + nodeclient.getAgentNodeState());
		 nodeclient.removeAgentsListener(this);
		 **/
	}
	
	/**
	 * Adds missing agent IDs to the configuration document. After completion of this method,
	 * all agents defined in spring should have their agentIds.
	 */
	private void addMissingIDs() {
		List<Element> agents = configdocument.getRootElement().getChildren("bean");
		for (Element agent : agents) {
			if (agent.getChild("constructor-arg") != null) {
				// already got an agent id, nothing to do
				continue;
			} else {
				String name = agent.getAttributeValue("name");
				if (agentIDtoName.containsValue(name)) {
					for (String key : agentIDtoName.keySet()) {
						if (agentIDtoName.get(key).equals(name)) {
							// id found, write into spring config
							JDOMFactory jdomfactory = new DefaultJDOMFactory();
							Element idProp = jdomfactory.element("constructor-arg");
							idProp.setAttribute("value", key);				
							agent.addContent(idProp);
							break;
						}
					}
				}	
			}
		}
	}
	
	
	/**
	 * This Method first stops the monitoring process in this Bean and afterwards saves the current
	 * configuration in the autosave configuration file.
	 **/
	public void doStop() throws Exception {
		// add missing agent ids
		addMissingIDs();
		// create file name
		File myconfigfilehandle = new File(configfilename);
		autoconfigfilename = produceAutosaveConfigurationFileName(myconfigfilehandle.getName());
		log.debug("autoconfigfilename generated: " + autoconfigfilename);
		File autoconfigfile = new File(autoconfigfilename);
		if (autoconfigfile.exists()){
			// TODO: Here some archiving mechanism should move the old file to a new name
			// For the Proof of Concept we simply delete the old file
			autoconfigfile.delete();
		}
		if (autoconfigfile.createNewFile()){
			log.debug("Writing to: " + autoconfigfile.getPath());
		} else {
			throw new IOException("Unable to create autosave configuration file: " + autoconfigfilename);
		}
		
		
		FileOutputStream fos = new FileOutputStream(autoconfigfile);
		//OutputStreamWriter osw = new OutputStreamWriter(fos);
		XMLOutputter xop = new XMLOutputter();
		xop.output(configdocument, fos);
		//debug output
//		xop.output(configdocument, System.out);
		fos.close();
		
		// deregister for Agent Node events and JMX interface
		// nodeclient.removeAgentsListener(this); // remove currently does not work, probably because of a bug in the management interface?
		
		agentIDtoName = null;
		nodeclient = null;
		jmclient.close();
		jmclient = null;
		
		// finally call parent method
		super.doStop();
	}
	
	
	
	/**
	 * Constructs the autosaved file name for a given configuration file name.
	 * Performs no checks whatsoever about the validity of the file name in the local file system.
	 *
	 * @param configfile The configuration file name used for startin the autosaving Agent Node.
	 * @return The name of autosave configuration file written in the current working directory if the Agent Node is shutdown. Returns the original configfile parameter if it ends with _autosave.xml .
	 **/
	public static String produceAutosaveConfigurationFileName(String configfilename){
		String autofilename = null;
		if (configfilename.endsWith("_autosave.xml")) {
			return configfilename; // The file is already a autosave file, no change needed.
		} else {
			autofilename = configfilename.substring(0,configfilename.indexOf(".xml"));
		}
		File autoconfigfile = new File(autofilename + "_autosave.xml");
		return autoconfigfile.getName(); // Weird construction to get a handle without path, only the pure file name.
	}
	
	
	/**
	 * Handles Notifications about added or removed agents on the Agent Node.
	 *
	 * @see javax.management.NotificationListener
	 **/
	public synchronized void handleNotification(Notification notification, Object handback){
		if (this.getState() == LifecycleStates.STARTED){
			log.debug("NCMB handles a notification.");
			try {
				AttributeChangeNotification acn = (AttributeChangeNotification)notification;
				LinkedList<String> listofnewagents = new LinkedList<String>((List<String>)acn.getNewValue()); // lists are cloned to protect them from change in this operation.
				LinkedList<String> listofoldagents = new LinkedList<String>((List<String>)acn.getOldValue());
				// check which list (new/old) is longer
				if (listofnewagents.size() > listofoldagents.size()){
					// An agent has been added
					log.debug("Removing list: \n" + listofoldagents.toString() + "\n from list: \n" + listofnewagents.toString());
					if ((listofnewagents.removeAll(listofoldagents)) || (listofoldagents.size()==0)){
						String newagentid = listofnewagents.get(0); // Fetching the remaining agentid, only one agent should appear here and is dealt with
						this.addAgentConfiguration(newagentid);
					} else {
						log.error("Unable to filter new agent from agent lists. Ignoring new agent.\nAutosaved configuration file will be inconsistent with Agent Node state!!!");
					}
				} else {
					if (listofoldagents.size() > listofnewagents.size()){
						// An agent has been removed
						if ((listofoldagents.removeAll(listofnewagents)) || (listofnewagents.size()==0)) {
							String oldagentid = listofoldagents.get(0); // Fetching the remaining agentid, only one agent should appear here and is dealt with
							this.removeAgentConfiguration(oldagentid);
						} else {
							log.error("Unable to filter old agent from agent lists. Ignoring removed agent.\nAutosaved configuration file will be inconsistent with Agent Node state!!!");
						}
					} else {
						// Same number of agents in new and old list???? WTF?
						log.warn("Received Notification about a change in agent number but cannot identify change. Ignoring this notification and continuing....");
					}
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		} else {
			log.warn("Received notification while not in active state, ignoring event.");
		}
	}
	
	/**
	 * Adds new imports to the node configuration.
	 * @param agentConfig the new agent's spring configuration skript
	 */
	private void addMissingImports(Document agentConfig) {
		// TODO: not tested yet
		List<Element> agentImports = agentConfig.getRootElement().getChildren("import");
		List<Element> nodeImports = configdocument.getRootElement().getChildren("import");
		List<Content> importsToAdd = new ArrayList<Content>();
		for (Element imp: agentImports) {
			String importPath = imp.getAttribute("resource").getValue();
			boolean found = false;
			for (Element nodeimp: nodeImports) {
				if (nodeimp.getAttribute("resource").getValue().equals(importPath)) {
					found = true;
					break;
				}
			}
			if (!found) {
				importsToAdd.add(((Element)imp.clone()).detach());
			}
		}
		if (importsToAdd.size() > 0) {
			agentConfig.getRootElement().addContent(nodeImports.size(), importsToAdd);
		}
	}
	
	/**
	 * Returns the JDOM element that contains the agent list in the node spring configuration
	 * @return Agent list JDOM element
	 */
	private Element getNodeAgentList() {
		for (Object e: configdocument.getRootElement().getChildren("bean")) {
			for (Object prop : ((Element)e).getChildren("property")) {
				if (((Element)prop).getAttribute("name").getValue().equals("agents")) {
					return ((Element)prop).getChild("list");
				}
			}
		}
		return null;
	}
	
	/**
	 * Adds the agent name to the agent node's configuration list.
	 * @param agentName the agent name to add
	 */
	private void addAgentToConfigurationList(String agentName) {
		Element xmlagentlist = getNodeAgentList();
		if (xmlagentlist != null) {
			JDOMFactory jdomfactory = new DefaultJDOMFactory();
			Element newAgentEntry = jdomfactory.element("ref");
			newAgentEntry.setAttribute("bean", agentName);
			xmlagentlist.addContent(newAgentEntry);
		}
	}
	
	/**
	 * Removes an agent's name from the node's configuration list.
	 * @param agentName agentName to remove
	 */
	private boolean removeAgentFromConfigurationList(String agentName) {
		List<Element> xmlagentlist = getNodeAgentList().getChildren("ref");
		if (xmlagentlist != null) {
			for (Iterator<Element> entry = xmlagentlist.iterator(); entry.hasNext();) {
				Element current = entry.next();
				if (current.getAttribute("bean").getValue().equals(agentName)) {
					entry.remove();
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * Adds the configuration for the given agent to the current configuration document.
	 *
	 * @param agentid The ID of the new agent whose configuration is added
	 **/
	public void addAgentConfiguration(String agentid){
		log.debug("Adding agent configuration for new agent " + agentid);
		try {
			Set<String> nodeNames = jmclient.getAgentNodeNames();
			
			// connect to agent
			JmxAgentManagementClient agentClient = 
				jmclient.getAgentManagementClient((String)((nodeNames.toArray())[0]), agentid);
			if (agentClient == null) {
				log.error ("Client is null!");
				return;
			}
			// fetch configuration
			byte[] configuration = agentClient.getSpringConfigXml().clone();
			String agentName = agentClient.getAgentName();
			if (configuration != null) {
				log.debug("Got configuration for agent: \""+agentName+"\"");
				// create document from spring configuration
				SAXBuilder saxb = new SAXBuilder();
				Document agentConfig = saxb.build(new ByteArrayInputStream(configuration));
				// check and add missing imports
				addMissingImports(agentConfig);
				// get agent definitions
				List<Element> beans = agentConfig.getRootElement().getChildren("bean");
				// add definitions to spring configuration
				for (Iterator<Element> it = beans.iterator(); it.hasNext();) {
					Element agentbean = (Element)it.next().clone();
					agentbean.detach();
					//write agent id
					JDOMFactory jdomfactory = new DefaultJDOMFactory();
					Element idProp = jdomfactory.element("constructor-arg");
					idProp.setAttribute("value", agentid);				
					agentbean.addContent(idProp);
					
					configdocument.getRootElement().addContent(agentbean);
				}
				// add agent name to agent node's agent list
				addAgentToConfigurationList(agentName);
				agentIDtoName.put(agentid, agentName);
				
//				// debug output
//				XMLOutputter testxop = new XMLOutputter();
//				testxop.output(configdocument, System.out);
				
			} else {
				log.error("Spring Configuration was null, agent was NOT added to configuration.");
			}	
		} catch (Exception e) {
			log.error("An Exception occured: ", e);
		}
	}
	
	
	/**
	 * Removes the configuration for the given agent from the current configuration document.
	 *
	 * @param agentid The ID of the old agent whose configuration is removed
	 **/
	public void removeAgentConfiguration(String agentid){
		log.debug("Removing agent configuration of old agent " + agentid);
		
		String agentName = agentIDtoName.get(agentid);
		if (agentName != null) {
			log.debug("Agent name is "+agentName);
			// remove agent from node's agent list
			if (!removeAgentFromConfigurationList(agentName)) {
				log.error("Could not remove agent from agent list.");
			}
			// remove agent's spring configuration
			List<Element> beans = configdocument.getRootElement().getChildren("bean");
			for (Iterator<Element> it = beans.iterator(); it.hasNext();) {
				Element agent = it.next();
				if (agent.getAttribute("name").getValue().equals(agentName)) {
					it.remove();
				}
			}
			agentIDtoName.remove(agentid);
			
//			// debug output
//			try {
//				XMLOutputter testxop = new XMLOutputter();
//				testxop.output(configdocument, System.out);
//			} catch (Exception e) {
//				log.error("An Exception occured: ", e);
//			}	
		} else {
			log.error("Agent name for \"" + agentid + "\" could not be resolved!");
		}
	}
}
