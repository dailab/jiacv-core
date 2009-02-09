package de.dailab.jiactng.agentcore.conf;

// imports
import de.dailab.jiactng.agentcore.AbstractAgentNodeBean;

import java.io.IOException;


/**
 * The NodeConfigurationMonitor saves the current Spring configuration of the Agent Node
 * to harddisk in order to allow restarting the node with it's current configuration.
 *
 * @author Silvan Kaiser
 */
public class NodeConfigurationMonitorBean extends AbstractAgentNodeBean {
	
	// variables
	String myconfigfile = null;
	boolean initsuccess = true;
	
	
	/**
	 * Adds th Initialization of the DOM object for the Agent Nodes configuration
	 * to the startup sequence for this AgentNodeBean.
	 **/
	public void doInit() throws Exception {
		super.doInit(); // Call parent method
		
		myconfigfile = System.getProperty("spring.rootconfigfile");
		if (myconfigfile == null){
			log.warn("No config file found at startup, unable to monitor configuration.\n Autosave is disabled for this Agent Node.");
			throw new IOException("Unable to load spring startup configuration file for Agent Node.");
		} else {
			log.debug("Found " + myconfigfile + " as root configuration file. Starting to Monitor...");
		}
	}
	
}
