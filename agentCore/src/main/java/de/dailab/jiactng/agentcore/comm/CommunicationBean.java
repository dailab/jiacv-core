/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.annotation.Expose;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * This bean specifies the way an agent communicates. It implements a message-based approach for information
 * exchange and group administration.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class CommunicationBean extends AbstractMethodExposingBean {
    public CommunicationBean() {}

    /**
     * An invocation of this action will associate this agent with a group (a logical destination)
     * 
     * @param group  the group to join
     */
    @Expose
    public void joinGroup(IGroupAddress group) throws CommunicationException {
        
    }
    
    /**
     * An invocation of this action will remove this agent from the specified group.
     * 
     * @param group  the group to leave
     */
    @Expose
    public void leaveGroup(IGroupAddress group) throws CommunicationException {
        
    }
    
    /**
     * This action will create a new message box for this agent. Messages that are sent to it
     * will be received by this agent exclusivly.
     * 
     * @param messageBox    the address of the new message box
     */
    @Expose
    public void establishMessageBox(IMessageBoxAddress messageBox) throws CommunicationException {
        
    }
    
    /**
     * This action destroys the message box with the specified address.
     * 
     * @param messageBox    the address to the message box which should be destroyed
     */
    @Expose
    public void destroyMessageBox(IMessageBoxAddress messageBox) throws CommunicationException {
        
    }
    
    /**
     * This method sends a message to the given destination.
     * 
     * @param message   the message to send
     * @param address   the address which points 
     */
    @Expose
    public void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException {
        
    }
}
