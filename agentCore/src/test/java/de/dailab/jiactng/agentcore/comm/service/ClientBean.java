/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.service;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ClientBean extends AbstractAgentBean implements ResultReceiver {
    private static class ResultObject {
        ActionResult actionResult= null;
    }
    
    private final static String LOG_ACTION= "log";
    private final static String TIME_ACTION= "getCurrentTime";
    
    private final static int MAX_TIMEOUT= 10000;
    
    private final ResultObject _timeLock= new ResultObject();
    private final ResultObject _logLock= new ResultObject();
    
    public void receiveResult(ActionResult result) {
        Action action= result.getAction();
        log.debug("received result for '" + action + "'");
        if(action.getName().equals(TIME_ACTION)) {
            synchronized (_timeLock) {
                _timeLock.actionResult= result;
                _timeLock.notify();
            }
        } else if (action.getName().equals(LOG_ACTION)){
            synchronized(_logLock) {
                _logLock.actionResult= result;
                _logLock.notify();
            }
        }
    }
    
    public Object printHelloWorld() {
        log.debug("use remote action to print hello world");
        synchronized (_logLock) {
            
            Action action= memory.read(new Action(LOG_ACTION));
            if(action == null) {
                return "action '" + LOG_ACTION + "' not found";
            }
            
            DoAction doAction= action.createDoAction(new Object[]{"Hallo Welt"}, this);
            String check;
            if((check= doAction.typeCheck()) != null) {
                throw new IllegalStateException("something wrong here [" + check + "]");
            }
            
            memory.write(action.createDoAction(new Object[]{"Hallo Welt"}, this));
            
            try {
                _logLock.wait(MAX_TIMEOUT);
                // if we are here, then everything was successful
            } catch (Exception e) {
                return e;
            }
        }
        
        return _logLock.actionResult == null ? "timed out" : null;
    }
    
    public Object printTimes() {
        log.debug("use remote action to calculate delays");
        synchronized (_timeLock) {
            long start= System.currentTimeMillis();
            
            Action action= memory.read(new Action(TIME_ACTION));
            if(action == null) {
                return "action '" + TIME_ACTION + "' not found";
            }
            
            memory.write(action.createDoAction(new Object[0], this));
            
            try {
                _timeLock.wait(MAX_TIMEOUT);
                
                if(_timeLock.actionResult == null) {
                    return "timed out";
                }
                
                long measuredTime= ((Long)_timeLock.actionResult.getResults()[0]).longValue();
                
                long end= System.currentTimeMillis();
                log.debug("start time: " + start);
                log.debug("process time: " + measuredTime);
                log.debug("end time: " + end);
                log.debug("delays: " + (measuredTime - start) + " -> " + (end - measuredTime));
            } catch (Exception e) {
                return e;
            }
        }
        
        return null;
    }
}
