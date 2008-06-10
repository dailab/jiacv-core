package de.dailab.jiactng.agentcore.comm.wp;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.IAgentNodeBean;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.comm.wp.WhitePagesTestBean;
import de.dailab.jiactng.agentcore.comm.wp.helpclasses.TimeoutException;

import junit.framework.TestCase;

public class WhitePagesIntegrationTestCase extends TestCase {

	private static ClassPathXmlApplicationContext _xmlContext;
	private static boolean _setup = true;
	private static boolean _lastTestDone = false;
	private static boolean _debug = false;
	private static boolean _deactivateTests = false;
	
	private static IAgentNode _agentNode;
//	private static IAgentNode _otherNode;
	private static IAgent _whitePagesAgent;
	private static WhitePagesTestBean _whitePagesTestBean;
	private static RemoteActionTestBean _remoteActionTestBean;
//	private static DirectoryAccessBean _directoryAccessBean;
	private static DirectoryAgentNodeBean _directoryAgentNodeBean;
	
	private static Action _sendAction = null;
	
	
	@Override
	protected void setUp() throws Exception {
		if (_setup){
			super.setUp();
			_setup = false;
			
			_xmlContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/wp/WhitePagesIntegrationTestContext.xml");
			
			_agentNode = (IAgentNode) _xmlContext.getBean("WhitePagePlatform");
//			_otherNode = (IAgentNode) _xmlContext.getBean("RemoteWhitePagePlatform");
			
			List<IAgentNodeBean> agentNodeBeans = _agentNode.getAgentNodeBeans();
			for (IAgentNodeBean bean : agentNodeBeans){
				if (bean instanceof DirectoryAgentNodeBean){
					_directoryAgentNodeBean = (DirectoryAgentNodeBean) bean;
				}
			}
			
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
				} else if (bean.getBeanName().equalsIgnoreCase("RemoteActionTestBean")){
					_remoteActionTestBean = (RemoteActionTestBean) bean;
				} 
//				else if (bean.getBeanName().equalsIgnoreCase("DirectoryAccessBean")){
//					_directoryAccessBean = (DirectoryAccessBean) bean;
//				}
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
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestFindAgent ---");
		}
		/* check if the setup is done properly */
		assertNotNull("Setup Failure", _agentNode);
		assertNotNull("Setup Failure", _whitePagesAgent);
		assertNotNull("Setup Failure", _whitePagesTestBean);
		
		_whitePagesTestBean.searchForAgentDesc("FindMeAgent", false);
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
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestNothingToFind ---");
		}
		_whitePagesTestBean.searchForAgentDesc("NixaAgentos", false);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<IFact> results = _whitePagesTestBean.getLastResult();
		
		assertNotNull(results);
		assertEquals(0, results.size());
	}
	
	public void testActionStorage(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestActionStorage ---");
		}
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
		_whitePagesTestBean.searchForActionDesc(actionDesc, false);
		
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		List<IFact> results = _whitePagesTestBean.getLastResult();
		
		assertNotNull(results);
		
		if (results.size() > 0){
			Action resultAction = (Action) results.get(0);
			assertEquals(actualActionDesc.hashCode(), resultAction.hashCode());
		} else {
			// Let the world know that there was no result coming through.
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
		_whitePagesTestBean.searchForActionDesc(actionDesc, false);
		
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		results = _whitePagesTestBean.getLastResult();
		assertNotNull(results);
		assertEquals("There shouldn't be any results", 0, results.size());
		
	}
	
	public void testRemoteActionTimeoutHandling(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestRemoteActionTimeoutHandling ---");
		}
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Action action = new Action(RemoteActionProviderBean.ACTION_TIMEOUT_TEST);
		_whitePagesTestBean.searchForActionDesc(action, false);
		
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		List<IFact> results = _whitePagesTestBean.getLastResult();
		assertNotNull(results);
		
		if (results.get(0) instanceof Action){
			Action remoteAction = (Action) results.get(0);
			_remoteActionTestBean.useRemoteAction(remoteAction, new Serializable[] {}, 1000);
		}
		
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ActionResult actionResult = _remoteActionTestBean.getLastActionResult();
		assertNotNull(actionResult);
		
		assertNull(actionResult.getResults());
		assertNotNull(actionResult.getFailure());
		assertTrue(actionResult.getFailure() instanceof TimeoutException);
		
	}
	
	public void testSearchTimeout(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestSearchTimeout ---");
		}
		_whitePagesTestBean.TimeoutTest();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ActionResult result = _whitePagesTestBean.getLastFailure();
		assertNotNull(result);
		assertNull(result.getResults());
		assertNotNull(result.getFailure());
		assertTrue(result.getFailure() instanceof TimeoutException);
	}
	
	public void testRemoteActionHandling(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestRemoteActionHandling ---");
		}
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Searching for Action");
		}
		final String REMOTE_RESULT = "Live long and prosper";
	
		Action action = new Action(RemoteActionProviderBean.ACTION_GET_SOME_RESULT);
		_whitePagesTestBean.searchForActionDesc(action, false);
		
		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Checking for Result");
		}
		List<IFact> remoteActions = _whitePagesTestBean.getLastResult();
		assertNotNull(remoteActions);
		assertNotNull(remoteActions.get(0));
		assertTrue(remoteActions.get(0) instanceof Action);
		
		Action remoteAction = (Action) remoteActions.get(0);
		Serializable[] params = {REMOTE_RESULT};
		
		if (_debug) {
			System.err.println(": : Trying to use the remote Action");
		}
		_remoteActionTestBean.useRemoteAction(remoteAction, params);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Checking for Result again");
		}
		ActionResult result = _remoteActionTestBean.getLastActionResult();
		assertNotNull(result);
		
		Object[] actionResults = result.getResults();
		
		assertTrue(actionResults[0] instanceof String);
		String remoteResult = (String) actionResults[0];
		
		assertTrue(remoteResult.equalsIgnoreCase(REMOTE_RESULT));
	}
	
	public void testEnlistening(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestEnlistening ---");
		}
		_whitePagesTestBean.searchForActionDesc(_whitePagesTestBean.getSendAction(), false);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		List<IFact> foundSendActions = _whitePagesTestBean.getLastResult();
		assertTrue(foundSendActions.isEmpty());
		
		Action template = new Action("de.dailab.jiactng.agentcore.comm.ICommunicationBean#send");
		
		ArrayList<Action> templates = new ArrayList<Action>();
		templates.add(template);
		
		_whitePagesTestBean.addAutoEnlistActionTemplate(templates);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		_whitePagesTestBean.searchForActionDesc(_whitePagesTestBean.getSendAction(), false);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		foundSendActions = _whitePagesTestBean.getLastResult();
		assertEquals(1, foundSendActions.size() ); 
		
		_whitePagesTestBean.removeAutoEnlistActionTemplate(templates);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		_whitePagesTestBean.searchForActionDesc(_whitePagesTestBean.getSendAction(), false);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		foundSendActions = _whitePagesTestBean.getLastResult();
		assertEquals(0, foundSendActions.size() ); 
			
	}
	
	public void testGlobalRemoteActionSearchingAndHandling(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
			
		if (_debug) {
			System.err.println("--- TestGlobalRemoteActionSearchingAndHandling ---");
		}
		
		if (_debug) {
			System.err.println(": : Searching for Action on other Node");
		}
	
		Action action = new Action(GlobalRemoteActionProviderBean.ACTION_GET_GLOBAL_RESULT);
		_whitePagesTestBean.searchForActionDesc(action, true, 5000);
		
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Checking for Result");
		}
		List<IFact> remoteActions = _whitePagesTestBean.getLastResult();
		if (_debug) {
			System.err.println("Result found reads: " + remoteActions);
		}
		
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		assertNotNull(remoteActions);
		assertEquals(1, remoteActions.size());
		assertNotNull(remoteActions.get(0));
		assertTrue(remoteActions.get(0) instanceof Action);
		
		Action remoteAction = (Action) remoteActions.get(0);
		Serializable[] params = {};
		
		if (_debug) {
			System.err.println(": : Trying to use the global remote Action");
		}
		_remoteActionTestBean.useRemoteAction(remoteAction, params);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Checking for Result again");
		}
		ActionResult result = _remoteActionTestBean.getLastActionResult();
		assertNotNull(result);
		
		Object[] actionResults = result.getResults();
		
		assertTrue(actionResults[0] instanceof String);
		String remoteResult = (String) actionResults[0];
		
		if(_debug){
			System.err.println("REMOTE RESULT READS: " + remoteResult);
		}
		
		assertTrue(remoteResult.equalsIgnoreCase("RemoteAgentOnOtherNode"));
	}
	
	public void testGlobalAgentSearching(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestGlobalAgentSearching ---");
		}
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Searching for RemoteAgentOnOtherNode");
		}
		
		_whitePagesTestBean.searchForAgentDesc("RemoteAgentOnOtherNode", true, 5000);
		
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Checking for Results");
		}
		
		List<IFact> results = _whitePagesTestBean.getLastResult();
		
		if (_debug) {
			System.err.println(": : Result found reads " + results);
		}
		
		assertNotNull(results);
		assertEquals(1, results.size());
		assertNotNull(results.get(0));
		assertTrue(results.get(0) instanceof AgentDescription);
		AgentDescription agentDesc = (AgentDescription) results.get(0);
		assertEquals("RemoteAgentOnOtherNode", agentDesc.getName());
	}
	
	public void testGlobalRemoteActionSearchingAndHandlingWithoutCache(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
			
		_directoryAgentNodeBean.setCacheIsActive(false);
		
		if (_debug) {
			System.err.println("--- TestGlobalRemoteActionSearchingAndHandling ---");
		}
		
		if (_debug) {
			System.err.println(": : Searching for Action on other Node");
		}
	
		Action action = new Action(GlobalRemoteActionProviderBean.ACTION_GET_GLOBAL_RESULT);
		_whitePagesTestBean.searchForActionDesc(action, true, 5000);
		
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Checking for Result");
		}
		List<IFact> remoteActions = _whitePagesTestBean.getLastResult();
		if (_debug) {
			System.err.println("Result found reads: " + remoteActions);
		}
		
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		assertNotNull(remoteActions);
		assertEquals(2, remoteActions.size());
		assertNotNull(remoteActions.get(0));
		assertNotNull(remoteActions.get(1));
		assertTrue(remoteActions.get(0) instanceof Action);
		assertTrue(remoteActions.get(1) instanceof Action);	
		
		Action remoteAction = (Action) remoteActions.get(0);
		Serializable[] params = {};
		
		if (_debug) {
			System.err.println(": : Trying to use the global remote Action");
		}
		_remoteActionTestBean.useRemoteAction(remoteAction, params);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Checking for Result again");
		}
		ActionResult result = _remoteActionTestBean.getLastActionResult();
		assertNotNull(result);
		
		Object[] actionResults = result.getResults();
		
		assertTrue(actionResults[0] instanceof String);
		String remoteResult = (String) actionResults[0];
		
		if(_debug){
			System.err.println("REMOTE RESULT READS: " + remoteResult);
		}
		
		assertTrue(remoteResult.equalsIgnoreCase("RemoteAgentOnOtherNode"));
	}
	
	public void testGlobalAgentSearchingWithoutCache(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		
		_directoryAgentNodeBean.setCacheIsActive(false);
		
		if (_debug) {
			System.err.println("--- TestGlobalAgentSearching ---");
		}
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Searching for RemoteAgentOnOtherNode");
		}
		
		_whitePagesTestBean.searchForAgentDesc("RemoteAgentOnOtherNode", true, 5000);
		
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (_debug) {
			System.err.println(": : Checking for Results");
		}
		
		List<IFact> results = _whitePagesTestBean.getLastResult();
		
		if (_debug) {
			System.err.println(": : Result found reads " + results);
		}
		
		assertNotNull(results);
		assertEquals(2, results.size());
		
		assertNotNull(results.get(0));
		assertTrue(results.get(0) instanceof AgentDescription);
		AgentDescription agentDesc = (AgentDescription) results.get(0);
		assertEquals("RemoteAgentOnOtherNode", agentDesc.getName());
		
		assertNotNull(results.get(1));
		assertTrue(results.get(1) instanceof AgentDescription);
		agentDesc = (AgentDescription) results.get(1);
		assertEquals("RemoteAgentOnOtherNode", agentDesc.getName());
		
		_lastTestDone = true;
	}
		
	
	//TODO Testcase for global searches for agents on other Nodes. 
	
	//TODO NoSuchAction Exception isn't thrown at the moment. Check and Resolve
//	public void testNoSuchAction(){
//		
//		if (_debug) {
//			System.err.println("TestNoSuchAction");
//		}
//		Action fakeRemoteAction = new Action("NoSuchAction", _directoryAccessBean, new Class[] {}, new Class[] {Object.class});
//		_remoteActionTestBean.useRemoteAction(fakeRemoteAction, new Object[] {Object.class});
//		
//		try {
//			Thread.sleep(3500);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		ActionResult result = _remoteActionTestBean.getLastActionResult();
//		assertNotNull(result);
//		assertNotNull(result.getFailure());
//		assertTrue(result.getFailure() instanceof NoSuchActionException);
//		_lastTestDone = true;
//	}

	
}
