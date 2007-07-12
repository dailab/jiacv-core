/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.examples;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.activemq.broker.BrokerService;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class BrokerStart {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        BrokerService broker= new BrokerService();
        String destination= "localhost:61616";
        System.out.println("setup Broker on " + destination);
        broker.setPersistent(false);
        broker.setUseJmx(true);
        broker.addConnector("tcp://" + destination);
        
        System.out.println("start Broker...");
        broker.start();
        BufferedReader keyboard = new BufferedReader (new InputStreamReader (System.in));
        
        while(!keyboard.readLine().equalsIgnoreCase("quit"));
        System.out.println("shutdown Broker...");
        broker.stop();
    }
}
