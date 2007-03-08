/*
 * DefaultLifecycleHandlerTest.java
 * JUnit based test
 *
 * Created on 7. M�rz 2007, 15:43
 */

package de.dailab.jiactng.agentcore.lifecycle;

import junit.framework.*;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;
import static de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates.*;

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

        instance = new DefaultLifecycleHandler(this, false);
        instance.addLifecycleListener(this);

        init();
        start();
        stop();
        cleanup();
        
        cleanup();
        start();
        init();
        stop();
        stop();
        cleanup();
        start();
        init();

    }
    
    public void testStrict() throws LifecycleException {
        
        instance = new DefaultLifecycleHandler(this, true);
        
        try {
            
            start();
            fail("not initialized");
            
        } catch (IllegalStateException expected) {
            
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
        try {
            
            stop();
            fail("not initialized");
            
        } catch (IllegalStateException expected) {
            
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
        try {
            
            cleanup();
            fail("not initialized");
            
        } catch (IllegalStateException expected) {
            
            assertEquals(IllegalStateException.class, expected.getClass());
            
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
            
        } catch (IllegalStateException expected) {
         
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
        try {
            
            stop();
            
        } catch (IllegalStateException ise) {
            
            fail(ise.toString());
            
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
            
        } catch (IllegalStateException expected) {
         
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
        try {
            
            start();
            
        } catch (IllegalStateException expected) {
         
            assertEquals(IllegalStateException.class, expected.getClass());
            
        }
        
    }
    
    /**
     * getState()
     *
     */
    public void testGetState() throws LifecycleException {
        
        instance = new DefaultLifecycleHandler(this, true);
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
        
        instance = new DefaultLifecycleHandler(this, true);
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
        instance.afterStart();
        
    }
    
    public void stop() throws LifecycleException {
        
        instance.beforeStop();
        instance.afterStop();
        
    }
    
    public void init() throws LifecycleException {
        
        instance.beforeInit();
        instance.afterInit();
        
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
        
        System.out.println("*** " + evt.getSource() + " > " + evt.getState());
        eventBeforeLastEvent = lastEvent;
        lastEvent = evt;
        
    }
    
}
