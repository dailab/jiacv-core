/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.performance;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ByteArrayResource;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class NodePerformance {
    private static ByteArrayResource getAgentConfiguration(String agentName) {
        ByteArrayOutputStream buffer= new ByteArrayOutputStream();
        PrintStream printer= new PrintStream(buffer);
        printer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        printer.println("<!DOCTYPE beans PUBLIC \"-//SPRING//DTD BEAN//EN\" \"http://www.springframework.org/dtd/spring-beans.dtd\">");
        printer.println("<beans>");
        printer.append("<bean name=\"").append(agentName).append("\" parent=\"PerformanceAgentTemplate\" singleton=\"false\" />");
        printer.println("</beans>");
        printer.flush();
        printer.close();
        return new ByteArrayResource(buffer.toByteArray());
    }
    
    private static SimpleAgentNode performanceTestNode;
    private static ApplicationContext rootContext;
    
    public static void main(String[] args) throws Exception {
        rootContext = new ClassPathXmlApplicationContext(new String[]{
            "de/dailab/jiactng/agentcore/performance/performanceTestNode.xml",
            "de/dailab/jiactng/agentcore/performance/performanceAgentTemplate.xml"
        });

        performanceTestNode= (SimpleAgentNode) rootContext.getBean("PerformanceTestNode");
        
        for(int i= 0; i < 100; ++i) {
            addAgent("testAgent" + i);
            Thread.sleep(10);
        }
    }
    
    private static void addAgent(String agentName) {
        GenericApplicationContext context= new GenericApplicationContext(rootContext);
        XmlBeanDefinitionReader xmlReader= new XmlBeanDefinitionReader(context);
        xmlReader.loadBeanDefinitions(getAgentConfiguration(agentName));
        context.refresh();

        IAgent newAgent = (IAgent) context.getBean(agentName);
        performanceTestNode.addAgent(newAgent);
        try {
            newAgent.init();
            newAgent.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }
    }
}
