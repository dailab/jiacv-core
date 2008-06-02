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
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class PongerBean extends AbstractAgentBean implements ResultReceiver {
    private class MessageObserver implements SpaceObserver<IFact> {

        @SuppressWarnings("unchecked")
        public void notify(SpaceEvent<? extends IFact> event) {
            if(event instanceof WriteCallEvent) {
                WriteCallEvent callEvent= (WriteCallEvent) event;
                Object obj= callEvent.getObject();
                
                if(!IJiacMessage.class.isInstance(obj)) {
                    return;
                }
                
                IJiacMessage message= (IJiacMessage) obj;
                ObjectContent content= (ObjectContent) message.getPayload();
                log.info("PongerBean:: received " + content.getObject());

                Action action= memory.read(new Action(PingPongTestCase.ACTION_NAME));
                IJiacMessage  pongMessage= new JiacMessage(new ObjectContent("Pong"));
                    DoAction doAction= action.createDoAction(new Serializable[]{
                        pongMessage,
                        message.getSender().toUnboundAddress()
                    },
                    PongerBean.this
                );
                memory.write(doAction);
            }
        }
    }
    
    @Override
    public void doInit() throws Exception {
        super.doInit();
        memory.attach(new MessageObserver(), new JiacMessage(null));
    }

    public void receiveResult(ActionResult result) {
        log.debug(result);
    }
}
