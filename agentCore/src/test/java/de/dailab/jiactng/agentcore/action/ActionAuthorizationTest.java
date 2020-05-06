package de.dailab.jiactng.agentcore.action;

import java.util.List;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.JIACTestForJUnit3;
import de.dailab.jiactng.agentcore.IAgentNode;
import de.dailab.jiactng.agentcore.SimpleAgentNodeMBean;

/**
 * @author Jan Keiser
 * @version $Revision$
 */
public class ActionAuthorizationTest extends JIACTestForJUnit3 {

    private IAgentNode _node;
    private AuthorizedActionCallerBean _testBean;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext("de/dailab/jiactng/agentcore/action/authorizationTestNode.xml");
        _node = (IAgentNode) newContext.getBean("myNode");
        _testBean = (AuthorizedActionCallerBean) newContext.getBean("userBean");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ((SimpleAgentNodeMBean)_node).shutdown();
    }

    public void testAuthorization() {
    	final long timeout = 10000;
    	final int numberRequests = 10;
	    final ActionResultsListener listener = new ActionResultsListener(numberRequests);
	    
	    // invoke actions in parallel
    	final String sessionExample1User1 = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE1, "user1", listener);
    	final String sessionExample2User1 = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE2, "user1", listener);
    	final String sessionExample3User1 = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE3, "user1", listener);
    	final String sessionExample1User2 = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE1, "user2", listener);
    	final String sessionExample2User2 = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE2, "user2", listener);
    	final String sessionExample3User2 = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE3, "user2", listener);
    	final String sessionExample1Anonym = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE1, "", listener);
    	final String sessionExample2UnknownUser = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE2, null, listener);
    	final String sessionExample3Anonym = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE3, "", listener);
    	final String sessionExample3UnknownUser = _testBean.invoke(AuthorizedActionBean.ACTION_EXAMPLE3, null, listener);

    	// wait for results
	    synchronized (listener) {
	      if (!listener.isFinished()) {
	        try {
	          listener.wait(timeout);
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	      }
	    }

	    // check results
	    final List<ActionResult> results = listener.getResults();
	    if (!listener.isFinished()) {
	    	fail("Only " + results.size() + " of " + numberRequests + " action requests return within " + timeout + " milliseconds");
	    }
	    for (ActionResult result : results) {
	    	final String sessionId = result.getSessionId();
	    	if (sessionId.equals(sessionExample1User1)) {
	    		assertEquals("Got wrong user as result of example1", "user1", (String)result.getResults()[0]);
	    	}
	    	else if (sessionId.equals(sessionExample2User1)) {
	    		assertEquals("Got wrong failure of example2 for user1", "Not authorized", (String)result.getFailure());	
	    	}
	    	else if (sessionId.equals(sessionExample3User1)) {
	    		assertEquals("Got wrong user as result of example3", "", (String)result.getResults()[0]);
	    	}
	    	else if (sessionId.equals(sessionExample1User2)) {
	    		assertEquals("Got wrong failure of example1 for user2", "Not authorized", (String)result.getFailure());	
	    	}
	    	else if (sessionId.equals(sessionExample2User2)) {
	    		assertEquals("Got wrong user as result of example2", "user2", (String)result.getResults()[0]);
	    	}
	    	else if (sessionId.equals(sessionExample3User2)) {
	    		assertEquals("Got wrong user as result of example3", "", (String)result.getResults()[0]);
	    	}
	    	else if (sessionId.equals(sessionExample1Anonym)) {
	    		assertEquals("Got wrong failure of example1 for anonym user", "Unknown user token", (String)result.getFailure());	
	    	}
	    	else if (sessionId.equals(sessionExample2UnknownUser)) {
	    		assertEquals("Got wrong failure of example2 for unknown user", "Not authorized", (String)result.getFailure());	
	    	}
	    	else if (sessionId.equals(sessionExample3Anonym)) {
	    		assertEquals("Got wrong failure of example3 for anonym user", "Unknown user token", (String)result.getFailure());
	    	}
	    	else if (sessionId.equals(sessionExample3UnknownUser)) {
	    		assertEquals("Got wrong user as result of example3", "", (String)result.getResults()[0]);
	    	}
	    }
    }

}
