package de.dailab.jiactng.examples;

import java.io.Serializable;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.CommunicationAddressFactory;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IGroupAddress;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.knowledge.IFact;

public class SimpleChatAgentBean extends AbstractAgentBean {

	private static final String GROUPNAME = "SimpleWorldChatGroup";

	private final SpaceObserver<IFact> chatMessageSpaceObserver = new SpaceObserver<IFact>() {

		private static final long serialVersionUID = -9214367427027118819L;

		@Override
		public void notify(final SpaceEvent<? extends IFact> arg0) {
			/*
			 * Get notified about new chat messages, now deliver these messages to the
			 * GUI.
			 */
		}

	};

	@Override
	public void doInit() throws Exception {
		/*
		 * Adding a space observer to get notification about new messages. The
		 * second parameter will be used as template. The first parameter is the
		 * observer, that will be notified.
		 */
		this.memory.attach(this.chatMessageSpaceObserver, new ChatMessage());
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

	private void sendMessage(final String message) {
		String sender = this.thisAgent.getOwner();
		Long time = System.currentTimeMillis();
		ChatMessage cm = new ChatMessage(sender, message, time);

		IGroupAddress groupAddress = CommunicationAddressFactory.createGroupAddress(GROUPNAME);
		Action action = this.memory.read(new Action(ICommunicationBean.ACTION_SEND));
		this.invoke(action, new Serializable[] { new JiacMessage(cm), groupAddress });

	}

	private void displayMessage(final ChatMessage message) {

	}
}
