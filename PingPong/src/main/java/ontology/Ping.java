package ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Ping implements IFact {
	private String ping;

	public Ping(String ping) {
		this.ping = ping;
	}

	/**
	 * @return the ping
	 */
	public String getPing() {
		return ping;
	}

	/**
	 * @param ping the ping to set
	 */
	public void setPing(String ping) {
		this.ping = ping;
	}
	
}
