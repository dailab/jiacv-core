/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm.jms;

import javax.jms.Destination;

import de.dailab.jiactng.agentcore.comm.message.IEndPoint;
import de.dailab.jiactng.agentcore.comm.message.IJiacContent;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class DummyMessage implements IJiacMessage {
    private IJiacContent _content;
    
    public DummyMessage(IJiacContent content) {
        _content= content;
    }
    
    public IEndPoint getEndPoint() {
        return null;
    }

    public String getJiacDestination() {
        return null;
    }

    public String getOperation() {
        return null;
    }

    public IJiacContent getPayload() {
        return _content;
    }

    public Destination getSender() {
        return null;
    }

    public IEndPoint getStartPoint() {
        return null;
    }

    public void setSender(Destination destination) {
    }
}
