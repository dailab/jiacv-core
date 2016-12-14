package de.dailab.jiactng.agentcore;

import static org.junit.Assert.assertTrue;

import java.util.List;

import de.dailab.jiactng.JIACTestForJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

public class InvokeParallelTest extends JIACTestForJUnit4 {

	/**
	 * Spring application context
	 */
	private ClassPathXmlApplicationContext context;

	/**
	 * node for testing
	 */
	private SimpleAgentNode node;

	/**
	 * agent which calls invokeParallel method
	 */
	private InvokeParallelAgentBean testAgent;

	@Before
	public void startNode(){
		context = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/ParallelInvocationTestNode.xml");
		node = (SimpleAgentNode) context.getBean("TestNode");

		List<IAgent> agents = node.findAgents();

		for(IAgent agent : agents){
			try{
			if(agent.getAgentName().equals("InvokeParallelAgent")){
				testAgent = ((Agent)(agent)).findAgentBean(InvokeParallelAgentBean.class);
			}
			}catch(Exception e){
				System.err.println("Exception: " + e.getMessage());
			}
		}
	}

	@Test
	public void ThreeParallelInvocations(){
		boolean resulted = testAgent.callMethod(3);
		assertTrue(resulted);
	}

	@After
	public void shutDown(){
		try {
			node.shutdown();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}
		context.close();
	}
}
