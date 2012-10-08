package de.dailab.jiactng.agentcore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

public class InvokeWithBacktrackingTest {
	
	/**
	 * config id of node with one non faulty agent
	 */
	private final byte ONE_NON_FAULTY_AGENT = 0;
	
	/**
	 * config id of node with one non faulty agent and ten faulty agents
	 */
	private final byte ONE_NON_FAULTY_AGENT_TEN_FAULTY_AGENTS = 1;
	
	/**
	 * config id of node with one faulty agent
	 */
	private final byte ONE_FAULTY_AGENT = 2;
	
	/**
	 * path to the node config file
	 */
	private final String PATH_TO_NODE_CONFIG = "de" + File.separator + "dailab" + File.separator + 
			"jiactng" + File.separator + "agentcore" + File.separator;
	
	/**
	 * node for testing
	 */
	private static SimpleAgentNode node;
	
	/**
	 * agent which calls invokeAndBacktracking method
	 */
	private InvokeWithBacktrackingAgentBean testAgent;
	
	public void startNode(byte nodeID){
		String testNode = "";
		switch(nodeID){
			case ONE_NON_FAULTY_AGENT: 
				testNode = "TestNode1NonFaulty";
				break;
			case ONE_NON_FAULTY_AGENT_TEN_FAULTY_AGENTS:
				testNode = "TestNode1NonFaulty10Faulty";
				break;
			case ONE_FAULTY_AGENT:
				testNode = "TestNode1Faulty";
			
		}
		node = (SimpleAgentNode) new ClassPathXmlApplicationContext(PATH_TO_NODE_CONFIG + testNode + ".xml").getBean(testNode);
	
		List<IAgent> agents = node.findAgents();
		
		for(IAgent agent : agents){
			try{
			if(agent.getAgentName().equals("InvokeWithBacktrackingAgent")){
				testAgent = ((Agent)(agent)).findAgentBean(InvokeWithBacktrackingAgentBean.class);
			}
			}catch(Exception e){
				System.err.println("Exception: " + e.getMessage());
			}
		}
	}
	
	@Test
	public void OneNonFaultyAgent(){
		startNode(ONE_NON_FAULTY_AGENT);
		boolean resulted = testAgent.callMethod();
		assertTrue(resulted);
	}
	
	@Test
	public void OneNonFaultyAgentTenFaultyAgents(){
		startNode(ONE_NON_FAULTY_AGENT_TEN_FAULTY_AGENTS);
		boolean resulted = testAgent.callMethod();
		assertTrue(resulted);
	}
	
	@Test
	public void OneFaultyAgent(){
		startNode(ONE_FAULTY_AGENT);
		boolean resulted = testAgent.callMethod();
		assertFalse(resulted);
	}
	
	@After
	public void shutDown(){
		try {
			node.shutdown();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
	}
}
