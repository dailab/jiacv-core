package de.dailab.jiactng.agentcore.management.jmx.client;

import javax.management.remote.JMXServiceURL;

/**
 * This class allows testing of an JMX connection within an own thread.
 * @author Jan Keiser
 */
public class JmxConnectionTester implements Runnable {

	private JMXServiceURL url;
	private boolean success;

	/**
	 * Creates the tester for a JMX URL.
	 * @param url the JMX service URL
	 */
	public JmxConnectionTester(JMXServiceURL url) {
		this.url = url;
		success = false;
	}

	/**
	 * Tries to create the JMX connection.
	 */
	public void run() {
		try {
			new JmxManagementClient(url);
			success = true;
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
