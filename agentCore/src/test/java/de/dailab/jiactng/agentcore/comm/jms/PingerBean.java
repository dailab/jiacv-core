/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import java.io.Serializable;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class PingerBean extends AbstractAgentBean implements ResultReceiver {
    
    private IJiacMessage pongMessage= null;
    
    private class MessageObserver implements SpaceObserver<IFact> {
		private static final long serialVersionUID = 6443856144893774561L;

		@SuppressWarnings("rawtypes")
        public void notify(SpaceEvent<? extends IFact> event) {
            if(event instanceof WriteCallEvent) {
                WriteCallEvent callEvent= (WriteCallEvent) event;
                Object obj= callEvent.getObject();
                
                if(!IJiacMessage.class.isInstance(obj)) {
                    return;
                }
                synchronized(PingerBean.this) {
                    pongMessage= (IJiacMessage) obj;
                    PingerBean.this.notify();
                }
            }
        }
    }
    
    @Override
    public void doInit() throws Exception {
        super.doInit();
        memory.attach(new MessageObserver(), new JiacMessage());
    }
    
    public void startPingProcess() {
        log.debug("start the ping process...");
        
        Action action= memory.read(new Action(PingPongTestCase.ACTION_NAME));
        IJiacMessage  message= new JiacMessage(new ObjectContent("Ping"));
            DoAction doAction= action.createDoAction(new Serializable[]{
                message,
                CommunicationAddressFactory.createGroupAddress(PingPongTestCase.DUMMY_ADDRESS)
            },
            this
        );
        memory.write(doAction);

    }

    public void receiveResult(ActionResult result) {
        log.debug(result);
    }
    
    public synchronized boolean waitForPong() {
        if(pongMessage == null) {
            try {
                wait(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        if(pongMessage == null) {
            return false;
        }
        
        log.info("PingerBean:: received " + pongMessage);
        return true;
    }
}
