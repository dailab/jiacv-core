package de.dailab.jiactng.agentcore.conf;

// imports
import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient;
import de.dailab.jiactng.agentcore.management.jmx.client.JmxAgentNodeManagementClient;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Set;
import java.util.List;
import java.util.LinkedList;

import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.AttributeChangeNotification;

import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.Document; 


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
		
		/**
		 * The following code was for testing purposes only
		 log.debug("Accessing JMX node client, node state is: " + nodeclient.getAgentNodeState());
		 nodeclient.removeAgentsListener(this);
		 **/
	}
	
	
	/**
	 * This Method first stops the monitoring process in this Bean and afterwards saves the current
	 * configuration in the autosave configuration file.
	 **/
	public void doStop() throws Exception {
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
		fos.close();
		
		// deregister for Agent Node events and JMX interface
		// nodeclient.removeAgentsListener(this); // remove currently does not work, probably because of a bug in the management interface?
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
	 * Adds the configuration for the given agent to the current configuration document.
	 *
	 * @param agentid The ID of the new agent whose configuration is added
	 **/
	public void addAgentConfiguration(String agentid){
		log.debug("Adding agent configuration for new agent " + agentid);
	}
	
	
	/**
	 * Removes the configuration for the given agent from the current configuration document.
	 *
	 * @param agentid The ID of the old agent whose configuration is removed
	 **/
	public void removeAgentConfiguration(String agentid){
		log.debug("Removing agent configuration of old agent " + agentid);
	}
	
}
