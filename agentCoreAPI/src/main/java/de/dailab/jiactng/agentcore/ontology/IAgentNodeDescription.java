package de.dailab.jiactng.agentcore.ontology;

import java.util.Set;

import javax.management.remote.JMXServiceURL;

import de.dailab.jiactng.agentcore.comm.ICommunicationAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.management.jmx.JmxDescriptionSupport;

/**
 * The interface represents the description of an agent node.
 * 
 * @author axle
 */
public interface IAgentNodeDescription extends IFact, JmxDescriptionSupport {

	/** Item name which can be used to get the address of the agent node. */
    String ITEMNAME_ADDRESS = "address";

	/** Item name which can be used to get the JMX URLs of the agent node. */
    String ITEMNAME_JMXURLS = "jmxURLs";

	/** Item name which can be used to get the time of last alive of the agent node. */
    String ITEMNAME_ALIVE = "alive";

    /**
	 * Returns the messageBox address of the agent node.
	 * @return the messageBox address of the agent node
	 * @see ICommunicationAddress
	 */
	ICommunicationAddress getAddress();

	/**
	 * Returns the URLs of all JMX connector server of the agent node.
	 * @return the URLs of the JMX connector server of the agent node
	 */
	Set<JMXServiceURL> getJmxURLs();

	/**
	 * Returns the last time the agent node has sent a sign of life.
	 * @return the last time the agent node has sent a sign of life
	 */
	long getAlive();
}
