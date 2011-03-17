package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.ontology.IAgentNodeDescription;

/**
 * This class allows testing of JMX connections within an own thread and 
 * optional also recursively searching and testing other URLs by using the 
 * agent node directory.
 * @author Jan Keiser
 */
public class JmxConnectionTester {

	private Map<JMXServiceURL,JmxConnectionTesterThread> urls = new HashMap<JMXServiceURL,JmxConnectionTesterThread>();
	private boolean recursiveSearch; 

	/**
	 * Creates the connections tester.
	 * @param recursiveSearch <code>true</code> if the URLs should be 
	 * recursively searched and tested by using the agent node directories.
	 */
	public JmxConnectionTester(boolean recursiveSearch) {
		this.recursiveSearch = recursiveSearch;
	}

	/**
	 * Adds an URL to be tested within an own thread.
	 * @param url the new URL to be tested
	 * @see Thread#start()
	 */
	public void addURL(JMXServiceURL url) {
		// check whether the URL is already known
		synchronized(urls) {
			if (!urls.containsKey(url)) {
				//start connection tester thread for the URL
				JmxConnectionTesterThread tester = new JmxConnectionTesterThread(url);
				urls.put(url, tester);
				new Thread(tester).start();
			}
		}
	}

	/**
	 * Gets the result of all tester threads.
	 * @return the list of successfully tested URLs.
	 */
	public List<JMXServiceURL> getResult() {
		final List<JMXServiceURL> result = new ArrayList<JMXServiceURL>();
		synchronized (urls) {
			for (Map.Entry<JMXServiceURL,JmxConnectionTesterThread> entry : urls.entrySet()) {
				if (entry.getValue().getSuccess()) {
					result.add(entry.getKey());
				}
			}
		}
		return result;		
	}

	private class JmxConnectionTesterThread implements Runnable {

		private JMXServiceURL url;
		private boolean success;

		/**
		 * Creates the tester thread for a given JMX URL.
		 * @param url the new JMX service URL to be tested.
		 */
		public JmxConnectionTesterThread(JMXServiceURL url) {
			this.url = url;
			success = false;
		}

		/**
		 * Tries to create the JMX connection and optional to find other JMX URLs.
		 */
		public void run() {
			try {
				// test JMX connection
				JmxManagementClient client = new JmxManagementClient(url);
				success = true;

				// find other JMX URLs via agent node directory
				if (recursiveSearch) {
					JmxAgentNodeDirectoryManagementClient directoryClient = client.getDirectoryManagementClient(client.getAgentNodeUUID(url));
					TabularData knownNodes = null;
					try {
						knownNodes = directoryClient.getKnownNodes();
						Collection<CompositeData> values = (Collection<CompositeData>) knownNodes.values();
						if (values != null) {
							for (CompositeData value : values) {
								CompositeData desc =  (CompositeData) value.get("description");
								if (desc != null) {
									String[] otherURLs = (String[])desc.get(IAgentNodeDescription.ITEMNAME_JMXURLS);
									if (otherURLs != null) {
										for (String o : otherURLs) {
											try {
												JMXServiceURL otherURL = new JMXServiceURL(o);
												// check whether the other URL is already known
												synchronized(urls) {
													if (!urls.containsKey(otherURL)) {
														//start connection tester thread for the other URL
														JmxConnectionTesterThread tester = new JmxConnectionTesterThread(otherURL);
														urls.put(otherURL, tester);
														new Thread(tester).start();													
													}
												}
											}
											catch (MalformedURLException e) {
												System.err.println("Found URL is malformed: " + o);
											}
										}
									}
								}
							}
						}
					} catch (InstanceNotFoundException e) {
						// can not read agent node directory to get other agent nodes
					}
				}

				// close JMX connection
				try {
					client.close();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			catch (SecurityException se) {
				success = true;
			}
			catch (Exception e) {
				System.err.println("Unable to connect to " + url + ": " + e.getLocalizedMessage());
			}
		}

		/**
		 * Gets the success of connecting to JMX URL.
		 * @return <code>true</code> if the connection can be established or only
		 * security problems occur.
		 */
		public boolean getSuccess() {
			return success;
		}
	}

}
