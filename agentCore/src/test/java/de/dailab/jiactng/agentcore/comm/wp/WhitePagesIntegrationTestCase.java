package de.dailab.jiactng.agentcore.comm.wp;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.List;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.wp.WhitePagesTestBean;

import junit.framework.TestCase;

public class WhitePagesIntegrationTestCase extends TestCase {

	private static ClassPathXmlApplicationContext _xmlContext;
	private static boolean _setup = true;
	private static boolean _lastTestDone = false;
	
	private static IAgentNode _agentNode;
	private static IAgent _whitePagesAgent;
	private static WhitePagesTestBean _whitePagesTestBean;
	
	private static Action _sendAction = null;
	
	
	@Override
	protected void setUp() throws Exception {
		if (_setup){
			super.setUp();
			_setup = false;
			
			_xmlContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/wp/WhitePagesIntegrationTestContext.xml");
			
			_agentNode = (IAgentNode) _xmlContext.getBean("WhitePagePlatform");
			List<IAgent> agents = _agentNode.findAgents();
			
			for (IAgent agent : agents){
				if (agent.getAgentName().equalsIgnoreCase("WhitePagesAgent")){
					_whitePagesAgent = agent;
				}
			}
			List<IAgentBean> beans = _whitePagesAgent.getAgentBeans();
			
			for (IAgentBean bean : beans){
				if (bean.getBeanName().equalsIgnoreCase("WhitePagesTestbean")){
					_whitePagesTestBean = (WhitePagesTestBean) bean;
				}
			}
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(_lastTestDone){
			super.tearDown();
		}
	}
	
	public void testFindAgent(){
		/* check if the setup is done properly */
		assertNotNull("Setup Failure", _agentNode);
		assertNotNull("Setup Failure", _whitePagesAgent);
		assertNotNull("Setup Failure", _whitePagesTestBean);
		
		_whitePagesTestBean.searchForAgentDesc("FindMeAgent");
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<IFact> results = _whitePagesTestBean.getLastResult();
		AgentDescription findme = null;
		if (results.size() > 0){
			if (results.get(0) instanceof AgentDescription)
				findme = (AgentDescription) results.get(0);
		}
		
		assertNotNull(results);
		assertEquals(1, results.size());
		assertTrue(findme.getName().equalsIgnoreCase("FindMeAgent"));
	}
	
	public void testNothingToFind(){
		_whitePagesTestBean.searchForAgentDesc("NixaAgentos");
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<IFact> results = _whitePagesTestBean.getLastResult();
		
		assertNotNull(results);
		assertEquals(0, results.size());
		_lastTestDone = true;
	}
	
	public void testActionHandling(){
		// Test for storing, searching for and removing actions
		_sendAction = _whitePagesTestBean.getSendAction();
		IActionDescription actualActionDesc = (IActionDescription) _sendAction;
		
		// first add a description to the directory
		_whitePagesTestBean.addActionToDirectory(actualActionDesc);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// now let's search for it
		IActionDescription actionDesc = new Action("de.dailab.jiactng.agentcore.comm.ICommunicationBean#send");
		_whitePagesTestBean.searchForActionDesc(actionDesc);
		
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		List<IFact> results = _whitePagesTestBean.getLastResult();
		assertNotNull(results);
		
		if (results.size() > 0){
			IActionDescription resultDesc = (IActionDescription) results.get(0);
			assertEquals(actualActionDesc.hashCode(), resultDesc.hashCode());
		} else {
			// Let the world now that there was no result coming through.
			assertTrue("No Results were delivered", false);
		}
		
		
		// given that the action was stored properly and found now let's remove it and check if it will be removed.
		_whitePagesTestBean.removeActionFromDirectory(actualActionDesc);
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// no let's see if it was removed properly
		_whitePagesTestBean.searchForActionDesc(actionDesc);
		
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		results = _whitePagesTestBean.getLastResult();
		assertNotNull(results);
		
		assertEquals("There shouldn't be any results", 0, results.size());
		
	}
	
}
