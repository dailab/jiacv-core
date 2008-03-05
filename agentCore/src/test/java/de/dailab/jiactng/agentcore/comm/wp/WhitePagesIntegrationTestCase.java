package de.dailab.jiactng.agentcore.comm.wp;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.util.List;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import junit.framework.TestCase;

public class WhitePagesIntegrationTestCase extends TestCase {

	ClassPathXmlApplicationContext _xmlContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/wp/WhitePagesIntegrationTestContext.xml");
	private boolean _setup = true;
	
	IAgentNode _agentNode;
	IAgent _whitePageAgent;
	WhitePagesTestBean _whitePagesTestBean;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (_setup){
			_setup = false;
			
			_agentNode = (IAgentNode) _xmlContext.getBean("WhitePagePlatform");
			List<IAgent> agents = _agentNode.findAgents();
			
			for (IAgent agent : agents){
				if (agent.getAgentName().equalsIgnoreCase("WhitePageAgent")){
					_whitePageAgent = agent;
				}
			}
			List<IAgentBean> beans = _whitePageAgent.getAgentBeans();
			
			for (IAgentBean bean : beans){
				if (bean.getBeanName().equalsIgnoreCase("WhitePagesTestbean")){
					_whitePagesTestBean = (WhitePagesTestBean) bean;
				}
			}	
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
	}
	
	public void testFindAgent(){
//		_whitePagesTestBean.searchForAgentDesc("FindMeAgent");
//		try {
//			Thread.sleep(2500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		List<AgentDescription> results = _whitePagesTestBean.getLastResult();
//		
//		assertNotNull(results);
//		assertEquals(results.size(), 1);
//		assertTrue(results.get(1).getName().equalsIgnoreCase("FindMeAgent"));
	}
	
	public void testNothingToFind(){
//		_whitePagesTestBean.searchForAgentDesc("NixaAgentos");
//		try {
//			Thread.sleep(2500);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		List<AgentDescription> results = _whitePagesTestBean.getLastResult();
//		
//		assertNull(results);
//		assertEquals(results.size(), 0);
	}
	
}
