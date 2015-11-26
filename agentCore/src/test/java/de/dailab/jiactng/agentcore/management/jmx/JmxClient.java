/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.management.jmx;

import java.util.List;

import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.management.jmx.client.JmxManagementClient;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class JmxClient {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        JMXServiceURL requiredUrl= new JMXServiceURL(args[0]);
        System.out.println("collecting JMX URLs from multicast ...");
        
        List<JMXServiceURL> serviceUrls= JmxManagementClient.getURLsFromMulticast();
        
        if(!serviceUrls.contains(requiredUrl)) {
            System.err.println("did not find URL");
            System.exit(1);
        }
        System.out.println("found service url in multicast result");

        try {
        	JmxManagementClient client= new JmxManagementClient(requiredUrl, "marcel", "lecram");
        	System.out.println("have a client connection");

        	// TODO: noch irgendwas mit'm client machen?
        	client.close();
        	System.exit(0);
        }
        catch (Exception e) {
        	System.err.println("can not establish a client connection: " + e.getMessage());
        	System.exit(1);
        }
    }
}
