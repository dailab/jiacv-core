package de.dailab.jiactng.agentcore.comm.wp;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.IAgentNodeBean;
import de.dailab.jiactng.agentcore.SimpleAgentNodeMBean;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.comm.wp.WhitePagesTestBean;
import de.dailab.jiactng.agentcore.comm.wp.exceptions.TimeoutException;

import junit.framework.TestCase;

public class WhitePagesIntegrationTestCase extends TestCase {

	private static ClassPathXmlApplicationContext _xmlContext;
	private static boolean _setup = true;
	private static boolean _lastTestDone = false;
	private static boolean _debug = false;
	private static boolean _deactivateTests = false;

	private static IAgentNode _agentNode;
	private static IAgentNode _otherNode;
	private static IAgent _whitePagesAgent;
	private static WhitePagesTestBean _whitePagesTestBean;
	private static RemoteActionTestBean _remoteActionTestBean;
//	private static DirectoryAccessBean _directoryAccessBean;
	private static DirectoryAgentNodeBean _directoryAgentNodeBean;
	private static TimeoutSimulatorBean _timeoutSimulator;

	private static Action _sendAction = null;


	@Override
	protected void setUp() throws Exception {
		if (_setup){
			super.setUp();
			_setup = false;

			_xmlContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/comm/wp/WhitePagesIntegrationTestContext.xml");

			_agentNode = (IAgentNode) _xmlContext.getBean("WhitePagePlatform");
			_otherNode = (IAgentNode) _xmlContext.getBean("RemoteWhitePagePlatform");

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
				} else if (bean.getBeanName().equalsIgnoreCase("TimeoutSimulatorBean")){
					_timeoutSimulator = (TimeoutSimulatorBean) bean;
				}
//				else if (bean.getBeanName().equalsIgnoreCase("DirectoryAccessBean")){
//				_directoryAccessBean = (DirectoryAccessBean) bean;
//				}
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		if(_lastTestDone){
			super.tearDown();
			((SimpleAgentNodeMBean)_agentNode).shutdown();
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
			Thread.sleep(1500);
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
			Thread.sleep(1500);
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
			Thread.sleep(2500);
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
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		results = _whitePagesTestBean.getLastResult();
		assertNotNull(results);
		assertEquals("There shouldn't be any results", 0, results.size());

	}

	public void testAddAndRemoveAgent(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestAddAndRemoveAgent ---");
		}

		_whitePagesTestBean.searchForAgentDesc("AddMeAgent", false);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<IFact> results = _whitePagesTestBean.getLastResult();

		assertNotNull(results);
		assertEquals(0, results.size());
		// Thats right because the agent should be on the node at this point

		// now let's add a new agent during runtime
		Agent newAgent = (Agent) _xmlContext.getBean("AddMeAgent");
		_agentNode.addAgent(newAgent);
		try {
			newAgent.init();
			newAgent.start();
		} catch (LifecycleException e1) {
			e1.printStackTrace();
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		_whitePagesTestBean.searchForAgentDesc("AddMeAgent", false);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		results = _whitePagesTestBean.getLastResult();

		assertNotNull(results);
		assertEquals(1, results.size());

		AgentDescription addme = null;
		if (results.size() > 0){
			if (results.get(0) instanceof AgentDescription)
				addme = (AgentDescription) results.get(0);
		}

		assertNotNull(results);
		assertEquals(1, results.size());
		assertTrue(addme.getName().equalsIgnoreCase("AddMeAgent"));

		// now let's remove the agent again
		try {
			newAgent.stop();
			newAgent.cleanup();
		} catch (LifecycleException e) {
			e.printStackTrace();
		}

		// last but not least check if it realy is gone
		_whitePagesTestBean.searchForAgentDesc("AddMeAgent", false);
		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		results = _whitePagesTestBean.getLastResult();

		assertNotNull(results);
		assertEquals(0, results.size());


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

		assertTrue(results.size() >= 1);
		if (results.size() >= 1){
			if (results.get(0) instanceof Action){
				Action remoteAction = (Action) results.get(0);
				_remoteActionTestBean.useRemoteAction(remoteAction, new Serializable[] {}, new Long(1000));
			}
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
			Thread.sleep(4000);
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
		_whitePagesTestBean.searchForActionDesc(action, true, new Long(2000));

		try {
			Thread.sleep(3500);
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
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (_debug) {
			System.err.println(": : Searching for RemoteAgentOnOtherNode");
		}

		_whitePagesTestBean.searchForAgentDesc("RemoteAgentOnOtherNode", true, new Long(2000));

		try {
			Thread.sleep(2500);
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
			System.err.println("--- TestGlobalRemoteActionSearchingAndHandlingWithoutCache ---");
		}

		if (_debug) {
			System.err.println(": : Searching for Action on other Node");
		}

		Action action = new Action(GlobalRemoteActionProviderBean.ACTION_GET_GLOBAL_RESULT);
		_whitePagesTestBean.searchForActionDesc(action, true, new Long(2000));

		try {
			Thread.sleep(2500);
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

	public void testGlobalAgentSearchingWithoutCache(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}

		_directoryAgentNodeBean.setCacheIsActive(false);

		if (_debug) {
			System.err.println("--- TestGlobalAgentSearchingWithoutCache ---");
		}
		try {
			Thread.sleep(7500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (_debug) {
			System.err.println(": : Searching for RemoteAgentOnOtherNode");
		}

		_whitePagesTestBean.searchForAgentDesc("RemoteAgentOnOtherNode", true, new Long(5000));

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


	public void testAgentNodeShutdown(){
		if (_deactivateTests) {
			assertTrue(true);
			return;
		}
		if (_debug) {
			System.err.println("--- TestAgentNodeShutdown ---");
		}


		_directoryAgentNodeBean.setCacheIsActive(true);

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Action action = new Action();

		_whitePagesTestBean.searchForActionDesc(action, true, new Long(2000));

		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		List<IFact> results = _whitePagesTestBean.getLastResult();

		assertEquals(3, results.size());

		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			((SimpleAgentNodeMBean)_otherNode).shutdown();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		_whitePagesTestBean.searchForActionDesc(action, true, new Long(2000));

		try {
			Thread.sleep(3500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		results = _whitePagesTestBean.getLastResult();

		assertEquals(2, results.size());

	}	

	public void testAgentNodeTimeout(){
//		if (_deactivateTests) {
//		assertTrue(true);
//		return;
//		}

		if (_debug) {
			System.err.println("--- TestAgentNodeTimeout ---");
		}

		if (_debug) {
			System.err.println("	searching for TimeoutAgent");
		}

		_whitePagesTestBean.searchForAgentDesc("TimeoutAgent", true, new Long(2000));

		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<IFact> results = _whitePagesTestBean.getLastResult();

		// at this point there shouldn't be such an agent
		assertNotNull (results);
		assertEquals(0, results.size());

		if (_debug) {
			System.err.println("	TimeoutAgent wasn't found (as expected)");
			System.err.println("	Creating TimeoutAgent and propagating it");
		}
		// now let's create it
		_timeoutSimulator.initTimeout();

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (_debug) {
			System.err.println("	searching for Timeoutagent");
		}

		_whitePagesTestBean.searchForAgentDesc("TimeoutAgent", true, new Long(2000));

		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		results = _whitePagesTestBean.getLastResult();

		// so now there it should be
		AgentDescription timeoutAgent = null;
		if (results.size() > 0){
			if (results.get(0) instanceof AgentDescription)
				timeoutAgent = (AgentDescription) results.get(0);
		}

		assertNotNull(results);
		assertEquals(1, results.size());
		assertTrue(timeoutAgent.getName().equalsIgnoreCase("TimeoutAgent"));

		// wait for agentnode timeout
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		_whitePagesTestBean.searchForAgentDesc("TimeoutAgent", true, new Long(2000));

		try {
			Thread.sleep(2500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		results = _whitePagesTestBean.getLastResult();

		// and now it's gone again
		assertNotNull (results);
		assertEquals(0, results.size());

		try {
			Thread.sleep(4500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		_lastTestDone = true;
	}
}
