package de.dailab.jiactng.agentcore.comm.examples;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Log4jConfigurer;

import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.comm.CommunicationBean;
import de.dailab.jiactng.agentcore.comm.broker.JmsBrokerAMQ;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

public class CommunicationBeanExample {


	/** A customized logging configuration will be used instead of the default configuration. */
	public Log log = LogFactory.getLog(getClass().getName());
	
	private String loggingConfig = "classpath:de/dailab/jiactng/agentcore/comm/examples/myLog4j.properties";
	private static ClassPathXmlApplicationContext _xmlCommunicationBeanContext;
	
	private static SimpleAgentNode _communicationPlatform;
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CommunicationBeanExample cbe = new CommunicationBeanExample();
		cbe.run();
	}

	public void run(){
		setup();
		
		BufferedReader keyboard = new BufferedReader (new InputStreamReader (System.in));
		String input = "";
		
		while(true){
			System.out.println();
			System.out.println("Bitte quit eingeben");
			System.out.println("--------------------");
			try {
				input = keyboard.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			if (input.equalsIgnoreCase("quit")){
				System.out.println();
				System.out.println();
				break;
			}
			
		}
		
		
		cleanup();
	}
	
	private void setup(){
		log.debug("Setup Begins...");
		
		setupLoggingConfig();
				
		_xmlCommunicationBeanContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/communicationTestContext.xml");
		
		_communicationPlatform = (SimpleAgentNode) _xmlCommunicationBeanContext.getBean("CommunicationPlatform");

		log.debug("...Setup Finished");
	}
	
	
	private void cleanup(){
		log.debug("Cleanup Begins...");
	
		log.debug("Begin Cleanup Of CommunicationPlatform");
		try {
			_communicationPlatform.shutdown();
		} catch (LifecycleException e) {
			log.error("Error while cleaning up CommunicationPlatform: " + e.getStackTrace());
		}
		log.debug("Cleanup Of CommunicationPlatform finished");
				
		log.debug("...Cleanup Finished");
		
	}
	
	private void setupLoggingConfig() {
		try {
			Log4jConfigurer.initLogging(this.loggingConfig);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}
}
