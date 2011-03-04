package de.dailab.jiactng.agentcore.management.jmx.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.ontology.IAgentNodeDescription;

/**
 * This class allows testing of a JMX connection within an own thread and 
 * optional also recursively finding and testing other URLs by using the 
 * agent node directory.
 * @author Jan Keiser
 */
public class JmxConnectionTester implements Runnable {

	private JMXServiceURL url;
	private Map<JMXServiceURL,JmxConnectionTester> urls;
	private boolean success;

	/**
	 * Creates the tester for a given JMX URL and optional put it to the map of URLs.
	 * @param url the new JMX service URL to be tested.
	 * @param urls the map of already found and tested URLs to be recursively 
	 * filled by the testers or <code>null</code> if only the given URL will 
	 * be tested. 
	 */
	public JmxConnectionTester(JMXServiceURL url, Map<JMXServiceURL,JmxConnectionTester> urls) {
		this.url = url;
		this.urls = urls;
		success = false;
		if (urls != null) {
			synchronized (urls) {
				urls.put(url, this);
			}
		}
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
			if (urls != null) {
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
									for (String otherURL : otherURLs) {
										try {
											// check whether the other URL is already known
											if (!urls.containsKey(otherURL)) {
												//start finding and testing URLs recursively by using agent node directories
												JmxConnectionTester tester = new JmxConnectionTester(new JMXServiceURL(otherURL), urls);
												new Thread(tester).start();
											}
										}
										catch (MalformedURLException e) {
											System.err.println("Found URL is malformed: " + otherURL);
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
