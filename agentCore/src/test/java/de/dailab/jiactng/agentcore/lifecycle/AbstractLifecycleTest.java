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

import java.lang.management.ManagementFactory;

import javax.management.AttributeChangeNotification;
import javax.management.AttributeChangeNotificationFilter;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import junit.framework.TestCase;

/**
 *
 * @author joachim
 */
public class AbstractLifecycleTest extends TestCase implements ILifecycleListener, NotificationListener {
    
    LifecycleEvent lastEvent = null;
    LifecycleEvent eventBeforeLastEvent = null;
    Notification lastNotification = null;
    
    AbstractLifecycle mock = null;
    
    public AbstractLifecycleTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        
        mock = new MockLifecycle();
        
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
        
        mock = new MockLifecycle();
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
        
        MockLifecycle instance = new MockLifecycle();
        
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
        
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = null;
        try {
            // register mock lifecycle
        	name = new ObjectName("test:type=MockLifecycle");
        	mbs.registerMBean(mock, name);
            
    		// add listener for change of agent's lifecycle state
    		AttributeChangeNotificationFilter acnf = new AttributeChangeNotificationFilter();
    		acnf.enableAttribute("LifecycleState");
    		mbs.addNotificationListener(name, this, acnf, null);
        } catch (Exception e) {
        	fail("Registration of lifecycle or adding notification listener failed.");
        }

        ILifecycle.LifecycleStates oldState = ILifecycle.LifecycleStates.INITIALIZING;
        ILifecycle.LifecycleStates newState = ILifecycle.LifecycleStates.INITIALIZED;

        mock.stateChanged(oldState, newState);

        assertEquals("LifecycleState", ((AttributeChangeNotification)lastNotification).getAttributeName());
        assertEquals("java.lang.String", ((AttributeChangeNotification)lastNotification).getAttributeType());
        assertEquals(newState.toString(), ((AttributeChangeNotification)lastNotification).getNewValue());
        assertEquals(oldState.toString(), ((AttributeChangeNotification)lastNotification).getOldValue());
        
        try {
        	mbs.removeNotificationListener(name, this);
        	mbs.unregisterMBean(name);
        } catch (Exception e) {
        	fail("Deregistration of lifecycle or removing notification listener failed.");
        }
    }
    
    /**
     * Test of getNotificationInfo method, of class de.dailab.jiactng.agentcore.lifecycle.MockLifecycle.
     */
    public void testGetNotificationInfo() {
        System.out.println("getNotificationInfo");
        
        MBeanNotificationInfo[] result = mock.getNotificationInfo();
        for (int i=0; i<result.length; i++) {
        	MBeanNotificationInfo info = result[i];
        	if (info.getName().equals("javax.management.AttributeChangeNotification") && 
        			info.getNotifTypes()[0].equals("jmx.attribute.change")) {
        		return;
        	}
        }
        fail("Missing attribute change notification.");
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

	@Override
	public void handleNotification(Notification notification, Object handle) {
		lastNotification = notification;
	}
    
}
