package de.dailab.jiactng.agentcore.conf;

// imports
import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.Document; 


/**
 * The NodeConfigurationMonitor saves the current Spring configuration of the Agent Node
 * to harddisk in order to allow restarting the node with it's current configuration.
 *
 * @author Silvan Kaiser
 */
public class NodeConfigurationMonitorBean extends AbstractAgentNodeBean {
	
	// variables
	boolean initsuccess = true;
	Document configdocument = null;
	String configfilename = null; // stores the filename of the configuration to be monitored.
	String autoconfigfilename = null; // stores the filename of the autosave configuration to be saved.
	boolean shuttingdown = false;
	
	
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
	
	
}
