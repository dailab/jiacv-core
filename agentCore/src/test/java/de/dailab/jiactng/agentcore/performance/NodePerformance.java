/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.performance;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

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
 * @version $Revision$
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

        long startTime = System.currentTimeMillis();
        
        performanceTestNode= (SimpleAgentNode) rootContext.getBean("PerformanceTestNode");
        
        for(int i= 0; i < 2000; ++i) {
            addAgent("testAgent" + i);
            Thread.sleep(10);
        }

        long duration = System.currentTimeMillis() - startTime;        
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        memoryBean.gc();
        long heap = memoryBean.getHeapMemoryUsage().getUsed();
        long nonHeap = memoryBean.getNonHeapMemoryUsage().getUsed();
        
        // get number of loaded classes
        int classes = ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
        
        // get number of live threads
        int threads = ManagementFactory.getThreadMXBean().getThreadCount();

          
        
        
        // print all data to console
        System.out.println("Duration (sec): " + (duration/1000.0));
        System.out.println("Heap size(kb): " + (heap/1024));
        System.out.println("Non-heap (kb): " + (nonHeap/1024));
        System.out.println("Threads (number): " + threads);
        System.out.println("Classes (number): " + classes);
        
        
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
