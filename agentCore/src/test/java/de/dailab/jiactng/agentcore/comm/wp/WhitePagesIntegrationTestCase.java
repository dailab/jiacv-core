package de.dailab.jiactng.agentcore.comm.wp;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.List;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import junit.framework.TestCase;

public class WhitePagesIntegrationTestCase extends TestCase {

	private static ClassPathXmlApplicationContext _xmlContext;
	private static boolean _setup = true;
	private static boolean _lastTestDone = false;
	
	private static IAgentNode _agentNode;
	private static IAgent _whitePagesAgent;
	private static WhitePagesTestBean _whitePagesTestBean;
	
	
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
		_whitePagesTestBean.searchForAgentDesc("findmeagent");
		try {
			Thread.sleep(2500);
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
	
}
