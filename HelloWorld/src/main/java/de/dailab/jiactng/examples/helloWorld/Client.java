/*
 * A client to test the JMX functionality. It connects to the specified server, 
 * lists all registered MBeans, prints the lifecycle state of the agent node 
 * running at server side and shuts down this agent node remotely.
 * 
 * @(#)file      Client.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.1
 * @(#)lastedit  04/01/12
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package de.dailab.jiactng.examples.helloWorld;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Client {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
            	echo("\nUsage: java -cp <protocol-libs> Client <server-url>");
            }
            
            // Create an JMX connector client and
            // connect it to the JMX connector server
            //
            echo("\nCreate an JMX connector client and " +
		 "connect it to the JMX connector server");
            JMXServiceURL url = new JMXServiceURL(args[0]);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            // Get an MBeanServerConnection
            //
            echo("\nGet an MBeanServerConnection");
            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
	    waitForEnterPressed();

            // Get domains from MBeanServer
            //
            echo("\nDomains:");
            String domains[] = mbsc.getDomains();
            for (int i = 0; i < domains.length; i++) {
                echo("\tDomain[" + i + "] = " + domains[i]);
            }
	    waitForEnterPressed();

            // Get MBean count
            //
            echo("\nMBean count = " + mbsc.getMBeanCount());

	    // Query MBean names
	    //
            echo("\nQuery MBeanServer MBeans:");
	    Set names = mbsc.queryNames(null, null);
	    for (Iterator i = names.iterator(); i.hasNext(); ) {
		echo("\tObjectName = " + (ObjectName) i.next());
	    }
	    waitForEnterPressed();

	    // -------------------------------
	    // Manage the SimpleAgentNode MBean
	    // -------------------------------
            echo("\n>>> Perform operations on SimpleAgentNode MBean <<<");
            ObjectName node = new ObjectName("de.dailab.jiactng.agentcore:type=SimpleAgentNode,name=myPlatform");
            
            // Get LifecycleState attribute in SimpleAgentNode MBean
            //
            if (mbsc.isRegistered(node)) {
            	echo("\nLifecycleState = " + mbsc.getAttribute(node, "LifecycleState"));
            }

            // Invoke "shutdown" in SimpleAgentNode MBean
            //
            echo("\nInvoke shutdown() in SimpleAgentNode MBean...");
            mbsc.invoke(node, "shutdown", null, null);
	    waitForEnterPressed();

            // Close MBeanServer connection
            //
            echo("\nClose the connection to the server");
            jmxc.close();
            echo("\nBye! Bye!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void echo(String msg) {
	System.out.println(msg);
    }

    private static void waitForEnterPressed() {
	try {
	    echo("\nPress <Enter> to continue...");
	    System.in.read();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
