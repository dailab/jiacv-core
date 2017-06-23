package de.dailab.jiactng.examples.tls.pingPong;

import java.io.Serializable;

import org.sercho.masp.space.event.SpaceEvent;
import org.sercho.masp.space.event.SpaceObserver;
import org.sercho.masp.space.event.WriteCallEvent;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.agentcore.action.Action;
import de.dailab.jiactng.agentcore.comm.ICommunicationBean;
import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.comm.message.IJiacMessage;
import de.dailab.jiactng.agentcore.comm.message.JiacMessage;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.AgentDescription;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;
import de.dailab.jiactng.examples.tls.pingPong.facts.Ping;

public class PingBean extends AbstractAgentBean {

	private IActionDescription sendAction = null;
	private final String pongAgentName = "PongAgent1";
	private String agentName = "";

	@Override
	public void doStart() throws Exception {
		super.doStart();
		agentName = thisAgent.getAgentName();
		log.info(String.format("%s: Starting this agend on node '%s'.",
				agentName, thisAgent.getAgentNode().getName()));
		sendAction = getSendAction();
		memory.attach(new MessageObserver(), new JiacMessage(new Ping()));
	}

	@Override
	public void execute() {
		log.info(String.format("%s: execute.", agentName));
		sendPing();
	}

	private void sendPing() {
		IAgentDescription agent = getAgentByName(pongAgentName);
		if (agent == null) {
			log.info(String.format(
					"%s: No pong agent found. Cannot send pong message.",
					agentName));
			return;
		}
		log.info(String.format("%s: Send ping.", agentName));
		IMessageBoxAddress receiver = agent.getMessageBoxAddress();
		JiacMessage message = new JiacMessage(new Ping("ping"));
		invoke(sendAction, new Serializable[] { message, receiver });
	}

	private IAgentDescription getAgentByName(String agentName) {
		AgentDescription template = new AgentDescription();
		template.setName(agentName);
		IAgentDescription agent = thisAgent.searchAgent(template);
		return agent;
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
			if (event instanceof WriteCallEvent<?>) {
				WriteCallEvent<IJiacMessage> wce = (WriteCallEvent<IJiacMessage>) event;
				IJiacMessage message = memory.remove(wce.getObject());
				IFact payload = message.getPayload();
				if (!(payload instanceof Ping)) {
					log.info(String
							.format("%s: Message received, but not expected one (Ping).",
									agentName));
					return;
				}
				Ping ping = (Ping) payload;
				log.info(String.format("%s: Ping received! Message: %s", agentName,
						ping.getMessage()));
			}
		}
	}
}
