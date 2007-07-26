/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.action.ActionResult;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.ObjectContent;
import de.dailab.jiactng.agentcore.environment.ResultReceiver;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class PingerBean extends AbstractAgentBean implements ResultReceiver {
    private static String ACTION_NAME= "de.dailab.jiactng.agentcore.comm.CommunicationBean#send";
    
    public void startPingProcess() {
        System.out.println("start the ping process...");
        
        Action action= memory.read(new Action(ACTION_NAME, null, null, null));
        IJiacMessage  message= new DummyMessage(new ObjectContent("Ping"));
            DoAction doAction= action.createDoAction(new Object[]{
                message,
                CommunicationAddressFactory.createGroupAddress(PingPongTestCase.DUMMY_ADDRESS)
            },
            this
        );
        memory.write(doAction);

    }

    public void receiveResult(ActionResult result) {
        System.out.println(result);
    }
}
