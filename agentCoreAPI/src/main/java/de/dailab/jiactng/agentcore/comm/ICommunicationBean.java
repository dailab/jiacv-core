/*
 * $Id: ICommunicationBean.java 24453 2009-08-17 13:29:03Z loeffelholz $ 
 */
package de.dailab.jiactng.agentcore.comm;

import de.dailab.jiactng.agentcore.action.IMethodExposingBean.Expose;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;

/**
 * This interface describes a standard communication component of an agent.
 * 
 * @author Marcel Patzlaff
 * @version $Revision: 24453 $
 */
public interface ICommunicationBean {
	
	/**
	 * Action to join a Group, which means start receiving all messages that are send to it.
	 * 
	 * <br />
	 * <b>InputParameter</b> <br /> 
	 * 	IGroupAddress	-  The Groupaddress you want to listen to. Must not be null<br />
	 *
	 * <br />
	 * <b>Result Types:</b> <br />
	 * 	none <br />
	 * 
	 * <b>Exceptions:</b> <br />
	 * 	CommunicationException is thrown if Address is null or there is a problem with the transport <br />
	 */
    String ACTION_JOIN_GROUP= "de.dailab.jiactng.agentcore.comm.ICommunicationBean#joinGroup";
    
    /**
	 * Action to leave a Group, which means stop receiving any messages that are send to it.
	 * 
	 * <br />
	 * <b>InputParameter</b> <br /> 
	 * 	IGroupAddress	-  The Groupaddress you stop want to listen to. Must not be null<br />
	 *
	 * <br />
	 * <b>Result Types:</b> <br />
	 * 	none <br />
	 * 
	 * <b>Exceptions:</b> <br />
	 * 	CommunicationException is thrown if Address is null or there is a problem with the transport <br />
	 */
    String ACTION_LEAVE_GROUP= "de.dailab.jiactng.agentcore.comm.ICommunicationBean#leaveGroup";
    
    /**
	 * Action that checks whether the provided address references an agent that is
     * local, thus on the same agentnode as the agent this bean belongs to.
	 * 
	 * <br />
	 * <b>InputParameter</b> <br /> 
	 * 	IMessageBoxAddress	-  the address to check<br />
	 *
	 * <br />
	 * <b>Result Types:</b> <br />
	 * 	boolean -  <code>true</code> if the address references a local
     *                      agent and <code>false</code> otherwise.<br />
	 * 
	 * <b>Exceptions:</b> <br />
	 * 	none <br />
	 */
    String ACTION_IS_LOCAL= "de.dailab.jiactng.agentcore.comm.ICommunicationBean#isLocal";
    
    /**
     * This method provides access to the message bus.
     * It delivers the message to the specified address.
     * 
     * <br />
	 * <b>InputParameter</b> <br /> 
	 * 	IJiacMessage	-  the message to send<br />
	 *  ICommunicationAddress - the address to send the message to<br />
	 *
	 * <br />
	 * <b>Result Types:</b> <br />
	 * 	boolean -  <code>true</code> if the address references a local
     *                      agent and <code>false</code> otherwise.<br />
	 * 
	 * <b>Exceptions:</b> <br />
	 * 	CommunicationException -  if an error occurs on the message bus <br />
     */
    String ACTION_SEND= "de.dailab.jiactng.agentcore.comm.ICommunicationBean#send";
    
    /**
	 * Action to start receiving messages that are send to a given address.
	 * It is possible to limit the kind of messages that are received by adding a template
	 * It is possible to register multiple times for an address using different templates to
	 * thus receive only messages that are matching to some profiles.
	 * 
	 * <br />
	 * <b>InputParameter</b> <br /> 
	 * 	ICommunicationAddress	-  	The address you want to listen to. Must not be null.<br />
	 * 	IJiacMessage			-  	A template to filter messages, set to null if you don't want to use it. 
	 * 								For informations how to use it see the communication site in the xdocs
	 * <br />
	 * <b>Result Types:</b> <br />
	 * 	none <br />
	 * 
	 * <b>Exceptions:</b> <br />
	 * 	CommunicationException is thrown if Address is null or there is a problem with the transport <br />
	 */
    String ACTION_REGISTER = "de.dailab.jiactng.agentcore.comm.ICommunicationBean#register";
    
    /**
	 * Action to stop receiving messages that are send to a given address.
	 * If you used a template registering to this address you have to use it again to unregister.
	 * Only the combination of address and template will be unregistered. So if you registered multiple
	 * times the other registrations will not be touched and left active.
	 * 
	 * <br />
	 * <b>InputParameter</b> <br /> 
	 * 	ICommunicationAddress	-  	The address you want to listen to. Must not be null.<br />
	 * 	IJiacMessage			-  	A template to filter messages, set to null if you don't want to use it. 
	 * 								For informations how to use it see the communication site in the xdocs
	 * <br />
	 * <b>Result Types:</b> <br />
	 * 	none <br />
	 * 
	 * <b>Exceptions:</b> <br />
	 * 	CommunicationException is thrown if Address is null or there is a problem with the transport <br />
	 */
    String ACTION_UNREGISTER = "de.dailab.jiactng.agentcore.comm.ICommunicationBean#unregister";
    
    /**
     * Joins a group with the given address
     * 
     * @param group         the address of the group
     * @throws CommunicationException
     *                      if an error occurs on the message bus
     */
    @Expose(name = ACTION_JOIN_GROUP)
    void joinGroup(IGroupAddress group) throws CommunicationException;
    
    /**
     * Leaves the group with the given address
     * 
     * @param group         the address of the group
     * @throws CommunicationException
     *                      if an error occurs on the message bus
     */
    @Expose(name = ACTION_LEAVE_GROUP)
    void leaveGroup(IGroupAddress group) throws CommunicationException;
    
    
    /**
     * Checks whether the provided address references an agent that is
     * local to the agent this bean belongs to.
     * 
     * @param messageBox    the address to check
     * @return              <code>true</code> if the address references a local
     *                      agent and <code>false</code> otherwise.
     */
    @Expose(name = ACTION_IS_LOCAL)
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
    @Expose(name = ACTION_SEND)
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
