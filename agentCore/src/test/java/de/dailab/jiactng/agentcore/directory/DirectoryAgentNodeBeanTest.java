package de.dailab.jiactng.agentcore.directory;

import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import junit.framework.TestCase;

public class DirectoryAgentNodeBeanTest extends TestCase {
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
	
	public void testGetterSetter() {
		//advertiseInterval
		assertEquals(10800, directoryRef.getAdvertiseInterval());
		directoryRef.setAdvertiseInterval(20000);
		assertEquals(20000, directoryRef.getAdvertiseInterval());
		
		//aliveInterval
		assertEquals(2000, directoryRef.getAliveInterval());
		directoryRef.setAliveInterval(1000);
		assertEquals(1000, directoryRef.getAliveInterval());
	}

	public void test() throws Exception{
		while (!nodeRef.getState().equals(LifecycleStates.STARTED));
		assertEquals(1, directoryRef.searchAllAgents(new AgentDescription()).size());
		assertEquals(2, directoryRef.searchAllActions(new Action()).size());
		
		IActionDescription action = directoryRef.searchAction(new Action("de.dailab.jiactng.agentcore.directory.AgentBeanWithActions#mirror"));
		assertNotNull(action);
		
		directoryRef.deregisterAction(action);
		assertEquals(1, directoryRef.searchAllActions(new Action()).size());
		
		List<IAgent> agents = nodeRef.findAgents();
		for (IAgent agent :agents) {
			agent.stop();
			agent.cleanup();
		}
		assertEquals(0, directoryRef.searchAllAgents(new AgentDescription()).size());
		assertEquals(0, directoryRef.searchAllActions(new Action()).size());
	}
	
	public void testAgentSide() throws Exception {
		while (!agentBeanRef.getState().equals(LifecycleStates.STARTED));
		assertNotNull(agentBeanRef.searchAction("de.dailab.jiactng.agentcore.directory.AgentBeanWithActions#concat"));
		assertEquals(2, agentBeanRef.searchAllActions("de.dailab.jiactng.agentcore.comm.ICommunicationBean#joinGroup").size());
		assertEquals(15, agentBeanRef.searchAllActions(null).size());
	}
}
