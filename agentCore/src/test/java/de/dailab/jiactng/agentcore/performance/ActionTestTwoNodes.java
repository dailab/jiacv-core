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
import de.dailab.jiactng.agentcore.performance.actionTest.twoNodes.ActionCallingAgentBean;
import de.dailab.jiactng.agentcore.performance.actionTest.twoNodes.ActionProvidingAgentBean;

/**
 * Tests the time of calling actions
 * 
 * @author Hilmi Yildirim
 *
 */
public class ActionTestTwoNodes {
	
	/**
	 * path to the xml where the node 1 is described
	 */
	private final static String PATH_TO_NODE1_XML = "de" + File.separator + "dailab" + File.separator + 
			"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
			+ "actionTest" + File.separator + "twoNodes" + File.separator + "TestNode1.xml";
	
	/**
	 * path to the xml where the node 2 is described
	 */
	private final static String PATH_TO_NODE2_XML = "de" + File.separator + "dailab" + File.separator + 
			"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
			+ "actionTest" + File.separator + "twoNodes" + File.separator + "TestNode2.xml";
	
	/**
	 * agent which calls methods
	 */
	private static ActionCallingAgentBean actionCallingAgentBean = null;
	
	/**
	 * agent which provide a method
	 */
	private static ActionProvidingAgentBean actionProvidingAgentBean = null;
	
	/**
	 * the node where the calling agent runs
	 */
	private static SimpleAgentNode node1;
	
	/**
	 * the node where the action providing agent runs
	 */
	private static SimpleAgentNode node2;
	
	/**
	 * name of the action with two agents
	 */
	public final static String CALCULATE_ACTION_CALLING_TIME = "calculateActionCallingTime";
	
	/**
	 * name of the action with two agents asynchron
	 */
	public final static String CALCULATE_ACTION_CALLING_TIME_ASYNC = "calculateActionCallingTimeAsync";
	
	
	
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
	 * locking object
	 */
	public static Object lockObject;
	
	/**
	 * initialize the output
	 */
	@BeforeClass
	public static void createFile(){		
		output = new String[8];
		lockObject = new Object();
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
			if(agent.getAgentName().equals("ActionCallingAgent")){
				actionCallingAgentBean = ((Agent)(agent)).findAgentBean(ActionCallingAgentBean.class);
			}
			}catch(Exception e){
				System.err.println("Exception: " + e.getMessage());
			}
		}
		
		agents = node2.findAgents();
		
		for(IAgent agent : agents){
			try{
			if(agent.getAgentName().equals("ActionProvidingAgent")){
				actionProvidingAgentBean = ((Agent)(agent)).findAgentBean(ActionProvidingAgentBean.class);
			}
			}catch(Exception e){
				System.err.println("Exception: " + e.getMessage());
			}
		}
		if(actionCallingAgentBean == null){
			throw new RuntimeException("action calling agent not found");
		}
		if(actionProvidingAgentBean == null){
			throw new RuntimeException("action providing agent not found");
		}
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}
	
	/**
	 * initialize the agent
	 */
	public void init(int actionsToCall){
		actionCallingAgentBean.initialize(actionsToCall);
		actionProvidingAgentBean.inititialize(actionsToCall);
	}
	
	@Test
	public void actionTimeTest1(){
		init(1);
		actionCallingAgentBean.callAction();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(0, "yes", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTest10(){
		init(10);
		actionCallingAgentBean.callAction();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(1, "yes", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTest100(){
		init(100);
		actionCallingAgentBean.callAction();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(2, "yes", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTest1000(){
		init(1000);
		actionCallingAgentBean.callAction();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(3, "yes", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestAsync1(){
		init(1);
		actionCallingAgentBean.callActionAsync();
		long[] actionTimes = getActionCallingTimesTwoAsync();
		generateCsvOutput(4, "no", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestAsync10(){
		init(10);
		actionCallingAgentBean.callActionAsync();
		long[] actionTimes = getActionCallingTimesTwoAsync();
		generateCsvOutput(5, "no", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestAsync100(){
		init(100);
		actionCallingAgentBean.callActionAsync();
		long[] actionTimes = getActionCallingTimesTwoAsync();
		generateCsvOutput(6, "no", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestAsync1000(){
		init(1000);
		actionCallingAgentBean.callActionAsync();
		long[] actionTimes = getActionCallingTimesTwoAsync();
		generateCsvOutput(7, "no", "2", actionTimes);
	}
	
	/**
	 * shut down the current node after every test
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
	public static void writeInCSVFile(){
		try {
			csvFileWriter = new BufferedWriter(new FileWriter(new File(CSVPATH), true));
			
			SimpleDateFormat format = new SimpleDateFormat ("dd.MM.yyyy 'um' HH:mm:ss");
			Date currentTime = new Date();
			csvFileWriter.write("Action test mit zwei Knoten: Durchgefuehrt am: " + format.format(currentTime));
			csvFileWriter.newLine();
			csvFileWriter.write("number of agents ,number of action callings,total time, average time, all times...");
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
	 * generates a line of the csv file and save it in output
	 */
	public void generateCsvOutput(int order, String sync, String numberOfAgents, long[] times){	
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
		//number of agents
		output[order] += numberOfAgents + ",";
		//number of messages
		output[order] += times.length + ",";
		//total time
		output[order] += totalTime + ",";
		//average time
		output[order] += averageTime + ",";
		//all times
		output[order] += allTimes;
	}
	
	/**
	 * @return the time of getting a result of calling a method from another node asynchrony
	 */
	public long[] getActionCallingTimesTwoAsync(){
		long[] receiveTimes = null;
		synchronized (lockObject) {
			while(!actionProvidingAgentBean.allActionsCalled()){
				try {
					lockObject.wait();
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
			}
			receiveTimes = actionProvidingAgentBean.getActionTimes();
		}
		return receiveTimes;
	}
	
	/**
	 * @return the time of getting a result of calling his own asynchrony
	 */
	public long[] getActionCallingTimesOneAsync(){
		long[] receiveTimes = null;
		synchronized (lockObject) {
			while(!actionCallingAgentBean.allActionsCalled()){
				try {
					lockObject.wait();
				} catch (InterruptedException e) {
					System.err.println(e.getMessage());
				}
			}
			receiveTimes = actionCallingAgentBean.getActionTimes();
		}
		return receiveTimes;
	}
}
