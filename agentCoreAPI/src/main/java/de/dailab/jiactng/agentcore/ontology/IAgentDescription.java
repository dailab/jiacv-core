/*
 * $Id: IAgentDescription.java 24780 2009-09-30 08:42:07Z burkhardt $ 
 */
package de.dailab.jiactng.agentcore.ontology;

import de.dailab.jiactng.agentcore.comm.IMessageBoxAddress;
import de.dailab.jiactng.agentcore.knowledge.IFact;
import de.dailab.jiactng.agentcore.management.jmx.JmxDescriptionSupport;

/**
 * This interface represents a description of an agent.
 * 
 * @author Marcel Patzlaff
 * @version $Revision: 24780 $
 */
public interface IAgentDescription extends IFact, JmxDescriptionSupport {

	/** Item name which can be used to get the Id of the JMX-based agent description. */
    String ITEMNAME_ID = "id";

	/** Item name which can be used to get the name of the JMX-based agent description. */
    String ITEMNAME_NAME = "name";

	/** Item name which can be used to get the owner of the JMX-based agent description. */
    String ITEMNAME_OWNER = "owner";

	/** Item name which can be used to get the node UUID of the JMX-based agent description. */
    String ITEMNAME_NODE = "node UUID";

	/** Item name which can be used to get the state of the JMX-based agent description. */
    String ITEMNAME_STATE = "state";

	/** Item name which can be used to get the mobility of the JMX-based agent description. */
    String ITEMNAME_MOBILE = "mobile";

	/** Item name which can be used to get the message box address of the JMX-based agent description. */
    String ITEMNAME_MESSAGEBOX = "message box address";

	/**
     * This method returns the unique agent identifier (AID) of
     * the agent this description refers to.
     * 
     * @return      the agent identifier
     */
    String getAid();
    
    /**
     * This method returns the display name of the agent this
     * description refers to.
     * 
     * @return      the display name of the agent
     */
    String getName();
    
    /**
     * This method returns the owner of the agent this
     * description refers to.
     * 
     * @return      the owner of the agent
     */
    String getOwner();
    
    /**
     * This method returns the unique communication address of
     * the agent this description refers to.
     * 
     * 
     * @return      the communication address
     */
    IMessageBoxAddress getMessageBoxAddress();

    /**
     * Gets the UUID of the agent node where the agent is located.
     * @return the agent node UUID
     */
    String getAgentNodeUUID();

    /**
     * Sets the UUID of the agent node where the agent is located.
     * @param uuid the agent node UUID
     */
    void setAgentNodeUUID(String uuid);

    /**
     * Gets the mobility of the agent.
     * @return <code>true</code> if the agent is able to migrate to other agent nodes.
     */
    Boolean isMobile();
}
