package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;

/**
 * The interface represents the description of an agentnode.
 * 
 * @author axle
 */
public interface IAgentNodeDescription extends IFact {
	
	/**
	 * Returns the messageBox address of the agentnode.
	 * @return the messageBox address of the agentnode
	 * @see ICommunicationAddress
	 */
	public ICommunicationAddress getAddress();
	
	/**
	 * Returns the last time the agentnode has sent a sign of life.
	 * @return the last time the agentnode has sent a sign of life
	 */
	public long getAlive();
}
