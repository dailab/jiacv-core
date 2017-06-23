package de.dailab.jiactng.examples.tls.pingPong;

import java.io.Serializable;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.examples.tls.pingPong.facts.Ping;

public class PongBean extends AbstractAgentBean {

	private IActionDescription sendAction = null;
	private String agentName = "";

	@Override
	public void doStart() throws Exception {
		super.doStart();
		agentName = thisAgent.getAgentName();
		log.info(String.format("%s: Starting this agend on node '%s'.",
				agentName, thisAgent.getAgentNode().getName()));
		sendAction = getSendAction();
		memory.attach(new MessageObserver(), new JiacMessage(new Ping("ping")));
	}

	@SuppressWarnings("unused")
	private void sendPong(ICommunicationAddress iCommunicationAddress) {
		log.info(String.format("%s: Sending pong message",
				thisAgent.getAgentName()));
		JiacMessage pongMessage = new JiacMessage(new Ping("pong"));
		invoke(sendAction, new Serializable[] { pongMessage,
				iCommunicationAddress });
	}

	private IActionDescription getSendAction() {
		sendAction = retrieveAction(ICommunicationBean.ACTION_SEND);
		if (sendAction == null) {
			IActionDescription template = new Action(
					ICommunicationBean.ACTION_SEND);
			sendAction = thisAgent.searchAction(template);
		}
		if (sendAction == null) {
			throw new RuntimeException("Send action not found.");
		}
		return sendAction;
	}

	private class MessageObserver implements SpaceObserver<IFact> {
		private static final long serialVersionUID = -7929316578559906995L;

		@SuppressWarnings("unchecked")
		public void notify(SpaceEvent<? extends IFact> event) {
			if (!(event instanceof WriteCallEvent<?>)) {
				return;
			}
			log.info(String.format("%s: Ping received.", agentName));
			WriteCallEvent<IJiacMessage> wce = (WriteCallEvent<IJiacMessage>) event;
			IJiacMessage message = memory.remove(wce.getObject());
			sendPong(message.getSender());
		}

		private void sendPong(ICommunicationAddress iCommunicationAddress) {
			log.info(String.format("%s: Sending pong message", agentName));
			JiacMessage pongMessage = new JiacMessage(new Ping("pong"));
			invoke(sendAction, new Serializable[] { pongMessage,
					iCommunicationAddress });
		}
	}

}
