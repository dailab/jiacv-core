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

    public static final String ITEMNAME_ID = "id";
    public static final String ITEMNAME_NAME = "name";
    public static final String ITEMNAME_NODE = "node UUID";
    public static final String ITEMNAME_STATE = "state";
    public static final String ITEMNAME_MOBILE = "mobile";
    public static final String ITEMNAME_MESSAGEBOX = "message box address";

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
     * This method returns the unique communication address of
     * the agent this description refers to.
     * 
     * 
     * @return      the communication address
     */
    IMessageBoxAddress getMessageBoxAddress();
  
    String getAgentNodeUUID();
    
    void setAgentNodeUUID(String UUID);
    
    Boolean isMobile();
}
