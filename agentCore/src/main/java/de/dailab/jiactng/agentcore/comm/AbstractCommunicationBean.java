/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.action.DoAction;
import de.dailab.jiactng.agentcore.action.annotation.Expose;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;

/**
 * This bean specifies the way an agent communicates. It implements a message-based approach for information
 * exchange and group administration.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractCommunicationBean extends AbstractMethodExposingBean {
//    /**
//     * Action name for joining a group. An invocation will associate this agent with a further logical destination.
//     */
//    public final static String DO_JOIN_GROUP= AbstractCommunicationBean.class.getName() + "#joinGroup";
//    
//    /**
//     * Action name for leaving a group.
//     */
//    public final static String DO_LEAVE_GROUP= AbstractCommunicationBean.class.getName() + "#leaveGroup";
//    
//    /**
//     * Action name for creating a message box for this agent exclusivly.
//     */
//    public final static String DO_CREATE_MESSAGE_BOX= AbstractCommunicationBean.class.getName() + "#createMessageBox";
//    
//    /**
//     * Action name for destroying an existing message box.
//     */
//    public final static String DO_DESTROY_MESSAGE_BOX= AbstractCommunicationBean.class.getName() + "#destroyMessageBox";
//    
//    /**
//     * Action name for sending a message to a group of agents.
//     */
//    public final static String DO_INFORM_GROUP= AbstractCommunicationBean.class.getName() + "#informGroup";
//    
//    /**
//     * Action name for sending a message directly to a message box.
//     */
//    public final static String DO_INFORM_ONE= AbstractCommunicationBean.class.getName() + "#informOne";
//    
//    /**
//     * Action name for a remote action invocation.
//     */
//    public final static String DO_REQUEST_REMOTE_ACTION= AbstractCommunicationBean.class.getName() + "#requestRemoteAction";
    
    protected AbstractCommunicationBean() {}

    /**
     * An invocation of this action will associate this agent with a group (a logical destination)
     * 
     * @param group  the group to join
     */
    @Expose
    protected abstract void joinGroup(IGroupAddress group);
    
    /**
     * An invocation of this action will remove this agent from the specified group.
     * 
     * @param group  the group to leave
     */
    @Expose
    protected abstract void leaveGroup(IGroupAddress group);
    
    /**
     * This action will create a new message box for this agent. Messages that are sent to it
     * will be received by this agent exclusivly.
     * 
     * @param messageBox    the address of the new message box
     */
    @Expose
    protected abstract void createMessageBox(IMessageBoxAddress messageBox);
    
    /**
     * This action destroys the message box with the specified address.
     * 
     * @param messageBox    the address to the message box which should be destroyed
     */
    @Expose
    protected abstract void destroyMessageBox(IMessageBoxAddress messageBox);
    
    /**
     * Sends a message to the specified group. The group address might referenced
     * a closed group or an open group.
     * 
     * @param message   the message to send
     * @param group     the logical address of the group
     */
    @Expose
    protected abstract void informGroup(IJiacMessage message, IGroupAddress group);
    
    /**
     * Sends a message to another agents message box.
     * 
     * @param message       the message to send
     * @param messageBox    the address of the target message box
     */
    @Expose
    protected abstract void informOne(IJiacMessage message, IMessageBoxAddress messageBox);
    
    /**
     * Request a remote action invocation.
     * 
     * @param doAction          the identifier object for the remote action
     * @param agentDescription  the agent description of the provider agent
     */
    @Expose
    protected abstract void requestRemoteAction(DoAction doAction, AgentDescription agentDescription);
 }
