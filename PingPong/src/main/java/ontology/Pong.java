package ontology;

import de.dailab.jiactng.agentcore.knowledge.IFact;

public class Pong implements IFact {
	private String pong;

	public Pong(String pong) {
		super();
		this.pong = pong;
	}

	/**
	 * @return the pong
	 */
	public String getPong() {
		return pong;
	}

	/**
	 * @param pong the pong to set
	 */
	public void setPong(String pong) {
		this.pong = pong;
	}
	
}
