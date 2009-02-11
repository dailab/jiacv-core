package de.dailab.jiactng.agentcore.conf;

// imports
import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;

import java.io.IOException;
import java.io.File;
import java.net.URL;

import org.jdom.input.SAXBuilder;
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
	
	
	/**
	 * Adds th Initialization of the DOM object for the Agent Nodes configuration
	 * to the startup sequence for this AgentNodeBean.
	 * Reads the property spring.rootconfigfile from the system properties
	 * in order to locate the current spring root config file.
	 **/
	public void doInit() throws Exception {
		super.doInit(); // Call parent method
		
		// find config file
		String configfilename = System.getProperty("jiactng.rootconfigfile");
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
		log.debug("configuration file read for monitoring starts:\n" + configdocument.getRootElement().toString() + "\n with " + configdocument.getRootElement().getContentSize() + " XML children elements.");
	}
	
}
