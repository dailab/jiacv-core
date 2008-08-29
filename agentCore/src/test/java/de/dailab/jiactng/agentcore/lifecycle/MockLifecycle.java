package de.dailab.jiactng.agentcore.lifecycle;

import de.dailab.jiactng.agentcore.management.Manager;

/**
 * @author Joachim Fuchs
 */
public class MockLifecycle extends AbstractLifecycle {
    
    /**
     * Creates a new instance of MockLifecycle
     *
     */
    public MockLifecycle() {
        
        super();
        
    }
    
//    public MockLifecycle(boolean strict) {
//        
//        super(strict);
//        
//    }

    public void doInit() throws LifecycleException {
    }

    public void doStart() throws LifecycleException {
    }

    public void doStop() throws LifecycleException {
    }

    public void doCleanup() throws LifecycleException {
    }
    
	public void enableManagement(Manager manager) {
	}
	  
	public void disableManagement() {
	}
}
