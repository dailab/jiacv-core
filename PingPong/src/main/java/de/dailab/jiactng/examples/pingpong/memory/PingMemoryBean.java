/*
 * Created on 16.02.2007
 */
package de.dailab.jiactng.examples.pingpong.memory;

import java.util.Set;

import de.dailab.jiactng.agentcore.AbstractAgentBean;
import de.dailab.jiactng.examples.pingpong.ontology.Ping;
import de.dailab.jiactng.examples.pingpong.ontology.Pong;

/**
 * One of the Beans for the local PingPong example. This bean creates a GUI
 * which allows the user to send 'ping's. The execute-method of the bean tries
 * to read a 'pong', and shows it in the GUI if successful.
 *
 * @author Thomas Konnerth
 */
public class PingMemoryBean extends AbstractAgentBean {

	/**
	 * Counter for sent pings, used to distinguish the pings.
	 */
	private int count = 0;

	/**
	 * Execution of the PingBean. Tries to read a pong and shows it in the GUI if
	 * successful.
	 *
	 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
	 */
	@Override
	public void execute() {
		// try to read pong with a template-tuple.
		Pong template = new Pong(null);
		Set<Pong> removed = this.memory.removeAll(template);
		this.log.info("removed " + removed.size() + " Pongs from agent's memory");
		this.memory.write(new Ping(this.count++));
	}
}
