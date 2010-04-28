package de.dailab.ccact.tools.agentunit;

// imports
import java.io.Serializable;

import org.junit.AfterClass;
import org.junit.Test;

/**
* JUnit 4 tester class for ActionTesterNode implementation. 
**/
public class ActionTesterNodeTest {
	
	// const
	static final String ACTIONTESTERNODECONFIG = "de/dailab/ccact/tools/agentunit/ActionTesterNodeTestConfig.xml";
	
	// vars
	static ActionTesterNode atn = new ActionTesterNode(ACTIONTESTERNODECONFIG, "ActionTesterNodeTestNode");
	
	/**
	 * Checks whether or not the <code>testAction</code> Action is available through the <code>invoke</code> method
	 * of the ActionTesterNode.
	 *  
	 */
	@Test
	public void ActionAccess(){
		try {
			Serializable[] results = atn.invoke("testAction",null);
			System.out.println("Test result for action access: " + results[0].toString());
		}
		catch (Exception e) {
			System.err.println("Access to test action failed: " + e.getLocalizedMessage());
			assert false;
		}
	}
	
	/**
	 * Shuts down the ActionTesterNode of this test.
	 */
	@AfterClass
	public static void shutdownTest(){
		atn.stop();
	}
	
}