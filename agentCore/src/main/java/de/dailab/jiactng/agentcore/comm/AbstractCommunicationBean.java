/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.annotation.Expose;
import de.dailab.jiactng.agentcore.comm.message.IEndPoint;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * This bean specifies the way an agent communicates. It implements a message-based approach for information
 * exchange and group administration.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractCommunicationBean extends AbstractMethodExposingBean {
    protected AbstractCommunicationBean() {}

    /**
     * An invocation of this action will associate this agent with a group (a logical destination)
     * 
     * @param group  the group to join
     */
    @Expose
    public abstract void joinGroup(IGroupAddress group) throws CommunicationException;
    
    /**
     * An invocation of this action will remove this agent from the specified group.
     * 
     * @param group  the group to leave
     */
    @Expose
    public abstract void leaveGroup(IGroupAddress group) throws CommunicationException;
    
    /**
     * This action will create a new message box for this agent. Messages that are sent to it
     * will be received by this agent exclusivly.
     * 
     * @param messageBox    the address of the new message box
     */
    @Expose
    public abstract void createMessageBox(IMessageBoxAddress messageBox) throws CommunicationException;
    
    /**
     * This action destroys the message box with the specified address.
     * 
     * @param messageBox    the address to the message box which should be destroyed
     */
    @Expose
    public abstract void destroyMessageBox(IMessageBoxAddress messageBox) throws CommunicationException;
    
    /**
     * This method sends a message to the given destination.
     * 
     * @param message   the message to send
     * @param address   the address which points 
     */
    @Expose
    public abstract void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException;
    
    @Expose
    public abstract void sendInSession(IJiacMessage message, IEndPoint endpoint, String sessionId) throws CommunicationException;
    
//    public abstract IGroupAddress createGroupAddress(String groupName);
//    public abstract IMessageBoxAddress createMessageBoxAddress(String boxName);
}
