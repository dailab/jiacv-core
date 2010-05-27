/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.perf;

import java.io.Serializable;
import java.util.ArrayList;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class MessageReaderBean extends AbstractAgentBean {
    @SuppressWarnings("serial")
    private final class MessageObserver implements SpaceObserver<IFact> {
        private final ArrayList<String> _countMarkers;
        private int _numBytes= 0;
        private long _startTime= -1;
        private long _endTime= -1;
        
        protected MessageObserver() {
            _countMarkers= new ArrayList<String>(max);
            _numBytes= 0;
        };
        
        @SuppressWarnings({ "unchecked", "synthetic-access" })
        public void notify(SpaceEvent<? extends IFact> event) {
            if(event instanceof WriteCallEvent) {
                if(_startTime < 0) {
                    _startTime= System.currentTimeMillis();
                }
                
                WriteCallEvent callEvent= (WriteCallEvent) event;
                Object obj= callEvent.getObject();
                
                if(!IJiacMessage.class.isInstance(obj)) {
                    return;
                }
                
                IJiacMessage message= (IJiacMessage) obj;
                ObjectContent content= (ObjectContent) message.getPayload();
                String count= message.getHeader("flood-counter");
                String str= (String) content.getObject();
                
                _numBytes+= str.length();
                _countMarkers.add(count);
            }
            
            if(_countMarkers.size() == max) {
                _endTime= System.currentTimeMillis();
                System.out.println("RECEIVED " + _countMarkers.size() + " messages (" + _numBytes +  ") between " + _startTime + " and " + _endTime + " total: " + (_endTime - _startTime));
                FlooderLauncher.shutdownNode(thisAgent.getAgentNode());
            }
        }
    }
    
    protected int max;
    
    private MessageObserver _observer;
    private String _groupName;
    
    public void doInit() throws Exception {
        super.doInit();
        _observer= new MessageObserver();
        
        IJiacMessage template= new JiacMessage(new ObjectContent(null));
        memory.attach(_observer, template);
    }

    public void doStart() throws Exception {
        super.doStart();
        
        IGroupAddress groupAddress= CommunicationAddressFactory.createGroupAddress(_groupName);
        Action action= memory.read(new Action(ICommunicationBean.ACTION_JOIN_GROUP));
        invoke(action, new Serializable[]{groupAddress});
    }
    
    public void setGroupName(String groupName) {
        _groupName= groupName;
    }
    
    public void setMax(int max) {
        this.max= max;
    }
}
