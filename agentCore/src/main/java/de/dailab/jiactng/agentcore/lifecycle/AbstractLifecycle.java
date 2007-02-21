package de.dailab.jiactng.agentcore.lifecycle;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;

/**
 * Abstract base class for <code>ILifecycle</code> implementations.
 * <b>Not threadsafe</b>
 *
 * @author Joachim Fuchs
 */
public abstract class AbstractLifecycle implements ILifecycle {
    
    protected DefaultLifecycleHandler lifecycle = new DefaultLifecycleHandler(this);
    
    public void init() throws LifecycleException {
        
        lifecycle.beforeInit();
        
        try {
            
            doInit();
            
        } catch (Throwable t) {
            
            throw new LifecycleException("Failed to initialize", t);
            
        }
        
        lifecycle.afterInit();
        
    }
    
    public void start() throws LifecycleException {
        
        lifecycle.beforeStart();
        
        try {
            
            doStart();
            
        } catch (Throwable t) {
            
            throw new LifecycleException("Failed to start", t);
            
        }
        
        lifecycle.afterStart();
        
    }
    
    public void stop() throws LifecycleException {
        
        lifecycle.beforeStop();
        
        try {
            
            doStop();
            
        } catch (Throwable t) {
            
            throw new LifecycleException("Failed to stop", t);
            
        }
        
        lifecycle.afterStop();
        
    }
    
    public void cleanup() throws LifecycleException {
        
        lifecycle.beforeCleanup();
        
        try {
            
            doCleanup();
            
        } catch (Throwable t) {
            
            throw new LifecycleException("Failed to clean up", t);
            
        }
        
        lifecycle.afterCleanup();
        
    }
    
    public void addLifecycleListener(ILifecycleListener listener) {
        
        lifecycle.addLifecycleListener(listener);
        
    }
    
    public void removeLifecycleListener(ILifecycleListener listener) {
        
        lifecycle.removeLifecycleListener(listener);
        
    }
    
    public LifecycleStates getState() {
        
        return lifecycle.getState();
        
    }
    
    /**
     * Put your initialization code here.
     */
    public abstract void doInit() throws LifecycleException ;
    
    /**
     * Put your start code here.
     */
    public abstract void doStart() throws LifecycleException ;
    
    /**
     * Put your stop code here.
     */
    public abstract void doStop() throws LifecycleException ;
    
    /**
     * Put your clean up code here.
     */
    public abstract void doCleanup() throws LifecycleException ;
    
}
