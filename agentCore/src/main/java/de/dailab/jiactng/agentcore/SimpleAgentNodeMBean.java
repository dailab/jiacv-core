package de.dailab.jiactng.agentcore;

import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * JMX compliant management interface of agent nodes to get information
 * about them and to change their lifecycle states.
 * 
 * @author Jan Keiser
 */
public interface SimpleAgentNodeMBean {

	/**
	 * Getter for attribute "Name" of the managed agent node.
	 */
	public String getName();

	/**
	 * Getter for attribute "UUID" of the managed agent node.
	 */
	public String getUUID();

	/**
	 * Getter for attribute "Host" of the managed agent node.
	 */
	public String getHost() throws UnknownHostException;

	/**
	 * Getter for attribute "Agents" of the managed agent node.
	 */
	public ArrayList<String> getAgents();

	/**
	 * Deploys new agents on this agent node.
	 * @param name of the XML file which contains the spring configuration of the agents
	 */
	public void addAgents(String configFile);

	/**
	 * Getter for attribute "LifecycleState" of the managed agent node.
	 */
	public String getLifecycleState();
	
	//public long getInitTime();
	
	//public long getStartTime();

	public String getOwner();
	
	//public String getJiacVersion();
	
	//public String getJiacVendor();

	/**
	 * Initializes the managed agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#init()}
	 */
	public void init() throws Exception;

	/**
	 * Starts the managed agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
	 *
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#start()}
	 */
	public void start() throws Exception;

	/**
	 * Stops the managed agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#stop()}
	 */
	public void stop() throws Exception;

	/**
	 * Cleanes up the managed agent node.
     * @throws de.dailab.jiangtng.agentcore.lifecycle.LifecycleException
     * 
     * @see {@link de.dailab.jiactng.agentcore.lifecycle.ILifecycle#cleanup()}
	 */
	public void cleanup() throws Exception;

	/**
	 * Shuts down the managed agent node.
     * @throws de.dailab.jiactng.agentcore.lifecycle.LifecycleException
	 */
	public void shutdown() throws Exception;

	//public void undeployAgent(Agent agent);
	
}
