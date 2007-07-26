/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class PongerBean extends AbstractAgentBean {
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
                System.out.println("PongerBean:: received " + content.getObject());

            }
        }
    }
    
    @Override
    public void doInit() throws Exception {
        super.doInit();
        memory.attach(new MessageObserver(), new DummyMessage(null));
    }
}
