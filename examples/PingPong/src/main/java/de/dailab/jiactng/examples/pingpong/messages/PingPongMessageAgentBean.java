package de.dailab.jiactng.examples.pingpong.messages;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;

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
import de.dailab.jiactng.examples.pingpong.ontology.Ping;
import de.dailab.jiactng.examples.pingpong.ontology.Pong;

public class PingPongMessageAgentBean extends AbstractAgentBean {

	public static final String GROUPADDR = "MyImportantUniqueGroupName";

	/*
	 * The space observer will be attached as 'listener' to the memory. The
	 * observer will be called on any event, that changes the agent's memory -
	 * write, update, remove. Within of every event is the information about the
	 * changes.
	 * 
	 * Handle this 'notify' method to get events and the contained changes.
	 */
	private final SpaceObserver<IFact> messageSpaceObserver = new SpaceObserver<IFact>() {

		private static final long serialVersionUID = -9214367427027118819L;

		@Override
		public void notify(final SpaceEvent<? extends IFact> arg0) {
			/*
			 * Get notified about new chat messages, now deliver these messages to the
			 * GUI.
			 */
			if (arg0 instanceof WriteCallEvent) {
				@SuppressWarnings("unchecked")
				WriteCallEvent<IFact> write = (WriteCallEvent<IFact>) arg0;
				IFact iFact = write.getObject();
				if (iFact instanceof IJiacMessage) {
					IFact payload = ((IJiacMessage) iFact).getPayload();
					if (payload instanceof Ping) {
						PingPongMessageAgentBean.this.log.info(((IJiacMessage) iFact).getSender());
						PingPongMessageAgentBean.this.handle((Ping) payload);
					}
					else if (payload instanceof Pong) {
						PingPongMessageAgentBean.this.handle((Pong) payload);
					}
					else {

					}
					PingPongMessageAgentBean.this.log.info("message received " + payload);
				}
				else {
					/*
					 * This can never be called, as this handler only reacts to
					 * JiacMessages
					 */
				}
			}
			else {
				/*
				 * There are other events, explore them!
				 */
			}
		}

	};

	@Override
	public void doInit() throws Exception {
		super.doInit();

		/*
		 * Adding a space observer to get notification about new messages. The
		 * second parameter will be used as template. The first parameter is the
		 * observer, that will be notified.
		 */
		this.memory.attach(this.messageSpaceObserver, new JiacMessage(new Ping()));
	}

	@Override
	public void doStart() throws Exception {
		super.doStart();
		/*
		 * The next three lines will join this agent to a separate communication
		 * group. First create a group address, second find the (agent) local action
		 * to join a group, third invoke the action to join a group. In the third
		 * step, there is no need to add a ResultReceiver.
		 */
		IGroupAddress groupAddress = CommunicationAddressFactory.createGroupAddress(GROUPADDR);
		Action action = this.memory.read(new Action(ICommunicationBean.ACTION_JOIN_GROUP));
		this.invoke(action, new Serializable[] { groupAddress });
	}

	@Override
	public void execute() {
		super.execute();
		Set<Ping> pings = this.memory.removeAll(new Ping());
		if (pings.size() > 0) {

		}

		Random random = new Random();
		Ping cm = new Ping(random.nextInt(6));

		IGroupAddress groupAddress = CommunicationAddressFactory.createGroupAddress(GROUPADDR);
		Action action = this.memory.read(new Action(ICommunicationBean.ACTION_SEND));
		this.invoke(action, new Serializable[] { new JiacMessage(cm), groupAddress });
	}

	@Override
	public void doStop() throws Exception {
		/*
		 * Like in doStart, we use JIAC agent methods to leave a communication group
		 * on stopping.
		 */
		IGroupAddress groupAddress = CommunicationAddressFactory.createGroupAddress(GROUPADDR);
		Action action = this.memory.read(new Action(ICommunicationBean.ACTION_LEAVE_GROUP));
		this.invoke(action, new Serializable[] { groupAddress });
		super.doStop();
	}

	@Override
	public void doCleanup() throws Exception {
		/*
		 * Like doInit we detach the observer from agents memory.
		 */
		this.memory.detach(this.messageSpaceObserver);
		super.doCleanup();
	};

	private void handle(final Ping ping) {
		this.memory.write(ping);
	}

	private void handle(final Pong pong) {
		this.log.info("the highest ping was: " + pong);
	}

}
