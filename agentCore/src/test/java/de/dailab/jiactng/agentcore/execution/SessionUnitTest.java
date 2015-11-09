/**
 * 
 */
package de.dailab.jiactng.agentcore.execution;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.Session;
import de.dailab.jiactng.agentcore.action.SessionEvent;
import de.dailab.jiactng.agentcore.knowledge.IMemory;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;

/**
 * @author moekon
 * 
 */
public class SessionUnitTest extends TestCase {

  private final static String CONFIG_PATH      = "de/dailab/jiactng/agentcore/execution/SessionTest.xml";

  private final static String NODE_NAME        = "SessionTestNode";

  private final static String FIRST_AGENT_NAME = "SessionTestAgent1";

  private final static String OTHER_AGENT_NAME = "SessionTestAgent2";

  private SimpleAgentNode     san              = null;

  private Agent               testAgent1       = null;
  private Agent               testAgent2       = null;

  SessionTestBean             testBean1        = null;
  SessionTestBean             testBean2        = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    initNode();
  }
  
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		san.shutdown();
	}

  protected void initNode() {

    System.setProperty("log4j.configuration", "jiactng_log4j.properties");
    System.setProperty("spring.rootconfigfile", CONFIG_PATH);
    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_PATH);

    Object node = context.getBean(NODE_NAME);
    san = (SimpleAgentNode) node;

    List<IAgent> agentList = san.findAgents();
    testAgent1 = (Agent) agentList.get(0);
    testAgent2 = (Agent) agentList.get(1);

    testBean1 = extractSessionTestBean(testAgent1.getAgentBeans());
    testBean2 = extractSessionTestBean(testAgent2.getAgentBeans());

    while (!LifecycleStates.STARTED.equals(san.getState())) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  public void testSimpleStartup() {

    assertEquals(FIRST_AGENT_NAME, testAgent1.getAgentName());
    assertEquals(OTHER_AGENT_NAME, testAgent2.getAgentName());

    assertNotNull("Could not find SessionTestBean1", testBean1);
    assertNotNull("Could not find SessionTestBean2", testBean2);

    checkMemoryEmpty(testAgent1.getMemory());
    checkMemoryEmpty(testAgent2.getMemory());

  }

  public void testSimpleUsage() {
    testBean1.setWaitWithActionA(false);
    testBean1.setWaitWithActionB(false);
    testBean1.setWaitWithActionC(false);
    testBean2.setWaitWithActionA(false);
    testBean2.setWaitWithActionB(false);
    testBean2.setWaitWithActionC(false);

    testBean1.startAction(SessionTestBean.ACTION_NAME_A, "TestStringSimple", 2000);

    try {
      Thread.sleep(4000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("TestStringSimple+A+B+C", testBean1.getFinalString());
    checkMemoryEmpty(testAgent1.getMemory());
    checkMemoryEmpty(testAgent2.getMemory());
  }

  public void testMassUsage() {
    testBean1.setWaitWithActionA(false);
    testBean1.setWaitWithActionB(false);
    testBean1.setWaitWithActionC(false);
    testBean2.setWaitWithActionA(false);
    testBean2.setWaitWithActionB(false);
    testBean2.setWaitWithActionC(false);

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < 1000; i++) {
      testBean2.setFinalString(null);
      testBean2.startAction(SessionTestBean.ACTION_NAME_C, "TestStringSimple", 10000);

      while (!"TestStringSimple+C".equals(testBean2.getFinalString())) {
        try {
          Thread.sleep(0, 5000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    long endTime = System.currentTimeMillis();

    System.err.println("\n\n\tTIME FOR PERFORMANCE: " + (endTime - startTime) + "\n\n");

    assertEquals("TestStringSimple+C", testBean2.getFinalString());
    checkMemoryEmpty(testAgent1.getMemory());
    checkMemoryEmpty(testAgent2.getMemory());
  }

  public void testLocalActionTimeout() {
    testBean1.setWaitWithActionA(false);
    testBean1.setWaitWithActionB(true);
    testBean1.setWaitWithActionC(false);
    testBean2.setWaitWithActionA(false);
    testBean2.setWaitWithActionB(true);
    testBean2.setWaitWithActionC(false);

    testBean1.startAction(SessionTestBean.ACTION_NAME_A, "TestStringBWait", 800);

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("Action canceled", testBean1.getFinalString());
    checkMemoryEmpty(testAgent1.getMemory());
    checkMemoryEmpty(testAgent2.getMemory());
  }

  public void testRemoteActionTimeout() {
    testBean1.setWaitWithActionA(false);
    testBean1.setWaitWithActionB(false);
    testBean1.setWaitWithActionC(true);
    testBean2.setWaitWithActionA(false);
    testBean2.setWaitWithActionB(false);
    testBean2.setWaitWithActionC(true);

    testBean1.startAction(SessionTestBean.ACTION_NAME_A, "TestStringCWait", 800);

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("Action canceled", testBean1.getFinalString());
    checkMemoryEmpty(testAgent1.getMemory());
    checkMemoryEmpty(testAgent2.getMemory());
  }

  private SessionTestBean extractSessionTestBean(List<IAgentBean> iabList) {
    SessionTestBean ret = null;

    for (IAgentBean iab : iabList) {
      if (iab instanceof SessionTestBean) {
        ret = (SessionTestBean) iab;
      }
    }

    return ret;
  }

  private void checkMemoryEmpty(IMemory agentMemory) {
    Session sessionTemplate = new Session();
    Set<Session> memorySessions = agentMemory.readAll(sessionTemplate);
    assertTrue("Memory contains unexpected Session", memorySessions.size() == 0);

    DoAction doActTemplate = new DoAction(null, null, null, null);
    Set<DoAction> memoryDoActs = agentMemory.readAll(doActTemplate);
    assertTrue("Memory contains unexpected DoAction", memoryDoActs.size() == 0);

    ActionResult actResultTemplate = new ActionResult(null, null);
    Set<ActionResult> memoryResults = agentMemory.readAll(actResultTemplate);
    assertTrue("Memory contains unexpected ActionResult", memoryResults.size() == 0);

    SessionEvent eventTemplate = new SessionEvent(null);
    Set<SessionEvent> memoryEvents = agentMemory.readAll(eventTemplate);
    assertTrue("Memory contains unexpected SessionEvent", memoryEvents.size() == 0);
  }

}
