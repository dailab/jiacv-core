/*
 * Created on 28.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;

public class IAgentTest extends TestCase {

  private IAgent     agent = null;

  private IAgentNode node  = null;

  private IAgentBean bean  = null;

  protected void setUp() throws Exception {
    super.setUp();
    ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext(
        "de/dailab/jiactng/agentcore/agentTests.xml");
    node = (IAgentNode) newContext.getBean("myPlatform");
    agent = node.findAgents().get(0);
    bean = agent.getAdaptors().get(0);
  }

  protected void tearDown() throws Exception {
    // agent.stop();
    // agent.cleanup();
    node.stop();
    node.cleanup();
  }

  public void testGetAgentName() {
    assertEquals("Agent.getName is wrong", "TestAgent", agent.getAgentName());
  }

  public void testGetAgentNode() {
    assertNotNull("Agent.getAgentNode is wrong", agent.getAgentNode());
  }

  public void testGetAdaptors() {
    assertNotNull("Agent.getAdaptors is null", agent.getAdaptors());
    assertEquals("Agent.getAdaptors().size() is not 1", 1, agent.getAdaptors()
        .size());
  }

  public void testGetThreadPool() {
    assertNotNull("Agent.getThreadPool is null", agent.getThreadPool());
  }

  public void testGetLog() {
    assertNotNull("Agent.getLog is null", agent.getLog(bean));
    Log log = agent.getLog(bean);
  }

  public void testInit() {
    try {
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());
      agent.stop();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STOPPED, agent
          .getState());
      agent.cleanup();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.CLEANED_UP,
          agent.getState());
      agent.init();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.INITIALIZED,
          agent.getState());
      agent.start();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());

    } catch (LifecycleException e) {
      fail("Statechange failed.");
    }
  }

  public void testStart() {
    try {
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());
      agent.stop();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STOPPED, agent
          .getState());
      agent.start();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());

    } catch (LifecycleException e) {
      fail("Statechange failed.");
    }
  }

  public void testStop() {
    try {
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());
      agent.stop();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STOPPED, agent
          .getState());
      agent.start();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());
    } catch (LifecycleException e) {
      fail("Statechange failed.");
    }
  }

  public void testCleanup() {
    try {
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());
      agent.stop();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STOPPED, agent
          .getState());
      agent.cleanup();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.CLEANED_UP,
          agent.getState());
      agent.init();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.INITIALIZED,
          agent.getState());
      agent.start();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());      
    } catch (LifecycleException e) {
      fail("Statechange failed.");
    }
  }

  public void testGetState() {
    try {
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.STARTED, agent
          .getState());
      agent.cleanup();
      assertEquals("Wrong state: ", ILifecycle.LifecycleStates.CLEANED_UP,
          agent.getState());
    } catch (LifecycleException e) {
      fail("Statechange failed.");
    }
  }

  // public void testGetMemory() {
  // assertNotNull("Agent.getMemory is null", agent.getMemory());
  // }

  public void testRun() {
    // try {
    // //((DummyBean) bean).setMode(DummyBean.Modes.Hello);
    // ((DummyBean) agent.getAdaptors().get(0)).setMode(DummyBean.Modes.Hello);
    // // agent.run();
    // // fail("failed: " + agent.getState());
    // // fail("failed: " + ((DummyBean) bean).getTest());
    //      
    // // fail("failed: " + ((DummyBean) bean).getTest());
    // // fail("failed: " + ((DummyBean) bean).getMode());
    // // fail("agentState:
    // // "+node.getState()+":"+agent.getState()+":"+bean.getState());
    // // ((DummyBean) bean).setMode(DummyBean.Modes.Endless);
    // // node.start();
    // } catch (Exception ex) {
    // fail("Exception got through. " + ex.getMessage());
    // }
    // removed because of thread-testing problems with junit
  }

}
