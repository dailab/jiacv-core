/*
 * Created on 16.02.2007
 */
package de.dailab.jiactng.examples.pingpong.memory;

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
public class PongMemoryBean extends AbstractAgentBean {

	/**
	 * Execution of the PongBean. Tries to read a ping and shows it in the GUI if
	 * successful.
	 *
	 * @see de.dailab.jiactng.agentcore.AbstractAgentBean#execute()
	 */
	@Override
	public void execute() {
		Ping template = new Ping(null);
		Ping removed = this.memory.remove(template);
		if (removed != null) {
			this.log.info("removed " + removed + " from agent's memory");
			Pong pong = new Pong(removed);
			this.memory.write(pong);
			this.log.info("write " + pong + " to agent's memory");
		}
	}

}
