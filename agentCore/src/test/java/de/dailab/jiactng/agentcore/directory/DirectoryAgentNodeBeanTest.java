package de.dailab.jiactng.agentcore.directory;

import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.JIACTestForJUnit3;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;

public class DirectoryAgentNodeBeanTest extends JIACTestForJUnit3 {
	private final String nodeName = "myNode";
	private final String beanName = "IDirectory";
	private final String actionBeanName = "AgentBeanWithActions";
	private ClassPathXmlApplicationContext context = null;
	private SimpleAgentNode nodeRef = null;
	private DirectoryAgentNodeBean directoryRef = null;
	private AgentBeanWithActions agentBeanRef = null;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		context = new ClassPathXmlApplicationContext(
		"de/dailab/jiactng/agentcore/directory/directoryTests.xml");
		nodeRef = (SimpleAgentNode) context.getBean(nodeName);
		directoryRef = (DirectoryAgentNodeBean)context.getBean(beanName);
		agentBeanRef = (AgentBeanWithActions)context.getBean(actionBeanName);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		agentBeanRef = null;
		directoryRef = null;
		nodeRef.shutdown();
		context.close();
		context = null;
		nodeRef = null;
	}
	
	public void testGetterSetter() throws Exception {
		//advertiseIntervals
		assertEquals(180000, directoryRef.getAdvertiseInterval("df@dfgroup"));
		directoryRef.setAdvertiseInterval(120000);
		directoryRef.cleanup();
		directoryRef.start();
		assertEquals(120000, directoryRef.getAdvertiseInterval("df@dfgroup"));
		
		//aliveIntervals
		assertEquals(15000, directoryRef.getAliveInterval("df@dfgroup"));
		directoryRef.setAliveInterval(10000);
		directoryRef.cleanup();
		directoryRef.start();
		assertEquals(10000, directoryRef.getAliveInterval("df@dfgroup"));

		//allowedAliveDelay
		assertEquals(10000, directoryRef.getAllowedAliveDelay());
		directoryRef.setAllowedAliveDelay(5000);
		assertEquals(5000, directoryRef.getAllowedAliveDelay());
		
		//maxAliveDelay
		assertEquals(20000, directoryRef.getMaxAliveDelay());
		directoryRef.setMaxAliveDelay(15000);
		assertEquals(15000, directoryRef.getMaxAliveDelay());
	}

	public void test() throws Exception{
		while (!nodeRef.getState().equals(LifecycleStates.STARTED));

		// create template for searching only local agents
		AgentDescription agentTemplate = new AgentDescription();
		agentTemplate.setAgentNodeUUID(nodeRef.getUUID());

		// create template for searching only local actions
		Action actionTemplate = new Action();
		actionTemplate.setProviderDescription(agentTemplate);

		assertEquals(1, directoryRef.searchAllAgents(agentTemplate).size());
		assertEquals(2, directoryRef.searchAllActions(actionTemplate).size());

		IActionDescription action = directoryRef.searchAction(new Action("de.dailab.jiactng.agentcore.directory.AgentBeanWithActions#mirror"));
		assertNotNull(action);

		directoryRef.deregisterAction(action);
		assertEquals(1, directoryRef.searchAllActions(actionTemplate).size());

		List<IAgent> agents = nodeRef.findAgents();
		for (IAgent agent :agents) {
			agent.stop();
			agent.cleanup();
		}
		assertEquals(0, directoryRef.searchAllAgents(agentTemplate).size());
		assertEquals(0, directoryRef.searchAllActions(actionTemplate).size());
	}

	public void testAgentSide() throws Exception {
		while (!agentBeanRef.getState().equals(LifecycleStates.STARTED));
		assertNotNull(agentBeanRef.searchAction("de.dailab.jiactng.agentcore.directory.AgentBeanWithActions#concat"));
		assertEquals(2, agentBeanRef.searchAllActions("de.dailab.jiactng.agentcore.comm.ICommunicationBean#joinGroup").size());
		assertEquals(15, agentBeanRef.searchAllActions(null).size());
	}
}
