/*
 * $Id$ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean.Expose;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * This interface describes a standard communication component of an agent.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public interface ICommunicationBean {
    /**
     * Joins a group with the given address
     * 
     * @param group         the address of the group
     * @throws CommunicationException
     *                      if an error occurs on the message bus
     */
    @Expose
    void joinGroup(IGroupAddress group) throws CommunicationException;
    
    /**
     * Leaves the group with the given address
     * 
     * @param group         the address of the group
     * @throws CommunicationException
     *                      if an error occurs on the message bus
     */
    @Expose
    void leaveGroup(IGroupAddress group) throws CommunicationException;
    
    /**
     * Checks whether the provided address references an agent that is
     * local to the agent this bean belongs to.
     * 
     * @param messageBox    the address to check
     * @return              <code>true</code> if the address references a local
     *                      agent and <code>false/<code> otherwise.
     */
    @Expose
    boolean isLocal(IMessageBoxAddress messageBox);
    
    /**
     * Creates a new message box at the given address and associate the requesting
     * agent with it.
     * 
     * @param messageBox    the address of the message box
     * @throws CommunicationException
     *                      if an error occurs on the message bus
     * 
     * @deprecated
     *      This method will be removed as soon as there is a white pages service
     *      available!
     */
    @Deprecated
    @Expose
    void establishMessageBox(IMessageBoxAddress messageBox) throws CommunicationException;
    
    /**
     * Disassociate the owner agent from the specified message box and destroys it.
     * 
     * @param messageBox    the address of the message box
     * @throws CommunicationException
     *                      if an error occurs on the message bus
     *                      
     * @deprecated
     *      This method will be removed as soon as there is a white pages service
     *      available!
     */
    @Deprecated
    @Expose
    void destroyMessageBox(IMessageBoxAddress messageBox) throws CommunicationException;
    
    /**
     * This method provides access to the message bus.
     * It delivers the message to the specified address.
     * 
     * @param message       the message to send
     * @param address       the address of the recipient (can be a group or a message box)
     * @throws CommunicationException
     *                      if an error occurs on the message bus
     */
    @Expose
    void send(IJiacMessage message, ICommunicationAddress address) throws CommunicationException;
    
    /**
     * Generic method for the registration of both message box and group.
     * 
     * @param address       the address to associate with
     * @throws CommunicationException
     *                      if an error occurs on the message bus
     *                      
     * @deprecated 
     *      This method will be removed soon as it collides with
     *      {@link #register(ICommunicationAddress, IJiacMessage)}!
     */
    @Expose
    @Deprecated
    void register(ICommunicationAddress address) throws CommunicationException;
    
    /**
     * Generic method for the registration of both message box and group. It also
     * accepts a template which acts as a filter for incoming messages.
     * Messages that do not fit to the specified template will be ignored.
     * 
     * @param address           the address to associate with
     * @param selectorTemplate  the template to filter incoming messages
     * @throws CommunicationException
     *                          if an error occurs on the message bus
     */
    @Expose
    void register(ICommunicationAddress address, IJiacMessage selectorTemplate) throws CommunicationException;
    
    /**
     * Generic method for the deregistration of both message box and group.
     * 
     * @param address           the address to disassociate from
     * @throws CommunicationException
     *                          if an error occurs on the message bus
     * 
     * @deprecated 
     *      This method will be removed soon as it collides with
     *      {@link #unregister(ICommunicationAddress, IJiacMessage)}! 
     */
    @Expose
    @Deprecated
    void unregister(ICommunicationAddress address) throws CommunicationException;
    
    /**
     * Generic method for the deregistration of both message box and group. It also
     * accepts a template. This method only takes effect if the specified
     * address - template combination was registered before
     * 
     * @param address           the address to disassociate from
     * @param selectorTemplate  the template that filtered incoming messages
     * @throws CommunicationException
     *                          if an error occurs on the message bus
     */
    @Expose
    void unregister(ICommunicationAddress address, IJiacMessage selectorTemplate) throws CommunicationException;
}
