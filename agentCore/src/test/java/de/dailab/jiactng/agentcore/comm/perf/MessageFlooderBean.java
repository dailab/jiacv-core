/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.perf;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.IAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class MessageFlooderBean extends AbstractAgentBean {
    private final class MessageGenerator extends Thread {
        public MessageGenerator() {
            super("Generator");
        }

        @SuppressWarnings("synthetic-access")
        public void run() {
            try {
                System.out.println("press return...");
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            System.out.println("start flooding");
            
            long startTime= System.currentTimeMillis();
            long numbytes= 0;
            long count= 0;
            for(int i= 0; i < max; ++i) {
                IJiacMessage message= new JiacMessage(new ObjectContent(content));
                message.setHeader("flood-counter", String.valueOf(i));
//                try {
//                    commBean.send(message, groupAddress);
//                } catch (CommunicationException e) {
//                    e.printStackTrace();
//                }
                
                DoAction doa= sendAction.createDoAction(new Serializable[]{message, groupAddress}, null);
                memory.write(doa);
//                invoke(sendAction, new Serializable[]{message, groupAddress});
                count++;
                numbytes+= content.length();
            }
            
            long stopTime= System.currentTimeMillis();
            System.out.println("TIME REQUIRED to flood " + count + " messages (" + numbytes + "): " + (stopTime - startTime));
        }
    }
    
    protected String content;
    protected IGroupAddress groupAddress;
    protected int max;
    protected Action sendAction;
    
    protected ICommunicationBean commBean;
    
    private final MessageGenerator _generator;
    private String _groupName;
    
    public MessageFlooderBean() {
        _generator= new MessageGenerator();
    }
    
    public void doInit() throws Exception {
        super.doInit();
        
        StringBuffer msg= new StringBuffer();
        Random r= new Random();
        
        for(int i= 0; i < 2000; ++i) {
            msg.append((char) (r.nextInt(95) + 32));
        }
        
        content= msg.toString();
        
        for(IAgentBean bean : thisAgent.getAgentBeans()) {
            if(bean instanceof ICommunicationBean) {
                commBean= (ICommunicationBean) bean;
                break;
            }
        }
    }

    public void doStart() throws Exception {
        super.doStart();
        groupAddress= CommunicationAddressFactory.createGroupAddress(_groupName);
        sendAction= memory.read(new Action(ICommunicationBean.ACTION_SEND));
        _generator.start();
    }
    
    public void setGroupName(String groupName) {
        _groupName= groupName;
    }
    
    public void setMax(int max) {
        this.max= max;
    }
}
