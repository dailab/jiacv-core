/**
 * 
 */
package de.dailab.jiactng.agentcore.roletest;

import java.util.List;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.dailab.jiactng.agentcore.Agent;
import de.dailab.jiactng.agentcore.IAgent;
import de.dailab.jiactng.agentcore.SimpleAgentNode;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;

/**
 * @author moekon
 * 
 */
public class AgentRoleUnitTest extends TestCase {

  private final static String CONFIG_PATH      = "de/dailab/jiactng/agentcore/agentRoleTest.xml";

  private final static String NODE_NAME        = "AgentRoleTestNode";

  private final static String FIRST_AGENT_NAME = "RoleTestAgent1";

  private final static String SECOND_AGENT_NAME = "RoleTestAgent2";
  
  private final static String THIRD_AGENT_NAME = "RoleTestAgent3";

  private SimpleAgentNode     san              = null;

  private Agent               testAgent1       = null;
  private Agent               testAgent2       = null;
  private Agent               testAgent3       = null;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    initNode();
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
    testAgent3 = (Agent) agentList.get(2);

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
    assertEquals(0,testAgent1.getRoles().size());
    assertEquals(1,testAgent2.getRoles().size());
    assertEquals(2,testAgent3.getRoles().size());

    assertEquals(1,testAgent1.getAgentBeans().size());
    assertEquals(2,testAgent2.getAgentBeans().size());
    assertEquals(3,testAgent3.getAgentBeans().size());
    
    assertEquals(12,testAgent1.getMemory().readAll(new Action()).size());
    assertEquals(13,testAgent2.getMemory().readAll(new Action()).size());
    assertEquals(14,testAgent3.getMemory().readAll(new Action()).size());
    
    
//    assertTrue(false);
  }

}
