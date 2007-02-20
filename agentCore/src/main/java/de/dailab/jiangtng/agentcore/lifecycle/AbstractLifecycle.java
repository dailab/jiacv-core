package de.dailab.jiangtng.agentcore.lifecycle;

import de.dailab.jiangtng.agentcore.lifecycle.Lifecycle.LifecycleStates;

/**
 * Abstract base class for <code>Lifecycle</code> implementations.
 * <b>Not threadsafe</b>
 *
 * @author Joachim Fuchs
 */
public abstract class AbstractLifecycle implements Lifecycle {
    
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
    
    public void addLifecycleListener(LifecycleListener listener) {
        
        lifecycle.addLifecycleListener(listener);
        
    }
    
    public void removeLifecycleListener(LifecycleListener listener) {
        
        lifecycle.removeLifecycleListener(listener);
        
    }
    
    public LifecycleStates getState() {
        
        return lifecycle.getState();
        
    }
    
    /**
     * Put your initialization code here.
     */
    public abstract void doInit();
    
    /**
     * Put your start code here.
     */
    public abstract void doStart();
    
    /**
     * Put your stop code here.
     */
    public abstract void doStop();
    
    /**
     * Put your clean up code here.
     */
    public abstract void doCleanup();
    
}
