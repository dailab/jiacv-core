/*
 * Created on 28.02.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.dailab.jiactng.agentcore;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class IAgentBeanTest extends TestCase {

  private IAgent     agent = null;

  private IAgentNode node  = null;

  private IAgentBean bean  = null;

  protected void setUp() throws Exception {
    super.setUp();
    ClassPathXmlApplicationContext newContext = new ClassPathXmlApplicationContext(
        "de/dailab/jiactng/agentcore/agentTests.xml");
    node = (IAgentNode) newContext.getBean("myNode");
    agent = node.findAgents().get(0);
    bean = agent.getAgentBeans().get(0);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    ((SimpleAgentNodeMBean)node).shutdown();
    //node.stop();
    //node.cleanup();
  }

  public void testSetBeanNameString() {
    assertEquals("dummyBean", bean.getBeanName());
    bean.setBeanName("helloOtherBean");
    assertEquals("helloOtherBean", bean.getBeanName());
  }

  public void testGetName() {
    assertEquals("dummyBean", bean.getBeanName());
  }

  public void testInit() {
    try {
      assertEquals(ILifecycle.LifecycleStates.STARTED, bean.getState());
      bean.stop();
      assertEquals(ILifecycle.LifecycleStates.STOPPED, bean.getState());
      bean.cleanup();
      assertEquals(ILifecycle.LifecycleStates.CLEANED_UP, bean.getState());
      bean.init();
      assertEquals(ILifecycle.LifecycleStates.INITIALIZED, bean.getState());
    } catch (Exception ex) {
      fail("Statechange exception: " + ex);
    }
  }

  public void testStart() {
    try {
      assertEquals(ILifecycle.LifecycleStates.STARTED, bean.getState());
      bean.stop();
      assertEquals(ILifecycle.LifecycleStates.STOPPED, bean.getState());
      bean.start();
      assertEquals(ILifecycle.LifecycleStates.STARTED, bean.getState());
    } catch (Exception ex) {
      fail("Statechange exception: " + ex);
    }
  }

  public void testStop() {
    try {
      assertEquals(ILifecycle.LifecycleStates.STARTED, bean.getState());
      bean.stop();
      assertEquals(ILifecycle.LifecycleStates.STOPPED, bean.getState());
    } catch (Exception ex) {
      fail("Statechange exception: " + ex);
    }
  }

  public void testCleanup() {
    try {
      assertEquals(ILifecycle.LifecycleStates.STARTED, bean.getState());
      bean.stop();
      assertEquals(ILifecycle.LifecycleStates.STOPPED, bean.getState());
      bean.cleanup();
      assertEquals(ILifecycle.LifecycleStates.CLEANED_UP, bean.getState());
    } catch (Exception ex) {
      fail("Statechange exception: " + ex);
    }
  }

  public void testGetState() {
    try {
      assertEquals(ILifecycle.LifecycleStates.STARTED, bean.getState());
    } catch (Exception ex) {
      fail("Statechange exception: " + ex);
    }
  }

  public void testExecute() {
    assertEquals("", ((DummyBean) bean).getTest());
    bean.execute();
    assertEquals("Hello World", ((DummyBean) bean).getTest());
  }

  // public void testSetThisAgent() {
  // fail("Not yet implemented");
  // }
  //
  // public void testSetMemory() {
  // fail("Not yet implemented");
  // }

  // public void testAddLifecycleListener() {
  // fail("Not yet implemented");
  // }
  //
  // public void testRemoveLifecycleListener() {
  // fail("Not yet implemented");
  // }

}
