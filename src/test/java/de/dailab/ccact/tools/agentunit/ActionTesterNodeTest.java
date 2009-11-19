package de.dailab.ccact.tools.agentunit;

// imports
import org.junit.*;
import org.junit.Test;
import static org.junit.Assert.*;

import de.dailab.ccact.tools.agentunit.ActionTesterNode;

import java.io.Serializable;

/**
* JUnit 4 tester class for ActionTesterNode implementation. 
**/
public class ActionTesterNodeTest {
	
	// const
	static final String ACTIONTESTERNODECONFIG = "de/dailab/ccact/tools/agentunit/ActionTesterNodeTestConfig.xml";
	
	// vars
	static ActionTesterNode atn = new ActionTesterNode(ACTIONTESTERNODECONFIG, "ActionTesterNodeTestPlatform");
	
	/**
	 * Checks wether or not the <code>relativeLoadAction</code> Action is available through the <code>invoke</code> method
	 * of the ActionTesterNode.
	 *  
	 */
	@Test
	public void ActionAccess(){
		Serializable[] results = atn.invoke("relativeLoadAction",null);
		System.out.println("Test result for action access: " + results[0].toString());
	}
	
	/**
	 * Shuts down the ActionTesterNode of this test.
	 */
	@AfterClass
	public static void shutdownTest(){
		atn.stop();
	}
	
}