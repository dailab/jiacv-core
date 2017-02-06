/*
 * Created on 16.02.2007
 */
package de.dailab.jiactng.examples.pingpong.memory;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.examples.pingpong.ontology.Ping;
import de.dailab.jiactng.examples.pingpong.ontology.Pong;

/**
 * The corresponding part to {@link PingMemoryBean}. Reading {@link Ping}s and
 * writing {@link Pong}s to agent's memory.
 *
 * <strong>Note: The agent's memory can be used as intra agent communication
 * blackboard.</strong>
 *
 * @author Michael Burkhardt
 * @author Thomas Konnerth
 */
public class PongMemoryBean extends AbstractAgentBean {

	/**
	 * An {@link #execute()} will be executed on every 'execution interval' that
	 * could be configured in the agent's configuration.
	 *
	 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
	 */
	@Override
	public void execute() {
		/*
		 * Formulate a template.
		 */
		Ping template = new Ping(null);
		/*
		 * Remove ONE Ping fact.
		 */
		Ping removed = this.memory.remove(template);
		if (removed != null) {
			/*
			 * If something was removed, create a Pong fact with a reference to the
			 * Ping.
			 */
			this.log.info("removed " + removed + " from agent's memory");
			Pong pong = new Pong(removed);
			this.memory.write(pong);
			this.log.info("write " + pong + " to agent's memory");
		}
	}

}
