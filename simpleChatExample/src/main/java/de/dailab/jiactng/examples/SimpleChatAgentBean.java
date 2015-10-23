package de.dailab.jiactng.examples;

import java.io.Serializable;

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
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.examples.SimpleChatUI.MessageHandler;

public class SimpleChatAgentBean extends AbstractAgentBean implements MessageHandler {

	/**
	 * A constant for a common communication group. This constant will be used as
	 * identifier and selector for message routing. You need this constant to
	 * register to a group and send messages to the group.
	 */
	private static final String GROUPNAME = "SimpleWorldChatGroup";

	/*
	 * The space observer will be attached as 'listener' to the memory. The
	 * observer will be called on any event, that changes the agent's memory -
	 * write, update, remove. Within of every event is the information about the
	 * changes.
	 * 
	 * Handle this 'notify' method to get events and the contained changes.
	 */
	private final SpaceObserver<IFact> chatMessageSpaceObserver = new SpaceObserver<IFact>() {

		private static final long serialVersionUID = -9214367427027118819L;

		@Override
		public void notify(final SpaceEvent<? extends IFact> arg0) {
			/*
			 * Get notified about new chat messages, now deliver these messages to the
			 * GUI.
			 */
			if (arg0 instanceof WriteCallEvent) {
				WriteCallEvent<IFact> write = (WriteCallEvent<IFact>) arg0;
				IFact iFact = write.getObject();
				if (iFact instanceof IJiacMessage) {
					Object payload = ((IJiacMessage) iFact).getPayload();
					if (payload instanceof ChatMessage) {
						SimpleChatAgentBean.this.simpleChatUI.addMessage((ChatMessage) payload);
					}
				}
				else {
					// XXX this can never be called, as this handler only reacts to JiacMessages
					SimpleChatAgentBean.this.log.warn("the space observer was notified on a different type as 'ChatMessage', currently got: "
							+ iFact.getClass().getCanonicalName());
				}
			}
			else {
				if (SimpleChatAgentBean.this.log.isDebugEnabled()) {
					SimpleChatAgentBean.this.log.debug("other events except WriteCallEvent we don't react on, currently called: "
							+ arg0.getClass().getCanonicalName());
				}
			}
		}

	};

	private SimpleChatUI simpleChatUI = null;

	@Override
	public void doInit() throws Exception {
		/*
		 * Adding a space observer to get notification about new messages. The
		 * second parameter will be used as template. The first parameter is the
		 * observer, that will be notified.
		 */
		this.memory.attach(this.chatMessageSpaceObserver, new JiacMessage());
		this.simpleChatUI = SimpleChatUI.createInstance(this);
	};

	@Override
	public void doStart() throws Exception {
		super.doStart();
		/*
		 * The next three lines will join this agent to a separate communication
		 * group. First create a group address, second find the (agent) local action
		 * to join a group, third invoke the action to join a group. In the third
		 * step, there is no need to add a ResultReceiver.
		 */
		IGroupAddress groupAddress = CommunicationAddressFactory.createGroupAddress(GROUPNAME);
		Action action = this.memory.read(new Action(ICommunicationBean.ACTION_JOIN_GROUP));
		this.invoke(action, new Serializable[] { groupAddress });
	}

	@Override
	public void doStop() throws Exception {
		/*
		 * Like in doStart, we use JIAC agent methods to leave a communication group
		 * on stopping.
		 */
		IGroupAddress groupAddress = CommunicationAddressFactory.createGroupAddress(GROUPNAME);
		Action action = this.memory.read(new Action(ICommunicationBean.ACTION_LEAVE_GROUP));
		this.invoke(action, new Serializable[] { groupAddress });
		super.doStop();
	}

	@Override
	public void doCleanup() throws Exception {
		/*
		 * Like doInit we detach the observer from agents memory.
		 */
		this.memory.detach(this.chatMessageSpaceObserver);
		super.doCleanup();
	};

	/**
	 * This method is called to send a text message to the 'global' chat
	 * communication group. The required field of the sender's timestamp and
	 * sender's name will be retrieved within of this method.
	 *
	 * @param message
	 *          a text message to be send
	 */
	@Override
	public void sendMessage(final String message) {
		/*
		 * Maybe you can save some line of code, if you move some lines to other
		 * methods. But we want wo present an example. Therefore we throw efficiency
		 * from your mind to have a self-comprehensive example within of one method.
		 */
		/*
		 * Retrieve the information needed for the chat message. The 'owner' field
		 * is the login name at this (users) host. The timestamp is the current
		 * system time as Long. The text message is the given parameter.
		 */
		String sender = this.thisAgent.getOwner();
		Long time = System.currentTimeMillis();
		ChatMessage cm = new ChatMessage(sender, message, time);

		/*
		 * Where to send the message? To the communication group address we are
		 * listening at. Create an address pattern like the registration pattern
		 * above. Find the 'send' action to send JIacMessages and wrap the chat
		 * message into the JiacMessage as container.
		 */
		IGroupAddress groupAddress = CommunicationAddressFactory.createGroupAddress(GROUPNAME);
		Action action = this.memory.read(new Action(ICommunicationBean.ACTION_SEND));
		this.invoke(action, new Serializable[] { new JiacMessage(cm), groupAddress });

	}

}
