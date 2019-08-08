package de.dailab.jiactng.agentcore.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.performance.actionTest.oneNode.ActionCallingAgentBean;
import de.dailab.jiactng.agentcore.performance.actionTest.oneNode.ActionProvidingAgentBean;


/**
 * Tests the time of calling actions
 * 
 * @author Hilmi Yildirim
 *
 */
public class ActionTestOneNode {
	
	/**
	 * path to the xml where the node is described
	 */
	private final String PATH_TO_NODE_XML = "de" + File.separator + "dailab" + File.separator + 
			"jiactng" + File.separator + "agentcore" + File.separator + "performance" + File.separator 
			+ "actionTest" + File.separator + "oneNode" + File.separator + "TestNode.xml";
	
	/**
	 * agent which calls methods
	 */
	private ActionCallingAgentBean actionCallingAgentBean = null;
	
	/**
	 * agent which provide a method
	 */
	private ActionProvidingAgentBean actionProvidingAgentBean = null;
	
	/**
	 * the node where the agents run
	 */
	private SimpleAgentNode node;
	
	/**
	 * name of the action with two agents
	 */
	public final static String CALCULATE_ACTION_CALLING_TIME_TWO = "calculateActionCallingTimeTwo";
	
	/**
	 * name of the action with two agents asynchron
	 */
	public final static String CALCULATE_ACTION_CALLING_TIME_TWO_ASYNC = "calculateActionCallingTimeTwoAsync";
	
	/**
	 * name of the action with one agent
	 */
	public final static String CALCULATE_ACTION_CALLING_TIME_ONE = "calculateActionCallingTimeOne";
	
	/**
	 * name of the action with one agent asynchron
	 */
	public final static String CALCULATE_ACTION_CALLING_TIME_ONE_ASYNC = "calculateActionCallingTimeOneAsync";
	
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
		output = new String[16];
		lockObject = new Object();
	}
	
	/**
	 * starts the node and find the agent
	 */
	@Before
	public void startNode(){
		node = (SimpleAgentNode) new ClassPathXmlApplicationContext(PATH_TO_NODE_XML).getBean("TestNode");
		
		List<IAgent> agents = node.findAgents();
		
		for(IAgent agent : agents){
			try{
			if(agent.getAgentName().equals("ActionCallingAgent")){
				actionCallingAgentBean = ((Agent)(agent)).findAgentBean(ActionCallingAgentBean.class);
			}
			if(agent.getAgentName().equals("ActionProvidingAgent")){
				actionProvidingAgentBean = ((Agent)agent).findAgentBean(ActionProvidingAgentBean.class);
			}
			}catch(Exception e){
				System.err.println("Exception: " + e.getMessage());
			}
		}
		if(actionCallingAgentBean == null)
			throw new RuntimeException("action calling agent not found");
		if(actionProvidingAgentBean == null)
			throw new RuntimeException("action providing agent not found");
	}
	
	/**
	 * initialize the agent
	 */
	public void init(int actionsToCall){
		actionCallingAgentBean.initialize(actionsToCall);
		actionProvidingAgentBean.inititialize(actionsToCall);
	}
	
	@Test
	public void actionTimeTestTwoAgents1(){
		init(1);
		actionCallingAgentBean.callActionFromAnotherNode();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(0, "yes", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestTwoAgents10(){
		init(10);
		actionCallingAgentBean.callActionFromAnotherNode();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(1, "yes", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestTwoAgents100(){
		init(100);
		actionCallingAgentBean.callActionFromAnotherNode();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(2, "yes", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestTwoAgents1000(){
		init(1000);
		actionCallingAgentBean.callActionFromAnotherNode();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(3, "yes", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestTwoAgentsAsync1(){
		init(1);
		actionCallingAgentBean.callActionFromAnotherNodeAsync();
		long[] actionTimes = getActionCallingTimesTwoAsync();
		generateCsvOutput(4, "no", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestTwoAgentsAsync10(){
		init(10);
		actionCallingAgentBean.callActionFromAnotherNodeAsync();
		long[] actionTimes = getActionCallingTimesTwoAsync();
		generateCsvOutput(5, "no", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestTwoAgentsAsync100(){
		init(100);
		actionCallingAgentBean.callActionFromAnotherNodeAsync();
		long[] actionTimes = getActionCallingTimesTwoAsync();
		generateCsvOutput(6, "no", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestTwoAgentsAsync1000(){
		init(1000);
		actionCallingAgentBean.callActionFromAnotherNodeAsync();
		long[] actionTimes = getActionCallingTimesTwoAsync();
		generateCsvOutput(7, "no", "2", actionTimes);
	}
	
	@Test
	public void actionTimeTestOneAgent1(){
		init(1);
		actionCallingAgentBean.callOwnAction();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(8, "yes", "1", actionTimes);
	}
	
	@Test
	public void actionTimeTestOneAgent10(){
		init(10);
		actionCallingAgentBean.callOwnAction();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(9, "yes", "1", actionTimes);
	}
	
	@Test
	public void actionTimeTestOneAgent100(){
		init(100);
		actionCallingAgentBean.callOwnAction();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(10, "yes", "1", actionTimes);
	}
	
	@Test
	public void actionTimeTestOneAgent1000(){
		init(1000);
		actionCallingAgentBean.callOwnAction();
		long[] actionTimes = actionCallingAgentBean.getActionTimes();
		generateCsvOutput(11, "yes", "1", actionTimes);
	}
	
	@Test
	public void actionTimeTestOneAgentAsync1(){
		init(1);
		actionCallingAgentBean.callOwnActionAsync();
		long[] actionTimes = getActionCallingTimesOneAsync();
		generateCsvOutput(12, "no", "1", actionTimes);
	}
	
	@Test
	public void actionTimeTestOneAgentAsync10(){
		init(10);
		actionCallingAgentBean.callOwnActionAsync();
		long[] actionTimes = getActionCallingTimesOneAsync();
		generateCsvOutput(13, "no", "1", actionTimes);
	}
	
	@Test
	public void actionTimeTestOneAgentAsync100(){
		init(100);
		actionCallingAgentBean.callOwnActionAsync();
		long[] actionTimes = getActionCallingTimesOneAsync();
		generateCsvOutput(14, "no", "1", actionTimes);
	}
	
	@Test
	public void actionTimeTestOneAgentAsync1000(){
		init(1000);
		actionCallingAgentBean.callOwnActionAsync();
		long[] actionTimes = getActionCallingTimesOneAsync();
		generateCsvOutput(15, "no", "1", actionTimes);
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
	public static void writeInCSVFile(){
		try {
			csvFileWriter = new BufferedWriter(new FileWriter(new File(CSVPATH), true));
			
			SimpleDateFormat format = new SimpleDateFormat ("dd.MM.yyyy 'um' HH:mm:ss");
			Date currentTime = new Date();
			csvFileWriter.write("Action test mit einem Knoten: Durchgefuehrt am: " + format.format(currentTime));
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
