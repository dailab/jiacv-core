package de.dailab.jiactng.examples.chat;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.Log4jConfigurer;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

public class CommunicationBeanExample {
    private static ByteArrayResource getChatAgentConfiguration(String agentName) {
        ByteArrayOutputStream buffer= new ByteArrayOutputStream();
        PrintStream printer= new PrintStream(buffer);
        printer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        printer.println("<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">");
        printer.println("<beans>");
        printer.append("<bean name=\"").append(agentName).append("\" parent=\"chatAgentTemplate\" singleton=\"false\" />");
        printer.println("</beans>");
        printer.flush();
        printer.close();
        return new ByteArrayResource(buffer.toByteArray());
    }

	/** A customized logging configuration will be used instead of the default configuration. */
	public Log log = LogFactory.getLog(getClass().getName());
	
	private String loggingConfig = "classpath:de/dailab/jiactng/examples/chat/myLog4j.properties";
	private ClassPathXmlApplicationContext _xmlCommunicationBeanContext;
	
	private SimpleAgentNode _communicationPlatform;
    
    private void addAgent(String agentName) {
        GenericApplicationContext context= new GenericApplicationContext(_xmlCommunicationBeanContext);
        XmlBeanDefinitionReader xmlReader= new XmlBeanDefinitionReader(context);
        xmlReader.loadBeanDefinitions(getChatAgentConfiguration(agentName));
        context.refresh();

        IAgent newAgent = (IAgent) context.getBean(agentName);
        _communicationPlatform.addAgent(newAgent);
        try {
            newAgent.init();
            newAgent.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }

	public void run(){
		setup();
		
		BufferedReader keyboard = new BufferedReader (new InputStreamReader (System.in));
		String input = "";
		
		while(true){
			System.out.println();
			System.out.println("Please type quit to do so");
			System.out.println();
			System.out.println("Other Options:");
			System.out.println(" \"addAgent\"");
			System.out.println(" \"ListAgents\"");
			System.out.println(" \"removeAgent\"");
			System.out.println("--------------------");
			try {
				input = keyboard.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			if (input.equalsIgnoreCase("addAgent")){
				System.out.println();
				System.out.println("Please give your new best Agent a decent Name");
				try {
					input = keyboard.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if (input != null){
                    addAgent(input);
				}
			}
			
			if (input.equalsIgnoreCase("listAgents")){
				System.out.println();
				System.out.println("Agents enlisted onto this Node:");
				System.out.println("IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII");
				ArrayList<String> agentList =_communicationPlatform.getAgents();
				
				if (agentList != null){
					for (String agentName : agentList){
						System.out.println(agentName);
					}
				}
				System.out.println("--- End of AgentList ---");
		
			}
			

			if (input.equalsIgnoreCase("removeAgent")){
				System.out.println();
				System.out.println("Please type the Name of the Agent you want to get rid off");
				try {
					input = keyboard.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (input != null){
					ArrayList<IAgent> agentList = _communicationPlatform.findAgents();
					
					if (agentList == null){
						System.out.println("There are currently no Agents on the Node!");
					} else {
						IAgent foundAgent = null;
						for (IAgent agent : agentList){
							if (agent.getAgentName().equalsIgnoreCase(input)){
								foundAgent = agent;
							}
						}

						if (foundAgent == null){
							System.out.println("Agent with given name doesn't exist on AgentNode!");
						} else {
							_communicationPlatform.removeAgent(foundAgent);
							try {
								foundAgent.stop();
								foundAgent.cleanup();
							} catch (LifecycleException e) {
								e.printStackTrace();
							}
							System.out.println("Agent with name " + input + " was removed!");
						}
					}
				}
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
				
		_xmlCommunicationBeanContext = new ClassPathXmlApplicationContext(new String[]{
            "de/dailab/jiactng/examples/chat/chatNode.xml",
            "de/dailab/jiactng/examples/chat/chatAgentTemplate.xml"
        });
		
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
    
    
    public static void main(String[] args) {
        CommunicationBeanExample cbe = new CommunicationBeanExample();
        cbe.run();
    }
}
