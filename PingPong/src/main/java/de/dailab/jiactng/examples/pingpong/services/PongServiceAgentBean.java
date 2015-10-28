package de.dailab.jiactng.examples.pingpong.services;

import de.dailab.jiactng.agentcore.action.AbstractMethodExposingBean;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.examples.pingpong.ontology.Ping;
import de.dailab.jiactng.examples.pingpong.ontology.Pong;

public class PongServiceAgentBean extends AbstractMethodExposingBean implements PongServices {

	@Override
	public Pong pingpong(final Ping ping) throws Exception {

		if (ping == null) {
			throw new Exception("ping message is null, send no pong message");
		}
		this.log.info("creating Pong based on " + ping);
		return new Pong(ping);
	}

	@Override
	public IFact echo(final IFact arg0) {
		return arg0;
	}

}
