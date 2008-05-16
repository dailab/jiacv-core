/*
 * DefaultLifecycleHandlerTest.java
 * JUnit based test
 *
 * Created on 7. Maerz 2007, 15:43
 */

package de.dailab.jiactng.agentcore.lifecycle;

import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.CLEANED_UP;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.CLEANING_UP;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.INITIALIZED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.INITIALIZING;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STARTED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STARTING;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STOPPED;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.STOPPING;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.UNDEFINED;
import junit.framework.TestCase;

/**
 *
 * @author Joachim Fuchs
 */
public class DefaultLifecycleHandlerTest extends TestCase implements ILifecycle, ILifecycleListener {
    
    DefaultLifecycleHandler instance = null;
    
    LifecycleEvent lastEvent = null;
    LifecycleEvent eventBeforeLastEvent = null;
    
    public DefaultLifecycleHandlerTest(String testName) {
        super(testName);
    }
    
    protected void tearDown() throws Exception {
        
        instance = null;
        
    }
    
    public void testLoose() throws LifecycleException {

        instance = new DefaultLifecycleHandler(this);
        instance.addLifecycleListener(this);

        init();
        start();
        stop();
        cleanup();
        
        boolean exception = false;
        
        try{
        cleanup();
        start();
        init();
        stop();
        stop();
        cleanup();
        start();
        init();
        } catch(IllegalStateException ise) {
        	exception = true;
        }

        assertTrue("Expected exception from lifecycle change",exception);
        
    }
    
    public void testStrict() throws LifecycleException {
        
        instance = new DefaultLifecycleHandler(this);
        
        try {
            
            start();
            
        } catch (IllegalStateException ise) {
            
            fail(ise.toString());
            
        }
        
        try {
            
            stop();
            
        } catch (IllegalStateException ise) {
            
            fail(ise.toString());
            
        }
        
        try {
            
            cleanup();
            
        } catch (IllegalStateException ise) {
            
            fail(ise.toString());
            
        }
        
        ////////////////////////////////////////////////////////////////////////
        
        init();
        
        try {
            
            init();
            fail("instance already initialized!");
            
        } catch (IllegalStateException expected) {
            
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
        try {
            
            stop();
            fail("instance never started!");
            
        } catch (IllegalStateException expected) {
            
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
        try {
            
            cleanup();
                        
        } catch (IllegalStateException ise) {
            
            fail(ise.toString());
            
        }

        init();
        
        try {
            
            start();
            
        } catch (IllegalStateException ise) {
            
            fail(ise.toString());
            
        }
        
        try {
            
            cleanup();
            
        } catch (IllegalStateException ise) {
         
            fail(ise.toString());
            
        }
        
        try {
            
            stop();
            fail("instance never started!");
            
        } catch (IllegalStateException expected) {
            
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
        try {
            
            start();
            
        } catch (IllegalStateException ise) {
            
            fail(ise.toString());
            
        }
        
        try {
            
            init();
            
        } catch (IllegalStateException expected) {
         
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
        try {
            
            cleanup();
            
        } catch (IllegalStateException ise) {
         
            fail(ise.toString());
            
        }
        
        try {
            
            start();
            
        } catch (IllegalStateException ise) {
         
            fail(ise.toString());
            
        }
        
    }
    
    /**
     * getState()
     *
     */
    public void testGetState() throws LifecycleException {
        
        instance = new DefaultLifecycleHandler(this);
        this.addLifecycleListener(this);
        
        assertEquals(instance.getState(), this.getState());
        assertEquals(instance.getState(), UNDEFINED);
        
        init();
        
        assertEquals(instance.getState(), this.getState());
        assertEquals(INITIALIZED, instance.getState());
        
        start();
        
        assertEquals(instance.getState(), this.getState());
        assertEquals(STARTED, instance.getState());
        
        stop();
        
        assertEquals(instance.getState(), this.getState());
        assertEquals(STOPPED, instance.getState());
        
        cleanup();
        
        assertEquals(instance.getState(), this.getState());
        assertEquals(CLEANED_UP, instance.getState());
        
    }
    
    /**
     * Correct and ordered event propagation
     *
     */
    public void testEvents() throws LifecycleException {
        
        instance = new DefaultLifecycleHandler(this);
        this.addLifecycleListener(this);
        
        init();
        
        assertEquals(INITIALIZING, eventBeforeLastEvent.getState());
        assertEquals(INITIALIZED, lastEvent.getState());
        
        start();
        
        assertEquals(STARTING, eventBeforeLastEvent.getState());
        assertEquals(STARTED, lastEvent.getState());
        
        stop();
        
        assertEquals(STOPPING, eventBeforeLastEvent.getState());
        assertEquals(STOPPED, lastEvent.getState());
        
        cleanup();
        
        assertEquals(CLEANING_UP, eventBeforeLastEvent.getState());
        assertEquals(CLEANED_UP, lastEvent.getState());
        
    }
    
    /**
     * Make sure the created listener is not accepted when added to its creator
     *
     */
    public void testChildListener() {
        
        instance = new DefaultLifecycleHandler(this);
        ILifecycleListener child = instance.createLifecycleListener();
        
        try {
            
            instance.addLifecycleListener(child);
            fail("Incest!");
            
        } catch (Exception e) {
            
            assertEquals(e.getClass(), IllegalArgumentException.class);
            
        }
        
    }
    
    
    // ---------------------------------- LIFECYCLE METHODS --------------------
    
    public void start() throws LifecycleException {
        
        instance.beforeStart();
        instance.afterStart(true);
        
    }
    
    public void stop() throws LifecycleException {
        
        instance.beforeStop();
        instance.afterStop();
        
    }
    
    public void init() throws LifecycleException {
        
        instance.beforeInit();
        instance.afterInit(true);
        
    }
    
    public void cleanup() throws LifecycleException {
        
        instance.beforeCleanup();
        instance.afterCleanup();
        
    }
    
    public void addLifecycleListener(ILifecycleListener listener) {
        instance.addLifecycleListener(listener);
    }
    
    public void removeLifecycleListener(ILifecycleListener listener) {
        instance.removeLifecycleListener(listener);
    }
    
    public ILifecycle.LifecycleStates getState() {
        
        return instance.getState();
        
    }
    
    //
    public void stateChanged(ILifecycle.LifecycleStates oldState, ILifecycle.LifecycleStates newState) {
    }
    
    public void onEvent(LifecycleEvent evt) {
        
        eventBeforeLastEvent = lastEvent;
        lastEvent = evt;
        
    }
    
}
