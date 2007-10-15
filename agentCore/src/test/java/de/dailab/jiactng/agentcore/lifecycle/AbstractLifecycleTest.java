/*
 * MockLifecycleTest.java
 * JUnit based test
 *
 * Created on 8. Maerz 2007, 14:10
 */

package de.dailab.jiactng.agentcore.lifecycle;

import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.CLEANED_UP;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.INITIALIZED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.INITIALIZING;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STARTED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STOPPED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.UNDEFINED;

import javax.management.MBeanNotificationInfo;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
/**
 *
 * @author joachim
 */
public class AbstractLifecycleTest extends TestCase implements ILifecycleListener {
    
    LifecycleEvent lastEvent = null;
    LifecycleEvent eventBeforeLastEvent = null;
    
    AbstractLifecycle mock = null;
    
    public AbstractLifecycleTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        
        mock = new MockLifecycle(true);
        
    }
    
    protected void tearDown() throws Exception {
        
        mock = null;
        
    }
    
    /**
     * Test of init method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testInit() throws Exception {
        System.out.println("init");
        
        MockLifecycle instance = new MockLifecycle();
        
        instance.init();
        
    }
    
    /**
     * Test of start method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testStart() throws Exception {
        System.out.println("start");
        
        MockLifecycle instance = new MockLifecycle();
        
        instance.start();
        
    }
    
    /**
     * Test of stop method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testStop() throws Exception {
        System.out.println("stop");
        
        MockLifecycle instance = new MockLifecycle();
        
        instance.stop();
        
    }
    
    /**
     * Test of cleanup method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testCleanup() throws Exception {
        System.out.println("cleanup");
        
        MockLifecycle instance = new MockLifecycle();
        
        instance.cleanup();
        
    }
    
    /**
     * Test of addLifecycleListener method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testAddLifecycleListener() throws LifecycleException {
        System.out.println("addLifecycleListener");
        
        MockLifecycle instance = new MockLifecycle();
        
        lastEvent = null;
        eventBeforeLastEvent = null;
        
        instance.addLifecycleListener(this);
        
        instance.init();
        
        assertEquals(instance, lastEvent.getSource());
        assertEquals(instance, eventBeforeLastEvent.getSource());
        assertEquals(INITIALIZED, lastEvent.getState());
        assertEquals(INITIALIZING, eventBeforeLastEvent.getState());
        
    }
    
    /**
     * Test of removeLifecycleListener method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testRemoveLifecycleListener() throws LifecycleException {
        System.out.println("removeLifecycleListener");
        
        MockLifecycle instance = new MockLifecycle();
        
        lastEvent = null;
        eventBeforeLastEvent = null;
        
        instance.addLifecycleListener(this);
        instance.removeLifecycleListener(this);
        instance.init();

        assertEquals(lastEvent, null);
        assertEquals(eventBeforeLastEvent, null);
        
    }
    
    /**
     * Test of getState method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testGetState() throws LifecycleException {
        
        mock = new MockLifecycle(true);
        mock.addLifecycleListener(this);
        
        assertEquals(UNDEFINED, mock.getState());
        mock.init();
        assertEquals(INITIALIZED, mock.getState());
        mock.cleanup();
        assertEquals(CLEANED_UP, mock.getState());
        try {
            mock.start();
        } catch (IllegalStateException expected) {
            assertEquals(IllegalStateException.class, expected.getClass());
        }
        mock.init();
        mock.start();
        assertEquals(STARTED, mock.getState());
        mock.stop();
        assertEquals(STOPPED, mock.getState());
        mock.cleanup();
        assertEquals(CLEANED_UP, mock.getState());
        
    }
    
    /**
     * Test of getLifecycleState method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testGetLifecycleState() throws LifecycleException {
        System.out.println("getLifecycleState");
        
        MockLifecycle instance = new MockLifecycle(true);
        
        assertEquals("UNDEFINED", instance.getLifecycleState());
        instance.init();
        assertEquals("INITIALIZED", instance.getLifecycleState());
        instance.cleanup();
        assertEquals("CLEANED_UP", instance.getLifecycleState());
        instance.init();
        assertEquals("INITIALIZED", instance.getLifecycleState());
        instance.start();
        assertEquals("STARTED", instance.getLifecycleState());
        instance.stop();
        assertEquals("STOPPED", instance.getLifecycleState());
        instance.cleanup();
        assertEquals("CLEANED_UP", instance.getLifecycleState());
        
    }
    
    /**
     * Test of stateChanged method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testStateChanged() {
        System.out.println("stateChanged");
        
        ILifecycle.LifecycleStates oldState = null;
        ILifecycle.LifecycleStates newState = null;
        MockLifecycle instance = new MockLifecycle();
        
        instance.stateChanged(oldState, newState);
        
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
    /**
     * Test of getNotificationInfo method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testGetNotificationInfo() {
        System.out.println("getNotificationInfo");
        
        MockLifecycle instance = new MockLifecycle();
        
        MBeanNotificationInfo[] expResult = null;
        MBeanNotificationInfo[] result = instance.getNotificationInfo();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
    /**
     * Test of setApplicationContext method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testSetApplicationContext() {
        System.out.println("setApplicationContext");
        
        ApplicationContext applicationContext = null;
        MockLifecycle instance = new MockLifecycle();
        
        // TODO check what this shalls! Das Ding ist keine Bean!
        //instance.setApplicationContext(applicationContext);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    /**
     * Test of doInit method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testDoInit() throws Exception {
        System.out.println("doInit");
        
        MockLifecycle instance = new MockLifecycle();
        instance.doInit();
        
    }
    
    /**
     * Test of doStart method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testDoStart() throws Exception {
        System.out.println("doStart");
        
        MockLifecycle instance = new MockLifecycle();
        instance.doStart();
        
    }
    
    /**
     * Test of doStop method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testDoStop() throws Exception {
        System.out.println("doStop");
        
        MockLifecycle instance = new MockLifecycle();
        instance.doStop();
        
    }
    
    /**
     * Test of doCleanup method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testDoCleanup() throws Exception {
        System.out.println("doCleanup");
        
        MockLifecycle instance = new MockLifecycle();
        instance.doCleanup();
        
    }
    
    public void onEvent(LifecycleEvent evt) {
        
        eventBeforeLastEvent = lastEvent;
        lastEvent = evt;
        
    }
    
}
