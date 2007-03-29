package de.dailab.jiactng.agentcore.communication;

import de.dailab.jiactng.agentcore.lifecycle.AbstractLifecycle;
import de.dailab.jiactng.agentcore.lifecycle.LifecycleException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sercho.masp.space.TupleSpace;

/**
 * Connects the communication bean with the tuple space to get objects that are
 * to be sent away.
 *
 * @author Joachim Fuchs
 */
public class TupleSpaceConnector extends AbstractLifecycle {
    
    protected Log log = null;
    
    protected TupleSpace tupleSpace = null;

    protected PollingRunnable pollingRunnable = null;
    
    protected CommunicationBean communicationBean = null;
    
    public final static long DEFAULT_TIMEOUT = 1000;
    
    protected long timeout = DEFAULT_TIMEOUT;
    
    public void setLog(Log log) {
        
        this.log = log;
        
    }
    
    public void setTupleSpace(TupleSpace tupleSpace) {
        
        this.tupleSpace = tupleSpace;
        
    }
    
    public void setCommunicationBean(CommunicationBean communicationBean) {
        
        this.communicationBean = communicationBean;
        
    }
    
    public void doInit() throws LifecycleException {
        
        if (this.log == null) {
            
            this.log = LogFactory.getLog(getClass());
            
        }
        
        if (tupleSpace == null) {
         
            throw new LifecycleException("Connector cannot be started, there is no tuple space");
            
        }
        
    }
    
    public void doStart() throws LifecycleException {
        
        pollingRunnable = new PollingRunnable();
        new Thread(pollingRunnable, "polling runner").start();
        
    }
    
    public void doStop() throws LifecycleException {
        
        pollingRunnable.stop();
        
    }
    
    public void doCleanup() throws LifecycleException {
    }
    
    /**
     * Pass a request to the bean for sending
     */
    protected void outboundRequest(Object outboundRequest) {
        
        communicationBean.process(outboundRequest);
        
    }
    
    /**
     * Pass a response to the bean for sending
     */
    protected void outboundResponse(Object outboundResponse) {
        
        throw new UnsupportedOperationException();
        
    }
    
    /**
     * Pass a request to the bean for agent for processing
     */
    public void inboundRequest(Object inboundRequest) {
        
        tupleSpace.write(inboundRequest);
        
    }
    
    /**
     * Pass a response to the agent for processing
     */
    public void inboundResponse(Object inboundResponse) {
        
        tupleSpace.write(inboundResponse);
        
    }
    
    /**
     * Pass an exception to the agent for whatever
     */
    public void onException(Exception exception) {
        
        tupleSpace.write(exception);
        
    }
    
    /**
     * an object that is read from the space must be dispatched
     */
    private void readFromSpace(Object readObject) {
     
    }
    
    private class PollingRunnable implements Runnable {
        
        private Object template = new Object();
        
        private boolean running = true;
        
        public void stop() {
            
            running = false;
            
        }
        
        public void run() {
            
            while (running == true) {
                
                // slow it down a bit
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                readFromSpace(tupleSpace.read(template, timeout));
                
            }
            
        }
        
    }
    
}
