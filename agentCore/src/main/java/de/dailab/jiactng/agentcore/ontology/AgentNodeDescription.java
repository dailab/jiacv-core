package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;

/**
 * The datastructure to store information about an agentnode.
 * 
 * @author axle
 */
public class AgentNodeDescription implements IAgentNodeDescription {
	private static final long serialVersionUID = -7460322120270381260L;

	/** The messageBox of the node.*/
	private ICommunicationAddress address;
	
	/** Last update received. */
	private long alive;

	/**
	 * Constructor for setting address and alive time of the agentnode.
	 * @param address the messageBox address of the agentnode
	 * @param alive the time when the node has reported alive
	 */
	public AgentNodeDescription(ICommunicationAddress address, long alive) {
		this.address = address;
		this.alive = alive;
	}

	/**
	 * Returns the messageBox address of the agentnode.
	 * @return the messageBox address of the agentnode
	 * @see ICommunicationAddress
	 */
	public ICommunicationAddress getAddress() {
		return address;
	}

	/**
	 * Sets the messageBox address of the node.
	 * @param address the address of the node
	 * @see ICommunicationAddress
	 */
	public void setAddress(ICommunicationAddress address) {
		this.address = address;
	}

	/**
	 * Returns the last time the agentnode has sent a sign of life.
	 * @return the last time the agentnode has sent a sign of life
	 */
	public long getAlive() {
		return alive;
	}

	/**
	 * Sets the time when the node has reported alive.
	 * @param alive the time when the node has reported alive
	 */
	public void setAlive(long alive) {
		this.alive = alive;
	}

}
