/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ClientBean extends AbstractAgentBean implements ResultReceiver {
    private final static String LOG_ACTION= "de.dailab.jiactng.agentcore.comm.service.OpenBean#log";
    private final static String TIME_ACTION= "de.dailab.jiactng.agentcore.comm.service.OpenBean#getCurrentTime";
    
    private final Object _timeLock= new Object();
    private final Object _logLock= new Object();
    
    private long _measuredTime;
    
    public void receiveResult(ActionResult result) {
        Action action= result.getAction();
        log.debug("received result for '" + action + "'");
        if(action.getName().equals(TIME_ACTION)) {
            synchronized (_timeLock) {
                _measuredTime= ((Long)result.getResults()[0]).longValue();
                _timeLock.notify();
            }
        } else if (action.getName().equals(LOG_ACTION)){
            synchronized(_logLock) {
                _logLock.notify();
            }
        }
    }
    
    public Object printHelloWorld() {
        synchronized (_logLock) {
            Action action= memory.read(new Action(LOG_ACTION, null, null, null));
            if(action == null) {
                return "action '" + LOG_ACTION + "' not found";
            }
            
            memory.write(action.createDoAction(new Object[]{"Hallo Welt"}, this));
            
            try {
                _logLock.wait();
                // if we are here, then everything was successful
            } catch (Exception e) {
                return e;
            }
        }
        
        return null;
    }
    
    public Object printTimes() {
        synchronized (_timeLock) {
            long start= System.currentTimeMillis();
            
            Action action= memory.read(new Action(TIME_ACTION, null, null, null));
            if(action == null) {
                return "action '" + TIME_ACTION + "' not found";
            }
            
            memory.write(action.createDoAction(new Object[0], this));
            
            try {
                _timeLock.wait();
                long end= System.currentTimeMillis();
                log.debug("start time: " + start);
                log.debug("process time: " + _measuredTime);
                log.debug("end time: " + end);
                log.debug("delays: " + (_measuredTime - start) + " -> " + (end - _measuredTime));
            } catch (Exception e) {
                return e;
            }
        }
        
        return null;
    }
}
