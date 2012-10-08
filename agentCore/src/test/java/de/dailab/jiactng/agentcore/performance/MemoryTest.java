package de.dailab.jiactng.agentcore.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

/**
 * A class for testing the memory usage
 * 
 * @author Hilmi Yildirim
 *
 */
public class MemoryTest {

	/**
	 * constant for a node with one agent
	 */
	private final byte TESTNODE1 = 0;
	
	/**
	 * constant for a node with 10 agents
	 */
	private final byte TESTNODE10 = 1;
	
	/**
	 * constant for a node with 100 agents
	 */
	private final byte TESTNODE100 = 2;
	
	/**
	 * constant for a node with 1000 agents
	 */
	private final byte TESTNODE1000 = 3;
	
	/**
	 * csv file
	 */
	private static BufferedWriter csvFileWriter;
	
	/**
	 * path to the csv file
	 */
	private static final String CSVPATH = "target" + File.separator + "performanceResult.csv";
	
	/**
	 * output which will be written in the csv file
	 */
	private static String[] output;
	
	/**
	 * initialize the output
	 */
	@BeforeClass
	public static void createFile(){		
		output = new String[4];
	}
	
	/**
	 * the node where the agents runs
	 */
	private SimpleAgentNode node;
	
	/**
	 * starts the node and find the agent
	 */
	public void startNode(byte testNode){
		switch(testNode){
			case TESTNODE1: node = (SimpleAgentNode) new ClassPathXmlApplicationContext("de" + File.separator + "dailab" + File.separator + 
					"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
					+ "memoryTest" + File.separator + "TestNode1.xml").getBean("TestNode1");
				break;
				
			case TESTNODE10: node = (SimpleAgentNode) new ClassPathXmlApplicationContext("de" + File.separator + "dailab" + File.separator + 
					"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
					+ "memoryTest" + File.separator + "TestNode10.xml").getBean("TestNode10");
					break;
					
			case TESTNODE100: node = (SimpleAgentNode) new ClassPathXmlApplicationContext("de" + File.separator + "dailab" + File.separator + 
					"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
					+ "memoryTest" + File.separator + "TestNode100.xml").getBean("TestNode100");
					break;
					
			case TESTNODE1000: node = (SimpleAgentNode) new ClassPathXmlApplicationContext("de" + File.separator + "dailab" + File.separator + 
					"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
					+ "memoryTest" + File.separator + "TestNode1000.xml").getBean("TestNode1000");
					break;
		}
	}
	
	@Test
	public void memoryTest1(){
		startNode(TESTNODE1);
		
		//get heap and non-heap memory usage
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		long usedHeap = memoryBean.getHeapMemoryUsage().getUsed()/1024;
		long usedNonHeap = memoryBean.getNonHeapMemoryUsage().getUsed()/1024;
		int numberOfThreads = ManagementFactory.getThreadMXBean().getThreadCount();
		
		generateCsvOutput(0, usedHeap, usedNonHeap, 1, numberOfThreads);
	}
	
	@Test
	public void memoryTest10(){
		startNode(TESTNODE10);
		
		//get heap and non-heap memory usage
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		long usedHeap = memoryBean.getHeapMemoryUsage().getUsed()/1024;
		long usedNonHeap = memoryBean.getNonHeapMemoryUsage().getUsed()/1024;
		int numberOfThreads = ManagementFactory.getThreadMXBean().getThreadCount();
		
		generateCsvOutput(1, usedHeap, usedNonHeap, 10, numberOfThreads);
		
		List<IAgent> agents = node.findAgents();
	}
	
	@Test
	public void memoryTest100(){
		startNode(TESTNODE100);
		
		//get heap and non-heap memory usage
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		long usedHeap = memoryBean.getHeapMemoryUsage().getUsed()/1024;
		long usedNonHeap = memoryBean.getNonHeapMemoryUsage().getUsed()/1024;
		int numberOfThreads = ManagementFactory.getThreadMXBean().getThreadCount();
		
		generateCsvOutput(2, usedHeap, usedNonHeap, 100, numberOfThreads);
	}
	
	@Test
	public void memoryTest1000(){
		startNode(TESTNODE1000);
		
		//get heap and non-heap memory usage
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		long usedHeap = memoryBean.getHeapMemoryUsage().getUsed()/1024;
		long usedNonHeap = memoryBean.getNonHeapMemoryUsage().getUsed() /1024;
		int numberOfThreads = ManagementFactory.getThreadMXBean().getThreadCount();
				
		generateCsvOutput(3, usedHeap, usedNonHeap, 1000, numberOfThreads);
	}
	
	/**
	 * shut down the current node after every test
	 */
	@After
	public void shutDown(){
		if(node != null){
			try {
				node.shutdown();
			} catch (LifecycleException e) {
				System.err.println(e.getMessage());
			}
		}
	}
	
	/**
	 * writes in the csv file
	 */
	@AfterClass
	public static void WriteInCSVFile(){
		try {
			csvFileWriter = new BufferedWriter(new FileWriter(new File(CSVPATH), true));
			
			SimpleDateFormat format = new SimpleDateFormat ("dd.MM.yyyy 'um' HH:mm:ss");
			Date currentTime = new Date();
			csvFileWriter.write("Memory Test: Durchgefuehrt am: " + format.format(currentTime));
			csvFileWriter.newLine();
			csvFileWriter.write("number of agents,used heap (kb), used heap per agent (kb),used non-heap (kb), used non-heap per agent (kb), number of threads");
			csvFileWriter.newLine();
			for(String o : output){
				csvFileWriter.write(o);
				csvFileWriter.newLine();
			}
			csvFileWriter.newLine();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		try {
			csvFileWriter.close();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * generates a line of the csv file and save it in output
	 * @param order in csv file
	 * @param synchronizes or not
	 * @param times
	 */
	public void generateCsvOutput(int order, long usedHeap, long usedNonHeap, int numberOfAgents, int numberOfThreads){	
		//number of agents
		output[order] = numberOfAgents + ",";
		//used heap
		output[order] += usedHeap + ",";
		//used heap per agent
		output[order] += usedHeap/numberOfAgents + ",";
		// used non-heap
		output[order] += usedNonHeap + ",";
		//used non-heap per agent
		output[order] += usedNonHeap/numberOfAgents + ",";
		//number of threads
		output[order] += numberOfThreads;
	}
}
