/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.management.jmx;

import de.dailab.jiactng.agentcore.lifecycle.ILifecycle;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycleListener;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleEvent;
import de.dailab.jiactng.agentcore.lifecycle.ILifecycle.LifecycleStates;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
class StartListener implements ILifecycleListener {
    private final ILifecycle _source;
    private final Object _mutex;
    
    StartListener(ILifecycle source) {
        _source= source;
        _mutex= new Object();
    }
    
    public void onEvent(LifecycleEvent evt) {
        if(evt.getSource() != _source) {
            return;
        }
        
        synchronized (_mutex) {
            _mutex.notify();
        }
    }
    
    public boolean ensureStarted(long timeout) {
        final long endTime= System.currentTimeMillis() + timeout;
        synchronized (_mutex) {
            while(true) {
                if(_source.getState() == LifecycleStates.STARTED) {
                    return true;
                }
                
                long wt= endTime - System.currentTimeMillis();
                
                if(wt <= 0) {
                    break;
                }
                
                try {
                    _mutex.wait(wt);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        
        return false;
    }
}
