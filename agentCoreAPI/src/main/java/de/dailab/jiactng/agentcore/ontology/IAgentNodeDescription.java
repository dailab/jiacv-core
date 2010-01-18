package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.management.jmx.JmxDescriptionSupport;

/**
 * The interface represents the description of an agentnode.
 * 
 * @author axle
 */
public interface IAgentNodeDescription extends IFact, JmxDescriptionSupport {

	/** Item name which can be used to get the address of the agent node. */
    static final String ITEMNAME_ADDRESS = "address";

	/** Item name which can be used to get the time of last alive of the agent node. */
    static final String ITEMNAME_ALIVE = "alive";

    /**
	 * Returns the messageBox address of the agentnode.
	 * @return the messageBox address of the agentnode
	 * @see ICommunicationAddress
	 */
	ICommunicationAddress getAddress();
	
	/**
	 * Returns the last time the agentnode has sent a sign of life.
	 * @return the last time the agentnode has sent a sign of life
	 */
	long getAlive();
}
