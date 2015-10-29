/*
 * Created on 16.02.2007
 */
package de.dailab.jiactng.examples.pingpong.memory;

import java.util.Set;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.examples.pingpong.ontology.Ping;
import de.dailab.jiactng.examples.pingpong.ontology.Pong;

/**
 * This is one of a simplest agent beans. This agent bean creates within of
 * every execution cycle {@link Ping} fact and writes it to the agent's memory.
 *
 * The corresponding part got the {@link PongMemoryBean} that removes one
 * {@link Ping} and writes a {@link Pong}.
 *
 * Finally all {@link Pong}s were removed by this agent bean to keep the agent's
 * memory clean.
 *
 * <strong>Note: The agent's memory can be used as intra agent communication
 * blackboard.</strong>
 *
 * @author Michael Burkhardt
 * @author Thomas Konnerth
 */
public class PingMemoryBean extends AbstractAgentBean {

	/**
	 * Counter for sent pings, used to distinguish the pings.
	 */
	private int count = 0;

	/**
	 * An {@link #execute()} will be executed on every 'execution interval' that
	 * could be configured in the agent's configuration.
	 *
	 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
	 */
	@Override
	public void execute() {
		/*
		 * create a Ping and write is to agent's memory
		 */
		this.memory.write(new Ping(this.count++));
		/*
		 * Formulate a search template for all Pongs that should be removed. Remove
		 * the Pongs by calling the agent's memory. Print a success message.
		 */
		Pong template = new Pong(null);
		Set<Pong> removed = this.memory.removeAll(template);
		this.log.info("removed " + removed.size() + " Pongs from agent's memory");
	}
}
