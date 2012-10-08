package de.dailab.jiactng.agentcore.performance;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.performance.messageTest.ReceiveAgentBean;
import de.dailab.jiactng.agentcore.performance.messageTest.SendAgentBean;

/**
 * Tests the time of sending messages with two nodes
 * 
 * @author Hilmi Yildirim
 *
 */
public class MessageTestTwoNodes {
	
	/**
	 * path to the xml where the node 1 is described
	 */
	private final static String PATH_TO_NODE1_XML = "de" + File.separator + "dailab" + File.separator + 
			"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
			+ "messageTest" + File.separator + "twoNodes" + File.separator + "TestNode1.xml";
	
	/**
	 * path to the xml where the node 2 is described
	 */
	private final static String PATH_TO_NODE2_XML = "de" + File.separator + "dailab" + File.separator + 
			"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
			+ "messageTest" + File.separator + "twoNodes" + File.separator + "TestNode2.xml";
	
	/**
	 * send agents which sends messages to the receive agent
	 */
	private static SendAgentBean sendAgentBean = null;
	
	/**
	 * receive agent which receives messages from the send agent
	 */
	private static ReceiveAgentBean receiveAgentBean = null;
	
	/**
	 * the node where the send agent runs
	 */
	private static SimpleAgentNode node1;
	
	/**
	 * the node where the receive agent runs
	 */
	private static SimpleAgentNode node2;
	
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
		output = new String[8];
		startNode();
	}
	
	/**
	 * starts the node and find the agent
	 */
	public static void startNode(){
		node1 = (SimpleAgentNode) new ClassPathXmlApplicationContext(PATH_TO_NODE1_XML).getBean("TestNode1");
		
		node2 = (SimpleAgentNode) new ClassPathXmlApplicationContext(PATH_TO_NODE2_XML).getBean("TestNode2");
		
		
		List<IAgent> agents = node1.findAgents();
		
		for(IAgent agent : agents){
			try{
				if(agent.getAgentName().equals("SendAgent")){
					sendAgentBean = ((Agent)(agent)).findAgentBean(SendAgentBean.class);
				}
			}catch(Exception e){
				System.err.println("Exception: " + e.getMessage());
			}
		}
		
		agents = node2.findAgents();
		
		for(IAgent agent : agents){
			try{
				if(agent.getAgentName().equals("ReceiveAgent")){
					receiveAgentBean = ((Agent)(agent)).findAgentBean(ReceiveAgentBean.class);
				}
			}catch(Exception e){
				System.err.println("Exception: " + e.getMessage());
			}
			
		}
		
		if(sendAgentBean == null){
			throw new RuntimeException("send agent not found");
		}
		if(receiveAgentBean == null){
			throw new RuntimeException("receive agent not found");
		}
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}		
	}
	
	/**
	 * initialize the node and the agents
	 */
	public void init(int numberOfMessages){
		receiveAgentBean.initialize(numberOfMessages);
		sendAgentBean.initialize(numberOfMessages);
	}
	
	@Test
	public void testReceiveTimeSync1(){
		init(1);
		sendAgentBean.sendTime();
		long[] receiveTimes = getReceiveTimes();
		generateCsvOutput(0, "yes", receiveTimes);
	}
	
	@Test
	public void testReceiveTimeSync10(){
		long[] receiveTimes = new long[10];
		for(int i = 0; i < 10; i++){
			init(1);
			sendAgentBean.sendTime();
			long receiveTime = getReceiveTimes()[0];
			receiveTimes[i] = receiveTime;
		}
		generateCsvOutput(1, "yes", receiveTimes);
	}
	
	@Test
	public void testReceiveTimeSync100(){
		long[] receiveTimes = new long[100];
		for(int i = 0; i < 100; i++){
			init(1);
			sendAgentBean.sendTime();
			long receiveTime = getReceiveTimes()[0];
			receiveTimes[i] = receiveTime;
		}
		generateCsvOutput(2, "yes", receiveTimes);
	}
	
	@Test
	public void testReceiveTimeSync1000(){
		long[] receiveTimes = new long[1000];
		for(int i = 0; i < 1000; i++){
			init(1);
			sendAgentBean.sendTime();
			long receiveTime = getReceiveTimes()[0];
			receiveTimes[i] = receiveTime;
		}
		generateCsvOutput(3, "yes", receiveTimes);
	}
	
	@Test
	public void testReceiveTimeAsync1(){
		init(1);
		sendAgentBean.send();
		long[] receiveTimes = getReceiveTimes();
		generateCsvOutput(4, "no", receiveTimes);
		
	}
	
	@Test
	public void testReceiveTimeAsync10(){
		init(10);
		sendAgentBean.send();
		long[] receiveTimes = getReceiveTimes();
		generateCsvOutput(5, "no", receiveTimes);
	}
	
	@Test
	public void testReceiveTimeAsync100(){
		init(100);
		sendAgentBean.send();
		long[] receiveTimes = getReceiveTimes();
		generateCsvOutput(6, "no", receiveTimes);
	}
	
	@Test
	public void testReceiveTimeAsync1000(){
		init(1000);
		sendAgentBean.send();
		long[] receiveTimes = getReceiveTimes();
		generateCsvOutput(7, "no", receiveTimes);
	}
	

	/**
	 * shut down the nodes after every test
	 */
	public static void shutDown(){
		if(node1 != null){
			try {
				node1.shutdown();
			} catch (LifecycleException e) {
				System.err.println(e.getMessage());
			}
		}
		
		if(node2 != null){
			try {
				node2.shutdown();
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
			csvFileWriter.write("Message test mit zwei Knoten: Durchgefuehrt am: " + format.format(currentTime));
			csvFileWriter.newLine();
			csvFileWriter.write("sync,number of messages,total time, average time, all times...");
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
		shutDown();
	}
	
	/**
	 * @return the time for receiving messages
	 */
	public long[] getReceiveTimes(){
		long[] receiveTimes = null;
		synchronized (receiveAgentBean) {
			while(!receiveAgentBean.allMessagesReceived()){
				try {
					receiveAgentBean.wait();
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
			}
			receiveTimes = receiveAgentBean.getReceiveTimes();
		}
		return receiveTimes;
	}
	
	/**
	 * generates a line of the csv file and save it in output
	 * @param order in csv file
	 * @param synchronizes or not
	 * @param times
	 */
	public void generateCsvOutput(int order, String sync, long[] times){	
		long totalTime = 0;
		String allTimes = "";
		for(int i = 0; i < times.length; i++){
			totalTime += times[i];
			allTimes += times[i] + "";
			if(i != times.length - 1){
				allTimes += ",";
			}
		}
		long averageTime = totalTime / times.length;
		
		//sync
		output[order] = sync + ",";
		//number of messages
		output[order] += times.length + ",";
		//total time
		output[order] += totalTime + ",";
		//average time
		output[order] += averageTime + ",";
		//all times
		output[order] += allTimes;
	}
}
