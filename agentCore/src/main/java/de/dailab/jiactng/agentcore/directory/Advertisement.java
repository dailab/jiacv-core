package de.dailab.jiactng.agentcore.directory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.ontology.IActionDescription;
import de.dailab.jiactng.agentcore.ontology.IAgentDescription;

public class Advertisement implements IFact {
	private static final long serialVersionUID = 4672359214129779792L;

	private Set<IActionDescription> actions = new HashSet<IActionDescription>();
	
	private Hashtable<String, IAgentDescription> agents = new Hashtable<String, IAgentDescription>();

	private Set<JMXServiceURL> jmxURLs = new HashSet<JMXServiceURL>();

	private long aliveInterval;

	public Advertisement(Hashtable<String, IAgentDescription> agents, long aliveInterval) {
		this(agents, null, aliveInterval);
	}
	
	public Advertisement(Set<IActionDescription> actions, long aliveInterval) {
		this(null, actions, aliveInterval);
	}

	public Advertisement(Hashtable<String, IAgentDescription> agents, Set<IActionDescription> actions, long aliveInterval) {
		if (agents != null) {
			this.agents.putAll(agents);
		}
		if (actions != null) {
			this.actions.addAll(actions);
		}
		this.aliveInterval = aliveInterval;
	}
	
	public final Set<IActionDescription> getActions() {
		return actions;
	}
	
	public final void setActions(Set<IActionDescription> newActions) {
		actions.addAll(newActions);
	}

	public final Hashtable<String, IAgentDescription> getAgents() {
		return agents;
	}

	public final void setAgents(Hashtable<String, IAgentDescription> newAgents) {
		agents.putAll(newAgents);
	}

	/**
	 * Returns the URLs of all JMX connector server of the advertising agent node.
	 * @return the URLs of the JMX connector server of the advertising agent node 
	 */
	public final Set<JMXServiceURL> getJmxURLs() {
		return jmxURLs;
	}

	/**
	 * Sets the URLs of all JMX connector server of the advertising agent node.
	 * @param newJmxURLs the URLs of the JMX connector server of the advertising agent node
	 */
	public final void setJmxURLs(Set<JMXServiceURL> newJmxURLs) {
		jmxURLs.addAll(newJmxURLs);
	}

	/**
	 * Gets the interval of sending alive messages by the node's directory.
	 * @return the interval in milliseconds
	 */
	public final long getAliveInterval() {
		return aliveInterval;
	}
}
